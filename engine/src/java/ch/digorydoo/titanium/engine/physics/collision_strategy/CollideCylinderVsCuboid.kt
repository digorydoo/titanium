package ch.digorydoo.titanium.engine.physics.collision_strategy

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.brick.BrickVolume.BrickFaceCovering
import ch.digorydoo.titanium.engine.physics.CuboidCheckResults
import ch.digorydoo.titanium.engine.physics.CuboidHit
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.HitResult
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sqrt

internal class CollideCylinderVsCuboid: CollisionStrategy<FixedCylinderBody, FixedCuboidBody>() {
    private sealed class FaceWithResults(val area: HitArea) {
        val checkResults = CuboidCheckResults()
    }

    private class VerticalFaceWithResults(area: HitArea, val normal: Point2f): FaceWithResults(area)
    private class HorizontalFaceWithResults(area: HitArea, val normal: Float): FaceWithResults(area)

    private val sides = listOf(
        VerticalFaceWithResults(HitArea.NORTH_FACE, Direction.northVector2D),
        VerticalFaceWithResults(HitArea.EAST_FACE, Direction.eastVector2D),
        VerticalFaceWithResults(HitArea.SOUTH_FACE, Direction.southVector2D),
        VerticalFaceWithResults(HitArea.WEST_FACE, Direction.westVector2D),
    )

    private val topBottom = listOf(
        HorizontalFaceWithResults(HitArea.TOP, 1.0f),
        HorizontalFaceWithResults(HitArea.BOTTOM, -1.0f),
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
        // Cylinder
        body1: FixedCylinderBody,
        centreX1: Float,
        centreY1: Float,
        centreZ1: Float,
        // Cuboid
        body2: FixedCuboidBody,
        centreX2: Float,
        centreY2: Float,
        centreZ2: Float,
        // out
        outHit: MutableHitResult?,
    ): Boolean {
        val cylinderRadius = body1.radius
        val cylinderRadiusSqr = cylinderRadius * cylinderRadius
        val cylinderHalfHeight = body1.height / 2.0f

        val cuboidSizeX = body2.sizeX
        val cuboidSizeY = body2.sizeY
        val cuboidSizeZ = body2.sizeZ
        val cuboidHalfSizeX = body2.halfSizeX
        val cuboidHalfSizeY = body2.halfSizeY
        val cuboidHalfSizeZ = body2.halfSizeZ

        val minTop = min(centreZ2 + cuboidHalfSizeZ, centreZ1 + cylinderHalfHeight)
        val maxBottom = max(centreZ2 - cuboidHalfSizeZ, centreZ1 - cylinderHalfHeight)
        val overlapHeight = minTop - maxBottom

        if (overlapHeight <= 0) {
            return false // the cylinder is completely above or below the cuboid
        }

        val overlapZ = maxBottom + overlapHeight * 0.5f

        val isCircleContainedInCuboid2D = (centreX2 - cuboidHalfSizeX <= centreX1 - cylinderRadius) &&
            (centreX2 + cuboidHalfSizeX >= centreX1 + cylinderRadius) &&
            (centreY2 - cuboidHalfSizeY <= centreY1 - cylinderRadius) &&
            (centreY2 + cuboidHalfSizeY >= centreY1 + cylinderRadius)

        if (isCircleContainedInCuboid2D) {
            // Since we already know that there is a z overlap, this must be a vertical hit!
            if (centreZ1 < overlapZ) {
                outHit?.apply {
                    hitPt.set(centreX1, centreY1, centreZ2 - cuboidHalfSizeZ)
                    area1 = HitArea.TOP // cylinder's top
                    area2 = HitArea.BOTTOM // cuboid's bottom
                    hitNormal12.set(0.0f, 0.0f, 1.0f)
                }
            } else {
                outHit?.apply {
                    hitPt.set(centreX1, centreY1, centreZ2 + cuboidHalfSizeZ)
                    area1 = HitArea.BOTTOM // cylinder's bottom
                    area2 = HitArea.TOP // cuboid's top
                    hitNormal12.set(0.0f, 0.0f, -1.0f)
                }
            }
            return true
        }

        val origMinTop = min(body2.pos.z + cuboidHalfSizeZ, body1.pos.z + cylinderHalfHeight)
        val origMaxBottom = max(body2.pos.z - cuboidHalfSizeZ, body1.pos.z - cylinderHalfHeight)
        val origOverlapHeight = origMinTop - origMaxBottom

        fun checkVerticalFace(normal: Point2f, result: CuboidCheckResults) {
            val normalX = normal.x
            val normalY = normal.y

            // Find the centre on the face described by the normal, and subtract it from the cylinder's centre.
            val faceCentreToCylinderCentreX = centreX1 - (centreX2 + normalX * cuboidHalfSizeX)
            val faceCentreToCylinderCentreY = centreY1 - (centreY2 + normalY * cuboidHalfSizeY)

            // Compute the projected distance between the cylinder's centre and the face.
            val d = faceCentreToCylinderCentreX * normal.x + faceCentreToCylinderCentreY * normal.y

            // If d > r, the circle is on the front side of the face, and is farther than r away. This means that the
            // cylinder cannot possibly collide with any of the other faces either.
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
                    // The cylinder is between the plane of this face and the plane of the face on the other side of the
                    // cuboid. It depends on the other dimensions whether this is a hit or miss.
                    result.hit = CuboidHit.UNKNOWN
                    return
                }
            }

