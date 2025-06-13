package ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cuboid

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.engine.brick.BrickFaceCovering
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.CuboidCheckHelper.*
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.UnexpectedHitAreaError
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionCheckStrategy
import ch.digorydoo.titanium.engine.utils.Direction
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal class CheckSphereVsCuboid: CollisionCheckStrategy<FixedSphereBody, FixedCuboidBody>() {
    private val faces = arrayOf(
        CuboidVerticalFace(HitArea.NORTH_FACE, Direction.northVector),
        CuboidVerticalFace(HitArea.EAST_FACE, Direction.eastVector),
        CuboidVerticalFace(HitArea.SOUTH_FACE, Direction.southVector),
        CuboidVerticalFace(HitArea.WEST_FACE, Direction.westVector),
        CuboidHorizontalFace(HitArea.TOP_FACE, Direction.upVector),
        CuboidHorizontalFace(HitArea.BOTTOM_FACE, Direction.downVector),
    )

    override fun check(
        body1: FixedSphereBody,
        centreX1: Float,
        centreY1: Float,
        centreZ1: Float,
        body1IsBrick: Boolean,
        body2: FixedCuboidBody,
        centreX2: Float,
        centreY2: Float,
        centreZ2: Float,
        body2IsBrick: Boolean,
        bfcr: IBrickFaceCoveringRetriever?,
        brickCoords: Point3i?,
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

        if (bfcr != null) {
            require(!body1IsBrick) { "Brick not implemented for body1" }
            require(body2IsBrick) { "Body2 is expected to be a brick when bfcr is passed" }
        }

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
                    area2 = HitArea.BOTTOM_FACE // cuboid's bottom
                    hitNormal12.set(0.0f, 0.0f, 1.0f)
                }
            } else {
                outHit?.apply {
                    hitPt.set(centreX1, centreY1, centreZ2 + cuboidHalfSizeZ)
                    area1 = HitArea.UNSPECIFIED // spheres do not have specific areas
                    area2 = HitArea.TOP_FACE // cuboid's top
                    hitNormal12.set(0.0f, 0.0f, -1.0f)
                }
            }
            return true
        }

        fun check(normal: Point3f, result: CuboidCheckResults) {
            result.hit = CuboidHit.UNKNOWN

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
                hitPt.set(clampedX, clampedY, clampedZ)
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
                bfcr == null -> CuboidHit.HIT_WITH_CLOSEST_PT_OUTSIDE_FACE
                else -> when (bfcr.getBrickFaceCovering(brickCoords!!, normal.x, normal.y, normal.z)) {
                    BrickFaceCovering.NOT_COVERED -> CuboidHit.HIT_WITH_CLOSEST_PT_OUTSIDE_FACE
                    BrickFaceCovering.PARTIALLY_COVERED -> CuboidHit.HIT_PARTIALLY_COVERED_FACE
                    BrickFaceCovering.FULLY_COVERED -> CuboidHit.HIT_FULLY_COVERED_FACE
                }
            }
        }

        var bestMatchHit = CuboidHit.UNKNOWN
        var bestMatchArea = HitArea.UNSPECIFIED
        var bestMatchDist = Float.NEGATIVE_INFINITY
        val bestMatchPt = MutablePoint3f()

        for (face in faces) {
            val results = face.checkResults
            check(face.normal, results)
            when (results.hit) {
                CuboidHit.HIT_WITH_CLOSEST_PT_INSIDE_FACE -> {
                    // This is a clear hit, and we can immediately return
                    outHit?.apply {
                        hitPt.set(results.hitPt)
                        area1 = HitArea.UNSPECIFIED // spheres do not have specific areas
                        area2 = face.area
                    }
                    return true
                }
                CuboidHit.HIT_FULLY_COVERED_FACE -> {
                    // Actually, it's impossible that the cuboid was hit this way, but since the cuboid actually
                    // collided, we keep this as a fallback in the unlikely case that no other face collided.
                    if (bestMatchHit == CuboidHit.UNKNOWN) {
                        bestMatchHit = results.hit
                        bestMatchArea = face.area
                        bestMatchDist = Float.NEGATIVE_INFINITY
                        bestMatchPt.set(results.hitPt)
                    }
                }
                CuboidHit.HIT_PARTIALLY_COVERED_FACE -> {
                    // It's possible that the cuboid was hit this way, but there may be another face with a clearer hit.
                    if (bestMatchHit == CuboidHit.UNKNOWN || bestMatchHit == CuboidHit.HIT_FULLY_COVERED_FACE) {
                        bestMatchHit = results.hit
                        bestMatchArea = face.area
                        bestMatchDist = Float.NEGATIVE_INFINITY
                        bestMatchPt.set(results.hitPt)
                    }
                }
                CuboidHit.HIT_WITH_CLOSEST_PT_OUTSIDE_FACE -> {
                    // The face was hit, but the closest point on the plane is outside the face, which means that the
                    // sphere overlaps one of the face edges. We counter-intuitively take the face whose closest point
                    // on its plane is *farther* away from the sphere centre. (Make a drawing to see this.)
                    if (results.distanceToClosestPtOnPlane > bestMatchDist) {
                        bestMatchHit = results.hit
                        bestMatchArea = face.area
                        bestMatchDist = results.distanceToClosestPtOnPlane
                        bestMatchPt.set(results.hitPt)
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
            HitArea.TOP_FACE -> Direction.upVector
            HitArea.BOTTOM_FACE -> Direction.downVector
            else -> throw UnexpectedHitAreaError(bestMatchArea)
        }

        outHit?.apply {
            hitPt.set(bestMatchPt)
            hitNormal12.set(-faceNormal.x, -faceNormal.y, -faceNormal.z)
            area1 = HitArea.UNSPECIFIED // spheres do not have specific areas
            area2 = bestMatchArea
        }
        return true
    }
}
