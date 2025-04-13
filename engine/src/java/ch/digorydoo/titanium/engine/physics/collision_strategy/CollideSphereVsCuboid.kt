package ch.digorydoo.titanium.engine.physics.collision_strategy

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.BrickFaceCovering
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.*
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

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

    private var bricks: IBrickFaceCoveringRetriever? = null
    private val brickCoords = MutablePoint3i()

    override fun configure(
        body1IsBrick: Boolean,
        body2IsBrick: Boolean,
        bricks: IBrickFaceCoveringRetriever?,
        brickCoords: Point3i?,
    ) {
        if (body1IsBrick) throw NotImplementedError()

        if (body2IsBrick) {
            require(bricks != null)
            require(brickCoords != null)
            this.bricks = bricks
            this.brickCoords.set(brickCoords)
        } else {
            this.bricks = null
        }
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
        val cuboidSizeX = body2.sizeX
        val cuboidSizeY = body2.sizeY
        val cuboidSizeZ = body2.sizeZ
        val cuboidHalfSizeX = body2.halfSizeX
        val cuboidHalfSizeY = body2.halfSizeY
        val cuboidHalfSizeZ = body2.halfSizeZ

        val minTop = min(centreZ2 + cuboidHalfSizeZ, centreZ1 + sphereRadius)
        val maxBottom = max(centreZ2 - cuboidHalfSizeZ, centreZ1 - sphereRadius)
        val overlapHeight = minTop - maxBottom

        if (overlapHeight <= 0) {
            return false // the sphere is completely above or below the cuboid
        }

        val overlapZ = maxBottom + overlapHeight * 0.5f

        val isCircleContainedInCuboid2D = (centreX2 - cuboidHalfSizeX <= centreX1 - sphereRadius) &&
            (centreX2 + cuboidHalfSizeX >= centreX1 + sphereRadius) &&
            (centreY2 - cuboidHalfSizeY <= centreY1 - sphereRadius) &&
            (centreY2 + cuboidHalfSizeY >= centreY1 + sphereRadius)

        if (isCircleContainedInCuboid2D) {
            // Since we already know that there is a z overlap, this must be a vertical hit!
            if (centreZ1 < overlapZ) {
                outHit?.apply {
                    hitPt.set(centreX1, centreY1, centreZ2 - cuboidHalfSizeZ)
                    area1 = HitArea.UNSPECIFIED // spheres do not have specific areas
                    area2 = HitArea.BOTTOM // cuboid's bottom
                    hitNormal12.set(0.0f, 0.0f, 1.0f)
                }
            } else {
                outHit?.apply {
                    hitPt.set(centreX1, centreY1, centreZ2 + cuboidHalfSizeZ)
                    area1 = HitArea.UNSPECIFIED // spheres do not have specific areas
                    area2 = HitArea.TOP // cuboid's top
                    hitNormal12.set(0.0f, 0.0f, -1.0f)
                }
            }
            return true
        }

        fun check(normal: Point3f, result: CuboidCheckResults) {
            result.apply {
                hit = CuboidHit.UNKNOWN
                hitPtValid = false
            }

            val normalX = normal.x
            val normalY = normal.y
            val normalZ = normal.z

            // Find the centre on the face described by the normal, and subtract it from the sphere's centre
            val faceCentreToSphereCentreX = centreX1 - (centreX2 + normalX * cuboidHalfSizeX)
            val faceCentreToSphereCentreY = centreY1 - (centreY2 + normalY * cuboidHalfSizeY)
            val faceCentreToSphereCentreZ = centreZ1 - (centreZ2 + normalZ * cuboidHalfSizeZ)

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

            val clampedX = clamp(closestPtOnPlaneX, centreX2 - cuboidHalfSizeX, centreX2 + cuboidHalfSizeX)
            val clampedY = clamp(closestPtOnPlaneY, centreY2 - cuboidHalfSizeY, centreY2 + cuboidHalfSizeY)
            val clampedZ = clamp(closestPtOnPlaneZ, centreZ2 - cuboidHalfSizeZ, centreZ2 + cuboidHalfSizeZ)

            result.apply {
                hitPtValid = true
                hitPtX = clampedX
                hitPtY = clampedY
                hitPtZ = clampedZ
                distanceToClosestPtOnPlane = abs(d)
            }

            // Check if the closestPtOnPlane is inside the face
            val closestPtInsideFace = closestPtOnPlaneX > centreX2 - cuboidHalfSizeX &&
                closestPtOnPlaneX < centreX2 + cuboidHalfSizeX &&
                closestPtOnPlaneY > centreY2 - cuboidHalfSizeY &&
                closestPtOnPlaneY < centreY2 + cuboidHalfSizeY &&
                closestPtOnPlaneZ > centreZ2 - cuboidHalfSizeZ &&
                closestPtOnPlaneZ < centreZ2 + cuboidHalfSizeZ

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
                else -> when (bricks?.getBrickFaceCovering(brickCoords, normal.x, normal.y, normal.z)) {
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
                        area1 = HitArea.UNSPECIFIED // spheres do not have specific areas
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

        val faceNormal = when (bestMatchArea) {
            HitArea.NORTH_FACE -> Direction.northVector
            HitArea.EAST_FACE -> Direction.eastVector
            HitArea.SOUTH_FACE -> Direction.southVector
            HitArea.WEST_FACE -> Direction.westVector
            HitArea.TOP -> Direction.upVector
            HitArea.BOTTOM -> Direction.downVector
            else -> throw UnexpectedHitAreaError(bestMatchArea)
        }

        outHit?.apply {
            hitPt.set(bestMatchX, bestMatchY, bestMatchZ)
            hitNormal12.set(-faceNormal.x, -faceNormal.y, -faceNormal.z)
            area1 = HitArea.UNSPECIFIED // spheres do not have specific areas
            area2 = bestMatchArea
        }
        return true
    }

    override fun bounce(body1: FixedSphereBody, body2: FixedCuboidBody, hit: HitResult) {
        val normal = hit.hitNormal12
        val normDir12X = normal.x
        val normDir12Y = normal.y
        val normDir12Z = normal.z

        separate(body1, body2, normDir12X, normDir12Y, normDir12Z)
        helper.applyFriction(body1, body2, normDir12X, normDir12Y, normDir12Z)

        if (hit.area2 == HitArea.TOP) {
            val deltaSpeedZ = body2.speed.z - body1.speed.z // positive if sphere falls down on cuboid

            if (deltaSpeedZ in 0.0f .. HOPPING_PREVENTION_MAX_SPEED) {
                // Prevent sphere standing on cuboid from constantly hopping due to gravity
                body1.nextSpeed.z = body2.nextSpeed.z
            }
        }

        helper.bounceAtPlane(body1, body2, normDir12X, normDir12Y, normDir12Z)
        verifySeparation(body1, body2, hit)
    }

    private fun separate(
        body1: FixedSphereBody,
        body2: FixedCuboidBody,
        normDir12X: Float,
        normDir12Y: Float,
        normDir12Z: Float,
    ) {
        val p1 = body1.nextPos
        val p2 = body2.nextPos
        val sphereRadius = body1.radius

        // Compute the position of the sphere if we moved it completely to the other side of the plane.

        val distanceAlongNormal = (p2.x - p1.x) * normDir12X + (p2.y - p1.y) * normDir12Y + (p2.z - p1.z) * normDir12Z
        val halfSizeDotN = body2.halfSizeX * normDir12X + body2.halfSizeY * normDir12Y + body2.halfSizeZ * normDir12Z
        val requiredDistanceAlongNormal = sphereRadius + abs(halfSizeDotN)
        var moveBy = requiredDistanceAlongNormal - distanceAlongNormal

        if (moveBy + EPSILON <= 0.0f) {
            Log.warn(TAG, "separate was called, but bodies seem to be separated already")
            return
        }

        val m1x = p1.x - normDir12X * moveBy
        val m1y = p1.y - normDir12Y * moveBy
        val m1z = p1.z - normDir12Z * moveBy

        // If the sphere is close to a cuboid corner, we may have moved it too far.

        val distanceToPlane = distanceAlongNormal - abs(halfSizeDotN)
        val sphereCentreProjectedOntoPlaneX = p1.x + distanceToPlane * normDir12X
        val sphereCentreProjectedOntoPlaneY = p1.y + distanceToPlane * normDir12Y
        val sphereCentreProjectedOntoPlaneZ = p1.z + distanceToPlane * normDir12Z

        val clampedX = clamp(sphereCentreProjectedOntoPlaneX, p2.x - body2.halfSizeX, p2.x + body2.halfSizeX)
        val clampedY = clamp(sphereCentreProjectedOntoPlaneY, p2.y - body2.halfSizeY, p2.y + body2.halfSizeY)
        val clampedZ = clamp(sphereCentreProjectedOntoPlaneZ, p2.z - body2.halfSizeZ, p2.z + body2.halfSizeZ)

        val m1cx = m1x - clampedX
        val m1cy = m1y - clampedY
        val m1cz = m1z - clampedZ
        val sqrDistOfM1ToClamped = m1cx * m1cx + m1cy * m1cy + m1cz * m1cz
        val distanceOfM1ToClamped = sqrt(sqrDistOfM1ToClamped)
        val m1TooFar = distanceOfM1ToClamped - sphereRadius

        if (m1TooFar > EPSILON) {
            // See physics.txt: "Separating a sphere and a cuboid"

            val m1dotn = m1x * normDir12X + m1y * normDir12Y + m1z * normDir12Z
            val pcdotn = clampedX * normDir12X + clampedY * normDir12Y + clampedZ * normDir12Z
            val b = 2.0f * (m1dotn - pcdotn)
            val c = sqrDistOfM1ToClamped - sphereRadius * sphereRadius
            val discriminant = b * b - 4 * c // A=1

            if (discriminant < 0.0f) {
                Log.warn(TAG, "Quadratic equation has no solution, probably a bug")
            } else {
                val sqrDiscr = sqrt(discriminant)
                val q1 = (-b + sqrDiscr) / 2.0f
                val q2 = (-b - sqrDiscr) / 2.0f

                // We take the smaller q, because the other solution is on the other side of the plane.
                val q = min(q1, q2)

                if (q < 0.0f) {
                    Log.warn(TAG, "q is negative, probably a bug")
                } else {
                    moveBy -= q
                }
            }
        }

        // Adding more than just EPSILON here because of flaky tests (must be floating-point inaccuracies)
        moveBy += 2.0f * EPSILON

        when {
            body1.mass < LARGE_MASS -> when {
                body2.mass < LARGE_MASS -> {
                    // Do not distribute the distance by mass! If the lighter object is cornered, the CollisionManager
                    // would have trouble moving the heavier object away!
                    val move1By = moveBy * 0.5f
                    val move2By = moveBy - move1By

                    p1.x -= normDir12X * move1By
                    p1.y -= normDir12Y * move1By
                    p1.z -= normDir12Z * move1By

                    p2.x += normDir12X * move2By
                    p2.y += normDir12Y * move2By
                    p2.z += normDir12Z * move2By
                }
                else -> {
                    p1.x -= normDir12X * moveBy
                    p1.y -= normDir12Y * moveBy
                    p1.z -= normDir12Z * moveBy
                }
            }
            body2.mass < LARGE_MASS -> {
                p2.x += normDir12X * moveBy
                p2.y += normDir12Y * moveBy
                p2.z += normDir12Z * moveBy
            }
            else -> {
                Log.warn(TAG, "Separating $body1 from $body2 failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("CollideSphereVsCuboid")
    }
}
