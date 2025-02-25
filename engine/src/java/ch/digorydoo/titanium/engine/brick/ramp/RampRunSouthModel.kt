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

class RampRunSouthModel: AbstrBrickModel() {
    private val lowerLastPt0 = MutablePoint3f()
    private val lowerLastPt1 = MutablePoint3f()
    private val run = BrickRun()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)
        run.getNSRun(ix, iy, iz, BrickShape.RAMP_RUN_SOUTH, subvolume, fromDownside = true)
        lowerLastPt0.set(run.to.pt0.x, run.to.pt0.y, downside.pt0.z)
        lowerLastPt1.set(run.to.pt1.x, run.to.pt1.y, downside.pt1.z)
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

    override fun tesselateNorthFace(tess: Tesselator) {}

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
    }
}
