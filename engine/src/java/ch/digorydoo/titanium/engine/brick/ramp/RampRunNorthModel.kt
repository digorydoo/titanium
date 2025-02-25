package ch.digorydoo.titanium.engine.brick.ramp

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickRun
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class RampRunNorthModel: AbstrBrickModel() {
    private val run = BrickRun()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)
        run.getNSRun(ix, iy, iz, BrickShape.RAMP_RUN_NORTH, subvolume, toDownside = true)
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

        run.forEachBrick { i, brick ->
            val j = run.length - 1 - i
            val a = j.toFloat() / run.length
            val b = (j + 1).toFloat() / run.length

            val pt02a = lerp(run.to.pt0, run.from.pt2, a)
            val pt13a = lerp(run.to.pt1, run.from.pt3, a)
            val pt02b = lerp(run.to.pt0, run.from.pt2, b)
            val pt13b = lerp(run.to.pt1, run.from.pt3, b)

            tess.addQuad(
                pt02a,
                pt13a,
                pt02b,
                pt13b,
                brick.upFaceIdx,
                null,
            )
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

    override fun tesselateSouthFace(tess: Tesselator) {}

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
    }
}
