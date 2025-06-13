package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt

internal class CuboidCheckHelper(private val delegate: Delegate) {
    interface Delegate {
        fun checkVerticalFace(normalX: Float, normalY: Float, result: CuboidCheckResults)
        fun checkHorizontalFace(normalZ: Float, result: CuboidCheckResults)
    }

    enum class WhenEdge { PREFER_HORIZONTAL_FACE, PREFER_VERTICAL_FACE, INSPECT_SPEEDS }

    enum class CuboidHit {
        UNKNOWN, // results not known yet, or results depend on other faces
        HIT_WITH_CLOSEST_PT_INSIDE_FACE, // a clear hit within the cuboid face
        HIT_WITH_CLOSEST_PT_OUTSIDE_FACE, // a hit of the edge or corner of the face
        HIT_PARTIALLY_COVERED_FACE, // a less likely hit with a face that's partially covered
        HIT_FULLY_COVERED_FACE, // a rather unlikely hit with a face that seems fully covered
        DECISIVE_MISS, // the face was not hit, and a hit with another face of the cuboid is not possible
    }

    class CuboidCheckResults {
        var hit = CuboidHit.UNKNOWN
        val hitPt = MutablePoint3f(Float.NaN, Float.NaN, Float.NaN)
        val hitNormal12 = MutablePoint3f(Float.NaN, Float.NaN, Float.NaN)
        var distanceToClosestPtOnPlane = 0.0f

        fun setDefaults(cuboidFaceNormal: Point3f) {
            hit = CuboidHit.UNKNOWN
            hitPt.set(Float.NaN, Float.NaN, Float.NaN)
            hitNormal12.set(-cuboidFaceNormal.x, -cuboidFaceNormal.y, -cuboidFaceNormal.z) // cuboid is body2
            distanceToClosestPtOnPlane = 0.0f
        }
    }

    sealed class CuboidFace(val area: HitArea, val normal: Point3f) {
        val checkResults = CuboidCheckResults()
    }

    class CuboidVerticalFace(area: HitArea, normal: Point3f): CuboidFace(area, normal)
    class CuboidHorizontalFace(area: HitArea, normal: Point3f): CuboidFace(area, normal)

    private val sides = listOf(
        CuboidVerticalFace(HitArea.NORTH_FACE, Direction.northVector),
        CuboidVerticalFace(HitArea.EAST_FACE, Direction.eastVector),
        CuboidVerticalFace(HitArea.SOUTH_FACE, Direction.southVector),
        CuboidVerticalFace(HitArea.WEST_FACE, Direction.westVector),
    )

    private val topBottom = listOf(
        CuboidHorizontalFace(HitArea.TOP_FACE, Direction.upVector),
        CuboidHorizontalFace(HitArea.BOTTOM_FACE, Direction.downVector),
    )

    fun check(body1: RigidBody, body2: FixedCuboidBody, whenEdge: WhenEdge): CuboidFace? {
        val topBottomFace = checkFaces(topBottom)
        val sidesFace = checkFaces(sides)

        return when {
            topBottomFace == null -> sidesFace
            sidesFace == null -> topBottomFace
            else -> determineTopBottomOrSide(body1, body2, topBottomFace, sidesFace, whenEdge)
        }
    }

    private fun checkFaces(faces: List<CuboidFace>): CuboidFace? {
        var bestMatchFace: CuboidFace? = null
        var bestMatchHit = CuboidHit.UNKNOWN
        var bestMatchDist = Float.NEGATIVE_INFINITY

        for (face in faces) {
            val result = face.checkResults
            result.setDefaults(face.normal)

            when (face) {
                is CuboidVerticalFace -> delegate.checkVerticalFace(face.normal.x, face.normal.y, result)
                is CuboidHorizontalFace -> delegate.checkHorizontalFace(face.normal.z, result)
            }

            when (result.hit) {
                CuboidHit.HIT_WITH_CLOSEST_PT_INSIDE_FACE -> return face // a clear hit
                CuboidHit.DECISIVE_MISS -> return null // a clear miss
                CuboidHit.UNKNOWN -> Unit // we still can't tell
                CuboidHit.HIT_FULLY_COVERED_FACE -> {
                    // Actually, it's impossible that the cuboid was hit this way, but since the cuboid actually
                    // collided, we keep this as a fallback in the unlikely case that no other face collided.
                    if (bestMatchHit == CuboidHit.UNKNOWN) {
                        bestMatchFace = face
                        bestMatchHit = result.hit
                        bestMatchDist = Float.NEGATIVE_INFINITY
                    }
                }
                CuboidHit.HIT_PARTIALLY_COVERED_FACE -> {
                    // It's possible that the cuboid was hit this way, but there may be another face with a clearer
                    // hit.
                    if (bestMatchHit == CuboidHit.UNKNOWN || bestMatchHit == CuboidHit.HIT_FULLY_COVERED_FACE) {
                        bestMatchFace = face
                        bestMatchHit = result.hit
                        bestMatchDist = Float.NEGATIVE_INFINITY
                    }
                }
                CuboidHit.HIT_WITH_CLOSEST_PT_OUTSIDE_FACE -> {
                    // The face was hit, but the closest point on the plane is outside the face, which means that
                    // the body overlaps one of the face edges. We counter-intuitively take the face whose closest pt
                    // on its plane is *farther* away from the cylinder centre. (Make a drawing to see this.)
                    if (result.distanceToClosestPtOnPlane > bestMatchDist) {
                        bestMatchFace = face
                        bestMatchHit = result.hit
                        bestMatchDist = result.distanceToClosestPtOnPlane
                    }
                }
            }
        }

        return bestMatchFace
    }

    private fun determineTopBottomOrSide(
        body1: RigidBody,
        body2: FixedCuboidBody,
        topBottomFace: CuboidFace,
        sidesFace: CuboidFace,
        whenEdge: WhenEdge,
    ): CuboidFace {
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

        if (whenEdge == WhenEdge.PREFER_VERTICAL_FACE) {
            return sidesFace
        } else if (whenEdge == WhenEdge.PREFER_HORIZONTAL_FACE) {
            return topBottomFace
        }

        // whenEdge must be INSPECT_SPEEDS

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
        // side face when the body is standing near the edge of the top face can be distastrous for bricks!
        return when {
            topBottomFace.area == HitArea.TOP_FACE -> topBottomFace
            relSpeedXYLen > abs(relSpeedZ) -> sidesFace
            else -> topBottomFace
        }
    }
}
