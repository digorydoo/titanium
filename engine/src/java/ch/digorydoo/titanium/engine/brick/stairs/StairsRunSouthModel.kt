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

class StairsRunSouthModel: AbstrBrickModel() {
    private val lowerLastPt0 = MutablePoint3f()
    private val lowerLastPt1 = MutablePoint3f()
    private val run = BrickRun()
    private var parts = arrayOf<Part>()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        run.getNSRun(ix, iy, iz, BrickShape.STAIRS_RUN_SOUTH, subvolume, fromDownside = true)
        lowerLastPt0.set(run.to.pt0.x, run.to.pt0.y, downside.pt0.z)
        lowerLastPt1.set(run.to.pt1.x, run.to.pt1.y, downside.pt1.z)

        val numSteps = numStepsPerBrickOfStair(run.length)

        parts = Array(run.length) { i ->
            val runRelA = i.toFloat() / run.length
            val runRelB = (i + 1).toFloat() / run.length
            val runPtA = lerp(run.from.pt2, run.to.pt0, runRelA)
            val runPtB = lerp(run.from.pt2, run.to.pt0, runRelB)

            Part(
                runRelA,
                run.getBrick(i),
                steps = Array(numSteps) { s ->
                    val relA = s.toFloat() / numSteps
                    val relB = (s + 1).toFloat() / numSteps
                    val dn0 = lerp(runPtA, runPtB, relA)
                    val up0 = lerp(runPtA, runPtB, relB)
                    val dn1 = MutablePoint3f(dn0).apply { y = upside.pt1.y }
                    val up2 = MutablePoint3f(dn0).apply { z = up0.z }
                    val up3 = MutablePoint3f(up2).apply { y = upside.pt1.y }
                    val up1 = MutablePoint3f(up0).apply { y = upside.pt1.y }

                    Step(relA, relB, up0, up1, up2, up3, dn0, dn1)
                }
            )
        }
    }

    override fun heightAt(x: Float, y: Float): Float {
        val a = run.from.pt2
        val b = run.to.pt0
        val rel = clamp((x - a.x) / (b.x - a.x))
        return lerp(a.z, b.z, rel)
    }

    override fun tesselateUpFace(tess: Tesselator) {
        if (!run.isFirstOfRun) {
            return
        }

        parts.forEach { part ->
            val texRelHeight = 1.0f / part.steps.size

            part.steps.forEach { step ->
                tess.addQuad(
                    step.up0,
                    step.up1,
                    step.up2,
                    step.up3,
                    part.brick.upFaceIdx,
                    Direction.upVector,
                    texRelY = step.relA,
                    texRelHeight = texRelHeight,
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

            val pt02a = lerp(lowerLastPt0, downside.pt2, a)
            val pt13a = lerp(lowerLastPt1, downside.pt3, a)
            val pt02b = lerp(lowerLastPt0, downside.pt2, b)
            val pt13b = lerp(lowerLastPt1, downside.pt3, b)

            tess.addQuad(
                pt13a,
                pt02a,
                pt13b,
                pt02b,
                brick.downFaceIdx,
                Direction.downVector,
            )
        }
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        if (!run.isFirstOfRun) {
            return
        }

        parts.forEach { part ->
            val texRelHeight = 1.0f / part.steps.size / run.length

            part.steps.forEach { step ->
                tess.addQuad(
                    step.dn1, // should be called dn3
                    step.dn0, // should be called dn2
                    step.up3,
                    step.up2,
                    part.brick.northFaceIdx,
                    Direction.northVector,
                    texRelY = 1.0f - part.runRelA - step.relB / run.length,
                    texRelHeight = texRelHeight
                )
            }
        }
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        if (!run.isFirstOfRun || isBrickToSouthFullyCoveringMySouthFace()) {
            return
        }

        tess.addQuad(
            lowerLastPt0,
            lowerLastPt1,
            run.to.pt0,
            run.to.pt1,
            run.to.brick.southFaceIdx,
            Direction.southVector,
        )
    }

    override fun tesselateEastFace(tess: Tesselator) {
        if (!run.isFirstOfRun) {
            return
        }

        if (run.length == 1 && isBrickToEastFullyCoveringMyEastFace()) {
            return
        }

        val mid = MutablePoint3f()

        run.forEachBrick { i, brick ->
            val rela = i.toFloat() / run.length
            val relb = (i + 1).toFloat() / run.length

            val up31a = lerp(downside.pt3, run.to.pt1, rela)
            val up31b = lerp(downside.pt3, run.to.pt1, relb)

            mid.set(up31b.x, downside.pt3.y, up31a.z)

            tess.addTriangle(
                up31b,
                mid,
                up31a,
                brick.eastFaceIdx,
                Direction.eastVector,
                texPivotX = 0.0f,
                texRelY = 1.0f - relb,
                texRelHeight = 1.0f / run.length,
            )

            if (i > 0) {
                val dn31a = lerp(downside.pt3, lowerLastPt1, rela)
                val dn31b = lerp(downside.pt3, lowerLastPt1, relb)

                tess.addQuad(
                    dn31b,
                    dn31a,
                    mid,
                    up31a,
                    brick.eastFaceIdx,
                    Direction.eastVector,
                    texRelY = 1.0f - rela,
                    texRelHeight = rela,
                )
            }
        }

        parts.forEach { part ->
            val texRelWidth = 1.0f / part.steps.size
            val texRelHeight = 1.0f / part.steps.size / run.length

            part.steps.forEach { step ->
                tess.addTriangle(
                    step.dn1, // should be called dn3
                    step.up3,
                    step.up1,
                    part.brick.eastFaceIdx,
                    Direction.eastVector,
                    texPivotX = 0.0f,
                    flipTexX = true,
                    flipTexY = true,
                    texRelX = 1.0f - step.relB,
                    texRelY = 1.0f - part.runRelA - step.relB / run.length,
                    texRelWidth = texRelWidth,
                    texRelHeight = texRelHeight
                )
            }
        }
    }

    override fun tesselateWestFace(tess: Tesselator) {
        if (!run.isFirstOfRun) {
            return
        }

        if (run.length == 1 && isBrickToWestFullyCoveringMyWestFace()) {
            return
        }

        val mid = MutablePoint3f()

        run.forEachBrick { i, brick ->
            val rela = i.toFloat() / run.length
            val relb = (i + 1).toFloat() / run.length

            val up20a = lerp(downside.pt2, run.to.pt0, rela)
            val up20b = lerp(downside.pt2, run.to.pt0, relb)

            mid.set(up20b.x, downside.pt2.y, up20a.z)

            tess.addTriangle(
                up20b,
                up20a,
                mid,
                brick.westFaceIdx,
                Direction.westVector,
                texPivotX = 1.0f,
                texRelY = 1.0f - relb,
                texRelHeight = 1.0f / run.length,
            )

            if (i > 0) {
                val dn20a = lerp(downside.pt2, lowerLastPt0, rela)
                val dn20b = lerp(downside.pt2, lowerLastPt0, relb)

                tess.addQuad(
                    dn20a,
                    dn20b,
                    up20a,
                    mid,
                    brick.westFaceIdx,
                    Direction.westVector,
                    texRelY = 1.0f - rela,
                    texRelHeight = rela,
                )
            }
        }

        parts.forEach { part ->
            val texRelWidth = 1.0f / part.steps.size
            val texRelHeight = 1.0f / part.steps.size / run.length

            part.steps.forEach { step ->
                tess.addTriangle(
                    step.dn0, // should be called dn2
                    step.up0,
                    step.up2,
                    part.brick.westFaceIdx,
                    Direction.westVector,
                    texPivotX = 1.0f,
                    flipTexX = true,
                    flipTexY = true,
                    texRelX = step.relA,
                    texRelY = 1.0f - part.runRelA - step.relB / run.length,
                    texRelWidth = texRelWidth,
                    texRelHeight = texRelHeight
                )
            }
        }
    }
}
