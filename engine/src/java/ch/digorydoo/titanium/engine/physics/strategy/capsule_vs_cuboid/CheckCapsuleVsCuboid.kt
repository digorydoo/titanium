package ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_cuboid

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.BrickFaceCovering
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.CuboidCheckHelper
import ch.digorydoo.titanium.engine.physics.CuboidCheckHelper.*
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionCheckStrategy
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

internal class CheckCapsuleVsCuboid: CollisionCheckStrategy<FixedCapsuleBody, FixedCuboidBody>() {
    private var centreX1 = 0.0f
    private var centreY1 = 0.0f
    private var centreZ1 = 0.0f
    private var capsuleRadius = 0.0f
    private var capsuleRadiusSqr = 0.0f
    private var capsuleHalfHeight = 0.0f

    private var centreX2 = 0.0f
    private var centreY2 = 0.0f
    private var centreZ2 = 0.0f
    private var cuboidSizeX = 0.0f
    private var cuboidSizeY = 0.0f
    private var cuboidSizeZ = 0.0f
    private var cuboidHalfSizeX = 0.0f
    private var cuboidHalfSizeY = 0.0f
    private var cuboidHalfSizeZ = 0.0f

    private var minTop = 0.0f
    private var maxBottom = 0.0f
    private var overlapZ = 0.0f
    private var overlapHeight = 0.0f
    private var maybeCapsuleLowerEnd = false
    private var maybeCapsuleUpperEnd = false
    private var capsuleEndOrOverlapZ = 0.0f

    private var bfcr: IBrickFaceCoveringRetriever? = null
    private val brickCoords = MutablePoint3i()

    private val helper = CuboidCheckHelper(DelegateImpl())