            // The circle collides with the plane of our face. Compute the point on the plane closest to the circle.
            val closestPtOnPlaneX = centreX1 - d * normalX
            val closestPtOnPlaneY = centreY1 - d * normalY
            val closestPtOnPlaneZ = overlapZ

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
                else -> when (brickVolume?.getBrickFaceCovering(brickCoords, normalX, normalY, 0.0f)) {
                    null, BrickFaceCovering.NOT_COVERED -> CuboidHit.HIT_WITH_CLOSEST_PT_OUTSIDE_FACE
                    BrickFaceCovering.PARTIALLY_COVERED -> CuboidHit.HIT_PARTIALLY_COVERED_FACE
                    BrickFaceCovering.FULLY_COVERED -> CuboidHit.HIT_FULLY_COVERED_FACE
                }
            }
        }

        fun checkHorizontalFace(normalZ: Float, result: CuboidCheckResults) {
            if (overlapHeight >= cuboidSizeZ) {
                // The z overlap covers the entire cuboid, so in case that there is in fact a collision, we expect that
                // both the top and bottom face as well as at least two of the vertical faces see a hit. We leave it up
                // to the vertical faces to decide!
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
                result.hitPtValid = true
                result.hitPtX = centreX1
                result.hitPtY = centreY1
                result.hitPtZ = faceZ
                result.distanceToClosestPtOnPlane = 0.0f

                result.hit = when (brickVolume?.getBrickFaceCovering(brickCoords, 0.0f, 0.0f, normalZ)) {
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
                result.hitPtValid = true
                result.hitPtX = outlineX
                result.hitPtY = outlineY
                result.hitPtZ = faceZ
                result.distanceToClosestPtOnPlane = 0.0f

                result.hit = when (brickVolume?.getBrickFaceCovering(brickCoords, 0.0f, 0.0f, normalZ)) {
                    null, BrickFaceCovering.NOT_COVERED -> CuboidHit.HIT_WITH_CLOSEST_PT_INSIDE_FACE
                    BrickFaceCovering.PARTIALLY_COVERED -> CuboidHit.HIT_PARTIALLY_COVERED_FACE
                    BrickFaceCovering.FULLY_COVERED -> CuboidHit.HIT_FULLY_COVERED_FACE
                }
            }
        }

        fun checkFaces(faces: List<FaceWithResults>): FaceWithResults? {
            var bestMatchFace: FaceWithResults? = null
            var bestMatchHit = CuboidHit.UNKNOWN
            var bestMatchDist = Float.NEGATIVE_INFINITY

            for (face in faces) {
                val results = face.checkResults.apply {
                    hit = CuboidHit.UNKNOWN
                    hitPtValid = false
                }

                when (face) {
                    is VerticalFaceWithResults -> checkVerticalFace(face.normal, results)
                    is HorizontalFaceWithResults -> checkHorizontalFace(face.normal, results)
                }

                when (results.hit) {
                    CuboidHit.HIT_WITH_CLOSEST_PT_INSIDE_FACE -> return face // a clear hit
                    CuboidHit.DECISIVE_MISS -> return null // a clear miss
                    CuboidHit.UNKNOWN -> Unit // we still can't tell
                    CuboidHit.HIT_FULLY_COVERED_FACE -> {
                        // Actually, it's impossible that the cuboid was hit this way, but since the cuboid actually
                        // collided, we keep this as a fallback in the unlikely case that no other face collided.
                        if (bestMatchHit == CuboidHit.UNKNOWN) {
                            bestMatchFace = face
                            bestMatchHit = results.hit
                            bestMatchDist = Float.NEGATIVE_INFINITY
                        }
                    }
                    CuboidHit.HIT_PARTIALLY_COVERED_FACE -> {
                        // It's possible that the cuboid was hit this way, but there may be another face with a clearer
                        // hit.
                        if (bestMatchHit == CuboidHit.UNKNOWN || bestMatchHit == CuboidHit.HIT_FULLY_COVERED_FACE) {
                            bestMatchFace = face
                            bestMatchHit = results.hit
                            bestMatchDist = Float.NEGATIVE_INFINITY
                        }
                    }
                    CuboidHit.HIT_WITH_CLOSEST_PT_OUTSIDE_FACE -> {
                        // The face was hit, but the closest point on the plane is outside the face, which means that
                        // the cylinder overlaps one of the face edges. We counter-intuitively take the face whose
                        // closest point on its plane is *farther* away from the cylinder centre. (Make a drawing to see
                        // this.)
                        if (results.distanceToClosestPtOnPlane > bestMatchDist) {
                            bestMatchFace = face
                            bestMatchHit = results.hit
                            bestMatchDist = results.distanceToClosestPtOnPlane
                        }
                    }
                }
            }

            return bestMatchFace
        }

        fun determineTopBottomOrSide(topBottomFace: FaceWithResults, sidesFace: FaceWithResults): FaceWithResults {
            val tbfHit = topBottomFace.checkResults.hit
            val sfHit = sidesFace.checkResults.hit

            val decisive = when {
                tbfHit == CuboidHit.DECISIVE_MISS -> topBottomFace
                sfHit == CuboidHit.DECISIVE_MISS -> sidesFace
                sfHit == CuboidHit.UNKNOWN -> topBottomFace
                tbfHit == CuboidHit.UNKNOWN -> sidesFace
                sfHit == CuboidHit.HIT_FULLY_COVERED_FACE && tbfHit != CuboidHit.HIT_FULLY_COVERED_FACE -> topBottomFace
                tbfHit == CuboidHit.HIT_FULLY_COVERED_FACE && sfHit != CuboidHit.HIT_FULLY_COVERED_FACE -> sidesFace
                else -> null
            }

            if (decisive != null) return decisive

            if (origOverlapHeight > 0.0f) {
                // If there was a z overlap at the original position already, it's impossible that the top or bottom
                // faces can be hit in a straight line.
                return sidesFace
            }

            val speed1 = body1.speedBeforeCollisions
            val speed2 = body2.speedBeforeCollisions

            val relSpeedZ = speed1.z - speed2.z
            val origPosDz = body2.pos.z - body1.pos.z

            if (abs(relSpeedZ) <= EPSILON || sign(relSpeedZ) != sign(origPosDz)) {
                // The relative speed in Z is insignificant, or the Zs are moving away from each-other. It must be side!
                return sidesFace
            }

            val relSpeedX = speed1.x - speed2.x
            val relSpeedY = speed1.y - speed2.y
            val relSpeedXYLen = sqrt(relSpeedX * relSpeedX + relSpeedY * relSpeedY)

            if (relSpeedXYLen <= EPSILON) {
                // The relative speed in XY was insignificant, so it must be top or bottom.
                return topBottomFace
            }

            val origPosDx = body2.pos.x - body1.pos.x
            val origPosDy = body2.pos.y - body1.pos.y
            val origPosXYLen = sqrt(origPosDx * origPosDx + origPosDy * origPosDy)

            if (origPosXYLen <= EPSILON) {
                // The original positions were very close in XY, which we interpret as being top or bottom.
                return topBottomFace
            }

            val xyNormRelSpeedX = relSpeedX / relSpeedXYLen
            val xyNormRelSpeedY = relSpeedY / relSpeedXYLen

            val xyNormPosX = origPosDx / origPosXYLen
            val xyNormPosY = origPosDx / origPosXYLen

            val speedXYDotPosXY = xyNormRelSpeedX * xyNormPosX + xyNormRelSpeedY * xyNormPosY

            if (speedXYDotPosXY <= 0.0f) {
                // The bodies are moving away from each-other in XY. It must be top or bottom!
                return topBottomFace
            }

            // The speeds look towards each-other both in XY and Z. Prefer the TOP face, because mistaking it for a
            // side face when cylinder is standing near the edge of the top face can be distastrous for bricks!
            return when {
                topBottomFace.area == HitArea.TOP -> topBottomFace
                relSpeedXYLen > abs(relSpeedZ) -> sidesFace
                else -> topBottomFace
            }
        }

        val topBottomFace = checkFaces(topBottom)
        val sidesFace = checkFaces(sides)

        val face = when {
            topBottomFace == null -> sidesFace
            sidesFace == null -> topBottomFace
            else -> determineTopBottomOrSide(topBottomFace, sidesFace)
        }

        if (face == null) return false

        val results = face.checkResults

        if (results.hit == CuboidHit.UNKNOWN) {
            Log.warn(TAG, "checkFaces() returned UNKNOWN when it should have returned null: $face")
            return false
        }

        if (results.hit == CuboidHit.HIT_FULLY_COVERED_FACE) {
            Log.warn(TAG, "The face is fully covered and should not be the only one that was hit")
            // continue
        }

        // There was a hit. If the caller does not need to know the hit point, we're done.
        if (outHit == null) return true

        require(results.hitPtValid)
        outHit.hitPt.set(results.hitPtX, results.hitPtY, results.hitPtZ)

        when (face.area) {
            HitArea.TOP -> {
                outHit.apply {
                    area1 = HitArea.BOTTOM // the cylinder's bottom
                    area2 = HitArea.TOP // the cuboid's top
                    hitNormal12.set(0.0f, 0.0f, -1.0f)
                }
            }
            HitArea.BOTTOM -> {
                outHit.apply {
                    area1 = HitArea.TOP // the cylinder's top
                    area2 = HitArea.BOTTOM // the cuboid's bottom
                    hitNormal12.set(0.0f, 0.0f, 1.0f)
                }
            }
            else -> {
                outHit.apply {
                    area1 = HitArea.SIDE // the cylinder's side
                    area2 = face.area
                    val n = (face as VerticalFaceWithResults).normal
                    hitNormal12.set(-n.x, -n.y, 0.0f)
                }
            }
        }

        return true
    }

    override fun bounce(body1: FixedCylinderBody, body2: FixedCuboidBody, hit: HitResult) {
        when (hit.area2) {
            HitArea.TOP -> {
                val normDir12Z = -1.0f // points from body1 to body2
                separateVertically(body1, body2, normDir12Z)
                helper.applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z)

                val deltaSpeedZ = body2.speed.z - body1.speed.z // positive if cylinder falls down on cuboid

                if (deltaSpeedZ in 0.0f .. HOPPING_PREVENTION_MAX_SPEED) {
                    // Prevent cylinder standing on cuboid from constantly hopping due to gravity
                    body1.nextSpeed.z = body2.nextSpeed.z
                }

                helper.bounceAtHorizontalPlane(body1, body2, normDir12Z)
            }
            HitArea.BOTTOM -> {
                val normDir12Z = 1.0f // points from body1 to body2
                separateVertically(body1, body2, normDir12Z)
                helper.applyFriction(body1, body2, 0.0f, 0.0f, normDir12Z) // z points from body1 to body2

                val deltaSpeedZ = body1.speed.z - body2.speed.z // positive if cuboid falls down on cylinder

                if (deltaSpeedZ in 0.0f .. HOPPING_PREVENTION_MAX_SPEED) {
                    // Prevent cuboid standing on cylinder from constantly hopping due to gravity
                    body2.nextSpeed.z = body1.nextSpeed.z
                }

                helper.bounceAtHorizontalPlane(body1, body2, normDir12Z)
            }
            else -> {
                val normDir12X = hit.hitNormal12.x
                val normDir12Y = hit.hitNormal12.y

                separateHorizontally(body1, body2, normDir12X, normDir12Y)

                helper.apply {
                    applyFriction(body1, body2, normDir12X, normDir12Y, 0.0f)
                    bounceAtVerticalPlane(body1, body2, normDir12X, normDir12Y)
                }
            }
        }

        verifySeparation(body1, body2, hit)
    }

    private fun separateVertically(cylinder: FixedCylinderBody, cuboid: FixedCuboidBody, normDir12Z: Float) {
        val p1 = cylinder.nextPos
        val p2 = cuboid.nextPos

        val currentDistance = (p2.z - p1.z) * normDir12Z
        val requiredDistance = cylinder.height / 2.0f + cuboid.halfSizeZ + EPSILON
        val moveBy = requiredDistance - currentDistance

        if (moveBy <= 0.0f) {
            Log.warn(TAG, "separateVertically was called, but bodies seem to be vertically separated already")
            return
        }

        when {
            cylinder.mass < LARGE_MASS -> when {
                cuboid.mass < LARGE_MASS -> {
                    val move1By = moveBy * cuboid.mass / (cylinder.mass + cuboid.mass)
                    val move2By = moveBy - move1By

                    p1.z -= normDir12Z * move1By
                    p2.z += normDir12Z * move2By
                }
                else -> {
                    p1.z -= normDir12Z * moveBy
                }
            }
            cuboid.mass < LARGE_MASS -> {
                p2.z += normDir12Z * moveBy
            }
            else -> {
                Log.warn(TAG, "Separating $cylinder from $cuboid failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    private fun separateHorizontally(
        body1: FixedCylinderBody,
        body2: FixedCuboidBody,
        normDir12X: Float,
        normDir12Y: Float,
    ) {
        val p1 = body1.nextPos
        val p2 = body2.nextPos
        val cylinderRadius = body1.radius

        // Compute the position of the circle if we moved it completely to the other side of the plane.

        val distanceAlongNormal = (p2.x - p1.x) * normDir12X + (p2.y - p1.y) * normDir12Y
        val halfSizeDotN = body2.halfSizeX * normDir12X + body2.halfSizeY * normDir12Y
        val requiredDistanceAlongNormal = cylinderRadius + abs(halfSizeDotN)
        var moveBy = requiredDistanceAlongNormal - distanceAlongNormal

        if (moveBy + EPSILON <= 0.0f) {
            Log.warn(TAG, "separate was called, but bodies seem to be separated already")
            return
        }

        val m1x = p1.x - normDir12X * moveBy
        val m1y = p1.y - normDir12Y * moveBy

        // If the circle is close to a cuboid corner, we may have moved it too far.

        val distanceToPlane = distanceAlongNormal - abs(halfSizeDotN)
        val sphereCentreProjectedOntoPlaneX = p1.x + distanceToPlane * normDir12X
        val sphereCentreProjectedOntoPlaneY = p1.y + distanceToPlane * normDir12Y

        val clampedX = clamp(sphereCentreProjectedOntoPlaneX, p2.x - body2.halfSizeX, p2.x + body2.halfSizeX)
        val clampedY = clamp(sphereCentreProjectedOntoPlaneY, p2.y - body2.halfSizeY, p2.y + body2.halfSizeY)

        val m1cx = m1x - clampedX
        val m1cy = m1y - clampedY
        val sqrDistOfM1ToClamped = m1cx * m1cx + m1cy * m1cy
        val distanceOfM1ToClamped = sqrt(sqrDistOfM1ToClamped)
        val m1TooFar = distanceOfM1ToClamped - cylinderRadius

        if (m1TooFar > EPSILON) {
            // See physics.txt: "Separating a sphere and a cuboid"

            val m1dotn = m1x * normDir12X + m1y * normDir12Y
            val pcdotn = clampedX * normDir12X + clampedY * normDir12Y
            val b = 2.0f * (m1dotn - pcdotn)
            val c = sqrDistOfM1ToClamped - cylinderRadius * cylinderRadius
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

        moveBy += EPSILON

        when {
            body1.mass < LARGE_MASS -> when {
                body2.mass < LARGE_MASS -> {
                    val move1By = moveBy * body2.mass / (body1.mass + body2.mass)
                    val move2By = moveBy - move1By

                    p1.x -= normDir12X * move1By
                    p1.y -= normDir12Y * move1By

                    p2.x += normDir12X * move2By
                    p2.y += normDir12Y * move2By
                }
                else -> {
                    p1.x -= normDir12X * moveBy
                    p1.y -= normDir12Y * moveBy
                }
            }
            body2.mass < LARGE_MASS -> {
                p2.x += normDir12X * moveBy
                p2.y += normDir12Y * moveBy
            }
            else -> {
                Log.warn(TAG, "Separating $body1 from $body2 failed, because both bodies are LARGE_MASS")
                return
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("CollideCylinderVsCuboid")
    }
}
