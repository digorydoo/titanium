package ch.digorydoo.titanium.engine.brick.stairs

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickRun
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.brick.stairs.StairsRunNorthModel.Companion.numStepsPerBrickOfStair
import ch.digorydoo.titanium.engine.brick.stairs.StairsRunNorthModel.Part
import ch.digorydoo.titanium.engine.brick.stairs.StairsRunNorthModel.Step
import ch.digorydoo.titanium.engine.utils.Direction

class StairsRunWestModel: AbstrBrickModel() {
    private val run = BrickRun()
    private var parts = arrayOf<Part>()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)
        run.getWERun(ix, iy, iz, BrickShape.STAIRS_RUN_WEST, subvolume, toDownside = true)

        val numSteps = numStepsPerBrickOfStair(run.length)

        parts = Array(run.length) { i ->
            val runRelA = i.toFloat() / run.length
            val runRelB = (i + 1).toFloat() / run.length
            val runPtA = lerp(run.from.pt0, run.to.pt1, runRelA)
            val runPtB = lerp(run.from.pt0, run.to.pt1, runRelB)

            Part(
                runRelA,
                run.getBrick(i),
                steps = Array(numSteps) { s ->
                    val relA = s.toFloat() / numSteps
                    val relB = (s + 1).toFloat() / numSteps
                    val up0 = lerp(runPtA, runPtB, relA)
                    val dn0 = lerp(runPtA, runPtB, relB)
                    val dn1 = MutablePoint3f(dn0).apply { x = upside.pt2.x }
                    val up1 = MutablePoint3f(dn0).apply { z = up0.z }
                    val up3 = MutablePoint3f(up1).apply { x = upside.pt2.x }
                    val up2 = MutablePoint3f(up0).apply { x = upside.pt2.x }

                    Step(relA, relB, up0, up1, up2, up3, dn0, dn1)
                }
            )
        }
    }

    override fun heightAt(x: Float, y: Float): Float {
        val a = run.from.pt0
        val b = run.to.pt1
        val rel = clamp((y - a.y) / (b.y - a.y))
        return lerp(a.z, b.z, rel)
    }

    override fun tesselateUpFace(tess: Tesselator) {
        if (!run.isFirstOfRun) {
            return
        }

        parts.forEach { part ->
            val texRelWidth = 1.0f / part.steps.size

            part.steps.forEach { step ->
                tess.addQuad(
                    step.up0,
                    step.up1,
                    step.up2,
                    step.up3,
                    part.brick.upFaceIdx,
                    Direction.upVector,
                    texRelX = step.relA,
                    texRelWidth = texRelWidth
                )
            }
        }
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (!run.isFirstOfRun) {
            return
        }

        if (run.length == 1 && isBrickBelowFullyCoveringMyDownFace()) {
            return
        }

        run.forEachBrick { i, brick ->
            val j = run.length - 1 - i
            val a = j.toFloat() / run.length
            val b = (j + 1).toFloat() / run.length

            val pt10a = lerp(run.to.pt1, downside.pt0, a)
            val pt32a = lerp(run.to.pt3, downside.pt2, a)
            val pt10b = lerp(run.to.pt1, downside.pt0, b)
            val pt32b = lerp(run.to.pt3, downside.pt2, b)

            tess.addQuad(
                pt10a,
                pt10b,
                pt32a,
                pt32b,
                brick.downFaceIdx,
                Direction.downVector,
            )
        }
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        if (!run.isFirstOfRun) {
            return
        }

        if (run.length == 1 && isBrickToNorthFullyCoveringMyNorthFace()) {
            return
        }

        val mid = MutablePoint3f()

        run.forEachBrick { i, brick ->
            val rela = i.toFloat() / run.length
            val relb = (i + 1).toFloat() / run.length

            val up23a = lerp(upside.pt2, run.to.pt3, rela)
            val up23b = lerp(upside.pt2, run.to.pt3, relb)

            mid.set(downside.pt2.x, lerp(downside.pt2.y, run.to.pt3.y, rela), up23b.z)

            tess.addTriangle(
                up23a,
                up23b,
                mid,
                brick.northFaceIdx,
                Direction.northVector,
                texPivotX = 1.0f,
                texRelY = rela,
                texRelHeight = 1.0f / run.length,
            )

            if (i < run.length - 1) {
                val dn23a = lerp(downside.pt2, run.to.pt3, rela)
                val dn23b = lerp(downside.pt2, run.to.pt3, relb)

                tess.addQuad(
                    dn23b,
                    dn23a,
                    up23b,
                    mid,
                    brick.northFaceIdx,
                    Direction.northVector,
                    texRelY = relb,
                    texRelHeight = 1.0f - relb,
                )
            }
        }

        parts.forEach { part ->
            val texRelWidth = 1.0f / part.steps.size
            val texRelHeight = 1.0f / part.steps.size / run.length

            part.steps.forEach { step ->
                tess.addTriangle(
                    step.dn1, // should be called dn3
                    step.up2,
                    step.up3,
                    part.brick.northFaceIdx,
                    Direction.northVector,
                    texPivotX = 1.0f,
                    flipTexX = true,
                    flipTexY = true,
                    texRelX = 1.0f - step.relB,
                    texRelY = part.runRelA + step.relA / run.length,
                    texRelWidth = texRelWidth,
                    texRelHeight = texRelHeight
                )
            }
        }
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        if (!run.isFirstOfRun) {
            return
        }

        if (run.length == 1 && isBrickToSouthFullyCoveringMySouthFace()) {
            return
        }

        val mid = MutablePoint3f()

        run.forEachBrick { i, brick ->
            val rela = i.toFloat() / run.length
            val relb = (i + 1).toFloat() / run.length

            val up01a = lerp(upside.pt0, run.to.pt1, rela)
            val up01b = lerp(upside.pt0, run.to.pt1, relb)

            mid.set(downside.pt0.x, lerp(downside.pt0.y, run.to.pt1.y, rela), up01b.z)

            tess.addTriangle(
                up01a,
                mid,
                up01b,
                brick.southFaceIdx,
                Direction.southVector,
                texPivotX = 0.0f,
                texRelY = rela,
                texRelHeight = 1.0f / run.length,
            )

            if (i < run.length - 1) {
                val dn01a = lerp(downside.pt0, run.to.pt1, rela)
                val dn01b = lerp(downside.pt0, run.to.pt1, relb)

                tess.addQuad(
                    dn01a,
                    dn01b,
                    mid,
                    up01b,
                    brick.southFaceIdx,
                    Direction.southVector,
                    texRelY = relb,
                    texRelHeight = 1.0f - relb,
                )
            }
        }

        parts.forEach { part ->
            val texRelWidth = 1.0f / part.steps.size
            val texRelHeight = 1.0f / part.steps.size / run.length

            part.steps.forEach { step ->
                tess.addTriangle(
                    step.dn0, // should be called dn1
                    step.up1,
                    step.up0,
                    part.brick.southFaceIdx,
                    Direction.southVector,
                    texPivotX = 0.0f,
                    flipTexX = true,
                    flipTexY = true,
                    texRelX = step.relA,
                    texRelY = part.runRelA + step.relA / run.length,
                    texRelWidth = texRelWidth,
                    texRelHeight = texRelHeight
                )
            }
        }
    }

    override fun tesselateEastFace(tess: Tesselator) {
        if (!run.isFirstOfRun) {
            return
        }

        parts.forEach { part ->
            val texRelHeight = 1.0f / part.steps.size / run.length

            part.steps.forEach { step ->
                tess.addQuad(
                    step.dn0, // should be called dn1
                    step.dn1, // should be called dn3
                    step.up1,
                    step.up3,
                    part.brick.eastFaceIdx,
                    Direction.eastVector,
                    texRelY = part.runRelA + step.relA / run.length,
                    texRelHeight = texRelHeight
                )
            }
        }
    }

    override fun tesselateWestFace(tess: Tesselator) {
        if (!run.isFirstOfRun || isBrickToWestFullyCoveringMyWestFace()) {
            return
        }

        tess.addQuad(
            downside.pt2,
            downside.pt0,
            upside.pt2,
            upside.pt0,
            westFaceIdx,
            Direction.westVector,
        )
    }
}
