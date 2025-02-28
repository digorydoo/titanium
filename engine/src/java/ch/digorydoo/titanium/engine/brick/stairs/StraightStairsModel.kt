package ch.digorydoo.titanium.engine.brick.stairs

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.Side

class StraightStairsModel(
    private val lowEdge: Side,
    numSteps: Int,
    private val relInsetUpFace: Float = 0.0f,
    private val relInsetDownFace: Float = 0.0f,
): AbstrBrickModel() {
    private class Step {
        val lowerPt0 = MutablePoint3f()
        val lowerPt1 = MutablePoint3f()
        val lowerPt2 = MutablePoint3f()
        val lowerPt3 = MutablePoint3f() // step's downside below next step
        val upperPt0 = MutablePoint3f()
        val upperPt1 = MutablePoint3f()
        val upperPt2 = MutablePoint3f()
        val upperPt3 = MutablePoint3f() // below next step
        var upFaceTexPt0 = MutablePoint2f()
        var upFaceTexPt1 = MutablePoint2f()
        var upFaceTexPt2 = MutablePoint2f()
        var upFaceTexPt3 = MutablePoint2f()
        var rightSideTexRelX = 0.0f
        var rightSideTexRelWidth = 0.0f
        var frontTexRelY = 0.0f
        var frontTexRelHeight = 0.0f
    }

    private val steps = Array(numSteps) { Step() }

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val lowerZ = downside.pt0.z + relInsetDownFace * WORLD_BRICK_SIZE
        val upperZ = upside.pt0.z - relInsetUpFace * WORLD_BRICK_SIZE

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

        val numSteps = steps.size.toFloat()

        steps.forEachIndexed { i, step ->
            // At the start of this step

            val rel1 = i.toFloat() / numSteps
            val z1 = lerp(lowerZ, upperZ, rel1)
            val rx0 = ascentDir.x * rel1
            val ry0 = ascentDir.y * rel1
            val rx1 = edgeDir.x + rx0
            val ry1 = edgeDir.y + ry0

            // At the end of this step / start of the next step

            val rel2 = (i + 1).toFloat() / numSteps
            val z2 = lerp(lowerZ, upperZ, rel2)
            val rx2 = ascentDir.x * rel2
            val ry2 = ascentDir.y * rel2
            val rx3 = edgeDir.x + rx2
            val ry3 = edgeDir.y + ry2

            val x0 = origin.x + WORLD_BRICK_SIZE * rx0
            val y0 = origin.y + WORLD_BRICK_SIZE * ry0
            val x1 = origin.x + WORLD_BRICK_SIZE * rx1
            val y1 = origin.y + WORLD_BRICK_SIZE * ry1
            val x2 = origin.x + WORLD_BRICK_SIZE * rx2
            val y2 = origin.y + WORLD_BRICK_SIZE * ry2
            val x3 = origin.x + WORLD_BRICK_SIZE * rx3
            val y3 = origin.y + WORLD_BRICK_SIZE * ry3

            val tz1 = (upperZ - z1) / WORLD_BRICK_SIZE // 1 <.. 0 (when no inset)
            val tz2 = (upperZ - z2) / WORLD_BRICK_SIZE // 1 ..< 0 (when no inset)

            step.apply {
                lowerPt0.set(x0, y0, z1)
                lowerPt1.set(x1, y1, z1)
                lowerPt2.set(x2, y2, z1)
                lowerPt3.set(x3, y3, z1)

                upperPt0.set(x0, y0, z2)
                upperPt1.set(x1, y1, z2)
                upperPt2.set(x2, y2, z2)
                upperPt3.set(x3, y3, z2)

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
    }

    override fun tesselateUpFace(tess: Tesselator) {
        steps.forEach { step ->
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
        }
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (isBrickBelowFullyCoveringMyDownFace()) {
            return
        }

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

    private fun tesselateStepsFront(tess: Tesselator, faceIdx: Int, normal: Point3f, coveredByNeighbour: Boolean) {
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

    private fun tesselateStepsBack(tess: Tesselator, faceIdx: Int, normal: Point3f, coveredByNeighbour: Boolean) {
        val first = steps.first()
        val last = steps.last()

        if (!coveredByNeighbour) {
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
        }

        val texRelY = last.frontTexRelY + last.frontTexRelHeight

        tess.addQuad(
            first.lowerPt3,
            first.lowerPt2,
            last.lowerPt3,
            last.lowerPt2,
            faceIdx,
            null,
            texRelY = texRelY,
            texRelHeight = 1.0f - texRelY,
        )
    }

    private fun tesselateRightSide(tess: Tesselator, faceIdx: Int, normal: Point3f) {
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
    }

    private fun tesselateLeftSide(tess: Tesselator, faceIdx: Int, normal: Point3f) {
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
