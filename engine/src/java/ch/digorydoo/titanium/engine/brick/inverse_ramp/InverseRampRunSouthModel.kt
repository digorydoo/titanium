package ch.digorydoo.titanium.engine.brick.inverse_ramp

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickRun
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class InverseRampRunSouthModel: AbstrBrickModel() {
    private val run = BrickRun()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)
        run.getNSRun(ix, iy, iz, BrickShape.INVERSE_RAMP_RUN_SOUTH, subvolume, fromDownside = true)
    }

    override fun heightAt(x: Float, y: Float) =
        upside.pt0.z

    override fun tesselateUpFace(tess: Tesselator) {
        if (!run.isFirstOfRun) {
            return
        }

        if (run.length == 1 && isBrickAboveFullyCoveringMyUpFace()) {
            return
        }

        for (i in 0 ..< run.length) {
            val a = i.toFloat() / run.length
            val b = (i + 1).toFloat() / run.length

            val pt02a = lerp(run.to.pt0, upside.pt2, a)
            val pt13a = lerp(run.to.pt1, upside.pt3, a)
            val pt02b = lerp(run.to.pt0, upside.pt2, b)
            val pt13b = lerp(run.to.pt1, upside.pt3, b)

            tess.addQuad(
                pt02a,
                pt13a,
                pt02b,
                pt13b,
                upFaceIdx,
                Direction.upVector,
            )
        }
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (!run.isFirstOfRun) {
            return
        }

        for (i in 0 ..< run.length) {
            val a = i.toFloat() / run.length
            val b = (i + 1).toFloat() / run.length

            val pt02a = lerp(run.to.pt0, run.from.pt2, a)
            val pt13a = lerp(run.to.pt1, run.from.pt3, a)
            val pt02b = lerp(run.to.pt0, run.from.pt2, b)
            val pt13b = lerp(run.to.pt1, run.from.pt3, b)

            tess.addQuad(
                pt02b,
                pt13b,
                pt02a,
                pt13a,
                downFaceIdx,
                null,
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

        for (i in 0 ..< run.length) {
            val rela = i.toFloat() / run.length
            val relb = (i + 1).toFloat() / run.length

            val dn31a = lerp(run.from.pt3, run.to.pt1, rela)
            val dn31b = lerp(run.from.pt3, run.to.pt1, relb)

            mid.set(
                dn31a.x,
                run.from.pt3.y,
                dn31b.z
            )

            tess.addTriangle(
                dn31a,
                mid,
                dn31b,
                eastFaceIdx,
                Direction.eastVector,
                texPivotX = 0.0f,
                texRelY = 1.0f - relb,
                flipTexX = true,
                flipTexY = true,
                texRelHeight = 1.0f / run.length,
            )

            if (i < run.length - 1) {
                val up31a = lerp(upside.pt3, run.to.pt1, rela)
                val up31b = lerp(upside.pt3, run.to.pt1, relb)

                tess.addQuad(
                    dn31b,
                    mid,
                    up31b,
                    up31a,
                    eastFaceIdx,
                    Direction.eastVector,
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

        for (i in 0 ..< run.length) {
            val rela = i.toFloat() / run.length
            val relb = (i + 1).toFloat() / run.length

            val dn20a = lerp(run.from.pt2, run.to.pt0, rela)
            val dn20b = lerp(run.from.pt2, run.to.pt0, relb)

            mid.set(
                dn20a.x,
                run.from.pt2.y,
                dn20b.z
            )

            tess.addTriangle(
                dn20a,
                dn20b,
                mid,
                westFaceIdx,
                Direction.westVector,
                texPivotX = 1.0f,
                texRelY = 1.0f - relb,
                flipTexX = true,
                flipTexY = true,
                texRelHeight = 1.0f / run.length,
            )

            if (i < run.length - 1) {
                val up20a = lerp(upside.pt2, run.to.pt0, rela)
                val up20b = lerp(upside.pt2, run.to.pt0, relb)

                tess.addQuad(
                    mid,
                    dn20b,
                    up20a,
                    up20b,
                    westFaceIdx,
                    Direction.westVector,
                    texRelHeight = 1.0f - relb,
                )
            }
        }
    }
}
