package ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_cuboid

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
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.strategy.CollisionCheckStrategy
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal class CheckCylinderVsCuboid: CollisionCheckStrategy<FixedCylinderBody, FixedCuboidBody>() {
    private var centreX1 = 0.0f
    private var centreY1 = 0.0f
    private var centreZ1 = 0.0f
    private var cylinderRadius = 0.0f
    private var cylinderRadiusSqr = 0.0f
    private var cylinderHalfHeight = 0.0f

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

    private var bfcr: IBrickFaceCoveringRetriever? = null
    private val brickCoords = MutablePoint3i()

    private val helper = CuboidCheckHelper(DelegateImpl())

    private inner class DelegateImpl: Delegate {
        override fun checkVerticalFace(normalX: Float, normalY: Float, result: CuboidCheckResults) {
            // Find the centre on the face described by the normal, and subtract it from the cylinder's centre.
            val faceCentreToCylinderCentreX = centreX1 - (centreX2 + normalX * cuboidHalfSizeX)
            val faceCentreToCylinderCentreY = centreY1 - (centreY2 + normalY * cuboidHalfSizeY)

            // Compute the projected distance between the cylinder's centre and the face.
            val d = faceCentreToCylinderCentreX * normalX + faceCentreToCylinderCentreY * normalY

            // If d > r, the circle is on the front side of the face, and is farther than r away. This means that
            // the cylinder cannot possibly collide with any of the other faces either.
            if (d > cylinderRadius) {
                result.hit = CuboidHit.DECISIVE_MISS
                return
            }

            if (d < -cylinderRadius) {
                // The circle is completely on the back side of this face.

                val sizeAlongNormal = abs(normalX * cuboidSizeX) + abs(normalY * cuboidSizeY)

                if (d < -(cylinderRadius + sizeAlongNormal)) {
                    // The cylinder is completely on the other side of the cuboid and thus does not collide.
                    result.hit = CuboidHit.DECISIVE_MISS
                    return
                } else {
                    // The cylinder is between the plane of this face and the plane of the face on the other side
                    // of the cuboid. It depends on the other dimensions whether this is a hit or miss.
                    result.hit = CuboidHit.UNKNOWN
                    return
                }
            }

            // The circle collides with the plane of our face. Compute the pt on the plane closest to the circle.
            val closestPtOnPlaneX = centreX1 - d * normalX
            val closestPtOnPlaneY = centreY1 - d * normalY
            val closestPtOnPlaneZ = overlapZ

            val clampedX = clamp(closestPtOnPlaneX, centreX2 - cuboidHalfSizeX, centreX2 + cuboidHalfSizeX)
            val clampedY = clamp(closestPtOnPlaneY, centreY2 - cuboidHalfSizeY, centreY2 + cuboidHalfSizeY)
            val clampedZ = clamp(closestPtOnPlaneZ, centreZ2 - cuboidHalfSizeZ, centreZ2 + cuboidHalfSizeZ)

            result.apply {
                hitPt.set(clampedX, clampedY, clampedZ)
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

            result.hit = when {
                dsqr > cylinderRadiusSqr -> CuboidHit.UNKNOWN // cylinder doesn't collide with this face
                else -> when (bfcr?.getBrickFaceCovering(brickCoords, normalX, normalY, 0.0f)) {
                    null, BrickFaceCovering.NOT_COVERED -> CuboidHit.HIT_WITH_CLOSEST_PT_OUTSIDE_FACE
                    BrickFaceCovering.PARTIALLY_COVERED -> CuboidHit.HIT_PARTIALLY_COVERED_FACE
                    BrickFaceCovering.FULLY_COVERED -> CuboidHit.HIT_FULLY_COVERED_FACE
                }
            }
        }

        override fun checkHorizontalFace(normalZ: Float, result: CuboidCheckResults) {
            if (overlapHeight >= cuboidSizeZ) {
                // The z overlap covers the entire cuboid, so in case that there is in fact a collision, we expect
                // that both the top and bottom face as well as at least two of the vertical faces see a hit. We
                // leave it up to the vertical faces to decide!
                result.hit = CuboidHit.UNKNOWN
                return
            }

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

            // Find the point on the outline of the face that's closest to the cylinder.

            val outlineX: Float
            val outlineY: Float

            if (centreX1 <= faceXLo) {
                // The cylinder centre is on the north side of the face.
                outlineX = faceXLo
                outlineY = clamp(centreY1, faceYLo, faceYHi)
            } else if (centreX1 >= faceXHi) {
                // The cylinder centre is on the south side of the face.
                outlineX = faceXHi
                outlineY = clamp(centreY1, faceYLo, faceYHi)
            } else if (centreY1 <= faceYLo) {
                // The cylinder centre is on the west side of the face.
                outlineX = clamp(centreX1, faceXLo, faceXHi)
                outlineY = faceYLo
            } else if (centreY1 >= faceYHi) {
                // The cylinder centre is on the east side of the face.
                outlineX = clamp(centreX1, faceXLo, faceXHi)
                outlineY = faceYHi
            } else {
                // The cylinder centre is within the face, which is clearly a hit.
                result.hitPt.set(centreX1, centreY1, faceZ)
                result.distanceToClosestPtOnPlane = 0.0f

                result.hit = when (bfcr?.getBrickFaceCovering(brickCoords, 0.0f, 0.0f, normalZ)) {
                    null, BrickFaceCovering.NOT_COVERED -> CuboidHit.HIT_WITH_CLOSEST_PT_INSIDE_FACE
                    BrickFaceCovering.PARTIALLY_COVERED -> CuboidHit.HIT_PARTIALLY_COVERED_FACE
                    BrickFaceCovering.FULLY_COVERED -> CuboidHit.HIT_FULLY_COVERED_FACE
                }
                return
            }

            // Check if the closest point on the outline is in range of the cylinder radius.

            val dx = outlineX - centreX1
            val dy = outlineY - centreY1
            val dsqr = dx * dx + dy * dy

            if (dsqr > cylinderRadiusSqr) {
                result.hit = CuboidHit.UNKNOWN // this face wasn't hit, but other faces may still see a hit
            } else {
                result.hitPt.set(outlineX, outlineY, faceZ)
                result.distanceToClosestPtOnPlane = 0.0f

                result.hit = when (bfcr?.getBrickFaceCovering(brickCoords, 0.0f, 0.0f, normalZ)) {
                    null, BrickFaceCovering.NOT_COVERED -> CuboidHit.HIT_WITH_CLOSEST_PT_INSIDE_FACE
                    BrickFaceCovering.PARTIALLY_COVERED -> CuboidHit.HIT_PARTIALLY_COVERED_FACE
                    BrickFaceCovering.FULLY_COVERED -> CuboidHit.HIT_FULLY_COVERED_FACE
                }
            }
        }
    }

    override fun check(
        body1: FixedCylinderBody,
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

        cylinderRadius = body1.radius
        cylinderRadiusSqr = cylinderRadius * cylinderRadius
        cylinderHalfHeight = body1.height / 2.0f

        cuboidSizeX = body2.sizeX
        cuboidSizeY = body2.sizeY
        cuboidSizeZ = body2.sizeZ
        cuboidHalfSizeX = body2.halfSizeX
        cuboidHalfSizeY = body2.halfSizeY
        cuboidHalfSizeZ = body2.halfSizeZ

        minTop = min(centreZ2 + cuboidHalfSizeZ, centreZ1 + cylinderHalfHeight)
        maxBottom = max(centreZ2 - cuboidHalfSizeZ, centreZ1 - cylinderHalfHeight)
        overlapHeight = minTop - maxBottom

        if (overlapHeight <= 0) {
            return false // the cylinder is completely above or below the cuboid
        }

        overlapZ = maxBottom + overlapHeight * 0.5f

        val isCircleContainedInCuboid2D = (centreX2 - cuboidHalfSizeX <= centreX1 - cylinderRadius) &&
            (centreX2 + cuboidHalfSizeX >= centreX1 + cylinderRadius) &&
            (centreY2 - cuboidHalfSizeY <= centreY1 - cylinderRadius) &&
            (centreY2 + cuboidHalfSizeY >= centreY1 + cylinderRadius)

        if (isCircleContainedInCuboid2D) {
            // Since we already know that there is a z overlap, this must be a vertical hit!
            if (centreZ1 < overlapZ) {
                outHit?.apply {
                    hitPt.set(centreX1, centreY1, centreZ2 - cuboidHalfSizeZ)
                    area1 = HitArea.TOP_FACE // cylinder's top
                    area2 = HitArea.BOTTOM_FACE // cuboid's bottom
                    hitNormal12.set(0.0f, 0.0f, 1.0f)
                }
            } else {
                outHit?.apply {
                    hitPt.set(centreX1, centreY1, centreZ2 + cuboidHalfSizeZ)
                    area1 = HitArea.BOTTOM_FACE // cylinder's bottom
                    area2 = HitArea.TOP_FACE // cuboid's top
                    hitNormal12.set(0.0f, 0.0f, -1.0f)
                }
            }
            return true
        }

        // If there was a z overlap at the original position already, it's impossible that the top or bottom faces
        // can be hit in a straight line.

        val origMinTop = min(body2.pos.z + cuboidHalfSizeZ, body1.pos.z + cylinderHalfHeight)
        val origMaxBottom = max(body2.pos.z - cuboidHalfSizeZ, body1.pos.z - cylinderHalfHeight)
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

        when (face.area) {
            HitArea.TOP_FACE -> {
                outHit.apply {
                    area1 = HitArea.BOTTOM_FACE // the cylinder's bottom
                    area2 = HitArea.TOP_FACE // the cuboid's top
                    hitNormal12.set(0.0f, 0.0f, -1.0f)
                }
            }
            HitArea.BOTTOM_FACE -> {
                outHit.apply {
                    area1 = HitArea.TOP_FACE // the cylinder's top
                    area2 = HitArea.BOTTOM_FACE // the cuboid's bottom
                    hitNormal12.set(0.0f, 0.0f, 1.0f)
                }
            }
            else -> {
                outHit.apply {
                    area1 = HitArea.SIDE // the cylinder's side
                    area2 = face.area
                    val n = face.normal
                    hitNormal12.set(-n.x, -n.y, 0.0f)
                }
            }
        }

        return true
    }

    companion object {
        private val TAG = Log.Tag("CheckCylinderVsCuboid")
    }
}
