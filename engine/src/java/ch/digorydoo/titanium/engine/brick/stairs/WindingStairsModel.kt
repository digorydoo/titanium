package ch.digorydoo.titanium.engine.brick.stairs

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.Side
import kotlin.math.PI
import kotlin.math.tan

class WindingStairsModel(
    private val lowEdge: Side,
    numSteps: Int,
    private val ccw: Boolean,
    private val relInsetUpFace: Float = 0.0f,
    private val relInsetDownFace: Float = 0.0f,
): AbstrBrickModel() {
    private class Step {
        val lowerInner = MutablePoint3f()
        val lowerOuter1 = MutablePoint3f()
        val lowerOuter2 = MutablePoint3f() // step's downside below next step
        val upperInner = MutablePoint3f()
        val upperOuter1 = MutablePoint3f() // above lowerOuter1
        val upperOuter2 = MutablePoint3f() // below next step
        val upFaceTexOuter1 = MutablePoint2f()
        val upFaceTexOuter2 = MutablePoint2f()
        var sideTexRelX = 0.0f
        var sideTexRelWidth = 0.0f
        var frontTexRelY = 0.0f
        var frontTexRelHeight = 0.0f
    }

    private val steps = Array(numSteps) { Step() }.also { require(it.size % 2 == 0) { "numSteps must be even!" } }
    private val upFaceTexInner = MutablePoint2f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val lowerZ = downside.pt0.z + relInsetDownFace * WORLD_BRICK_SIZE
        val upperZ = upside.pt0.z - relInsetUpFace * WORLD_BRICK_SIZE

        val edgeDir = when (lowEdge) {
            Side.NORTH -> if (ccw) Direction.westVector else Direction.eastVector
            Side.EAST -> if (ccw) Direction.northVector else Direction.southVector
            Side.SOUTH -> if (ccw) Direction.eastVector else Direction.westVector
            Side.WEST -> if (ccw) Direction.southVector else Direction.northVector
        }

        val ascentDir = when (lowEdge) {
            Side.NORTH -> Direction.southVector
            Side.EAST -> Direction.westVector
            Side.SOUTH -> Direction.northVector
            Side.WEST -> Direction.eastVector
        }

        val axis = when (lowEdge) {
            Side.SOUTH -> if (ccw) downside.pt0 else downside.pt1
            Side.EAST -> if (ccw) downside.pt1 else downside.pt3
            Side.NORTH -> if (ccw) downside.pt3 else downside.pt2
            Side.WEST -> if (ccw) downside.pt2 else downside.pt0
        }

        val texAtAxis = when (lowEdge) {
            Side.SOUTH -> if (ccw) Point2f.point01 else Point2f.point11
            Side.EAST -> if (ccw) Point2f.point11 else Point2f.point10
            Side.NORTH -> if (ccw) Point2f.point10 else Point2f.zero
            Side.WEST -> if (ccw) Point2f.zero else Point2f.point01
        }

        upFaceTexInner.set(texAtAxis)

        steps.forEachIndexed { i, step ->
            // At the start of this step

            val rel1 = i.toFloat() / steps.size
            val z1 = lerp(lowerZ, upperZ, rel1)
            val rho1 = rel1 * PI / 2.0
            val t1: Double
            val u1: Float
            val v1: Float

            if (rel1 < 0.5f) {
                t1 = tan(rho1) // 0..1
                u1 = (edgeDir.x + ascentDir.x * t1).toFloat()
                v1 = (edgeDir.y + ascentDir.y * t1).toFloat()
            } else {
                t1 = tan(PI / 2.0 - rho1) // 1..0
                u1 = (ascentDir.x + edgeDir.x * t1).toFloat()
                v1 = (ascentDir.y + edgeDir.y * t1).toFloat()
            }

            // At the end of this step / start of the next step

            val rel2 = (i + 1).toFloat() / steps.size
            val z2 = lerp(lowerZ, upperZ, rel2)
            val rho2 = rel2 * PI / 2.0
            val t2: Double
            val u2: Float
            val v2: Float

            if (rel2 < 0.5f) {
                t2 = tan(rho2) // 0..1
                u2 = (edgeDir.x + ascentDir.x * t2).toFloat()
                v2 = (edgeDir.y + ascentDir.y * t2).toFloat()
            } else {
                t2 = tan(PI / 2.0 - rho2) // 1..0
                u2 = (ascentDir.x + edgeDir.x * t2).toFloat()
                v2 = (ascentDir.y + edgeDir.y * t2).toFloat()
            }

            val x1 = WORLD_BRICK_SIZE * u1
            val y1 = WORLD_BRICK_SIZE * v1
            val x2 = WORLD_BRICK_SIZE * u2
            val y2 = WORLD_BRICK_SIZE * v2

            val tx1: Float
            val tx2: Float

            if (ccw) {
                tx1 = when {
                    rel1 < 0.5f -> t1.toFloat()
                    else -> 1.0f - t1.toFloat()
                }
                tx2 = when {
                    rel1 < 0.5f -> if (rel2 >= 0.5f) 1.0f else t2.toFloat()
                    else -> 1.0f - t2.toFloat()
                }
            } else {
                tx1 = when {
                    rel1 < 0.5f -> if (rel2 >= 0.5f) 0.0f else 1.0f - t2.toFloat()
                    else -> t2.toFloat()
                }
                tx2 = when {
                    rel1 < 0.5f -> 1.0f - t1.toFloat()
                    else -> t1.toFloat()
                }
            }

            val ty1 = (upperZ - z1) / WORLD_BRICK_SIZE // 1 <.. 0 (when no inset)
            val ty2 = (upperZ - z2) / WORLD_BRICK_SIZE // 1 ..< 0 (when no inset)

            step.apply {
                lowerInner.set(axis.x, axis.y, z1)
                lowerOuter1.set(axis.x + x1, axis.y + y1, z1)
                lowerOuter2.set(axis.x + x2, axis.y + y2, z1)
                upperInner.set(lowerInner.x, lowerInner.y, z2)
                upperOuter1.set(lowerOuter1.x, lowerOuter1.y, z2)
                upperOuter2.set(lowerOuter2.x, lowerOuter2.y, z2)
                upFaceTexOuter1.set(texAtAxis.x + v1, texAtAxis.y + u1)
                upFaceTexOuter2.set(texAtAxis.x + v2, texAtAxis.y + u2)
                sideTexRelX = tx1
                sideTexRelWidth = tx2 - tx1
                frontTexRelY = ty2
                frontTexRelHeight = ty1 - ty2
            }
        }
    }

    override fun tesselateUpFace(tess: Tesselator) {
        if (ccw) {
            steps.forEach { step ->
                tess.addTriangle(
                    step.upperInner,
                    step.upperOuter1,
                    step.upperOuter2,
                    upFaceIdx,
                    Direction.upVector,
                    upFaceTexInner,
                    step.upFaceTexOuter1,
                    step.upFaceTexOuter2,
                )
            }
        } else {
            steps.forEach { step ->
                tess.addTriangle(
                    step.upperInner,
                    step.upperOuter2,
                    step.upperOuter1,
                    upFaceIdx,
                    Direction.upVector,
                    upFaceTexInner,
                    step.upFaceTexOuter2,
                    step.upFaceTexOuter1,
                )
            }
        }
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (ccw) {
            steps.forEach { step ->
                tess.addTriangle(
                    step.lowerInner,
                    step.lowerOuter2,
                    step.lowerOuter1,
                    downFaceIdx,
                    Direction.downVector,
                    upFaceTexInner,
                    step.upFaceTexOuter2,
                    step.upFaceTexOuter1,
                    flipTexX = true,
                )
            }
        } else {
            steps.forEach { step ->
                tess.addTriangle(
                    step.lowerInner,
                    step.lowerOuter1,
                    step.lowerOuter2,
                    downFaceIdx,
                    Direction.downVector,
                    upFaceTexInner,
                    step.upFaceTexOuter1,
                    step.upFaceTexOuter2,
                    flipTexX = true,
                )
            }
        }
    }

    // The first step will be aligned with the face given by faceIdx; the other steps turn away
    private fun tesselateStepsFront(tess: Tesselator, faceIdx: Int, coveredByNeighbour: Boolean) {
        if (ccw) {
            steps.forEachIndexed { i, step ->
                if (!coveredByNeighbour || i > 0) {
                    tess.addQuad(
                        step.lowerInner,
                        step.lowerOuter1,
                        step.upperInner,
                        step.upperOuter1,
                        faceIdx,
                        null,
                        texRelY = step.frontTexRelY,
                        texRelHeight = step.frontTexRelHeight,
                    )
                }
            }
        } else {
            steps.forEachIndexed { i, step ->
                if (!coveredByNeighbour || i > 0) {
                    tess.addQuad(
                        step.lowerOuter1,
                        step.lowerInner,
                        step.upperOuter1,
                        step.upperInner,
                        faceIdx,
                        null,
                        texRelY = step.frontTexRelY,
                        texRelHeight = step.frontTexRelHeight,
                    )
                }
            }
        }
    }

    // The last step will be aligned with the face given by faceIdx; the other steps turn away
    private fun tesselateStepsBack(tess: Tesselator, faceIdx: Int, coveredByNeighbour: Boolean) {
        val lastIdx = steps.size - 1

        if (ccw) {
            steps.forEachIndexed { i, step ->
                if (!coveredByNeighbour || i < lastIdx) {
                    tess.addQuad(
                        step.lowerOuter2,
                        step.lowerInner,
                        step.upperOuter2,
                        step.upperInner,
                        faceIdx,
                        null,
                        texRelY = step.frontTexRelY,
                        texRelHeight = step.frontTexRelHeight,
                    )
                }
            }
        } else {
            steps.forEachIndexed { i, step ->
                if (!coveredByNeighbour || i < lastIdx) {
                    tess.addQuad(
                        step.lowerInner,
                        step.lowerOuter2,
                        step.upperInner,
                        step.upperOuter2,
                        faceIdx,
                        null,
                        texRelY = step.frontTexRelY,
                        texRelHeight = step.frontTexRelHeight,
                    )
                }
            }
        }
    }

    // The outer sides of the steps cover two brick sides; this is the first half
    private fun tesselateFirstStepsSide(tess: Tesselator, faceIdx: Int) {
        val halfIdx = steps.size / 2

        if (ccw) {
            steps.forEachIndexed { i, step ->
                if (i < halfIdx) {
                    tess.addQuad(
                        step.lowerOuter1,
                        step.lowerOuter2,
                        step.upperOuter1,
                        step.upperOuter2,
                        faceIdx,
                        null,
                        texRelX = step.sideTexRelX,
                        texRelWidth = step.sideTexRelWidth,
                        texRelY = step.frontTexRelY,
                        texRelHeight = step.frontTexRelHeight,
                    )
                }
            }
        } else {
            steps.forEachIndexed { i, step ->
                if (i < halfIdx) {
                    tess.addQuad(
                        step.lowerOuter2,
                        step.lowerOuter1,
                        step.upperOuter2,
                        step.upperOuter1,
                        faceIdx,
                        null,
                        texRelX = step.sideTexRelX,
                        texRelWidth = step.sideTexRelWidth,
                        texRelY = step.frontTexRelY,
                        texRelHeight = step.frontTexRelHeight,
                    )
                }
            }
        }
    }

    // The outer sides of the steps cover two brick sides; this is the second half
    private fun tesselateLastStepsSide(tess: Tesselator, faceIdx: Int) {
        val halfIdx = steps.size / 2

        if (ccw) {
            steps.forEachIndexed { i, step ->
                if (i >= halfIdx) {
                    tess.addQuad(
                        step.lowerOuter1,
                        step.lowerOuter2,
                        step.upperOuter1,
                        step.upperOuter2,
                        faceIdx,
                        null,
                        texRelX = step.sideTexRelX,
                        texRelWidth = step.sideTexRelWidth,
                        texRelY = step.frontTexRelY,
                        texRelHeight = step.frontTexRelHeight,
                    )
                }
            }
        } else {
            steps.forEachIndexed { i, step ->
                if (i >= halfIdx) {
                    tess.addQuad(
                        step.lowerOuter2,
                        step.lowerOuter1,
                        step.upperOuter2,
                        step.upperOuter1,
                        faceIdx,
                        null,
                        texRelX = step.sideTexRelX,
                        texRelWidth = step.sideTexRelWidth,
                        texRelY = step.frontTexRelY,
                        texRelHeight = step.frontTexRelHeight,
                    )
                }
            }
        }
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        val covered = isBrickToNorthFullyCoveringMyNorthFace()
        when (lowEdge) {
            Side.NORTH -> tesselateStepsFront(tess, northFaceIdx, covered)
            Side.SOUTH -> if (!covered) tesselateLastStepsSide(tess, northFaceIdx)
            Side.WEST -> when (ccw) {
                true -> tesselateStepsBack(tess, northFaceIdx, covered)
                false -> if (!covered) tesselateFirstStepsSide(tess, northFaceIdx)
            }
            Side.EAST -> when (ccw) {
                true -> if (!covered) tesselateFirstStepsSide(tess, northFaceIdx)
                false -> tesselateStepsBack(tess, northFaceIdx, covered)
            }
        }
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        val covered = isBrickToSouthFullyCoveringMySouthFace()
        when (lowEdge) {
            Side.SOUTH -> tesselateStepsFront(tess, southFaceIdx, covered)
            Side.NORTH -> if (!covered) tesselateLastStepsSide(tess, southFaceIdx)
            Side.EAST -> when (ccw) {
                true -> tesselateStepsBack(tess, southFaceIdx, covered)
                false -> if (!covered) tesselateFirstStepsSide(tess, southFaceIdx)
            }
            Side.WEST -> when (ccw) {
                true -> if (!covered) tesselateFirstStepsSide(tess, southFaceIdx)
                false -> tesselateStepsBack(tess, southFaceIdx, covered)
            }
        }
    }

    override fun tesselateEastFace(tess: Tesselator) {
        val covered = isBrickToEastFullyCoveringMyEastFace()
        when (lowEdge) {
            Side.EAST -> tesselateStepsFront(tess, eastFaceIdx, covered)
            Side.WEST -> if (!covered) tesselateLastStepsSide(tess, eastFaceIdx)
            Side.NORTH -> when (ccw) {
                true -> tesselateStepsBack(tess, eastFaceIdx, covered)
                false -> if (!covered) tesselateFirstStepsSide(tess, eastFaceIdx)
            }
            Side.SOUTH -> when (ccw) {
                true -> if (!covered) tesselateFirstStepsSide(tess, eastFaceIdx)
                false -> tesselateStepsBack(tess, eastFaceIdx, covered)
            }
        }
    }

    override fun tesselateWestFace(tess: Tesselator) {
        val covered = isBrickToWestFullyCoveringMyWestFace()
        when (lowEdge) {
            Side.WEST -> tesselateStepsFront(tess, westFaceIdx, covered)
            Side.EAST -> if (!covered) tesselateLastStepsSide(tess, westFaceIdx)
            Side.SOUTH -> when (ccw) {
                true -> tesselateStepsBack(tess, westFaceIdx, covered)
                false -> if (!covered) tesselateFirstStepsSide(tess, westFaceIdx)
            }
            Side.NORTH -> when (ccw) {
                true -> if (!covered) tesselateFirstStepsSide(tess, westFaceIdx)
                false -> tesselateStepsBack(tess, westFaceIdx, covered)
            }
        }
    }

    override fun heightAt(x: Float, y: Float) = upside.pt0.z // TODO
}