    private inner class DelegateImpl: Delegate {
        override fun checkVerticalFace(normalX: Float, normalY: Float, result: CuboidCheckResults) {
            if (maybeCapsuleLowerEnd || maybeCapsuleUpperEnd) {
                // We're checking the vertical faces only against the cylindrical part of the capsule, i.e. not
                // against the two ends. Thus, a hit with one of the capsule ends will always be caught by a
                // horizontal face even when it's an edge. This simplifies separation later.
                result.hit = CuboidHit.UNKNOWN
                return
            }

            // Find the centre on the face described by the normal, and subtract it from the capsule's centre.
            val faceCentreToCapsuleCentreX = centreX1 - (centreX2 + normalX * cuboidHalfSizeX)
            val faceCentreToCapsuleCentreY = centreY1 - (centreY2 + normalY * cuboidHalfSizeY)

            // Compute the projected distance between the capsule's centre and the face.
            val d = faceCentreToCapsuleCentreX * normalX + faceCentreToCapsuleCentreY * normalY

            // If d > r, the circle is on the front side of the face, and is farther than r away. This means that
            // the capsule cannot possibly collide with any of the other faces either.
            if (d > capsuleRadius) {
                result.hit = CuboidHit.DECISIVE_MISS
                return
            }

            if (d < -capsuleRadius) {
                // The circle is completely on the back side of this face.

                val sizeAlongNormal = abs(normalX * cuboidSizeX) + abs(normalY * cuboidSizeY)

                if (d < -(capsuleRadius + sizeAlongNormal)) {
                    // The capsule is completely on the other side of the cuboid and thus does not collide.
                    result.hit = CuboidHit.DECISIVE_MISS
                    return
                } else {
                    // The capsule is between the plane of this face and the plane of the face on the other side of
                    // the cuboid. It depends on the other dimensions whether this is a hit or miss.
                    result.hit = CuboidHit.UNKNOWN
                    return
                }
            }

            // The circle collides with the plane of our face. Compute the pt on the plane closest to the circle.
            val closestPtOnPlaneX = centreX1 - d * normalX
            val closestPtOnPlaneY = centreY1 - d * normalY
            // val closestPtOnPlaneZ = overlapZ

            val clampedX = clamp(closestPtOnPlaneX, centreX2 - cuboidHalfSizeX, centreX2 + cuboidHalfSizeX)
            val clampedY = clamp(closestPtOnPlaneY, centreY2 - cuboidHalfSizeY, centreY2 + cuboidHalfSizeY)
            // val clampedZ = overlapZ

            result.apply {
                hitPt.set(clampedX, clampedY, overlapZ)
                distanceToClosestPtOnPlane = abs(d)
            }

            // Check if the closestPtOnPlane is inside the face (no need to check z since it's always inside)
            val closestPtInsideFace = closestPtOnPlaneX > centreX2 - cuboidHalfSizeX &&
                closestPtOnPlaneX < centreX2 + cuboidHalfSizeX &&
                closestPtOnPlaneY > centreY2 - cuboidHalfSizeY &&
                closestPtOnPlaneY < centreY2 + cuboidHalfSizeY

            if (closestPtInsideFace) {
                result.hit = CuboidHit.HIT_WITH_CLOSEST_PT_INSIDE_FACE
                return
            }

            // The closestPtOnPlane is outside the face. Check if the clamped point is inside the circle.
            val dx = clampedX - centreX1
            val dy = clampedY - centreY1
            val dsqr = dx * dx + dy * dy

            if (dsqr > capsuleRadiusSqr) {
                result.hit = CuboidHit.UNKNOWN // this face wasn't hit, but other faces may still see a hit
            } else {
                val len = sqrt(dsqr)

                // If len is too small, we'll use the default (which is the negative face normal)
                if (len > EPSILON) {
                    result.hitNormal12.set(dx / len, dy / len, 0.0f)
                }

                result.hit = when (bfcr?.getBrickFaceCovering(brickCoords, normalX, normalY, 0.0f)) {
                    null, BrickFaceCovering.NOT_COVERED -> CuboidHit.HIT_WITH_CLOSEST_PT_OUTSIDE_FACE
                    BrickFaceCovering.PARTIALLY_COVERED -> CuboidHit.HIT_PARTIALLY_COVERED_FACE
                    BrickFaceCovering.FULLY_COVERED -> CuboidHit.HIT_FULLY_COVERED_FACE
                }
            }
        }

        override fun checkHorizontalFace(normalZ: Float, result: CuboidCheckResults) {
            val faceZ = centreZ2 + normalZ * cuboidHalfSizeZ

            if (faceZ > minTop || faceZ < maxBottom) {
                // The face lies outside the range of the z overlap and thus does not collide.
                result.hit = CuboidHit.UNKNOWN
                return
            }

            val faceXLo = centreX2 - cuboidHalfSizeX
            val faceXHi = centreX2 + cuboidHalfSizeX
            val faceYLo = centreY2 - cuboidHalfSizeY
            val faceYHi = centreY2 + cuboidHalfSizeY

            // Find the point on the outline of the face that's closest to the capsule.

            val outlineX: Float
            val outlineY: Float

            if (centreX1 <= faceXLo) {
                // The capsule centre is on the north side of the face.
                outlineX = faceXLo
                outlineY = clamp(centreY1, faceYLo, faceYHi)
            } else if (centreX1 >= faceXHi) {
                // The capsule centre is on the south side of the face.
                outlineX = faceXHi
                outlineY = clamp(centreY1, faceYLo, faceYHi)
            } else if (centreY1 <= faceYLo) {
                // The capsule centre is on the west side of the face.
                outlineX = clamp(centreX1, faceXLo, faceXHi)
                outlineY = faceYLo
            } else if (centreY1 >= faceYHi) {
                // The capsule centre is on the east side of the face.
                outlineX = clamp(centreX1, faceXLo, faceXHi)
                outlineY = faceYHi
            } else {
                // The capsule centre is within the face, which is clearly a hit.
                result.hitPt.set(centreX1, centreY1, faceZ)
                result.distanceToClosestPtOnPlane = 0.0f

                result.hit = when (bfcr?.getBrickFaceCovering(brickCoords, 0.0f, 0.0f, normalZ)) {
                    null, BrickFaceCovering.NOT_COVERED -> CuboidHit.HIT_WITH_CLOSEST_PT_INSIDE_FACE
                    BrickFaceCovering.PARTIALLY_COVERED -> CuboidHit.HIT_PARTIALLY_COVERED_FACE
                    BrickFaceCovering.FULLY_COVERED -> CuboidHit.HIT_FULLY_COVERED_FACE
                }
                return
            }

            // Check if the closest point on the outline is in range of the capsule radius.

            val dx = outlineX - centreX1
            val dy = outlineY - centreY1
            val dz = when {
                maybeCapsuleLowerEnd || maybeCapsuleUpperEnd -> faceZ - capsuleEndOrOverlapZ
                else -> 0.0f
            }
            val dsqr = dx * dx + dy * dy + dz * dz

            if (dsqr > capsuleRadiusSqr) {
                result.hit = CuboidHit.UNKNOWN // this face wasn't hit, but other faces may still see a hit
            } else {
                result.hitPt.set(outlineX, outlineY, faceZ)
                result.distanceToClosestPtOnPlane = 0.0f

                val len = sqrt(dsqr)

                // If len is too small, we'll use the default (which is the negative face normal)
                if (len > EPSILON) {
                    result.hitNormal12.set(dx / len, dy / len, dz / len)
                }

                result.hit = when (bfcr?.getBrickFaceCovering(brickCoords, 0.0f, 0.0f, normalZ)) {
                    null, BrickFaceCovering.NOT_COVERED -> CuboidHit.HIT_WITH_CLOSEST_PT_INSIDE_FACE
                    BrickFaceCovering.PARTIALLY_COVERED -> CuboidHit.HIT_PARTIALLY_COVERED_FACE
                    BrickFaceCovering.FULLY_COVERED -> CuboidHit.HIT_FULLY_COVERED_FACE
                }
            }
        }
    }

