package ch.digorydoo.titanium.engine.physics.collide_regular

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.brick.BrickVolume.BrickFaceCovering
import ch.digorydoo.titanium.engine.physics.*
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs

internal class CollideSphereVsCuboid: CollisionStrategy<FixedSphereBody, FixedCuboidBody>() {
    private class FaceWithResults(val area: HitArea, val normal: Point3f) {
        val checkResults = CuboidCheckResults()
    }

    private val faces = arrayOf(
        FaceWithResults(HitArea.NORTH_FACE, Direction.northVector),
        FaceWithResults(HitArea.EAST_FACE, Direction.eastVector),
        FaceWithResults(HitArea.SOUTH_FACE, Direction.southVector),
        FaceWithResults(HitArea.WEST_FACE, Direction.westVector),
        FaceWithResults(HitArea.TOP, Direction.upVector),
        FaceWithResults(HitArea.BOTTOM, Direction.downVector),
    )

    var brickVolume: BrickVolume? = null
    val brickCoords = MutablePoint3i()

    fun prepare(newBrickVolume: BrickVolume, newBrickCoords: Point3i) {
        brickVolume = newBrickVolume
        brickCoords.set(newBrickCoords)
    }

    override fun done() {
        brickVolume = null
    }

    override fun check(
        body1: FixedSphereBody,
        centreX1: Float,
        centreY1: Float,
        centreZ1: Float,
        body2: FixedCuboidBody,
        centreX2: Float,
        centreY2: Float,
        centreZ2: Float,
        outHit: MutableHitResult?,
    ): Boolean {
        val sphereRadius = body1.radius
        val sphereRadiusSqr = sphereRadius * sphereRadius
        val cuboidSizeX = body2.size.x
        val cuboidSizeY = body2.size.y
        val cuboidSizeZ = body2.size.z
        val halfSizeX = body2.halfSize.x
        val halfSizeY = body2.halfSize.y
        val halfSizeZ = body2.halfSize.z

        fun check(normal: Point3f, result: CuboidCheckResults) {
            result.apply {
                hit = CuboidHit.UNKNOWN
                hitPtValid = false
            }

            val normalX = normal.x
            val normalY = normal.y
            val normalZ = normal.z

            // Find the centre on the face described by the normal, and subtract it from the sphere's centre
            val faceCentreToSphereCentreX = centreX1 - (centreX2 + normalX * halfSizeX)
            val faceCentreToSphereCentreY = centreY1 - (centreY2 + normalY * halfSizeY)
            val faceCentreToSphereCentreZ = centreZ1 - (centreZ2 + normalZ * halfSizeZ)

            // Compute the signed distance between the sphere's centre and the face
            val d = faceCentreToSphereCentreX * normalX +
                faceCentreToSphereCentreY * normalY +
                faceCentreToSphereCentreZ * normalZ

            if (d > sphereRadius) {
                // The sphere is completely on the front side of this face and thus cannot collide with the cuboid.
                result.hit = CuboidHit.DECISIVE_MISS
                return
            }

            if (d < -sphereRadius) {
                // The sphere is completely on the back side of this face.

                val sizeAlongNormal = abs(normalX * cuboidSizeX) +
                    abs(normalY * cuboidSizeY) +
                    abs(normalZ * cuboidSizeZ)

                if (d < -(sphereRadius + sizeAlongNormal)) {
                    // The sphere is completely on the other side of the cuboid and thus does not collide.
                    result.hit = CuboidHit.DECISIVE_MISS
                    return
                } else {
                    // The sphere is between the plane of this face and the plane of the face on the other side of the
                    // cuboid. It depends on the other dimensions whether this is a hit or miss.
                    result.hit = CuboidHit.UNKNOWN
                    return
                }
            }

            // The sphere collides with the plane of our face.  Compute the point on the plane closest to the sphere.
            val closestPtOnPlaneX = centreX1 - d * normalX
            val closestPtOnPlaneY = centreY1 - d * normalY
            val closestPtOnPlaneZ = centreZ1 - d * normalZ

            val clampedX = clamp(closestPtOnPlaneX, centreX2 - halfSizeX, centreX2 + halfSizeX)
            val clampedY = clamp(closestPtOnPlaneY, centreY2 - halfSizeY, centreY2 + halfSizeY)
            val clampedZ = clamp(closestPtOnPlaneZ, centreZ2 - halfSizeZ, centreZ2 + halfSizeZ)

            result.apply {
                hitPtValid = true
                hitPtX = clampedX
                hitPtY = clampedY
                hitPtZ = clampedZ
                distanceToClosestPtOnPlane = abs(d)
            }

            // Check if the closestPtOnPlane is inside the face
            val closestPtInsideFace = closestPtOnPlaneX > centreX2 - halfSizeX &&
                closestPtOnPlaneX < centreX2 + halfSizeX &&
                closestPtOnPlaneY > centreY2 - halfSizeY &&
                closestPtOnPlaneY < centreY2 + halfSizeY &&
                closestPtOnPlaneZ > centreZ2 - halfSizeZ &&
                closestPtOnPlaneZ < centreZ2 + halfSizeZ

            if (closestPtInsideFace) {
                result.hit = CuboidHit.HIT_WITH_CLOSEST_PT_INSIDE_FACE
                return
            }

            // The closestPtOnPlane is outside the face. Check if the clamped point actually collides with the sphere.
            val dx = clampedX - centreX1
            val dy = clampedY - centreY1
            val dz = clampedZ - centreZ1
            val dsqr = dx * dx + dy * dy + dz * dz

            result.hit = when {
                dsqr > sphereRadiusSqr -> CuboidHit.UNKNOWN // sphere doesn't collide with this face
                else -> when (brickVolume?.getBrickFaceCovering(brickCoords, normal)) {
                    null, BrickFaceCovering.NOT_COVERED -> CuboidHit.HIT_WITH_CLOSEST_PT_OUTSIDE_FACE
                    BrickFaceCovering.PARTIALLY_COVERED -> CuboidHit.HIT_PARTIALLY_COVERED_FACE
                    BrickFaceCovering.FULLY_COVERED -> CuboidHit.HIT_FULLY_COVERED_FACE
                }
            }
        }

        var bestMatchHit = CuboidHit.UNKNOWN
        var bestMatchArea = HitArea.UNSPECIFIED
        var bestMatchDist = Float.NEGATIVE_INFINITY
        var bestMatchX = 0.0f
        var bestMatchY = 0.0f
        var bestMatchZ = 0.0f

        for (face in faces) {
            val results = face.checkResults
            check(face.normal, results)
            when (results.hit) {
                CuboidHit.HIT_WITH_CLOSEST_PT_INSIDE_FACE -> {
                    // This is a clear hit, and we can immediately return
                    require(results.hitPtValid)
                    outHit?.apply {
                        hitPt.set(results.hitPtX, results.hitPtY, results.hitPtZ)
                        area1 = HitArea.UNSPECIFIED // sphere do not have specific areas
                        area2 = face.area
                    }
                    return true
                }
                CuboidHit.HIT_FULLY_COVERED_FACE -> {
                    // Actually, it's impossible that the cuboid was hit this way, but since the cuboid actually
                    // collided, we keep this as a fallback in the unlikely case that no other face collided.
                    require(results.hitPtValid)
                    if (bestMatchHit == CuboidHit.UNKNOWN) {
                        bestMatchHit = results.hit
                        bestMatchArea = face.area
                        bestMatchDist = Float.NEGATIVE_INFINITY
                        bestMatchX = results.hitPtX
                        bestMatchY = results.hitPtY
                        bestMatchZ = results.hitPtZ
                    }
                }
                CuboidHit.HIT_PARTIALLY_COVERED_FACE -> {
                    // It's possible that the cuboid was hit this way, but there may be another face with a clearer hit.
                    require(results.hitPtValid)
                    if (bestMatchHit == CuboidHit.UNKNOWN || bestMatchHit == CuboidHit.HIT_FULLY_COVERED_FACE) {
                        bestMatchHit = results.hit
                        bestMatchArea = face.area
                        bestMatchDist = Float.NEGATIVE_INFINITY
                        bestMatchX = results.hitPtX
                        bestMatchY = results.hitPtY
                        bestMatchZ = results.hitPtZ
                    }
                }
                CuboidHit.HIT_WITH_CLOSEST_PT_OUTSIDE_FACE -> {
                    // The face was hit, but the closest point on the plane is outside the face, which means that the
                    // sphere overlaps one of the face edges. We counter-intuitively take the face whose closest point
                    // on its plane is *farther* away from the sphere centre. (Make a drawing to see this.)
                    require(results.hitPtValid)
                    if (results.distanceToClosestPtOnPlane > bestMatchDist) {
                        bestMatchHit = results.hit
                        bestMatchArea = face.area
                        bestMatchDist = results.distanceToClosestPtOnPlane
                        bestMatchX = results.hitPtX
                        bestMatchY = results.hitPtY
                        bestMatchZ = results.hitPtZ
                    }
                }
                CuboidHit.DECISIVE_MISS -> return false
                CuboidHit.UNKNOWN -> Unit
            }
        }

        if (bestMatchHit == CuboidHit.UNKNOWN) return false

        if (bestMatchHit == CuboidHit.HIT_FULLY_COVERED_FACE) {
            Log.warn("The face is fully covered and should not be the only one that was hit")
        }

        outHit?.apply {
            hitPt.set(bestMatchX, bestMatchY, bestMatchZ)
            area1 = HitArea.UNSPECIFIED // sphere do not have specific areas
            area2 = bestMatchArea
        }
        return true
    }

