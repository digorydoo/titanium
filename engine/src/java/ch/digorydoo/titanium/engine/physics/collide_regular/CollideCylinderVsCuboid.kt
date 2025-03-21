package ch.digorydoo.titanium.engine.physics.collide_regular

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.brick.BrickVolume.BrickFaceCovering
import ch.digorydoo.titanium.engine.physics.*
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
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

        val cuboidSizeX = body2.size.x
        val cuboidSizeY = body2.size.y
        val cuboidSizeZ = body2.size.y
        val cuboidHalfSizeX = body2.halfSize.x
        val cuboidHalfSizeY = body2.halfSize.y
        val cuboidHalfSizeZ = body2.halfSize.z

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
                }
            } else {
                outHit?.apply {
                    hitPt.set(centreX1, centreY1, centreZ2 + cuboidHalfSizeZ)
                    area1 = HitArea.BOTTOM // cylinder's bottom
                    area2 = HitArea.TOP // cuboid's top
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
                // The original positions were very close in XY, which we interprete as being top or bottom.
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

            // The speeds look towards each-other both in XY and Z.
            return if (relSpeedXYLen > abs(relSpeedZ)) sidesFace else topBottomFace
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
            Log.warn("checkFaces() returned UNKNOWN when it should have returned null: $face")
            return false
        }

        if (results.hit == CuboidHit.HIT_FULLY_COVERED_FACE) {
            Log.warn("The face is fully covered and should not be the only one that was hit")
            // continue
        }

        // There was a hit. If the caller does not need to know the hit point, we're done.
        if (outHit == null) return true

        require(results.hitPtValid)
        outHit.hitPt.set(results.hitPtX, results.hitPtY, results.hitPtZ)

        when (face.area) {
            HitArea.TOP -> {
                outHit.area1 = HitArea.BOTTOM // the cylinder's bottom
                outHit.area2 = HitArea.TOP // the cuboid's top
            }
            HitArea.BOTTOM -> {
                outHit.area1 = HitArea.TOP // the cylinder's top
                outHit.area2 = HitArea.BOTTOM // the cuboid's bottom
            }
            else -> {
                outHit.area1 = HitArea.SIDE // the cylinder's side
                outHit.area2 = face.area
            }
        }

        return true
    }

    override fun bounce(body1: FixedCylinderBody, body2: FixedCuboidBody, hit: HitResult) {
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
        throw NotImplementedError()

        // TODO
        // val elasticity = body1.elasticity * body2.elasticity
        //
        // val v1dotn = v1.x * normDir12X + v1.y * normDir12Y + v1.z * normDir12Z
        // val v1parallelX = normDir12X * v1dotn
        // val v1parallelY = normDir12Y * v1dotn
        // val v1parallelZ = normDir12Z * v1dotn
        //
        // val v2dotn = v2.x * normDir12X + v2.y * normDir12Y + v2.z * normDir12Z
        // val v2parallelX = normDir12X * v2dotn
        // val v2parallelY = normDir12Y * v2dotn
        // val v2parallelZ = normDir12Z * v2dotn
        //
        // val vparallelDx = v1parallelX - v2parallelX
        // val vparallelDy = v1parallelY - v2parallelY
        // val vparallelDz = v1parallelZ - v2parallelZ
        //
        // if (m1 >= LARGE_MASS) {
        //     val v2perpendX = v2.x - v2parallelX
        //     val v2perpendY = v2.y - v2parallelY
        //     val v2perpendZ = v2.z - v2parallelZ
        //
        //     v2.x = v2perpendX + v1parallelX + vparallelDx * elasticity
        //     v2.y = v2perpendY + v1parallelY + vparallelDy * elasticity
        //     v2.z = v2perpendZ + v1parallelZ + vparallelDz * elasticity
        // } else if (m2 >= LARGE_MASS) {
        //     val v1perpendX = v1.x - v1parallelX
        //     val v1perpendY = v1.y - v1parallelY
        //     val v1perpendZ = v1.z - v1parallelZ
        //
        //     v1.x = v1perpendX + v2parallelX - vparallelDx * elasticity
        //     v1.y = v1perpendY + v2parallelY - vparallelDy * elasticity
        //     v1.z = v1perpendZ + v2parallelZ - vparallelDz * elasticity
        // } else {
        //     val v1perpendX = v1.x - v1parallelX
        //     val v1perpendY = v1.y - v1parallelY
        //     val v1perpendZ = v1.z - v1parallelZ
        //
        //     val v2perpendX = v2.x - v2parallelX
        //     val v2perpendY = v2.y - v2parallelY
        //     val v2perpendZ = v2.z - v2parallelZ
        //
        //     val totalMass = m1 + m2
        //
        //     val sx = v1parallelX * m1 + v2parallelX * m2
        //     val sy = v1parallelY * m1 + v2parallelY * m2
        //     val sz = v1parallelZ * m1 + v2parallelZ * m2
        //
        //     v1.x = v1perpendX + (sx - vparallelDx * elasticity * m2) / totalMass
        //     v1.y = v1perpendY + (sy - vparallelDy * elasticity * m2) / totalMass
        //     v1.z = v1perpendZ + (sz - vparallelDz * elasticity * m2) / totalMass
        //
        //     v2.x = v2perpendX + (sx + vparallelDx * elasticity * m1) / totalMass
        //     v2.y = v2perpendY + (sy + vparallelDy * elasticity * m1) / totalMass
        //     v2.z = v2perpendZ + (sz + vparallelDz * elasticity * m1) / totalMass
        // }
    }

    override fun forceApart(
        body1: FixedCylinderBody,
        body2: FixedCuboidBody,
        normDirX1: Float,
        normDirY1: Float,
        normDirZ1: Float,
        hit: HitResult,
    ) {
        throw NotImplementedError()
        // TODO
        // val p1 = body1.nextPos
        // val p2 = body2.nextPos
        //
        // val normal = when (hit.area2) {
        //     HitArea.NORTH_FACE -> Direction.northVector
        //     HitArea.EAST_FACE -> Direction.eastVector
        //     HitArea.SOUTH_FACE -> Direction.southVector
        //     HitArea.WEST_FACE -> Direction.westVector
        //     HitArea.TOP -> Direction.upVector
        //     HitArea.BOTTOM -> Direction.downVector
        //     else -> throw UnexpectedHitAreaError(hit.area2)
        // }
        //
        // val nx = normal.x
        // val ny = normal.y
        // val nz = normal.z
        //
        // val divisor = normDirX1 * nx + normDirY1 * ny + normDirZ1 * nz
        //
        // if (abs(divisor) < EPSILON) {
        //     Log.warn("Cannot force $body1 and $body2 apart, because the divisor is too small")
        //     return
        // }
        //
        // val faceCentreX = p2.x + nx * body2.halfSize.x
        // val faceCentreY = p2.y + ny * body2.halfSize.y
        // val faceCentreZ = p2.z + nz * body2.halfSize.z
        //
        // val r = body1.radius
        //
        // val k = (nx * (r * nx - p1.x + faceCentreX) +
        //     ny * (r * ny - p1.y + faceCentreY) +
        //     nz * (r * nz - p1.z + faceCentreZ)) / divisor
        //
        // if (k < 0.0f) {
        //     Log.warn("Cannot force $body1 and $body2 apart, because moveBy has an unexpected value: $k")
        //     return
        // }
        //
        // val moveBy = k + 2.0f * EPSILON
        //
        // if (body1.mass >= LARGE_MASS) {
        //     if (body2.mass >= LARGE_MASS) {
        //         Log.warn("Cannot force $body1 and $body2 apart, because both are LARGE_MASS")
        //     } else {
        //         p2.x = p1.x - normDirX1 * moveBy
        //         p2.y = p1.y - normDirY1 * moveBy
        //         p2.z = p1.z - normDirZ1 * moveBy
        //     }
        // } else if (body2.mass >= LARGE_MASS) {
        //     p1.x = p2.x + normDirX1 * moveBy
        //     p1.y = p2.y + normDirY1 * moveBy
        //     p1.z = p2.z + normDirZ1 * moveBy
        // } else {
        //     val half = moveBy / 2.0f
        //
        //     p1.x += normDirX1 * half
        //     p1.y += normDirY1 * half
        //     p1.z += normDirZ1 * half
        //
        //     p2.x -= normDirX1 * half
        //     p2.y -= normDirY1 * half
        //     p2.z -= normDirZ1 * half
        // }
    }
}
