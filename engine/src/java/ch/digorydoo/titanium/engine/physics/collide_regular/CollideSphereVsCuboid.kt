package ch.digorydoo.titanium.engine.physics.collide_regular

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.utils.Direction
import kotlin.math.abs

internal class CollideSphereVsCuboid: CollisionStrategy<FixedSphereBody, FixedCuboidBody>() {
    private enum class FaceHit {
        UNKNOWN,
        HIT_WITH_CLOSEST_PT_INSIDE_FACE,
        HIT_WITH_CLOSEST_PT_OUTSIDE_FACE,
        HIT_OF_BLOCKED_FACE,
        DECISIVE_MISS,
    }

    private class CheckResults {
        var hit = FaceHit.UNKNOWN
        var hitPtValid = false
        var hitPtX = 0.0f
        var hitPtY = 0.0f
        var hitPtZ = 0.0f
        var distanceToClosestPtOnPlane = 0.0f
    }

    private class Face(val area: HitArea, val normal: Point3f) {
        val checkResults = CheckResults()
    }

    private val faces = arrayOf(
        Face(HitArea.NORTH_FACE, Direction.northVector),
        Face(HitArea.EAST_FACE, Direction.eastVector),
        Face(HitArea.SOUTH_FACE, Direction.southVector),
        Face(HitArea.WEST_FACE, Direction.westVector),
        Face(HitArea.TOP, Direction.upVector),
        Face(HitArea.BOTTOM, Direction.downVector),
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
        refPtZ1: Float,
        body2: FixedCuboidBody,
        centreX2: Float,
        centreY2: Float,
        refPtZ2: Float,
        outHit: MutableHitResult?,
    ): Boolean {
        val centreZ1 = refPtZ1 + body1.zOffset
        val centreZ2 = refPtZ2
        val sphereRadius = body1.radius
        val sphereRadiusSqr = sphereRadius * sphereRadius
        val cuboidSizeX = body2.size.x
        val cuboidSizeY = body2.size.y
        val cuboidSizeZ = body2.size.z
        val halfSizeX = body2.halfSize.x
        val halfSizeY = body2.halfSize.y
        val halfSizeZ = body2.halfSize.z

        fun check(normal: Point3f, result: CheckResults) {
            result.apply {
                hit = FaceHit.UNKNOWN
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
                result.hit = FaceHit.DECISIVE_MISS
                return
            }

            if (d < -sphereRadius) {
                // The sphere is completely on the back side of this face.

                val sizeAlongNormal = abs(normalX * cuboidSizeX) +
                    abs(normalY * cuboidSizeY) +
                    abs(normalZ * cuboidSizeZ)

                if (d < -(sphereRadius + sizeAlongNormal)) {
                    // The sphere is completely on the other side of the cuboid and thus does not collide.
                    result.hit = FaceHit.DECISIVE_MISS
                    return
                } else {
                    // The sphere is between the plane of this face and the plane of the face on the other side of the
                    // cuboid. It depends on the other dimensions whether this is a hit or miss.
                    result.hit = FaceHit.UNKNOWN
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
                result.hit = FaceHit.HIT_WITH_CLOSEST_PT_INSIDE_FACE
                return
            }

            // The closestPtOnPlane is outside the face. Check if the clamped point actually collides with the sphere.
            val dx = clampedX - centreX1
            val dy = clampedY - centreY1
            val dz = clampedZ - centreZ1
            val dsqr = dx * dx + dy * dy + dz * dz

            result.hit = when {
                dsqr > sphereRadiusSqr -> FaceHit.UNKNOWN // sphere doesn't collide with this face
                hasValidNeighbour(normal) -> FaceHit.HIT_OF_BLOCKED_FACE
                else -> FaceHit.HIT_WITH_CLOSEST_PT_OUTSIDE_FACE
            }
        }

        var bestMatchValid = false
        var bestMatchArea = HitArea.UNSPECIFIED
        var bestMatchDist = Float.NEGATIVE_INFINITY
        var bestMatchX = 0.0f
        var bestMatchY = 0.0f
        var bestMatchZ = 0.0f

        for (face in faces) {
            val results = face.checkResults
            check(face.normal, results)
            when (results.hit) {
                FaceHit.HIT_WITH_CLOSEST_PT_INSIDE_FACE -> {
                    // This is a clear hit, and we can immediately return
                    require(results.hitPtValid)
                    outHit?.apply {
                        hitPt.set(results.hitPtX, results.hitPtY, results.hitPtZ)
                        area1 = HitArea.UNSPECIFIED // sphere do not have specific areas
                        area2 = face.area
                    }
                    return true
                }
                FaceHit.HIT_OF_BLOCKED_FACE -> {
                    // Take this as the result only if we haven't got anything better yet
                    if (!bestMatchValid) {
                        bestMatchValid = true
                        bestMatchArea = face.area
                        bestMatchDist = Float.NEGATIVE_INFINITY
                        bestMatchX = results.hitPtX
                        bestMatchY = results.hitPtY
                        bestMatchZ = results.hitPtZ
                    }
                }
                FaceHit.HIT_WITH_CLOSEST_PT_OUTSIDE_FACE -> {
                    // The face was hit, but the closest point on the plane is outside the face, which means that the
                    // sphere overlaps one of the face edges. We counter-intuitively take the face whose closest point
                    // on its plane is *farther* away from the sphere centre. (Make a drawing to see this.)
                    if (results.hitPtValid) {
                        if (!bestMatchValid || results.distanceToClosestPtOnPlane > bestMatchDist) {
                            bestMatchValid = true
                            bestMatchArea = face.area
                            bestMatchDist = results.distanceToClosestPtOnPlane
                            bestMatchX = results.hitPtX
                            bestMatchY = results.hitPtY
                            bestMatchZ = results.hitPtZ
                        }
                    }
                }
                FaceHit.DECISIVE_MISS -> return false
                FaceHit.UNKNOWN -> Unit
            }
        }

        if (!bestMatchValid) return false

        outHit?.apply {
            hitPt.set(bestMatchX, bestMatchY, bestMatchZ)
            area1 = HitArea.UNSPECIFIED // sphere do not have specific areas
            area2 = bestMatchArea
        }
        return true
    }

    private fun hasValidNeighbour(faceNormal: Point3f): Boolean {
        val brickVolume = brickVolume ?: return false
        val neighbourX = (brickCoords.x + faceNormal.x).toInt()
        val neighbourY = (brickCoords.y + faceNormal.y).toInt()
        val neighbourZ = (brickCoords.z + faceNormal.z).toInt()

        if (brickCoords.x == neighbourX && brickCoords.y == neighbourY && brickCoords.z == neighbourZ) {
            // This shouldn't happen, since dir should always point in the direction of an axis
            return false
        } else {
            // FIXME should probably use relVolume
            return brickVolume.hasValidBrickAt(neighbourX, neighbourY, neighbourZ)
        }
    }

    override fun bounce(body1: FixedSphereBody, body2: FixedCuboidBody, hit: HitResult) {
        separate(body1, body1.zOffset, body2, 0.0f)
        // TODO
    }

    override fun forceApart(
        body1: FixedSphereBody,
        body2: FixedCuboidBody,
        normDirX1: Float,
        normDirY1: Float,
        normDirZ1: Float,
    ) {
        // TODO
    }
}