    override fun bounce(body1: FixedSphereBody, body2: FixedCuboidBody, hit: HitResult) {
        separate(body1, body2, hit)

        val m1 = body1.mass
        val m2 = body2.mass

        val v1 = body1.nextSpeed
        val v2 = body2.nextSpeed

        val normal = when (hit.area2) {
            HitArea.NORTH_FACE -> Direction.northVector
            HitArea.EAST_FACE -> Direction.eastVector
            HitArea.SOUTH_FACE -> Direction.southVector
            HitArea.WEST_FACE -> Direction.westVector
            HitArea.TOP -> Direction.upVector
            HitArea.BOTTOM -> Direction.downVector
            else -> throw UnexpectedHitAreaError(hit.area2)
        }

        val normDir12X = -normal.x
        val normDir12Y = -normal.y
        val normDir12Z = -normal.z

        // This may change v1 and/or v2
        applyFriction(
            m1 = m1,
            v1 = v1,
            friction1 = body1.friction,
            m2 = m2,
            v2 = v2,
            friction2 = body2.friction,
            normDir12X = normDir12X,
            normDir12Y = normDir12Y,
            normDir12Z = normDir12Z,
        )

        val elasticity = body1.elasticity * body2.elasticity

        val v1dotn = v1.x * normDir12X + v1.y * normDir12Y + v1.z * normDir12Z
        val v1parallelX = normDir12X * v1dotn
        val v1parallelY = normDir12Y * v1dotn
        val v1parallelZ = normDir12Z * v1dotn

        val v2dotn = v2.x * normDir12X + v2.y * normDir12Y + v2.z * normDir12Z
        val v2parallelX = normDir12X * v2dotn
        val v2parallelY = normDir12Y * v2dotn
        val v2parallelZ = normDir12Z * v2dotn

        val vparallelDx = v1parallelX - v2parallelX
        val vparallelDy = v1parallelY - v2parallelY
        val vparallelDz = v1parallelZ - v2parallelZ

        if (m1 >= LARGE_MASS) {
            val v2perpendX = v2.x - v2parallelX
            val v2perpendY = v2.y - v2parallelY
            val v2perpendZ = v2.z - v2parallelZ

            v2.x = v2perpendX + v1parallelX + vparallelDx * elasticity
            v2.y = v2perpendY + v1parallelY + vparallelDy * elasticity
            v2.z = v2perpendZ + v1parallelZ + vparallelDz * elasticity
        } else if (m2 >= LARGE_MASS) {
            val v1perpendX = v1.x - v1parallelX
            val v1perpendY = v1.y - v1parallelY
            val v1perpendZ = v1.z - v1parallelZ

            v1.x = v1perpendX + v2parallelX - vparallelDx * elasticity
            v1.y = v1perpendY + v2parallelY - vparallelDy * elasticity
            v1.z = v1perpendZ + v2parallelZ - vparallelDz * elasticity
        } else {
            val v1perpendX = v1.x - v1parallelX
            val v1perpendY = v1.y - v1parallelY
            val v1perpendZ = v1.z - v1parallelZ

            val v2perpendX = v2.x - v2parallelX
            val v2perpendY = v2.y - v2parallelY
            val v2perpendZ = v2.z - v2parallelZ

            val totalMass = m1 + m2

            val sx = v1parallelX * m1 + v2parallelX * m2
            val sy = v1parallelY * m1 + v2parallelY * m2
            val sz = v1parallelZ * m1 + v2parallelZ * m2

            v1.x = v1perpendX + (sx - vparallelDx * elasticity * m2) / totalMass
            v1.y = v1perpendY + (sy - vparallelDy * elasticity * m2) / totalMass
            v1.z = v1perpendZ + (sz - vparallelDz * elasticity * m2) / totalMass

            v2.x = v2perpendX + (sx + vparallelDx * elasticity * m1) / totalMass
            v2.y = v2perpendY + (sy + vparallelDy * elasticity * m1) / totalMass
            v2.z = v2perpendZ + (sz + vparallelDz * elasticity * m1) / totalMass
        }
    }

