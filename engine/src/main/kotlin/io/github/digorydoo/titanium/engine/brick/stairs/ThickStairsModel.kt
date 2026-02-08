package io.github.digorydoo.titanium.engine.brick.stairs

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.MutableVector3f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.brick.AbstrBrickModel
import io.github.digorydoo.titanium.engine.brick.BrickShape
import io.github.digorydoo.titanium.engine.brick.BrickSubvolume
import io.github.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import io.github.digorydoo.titanium.engine.brick.THICK_RELSIZE
import io.github.digorydoo.titanium.engine.brick.Tesselator
import io.github.digorydoo.titanium.engine.utils.Direction
import io.github.digorydoo.titanium.engine.utils.Side

class ThickStairsModel(
    private val lowEdge: Side,
    numSteps: Int,
    private val relInsetUpFace: Float = 0.0f,
    private val relInsetDownFace: Float = 0.0f,
    private val stretch: Float = 1.0f,
    private val firstStepStretch: Float = 1.0f,
    private val firstStepRelHeight: Float = 1.0f,
    private val lastStepRelHeight: Float = 1.0f,
): AbstrBrickModel() {
    private class Step {
        val lowerPt0 = MutableVector3f()
        val lowerPt1 = MutableVector3f()
        val lowerPt2 = MutableVector3f()
        val lowerPt3 = MutableVector3f() // step's downside below next step
        val upperPt0 = MutableVector3f()
        val upperPt1 = MutableVector3f()
        val upperPt2 = MutableVector3f()
        val upperPt3 = MutableVector3f() // below next step
        var upFaceTexPt0 = MutableVector2f()
        var upFaceTexPt1 = MutableVector2f()
        var upFaceTexPt2 = MutableVector2f()
        var upFaceTexPt3 = MutableVector2f()
        var rightSideTexRelX = 0.0f
        var rightSideTexRelWidth = 0.0f
        var frontTexRelY = 0.0f
        var frontTexRelHeight = 0.0f
    }

    private val steps = Array(numSteps) { Step() }
    private val lowerBackPt2 = MutableVector3f()
    private val lowerBackPt3 = MutableVector3f()
    private val upperBackPt2 = MutableVector3f()
    private val upperBackPt3 = MutableVector3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val shape = upside.brick.shape

        val overlapsBrickBelow = when (lowEdge) {
            Side.NORTH -> shape == BrickShape.THICK_STAIRS_32_LOWER_SOUTH && shapeToNorth == BrickShape.NONE
            Side.EAST -> shape == BrickShape.THICK_STAIRS_32_LOWER_WEST && shapeToEast == BrickShape.NONE
            Side.SOUTH -> shape == BrickShape.THICK_STAIRS_32_LOWER_NORTH && shapeToSouth == BrickShape.NONE
            Side.WEST -> shape == BrickShape.THICK_STAIRS_32_LOWER_EAST && shapeToWest == BrickShape.NONE
        }

        val lowerOverlapsBrickBelow = when (lowEdge) {
            Side.NORTH -> shapeToNorth == BrickShape.THICK_STAIRS_32_LOWER_SOUTH &&
                subvolume.getShape(ix - 2, iy, iz, acrossBounds = true) == BrickShape.NONE
            Side.EAST -> shapeToEast == BrickShape.THICK_STAIRS_32_LOWER_WEST &&
                subvolume.getShape(ix, iy + 2, iz, acrossBounds = true) == BrickShape.NONE
            Side.SOUTH -> shapeToSouth == BrickShape.THICK_STAIRS_32_LOWER_NORTH &&
                subvolume.getShape(ix + 2, iy, iz, acrossBounds = true) == BrickShape.NONE
            Side.WEST -> shapeToWest == BrickShape.THICK_STAIRS_32_LOWER_EAST &&
                subvolume.getShape(ix, iy - 2, iz, acrossBounds = true) == BrickShape.NONE
        }

        val numSteps = steps.size
        val stepSize = ((1.0f - relInsetUpFace) - relInsetDownFace) * WORLD_BRICK_SIZE / numSteps
        val upperStepsRatio = 3.0f / 7.0f // upper has 3 steps, lower has 4 steps

        val lowerZ = downside.pt0.z + relInsetDownFace * WORLD_BRICK_SIZE - when {
            overlapsBrickBelow -> THICK_RELSIZE * WORLD_BRICK_SIZE - stepSize
            lowerOverlapsBrickBelow -> stepSize * upperStepsRatio
            else -> 0.0f
        }
        val upperZ = upside.pt0.z - relInsetUpFace * WORLD_BRICK_SIZE - when (overlapsBrickBelow) {
            true -> (THICK_RELSIZE * WORLD_BRICK_SIZE - stepSize) * upperStepsRatio
            false -> 0.0f
        }

        val edgeDir = when (lowEdge) {
            Side.NORTH -> Direction.westVector
            Side.EAST -> Direction.northVector
            Side.SOUTH -> Direction.eastVector
            Side.WEST -> Direction.southVector
        }

        val ascentDir = when (lowEdge) {
            Side.NORTH -> Direction.southVector
            Side.EAST -> Direction.westVector
            Side.SOUTH -> Direction.northVector
            Side.WEST -> Direction.eastVector
        }

        val origin = when (lowEdge) {
            Side.SOUTH -> downside.pt0
            Side.EAST -> downside.pt1
            Side.NORTH -> downside.pt3
            Side.WEST -> downside.pt2
        }

        val backX = origin.x + WORLD_BRICK_SIZE * ascentDir.x
        val backY = origin.y + WORLD_BRICK_SIZE * ascentDir.y

        val backZ = lerp(
            lowerZ - (lastStepRelHeight - 1.0f) * WORLD_BRICK_SIZE - when (overlapsBrickBelow) {
                true -> THICK_RELSIZE * WORLD_BRICK_SIZE - stepSize // why is this necessary?
                false -> 0.0f
            },
            upperZ,
            (numSteps - 1).toFloat() / numSteps
        )

        val stepsOffset = stretch * (firstStepStretch - 1.0f) / numSteps

        steps.forEachIndexed { i, step ->
            // At the start of this step

            val rel1 = i.toFloat() / numSteps
            val rx0 = ascentDir.x * clamp(rel1 * stretch + stepsOffset)
            val ry0 = ascentDir.y * clamp(rel1 * stretch + stepsOffset)
            val rx1 = edgeDir.x + rx0
            val ry1 = edgeDir.y + ry0

            // At the end of this step / start of the next step

            val rel2 = (i + 1).toFloat() / numSteps
            val rx2 = ascentDir.x * clamp(rel2 * stretch + stepsOffset)
            val ry2 = ascentDir.y * clamp(rel2 * stretch + stepsOffset)
            val rx3 = edgeDir.x + rx2
            val ry3 = edgeDir.y + ry2

            val z0 = when {
                i == 0 -> lowerZ - (firstStepRelHeight - 1.0f) * WORLD_BRICK_SIZE - when (lowerOverlapsBrickBelow) {
                    true -> 0.019f // why is this necessary?
                    false -> 0.0f
                }
                i == 1 -> lerp(lowerZ, upperZ, rel1)
                i == numSteps - 1 && stretch > 1.0f -> lerp(lowerZ, upperZ, 1.0f / stretch - 1.0f / numSteps)
                else -> lerp(lowerZ, upperZ, rel1)
            }

            val z1 = when {
                i == 0 -> lowerZ - 0.25f * (1.0f - firstStepStretch) / numSteps
                i == numSteps - 1 -> backZ
                stretch <= 1.0f -> lerp(
                    lowerZ - 0.25f * (1.0f - firstStepStretch) / numSteps,
                    backZ,
                    i.toFloat() / (numSteps - 1)
                )
                else -> {
                    // Where does that 1.05f come from? Something's not right, but I don't care at the moment...
                    lerp(lowerZ, backZ, 1.05f * stretch * i.toFloat() / (numSteps - 1))
                }
            }

            val z2 = lerp(lowerZ, upperZ, rel2)

            val x0 = origin.x + WORLD_BRICK_SIZE * rx0
            val y0 = origin.y + WORLD_BRICK_SIZE * ry0
            val x1 = origin.x + WORLD_BRICK_SIZE * rx1
            val y1 = origin.y + WORLD_BRICK_SIZE * ry1
            val x2 = origin.x + WORLD_BRICK_SIZE * rx2
            val y2 = origin.y + WORLD_BRICK_SIZE * ry2
            val x3 = origin.x + WORLD_BRICK_SIZE * rx3
            val y3 = origin.y + WORLD_BRICK_SIZE * ry3

            val tz1 = (upperZ - z0) / WORLD_BRICK_SIZE // 1 <.. 0 (when no inset)
            val tz2 = (upperZ - z2) / WORLD_BRICK_SIZE // 1 ..< 0 (when no inset)

            step.apply {
                lowerPt0.set(x0, y0, z0)
                lowerPt1.set(x1, y1, z0)
                lowerPt2.set(x2, y2, z1)
                lowerPt3.set(x3, y3, z1)

                upperPt0.set(x0, y0, z2)
                upperPt1.set(x1, y1, z2)
                upperPt2.set(x2, y2, z2)
                upperPt3.set(x3, y3, z2)

                // FIXME some tex coords are wrong!

                when (lowEdge) {
                    Side.NORTH -> {
                        upFaceTexPt0.set(1.0f, rel1)
                        upFaceTexPt1.set(0.0f, rel1)
                        upFaceTexPt2.set(1.0f, rel2)
                        upFaceTexPt3.set(0.0f, rel2)
                    }
                    Side.EAST -> {
                        upFaceTexPt0.set(1.0f - rel1, 1.0f)
                        upFaceTexPt1.set(1.0f - rel1, 0.0f)
                        upFaceTexPt2.set(1.0f - rel2, 1.0f)
                        upFaceTexPt3.set(1.0f - rel2, 0.0f)
                    }
                    Side.SOUTH -> {
                        upFaceTexPt0.set(0.0f, 1.0f - rel1)
                        upFaceTexPt1.set(1.0f, 1.0f - rel1)
                        upFaceTexPt2.set(0.0f, 1.0f - rel2)
                        upFaceTexPt3.set(1.0f, 1.0f - rel2)
                    }
                    Side.WEST -> {
                        upFaceTexPt0.set(rel1, 0.0f)
                        upFaceTexPt1.set(rel1, 1.0f)
                        upFaceTexPt2.set(rel2, 0.0f)
                        upFaceTexPt3.set(rel2, 1.0f)
                    }
                }

                rightSideTexRelX = rel1
                rightSideTexRelWidth = rel2 - rel1
                frontTexRelY = tz2
                frontTexRelHeight = tz1 - tz2
            }
        }

        lowerBackPt2.set(backX, backY, backZ)
        upperBackPt2.set(backX, backY, upperZ)
        lowerBackPt3.set(backX + WORLD_BRICK_SIZE * edgeDir.x, backY + WORLD_BRICK_SIZE * edgeDir.y, backZ)
        upperBackPt3.set(backX + WORLD_BRICK_SIZE * edgeDir.x, backY + WORLD_BRICK_SIZE * edgeDir.y, upperZ)
    }

    override fun tesselateUpFace(tess: Tesselator) {
        val last = steps.last()
        steps.forEach { step ->
            if (step != last || stretch >= 1.0f) {
                tess.addQuad(
                    step.upperPt0,
                    step.upperPt1,
                    step.upperPt2,
                    step.upperPt3,
                    upFaceIdx,
                    Direction.upVector,
                    texRelPt1 = step.upFaceTexPt0,
                    texRelPt2 = step.upFaceTexPt1,
                    texRelPt3 = step.upFaceTexPt2,
                    texRelPt4 = step.upFaceTexPt3,
                )
            } else {
                tess.addQuad(
                    step.upperPt0,
                    step.upperPt1,
                    upperBackPt2,
                    upperBackPt3,
                    upFaceIdx,
                    Direction.upVector,
                    texRelPt1 = step.upFaceTexPt0,
                    texRelPt2 = step.upFaceTexPt1,
                    texRelPt3 = step.upFaceTexPt2,
                    texRelPt4 = step.upFaceTexPt3,
                )
            }
        }
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (!isBrickBelowFullyCoveringMyDownFace()) {
            val first = steps.first()
            tess.addQuad(
                first.lowerPt1,
                first.lowerPt0,
                first.lowerPt3,
                first.lowerPt2,
                downFaceIdx,
                Direction.downVector,
                texRelPt1 = first.upFaceTexPt1,
                texRelPt2 = first.upFaceTexPt0,
                texRelPt3 = first.upFaceTexPt3,
                texRelPt4 = first.upFaceTexPt2,
                flipTexX = true,
            )
        }

        if (stretch < 1.0f) {
            val last = steps.last()
            tess.addQuad(
                last.lowerPt3,
                last.lowerPt2,
                lowerBackPt3,
                lowerBackPt2,
                downFaceIdx,
                Direction.downVector,
            )
        }
    }

    private fun tesselateStepsFront(tess: Tesselator, faceIdx: Int, normal: Vector3f, coveredByNeighbour: Boolean) {
        steps.forEachIndexed { i, step ->
            if (!coveredByNeighbour || i > 0) {
                tess.addQuad(
                    step.lowerPt0,
                    step.lowerPt1,
                    step.upperPt0,
                    step.upperPt1,
                    faceIdx,
                    normal,
                    texRelY = step.frontTexRelY,
                    texRelHeight = step.frontTexRelHeight,
                )
            }
        }
    }

    private fun tesselateStepsBack(tess: Tesselator, faceIdx: Int, normal: Vector3f, coveredByNeighbour: Boolean) {
        val last = steps.last()

        if (!coveredByNeighbour) {
            if (stretch >= 1.0f) {
                tess.addQuad(
                    last.lowerPt3,
                    last.lowerPt2,
                    last.upperPt3,
                    last.upperPt2,
                    faceIdx,
                    normal,
                    texRelY = last.frontTexRelY,
                    texRelHeight = last.frontTexRelHeight,
                )
            } else {
                tess.addQuad(
                    lowerBackPt3,
                    lowerBackPt2,
                    upperBackPt3,
                    upperBackPt2,
                    faceIdx,
                    normal,
                    texRelY = last.frontTexRelY,
                    texRelHeight = last.frontTexRelHeight,
                )
            }
        }

        val first = steps[0]
        val second = steps[1]
        val texRelY1 = second.frontTexRelY + second.frontTexRelHeight
        val texRelY2 = last.frontTexRelY + last.frontTexRelHeight

        tess.addQuad(
            first.lowerPt3,
            first.lowerPt2,
            second.lowerPt3,
            second.lowerPt2,
            faceIdx,
            null,
            texRelY = texRelY1,
            texRelHeight = 1.0f - texRelY1,
        )
        tess.addQuad(
            second.lowerPt3,
            second.lowerPt2,
            last.lowerPt3,
            last.lowerPt2,
            faceIdx,
            null,
            texRelY = texRelY2,
            texRelHeight = texRelY1 - texRelY2,
        )
    }

    private fun tesselateRightSide(tess: Tesselator, faceIdx: Int, normal: Vector3f) {
        var prevStep: Step? = null
        steps.forEach { step ->
            tess.addQuad(
                step.lowerPt1,
                step.lowerPt3,
                step.upperPt1,
                step.upperPt3,
                faceIdx,
                normal,
                texRelX = step.rightSideTexRelX,
                texRelWidth = step.rightSideTexRelWidth,
                texRelY = step.frontTexRelY,
                texRelHeight = step.frontTexRelHeight,
            )

            prevStep?.let { prev ->
                tess.addTriangle(
                    step.lowerPt3,
                    step.lowerPt1,
                    prev.lowerPt3,
                    faceIdx,
                    normal,
                    texPivotX = 0.0f,
                    texRelX = prev.frontTexRelY,
                    texRelWidth = prev.frontTexRelHeight,
                    texRelY = 1.0f - step.rightSideTexRelX - step.rightSideTexRelWidth,
                    texRelHeight = step.rightSideTexRelWidth,
                    rotateTex = true,
                )
            }

            prevStep = step
        }

        if (stretch < 1.0f) {
            val last = steps.last()
            tess.addQuad(
                last.lowerPt3,
                lowerBackPt3,
                last.upperPt3,
                upperBackPt3,
                faceIdx,
                normal
            )
        }
    }

    private fun tesselateLeftSide(tess: Tesselator, faceIdx: Int, normal: Vector3f) {
        var prevStep: Step? = null
        steps.forEach { step ->
            tess.addQuad(
                step.lowerPt2,
                step.lowerPt0,
                step.upperPt2,
                step.upperPt0,
                faceIdx,
                normal,
                texRelX = 1.0f - step.rightSideTexRelX - step.rightSideTexRelWidth,
                texRelWidth = step.rightSideTexRelWidth,
                texRelY = step.frontTexRelY,
                texRelHeight = step.frontTexRelHeight,
            )

            prevStep?.let { prev ->
                tess.addTriangle(
                    step.lowerPt2,
                    prev.lowerPt2,
                    step.lowerPt0,
                    faceIdx,
                    normal,
                    texPivotX = 1.0f,
                    texRelX = prev.frontTexRelY,
                    texRelWidth = prev.frontTexRelHeight,
                    texRelY = step.rightSideTexRelX,
                    texRelHeight = step.rightSideTexRelWidth,
                    rotateTex = true,
                    flipTexX = true,
                    flipTexY = true,
                )
            }

            prevStep = step
        }

        if (stretch < 1.0f) {
            val last = steps.last()
            tess.addQuad(
                lowerBackPt2,
                last.lowerPt2,
                upperBackPt2,
                last.upperPt2,
                faceIdx,
                normal
            )
        }
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        val covered = isBrickToNorthFullyCoveringMyNorthFace()
        when (lowEdge) {
            Side.NORTH -> tesselateStepsFront(tess, northFaceIdx, Direction.northVector, covered)
            Side.EAST -> if (!covered) tesselateRightSide(tess, northFaceIdx, Direction.northVector)
            Side.SOUTH -> tesselateStepsBack(tess, northFaceIdx, Direction.northVector, covered)
            Side.WEST -> if (!covered) tesselateLeftSide(tess, northFaceIdx, Direction.northVector)
        }
    }

    override fun tesselateEastFace(tess: Tesselator) {
        val covered = isBrickToEastFullyCoveringMyEastFace()
        when (lowEdge) {
            Side.NORTH -> if (!covered) tesselateLeftSide(tess, eastFaceIdx, Direction.eastVector)
            Side.EAST -> tesselateStepsFront(tess, eastFaceIdx, Direction.eastVector, covered)
            Side.SOUTH -> if (!covered) tesselateRightSide(tess, eastFaceIdx, Direction.eastVector)
            Side.WEST -> tesselateStepsBack(tess, eastFaceIdx, Direction.eastVector, covered)
        }
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        val covered = isBrickToSouthFullyCoveringMySouthFace()
        when (lowEdge) {
            Side.NORTH -> tesselateStepsBack(tess, southFaceIdx, Direction.southVector, covered)
            Side.EAST -> if (!covered) tesselateLeftSide(tess, southFaceIdx, Direction.southVector)
            Side.SOUTH -> tesselateStepsFront(tess, southFaceIdx, Direction.southVector, covered)
            Side.WEST -> if (!covered) tesselateRightSide(tess, southFaceIdx, Direction.southVector)
        }
    }

    override fun tesselateWestFace(tess: Tesselator) {
        val covered = isBrickToWestFullyCoveringMyWestFace()
        when (lowEdge) {
            Side.NORTH -> if (!covered) tesselateRightSide(tess, westFaceIdx, Direction.westVector)
            Side.EAST -> tesselateStepsBack(tess, westFaceIdx, Direction.westVector, covered)
            Side.SOUTH -> if (!covered) tesselateLeftSide(tess, westFaceIdx, Direction.westVector)
            Side.WEST -> tesselateStepsFront(tess, westFaceIdx, Direction.westVector, covered)
        }
    }

    override fun heightAt(x: Float, y: Float) = upside.pt0.z // TODO
}
