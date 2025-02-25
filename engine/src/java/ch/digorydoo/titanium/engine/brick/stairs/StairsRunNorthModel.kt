package ch.digorydoo.titanium.engine.brick.stairs

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.*
import ch.digorydoo.titanium.engine.utils.Direction

class StairsRunNorthModel: AbstrBrickModel() {
    class Part(
        val runRelA: Float,
        val brick: Brick,
        val steps: Array<Step>,
    )

    class Step(
        val relA: Float,
        val relB: Float,
        val up0: Point3f,
        val up1: Point3f,
        val up2: Point3f,
        val up3: Point3f,
        val dn0: Point3f,
        val dn1: Point3f,
    )

    private val run = BrickRun()
    private var parts = arrayOf<Part>()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)
        run.getNSRun(ix, iy, iz, BrickShape.STAIRS_RUN_NORTH, subvolume, toDownside = true)

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
                    val up2 = lerp(runPtA, runPtB, relA)
                    val dn0 = lerp(runPtA, runPtB, relB)
                    val dn1 = MutablePoint3f(dn0).apply { y = upside.pt1.y }
                    val up0 = MutablePoint3f(dn0).apply { z = up2.z }
                    val up1 = MutablePoint3f(up0).apply { y = upside.pt1.y }
                    val up3 = MutablePoint3f(up2).apply { y = upside.pt1.y }

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
                    texRelHeight = texRelHeight
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

            val pt02a = lerp(run.to.pt0, downside.pt2, a)
            val pt13a = lerp(run.to.pt1, downside.pt3, a)
            val pt02b = lerp(run.to.pt0, downside.pt2, b)
            val pt13b = lerp(run.to.pt1, downside.pt3, b)

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
        if (!run.isFirstOfRun || isBrickToNorthFullyCoveringMyNorthFace()) {
            return
        }

        tess.addQuad(
            downside.pt3,
            downside.pt2,
            upside.pt3,
            upside.pt2,
            northFaceIdx,
            Direction.northVector,
        )
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        if (!run.isFirstOfRun) {
            return
        }

        parts.forEach { part ->
            val texRelHeight = 1.0f / part.steps.size / run.length

            part.steps.forEach { step ->
                tess.addQuad(
                    step.dn0,
                    step.dn1,
                    step.up0,
                    step.up1,
                    part.brick.southFaceIdx,
                    Direction.southVector,
                    texRelY = part.runRelA + step.relA / run.length,
                    texRelHeight = texRelHeight
                )
            }
        }
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

            val up31a = lerp(upside.pt3, run.to.pt1, rela)
            val up31b = lerp(upside.pt3, run.to.pt1, relb)

            mid.set(lerp(downside.pt3.x, run.to.pt1.x, rela), downside.pt3.y, up31b.z)

            tess.addTriangle(
                up31a,
                up31b,
                mid,
                brick.eastFaceIdx,
                Direction.eastVector,
                texPivotX = 1.0f,
                texRelY = rela,
                texRelHeight = 1.0f / run.length,
            )

            if (i < run.length - 1) {
                val dn31a = lerp(downside.pt3, run.to.pt1, rela)
                val dn31b = lerp(downside.pt3, run.to.pt1, relb)

                tess.addQuad(
                    dn31b,
                    dn31a,
                    up31b,
                    mid,
                    brick.eastFaceIdx,
                    Direction.eastVector,
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
                    step.dn1,
                    step.up3,
                    step.up1,
                    part.brick.eastFaceIdx,
                    Direction.eastVector,
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

            val up20a = lerp(upside.pt2, run.to.pt0, rela)
            val up20b = lerp(upside.pt2, run.to.pt0, relb)

            mid.set(lerp(downside.pt2.x, run.to.pt0.x, rela), downside.pt2.y, up20b.z)

            tess.addTriangle(
                up20a,
                mid,
                up20b,
                brick.westFaceIdx,
                Direction.westVector,
                texPivotX = 0.0f,
                texRelY = rela,
                texRelHeight = 1.0f / run.length,
            )

            if (i < run.length - 1) {
                val dn20a = lerp(downside.pt2, run.to.pt0, rela)
                val dn20b = lerp(downside.pt2, run.to.pt0, relb)

                tess.addQuad(
                    dn20a,
                    dn20b,
                    mid,
                    up20b,
                    brick.westFaceIdx,
                    Direction.westVector,
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
                    step.dn0,
                    step.up0,
                    step.up2,
                    part.brick.westFaceIdx,
                    Direction.westVector,
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

    companion object {
        fun numStepsPerBrickOfStair(stairLength: Int) = when (stairLength) {
            1 -> 4
            2 -> 3
            3 -> 3
            4 -> 3
            5 -> 3
            else -> 2
        }
    }
}