    override fun check(
        body1: FixedCapsuleBody,
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
        this.centreX1 = centreX1
        this.centreY1 = centreY1
        this.centreZ1 = centreZ1

        this.centreX2 = centreX2
        this.centreY2 = centreY2
        this.centreZ2 = centreZ2

        this.bfcr = bfcr
        brickCoords?.let { this.brickCoords.set(it) }

        if (bfcr != null) {
            require(!body1IsBrick) { "Brick not implemented for body1" }
            require(body2IsBrick) { "Body2 is expected to be a brick when bfcr is passed" }
        }

        capsuleRadius = body1.radius
        capsuleRadiusSqr = capsuleRadius * capsuleRadius
        capsuleHalfHeight = body1.height / 2.0f

        cuboidSizeX = body2.sizeX
        cuboidSizeY = body2.sizeY
        cuboidSizeZ = body2.sizeZ
        cuboidHalfSizeX = body2.halfSizeX
        cuboidHalfSizeY = body2.halfSizeY
        cuboidHalfSizeZ = body2.halfSizeZ

        minTop = min(centreZ2 + cuboidHalfSizeZ, centreZ1 + capsuleHalfHeight)
        maxBottom = max(centreZ2 - cuboidHalfSizeZ, centreZ1 - capsuleHalfHeight)
        overlapHeight = minTop - maxBottom

        if (overlapHeight <= 0) {
            return false // the capsule is completely above or below the cuboid
        }

        overlapZ = maxBottom + overlapHeight * 0.5f

        val isCircleContainedInCuboid2D = (centreX2 - cuboidHalfSizeX <= centreX1 - capsuleRadius) &&
            (centreX2 + cuboidHalfSizeX >= centreX1 + capsuleRadius) &&
            (centreY2 - cuboidHalfSizeY <= centreY1 - capsuleRadius) &&
            (centreY2 + cuboidHalfSizeY >= centreY1 + capsuleRadius)

        if (isCircleContainedInCuboid2D) {
            // Since we already know that there is a z overlap, this must be a vertical hit!
            if (centreZ1 < overlapZ) {
                outHit?.apply {
                    hitPt.set(centreX1, centreY1, centreZ2 - cuboidHalfSizeZ)
                    area1 = HitArea.TOP_SPHERE // capsule's top
                    area2 = HitArea.BOTTOM_FACE // cuboid's bottom
                    hitNormal12.set(0.0f, 0.0f, 1.0f)
                }
            } else {
                outHit?.apply {
                    hitPt.set(centreX1, centreY1, centreZ2 + cuboidHalfSizeZ)
                    area1 = HitArea.BOTTOM_SPHERE // capsule's bottom
                    area2 = HitArea.TOP_FACE // cuboid's top
                    hitNormal12.set(0.0f, 0.0f, -1.0f)
                }
            }
            return true
        }

        val capsuleLowerSphereCentreZ = centreZ1 - capsuleHalfHeight + capsuleRadius
        val capsuleUpperSphereCentreZ = centreZ1 + capsuleHalfHeight - capsuleRadius

        maybeCapsuleLowerEnd = centreZ2 + cuboidHalfSizeZ < capsuleLowerSphereCentreZ
        maybeCapsuleUpperEnd = centreZ2 - cuboidHalfSizeZ > capsuleUpperSphereCentreZ

        capsuleEndOrOverlapZ = when {
            maybeCapsuleLowerEnd -> capsuleLowerSphereCentreZ
            maybeCapsuleUpperEnd -> capsuleUpperSphereCentreZ
            else -> overlapZ
        }

        // If there was a z overlap at the original position already, it's impossible that the top or bottom faces
        // can be hit in a straight line.

        val origMinTop = min(body2.pos.z + cuboidHalfSizeZ, body1.pos.z + capsuleHalfHeight)
        val origMaxBottom = max(body2.pos.z - cuboidHalfSizeZ, body1.pos.z - capsuleHalfHeight)
        val origOverlapHeight = origMinTop - origMaxBottom

        val whenEdge = when {
            origOverlapHeight > 0.0f -> WhenEdge.PREFER_VERTICAL_FACE
            else -> WhenEdge.INSPECT_SPEEDS
        }

        val face = helper.check(body1, body2, whenEdge) ?: return false
        val results = face.checkResults

        if (results.hit == CuboidHit.UNKNOWN) {
            Log.warn(TAG, "checkFaces() returned UNKNOWN when it should have returned null: $face")
            return false
        }

        // There was a hit. If the caller does not need to know the hit point, we're done.
        if (outHit == null) return true

        outHit.hitPt.set(results.hitPt)
        outHit.hitNormal12.set(results.hitNormal12)
        outHit.area2 = face.area // cuboid face

        outHit.area1 = when (face.area) {
            HitArea.TOP_FACE -> HitArea.BOTTOM_SPHERE
            HitArea.BOTTOM_FACE -> HitArea.TOP_SPHERE
            else -> HitArea.SIDE
        }

        return true
    }

    companion object {
        private val TAG = Log.Tag("CheckCapsuleVsCuboid")
    }
}