    // See physics.txt "Forcing a sphere and a cuboid apart"
    override fun forceApart(
        body1: FixedSphereBody,
        body2: FixedCuboidBody,
        normDirX1: Float,
        normDirY1: Float,
        normDirZ1: Float,
        hit: HitResult,
    ) {
        val p1 = body1.nextPos
        val p2 = body2.nextPos

        val normal = when (hit.area2) {
            HitArea.NORTH_FACE -> Direction.northVector
            HitArea.EAST_FACE -> Direction.eastVector
            HitArea.SOUTH_FACE -> Direction.southVector
            HitArea.WEST_FACE -> Direction.westVector
            HitArea.TOP -> Direction.upVector
            HitArea.BOTTOM -> Direction.downVector
            else -> throw UnexpectedHitAreaError(hit.area2)
        }

        val nx = normal.x
        val ny = normal.y
        val nz = normal.z

        val divisor = normDirX1 * nx + normDirY1 * ny + normDirZ1 * nz

        if (abs(divisor) < EPSILON) {
            Log.warn("Cannot force $body1 and $body2 apart, because the divisor is too small")
            return
        }

        val faceCentreX = p2.x + nx * body2.halfSize.x
        val faceCentreY = p2.y + ny * body2.halfSize.y
        val faceCentreZ = p2.z + nz * body2.halfSize.z

        val r = body1.radius

        val k = (nx * (r * nx - p1.x + faceCentreX) +
            ny * (r * ny - p1.y + faceCentreY) +
            nz * (r * nz - p1.z + faceCentreZ)) / divisor

        if (k < 0.0f) {
            Log.warn("Cannot force $body1 and $body2 apart, because moveBy has an unexpected value: $k")
            return
        }

        val moveBy = k + 2.0f * EPSILON

        if (body1.mass >= LARGE_MASS) {
            if (body2.mass >= LARGE_MASS) {
                Log.warn("Cannot force $body1 and $body2 apart, because both are LARGE_MASS")
            } else {
                p2.x = p1.x - normDirX1 * moveBy
                p2.y = p1.y - normDirY1 * moveBy
                p2.z = p1.z - normDirZ1 * moveBy
            }
        } else if (body2.mass >= LARGE_MASS) {
            p1.x = p2.x + normDirX1 * moveBy
            p1.y = p2.y + normDirY1 * moveBy
            p1.z = p2.z + normDirZ1 * moveBy
        } else {
            val half = moveBy / 2.0f

            p1.x += normDirX1 * half
            p1.y += normDirY1 * half
            p1.z += normDirZ1 * half

            p2.x -= normDirX1 * half
            p2.y -= normDirY1 * half
            p2.z -= normDirZ1 * half
        }
    }
}
