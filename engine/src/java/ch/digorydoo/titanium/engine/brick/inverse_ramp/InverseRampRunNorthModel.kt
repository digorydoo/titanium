package ch.digorydoo.titanium.engine.brick.inverse_ramp

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickRun
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class InverseRampRunNorthModel: AbstrBrickModel() {
    private val upperLastPt0 = MutablePoint3f()
    private val upperLastPt1 = MutablePoint3f()
    private val run = BrickRun()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)
        run.getNSRun(ix, iy, iz, BrickShape.INVERSE_RAMP_RUN_NORTH, subvolume, toDownside = true)
        upperLastPt0.set(run.to.pt0.x, run.to.pt0.y, upside.pt0.z)
        upperLastPt1.set(run.to.pt1.x, run.to.pt1.y, upside.pt1.z)
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

            val pt02a = lerp(upperLastPt0, upside.pt2, a)
            val pt13a = lerp(upperLastPt1, upside.pt3, a)
            val pt02b = lerp(upperLastPt0, upside.pt2, b)
            val pt13b = lerp(upperLastPt1, upside.pt3, b)

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
                pt13a,
                pt02a,
                pt13b,
                pt02b,
                downFaceIdx,
                null,
            )
        }
    }

    override fun tesselateNorthFace(tess: Tesselator) {}

    override fun tesselateSouthFace(tess: Tesselator) {
        if (!run.isFirstOfRun || isBrickToSouthFullyCoveringMySouthFace()) {
            return
        }

        tess.addQuad(
            run.to.pt0,
            run.to.pt1,
            upperLastPt0,
            upperLastPt1,
            southFaceIdx,
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

        for (i in 0 ..< run.length) {
            val rela = i.toFloat() / run.length
            val relb = (i + 1).toFloat() / run.length

            val dn31a = lerp(run.from.pt3, run.to.pt1, rela)
            val dn31b = lerp(run.from.pt3, run.to.pt1, relb)

            mid.set(
                dn31b.x,
                run.from.pt3.y,
                dn31a.z
            )

            tess.addTriangle(
                dn31b,
                dn31a,
                mid,
                eastFaceIdx,
                Direction.eastVector,
                texPivotX = 1.0f,
                texRelY = rela,
                flipTexX = true,
                flipTexY = true,
                texRelHeight = 1.0f / run.length,
            )

            if (i > 0) {
                val up31a = lerp(run.from.pt3, upperLastPt1, rela)
                val up31b = lerp(run.from.pt3, upperLastPt1, relb)

                tess.addQuad(
                    mid,
                    dn31a,
                    up31b,
                    up31a,
                    eastFaceIdx,
                    Direction.eastVector,
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

        for (i in 0 ..< run.length) {
            val rela = i.toFloat() / run.length
            val relb = (i + 1).toFloat() / run.length

            val dn20a = lerp(run.from.pt2, run.to.pt0, rela)
            val dn20b = lerp(run.from.pt2, run.to.pt0, relb)

            mid.set(
                lerp(run.from.pt2.x, run.to.pt0.x, relb),
                run.from.pt2.y,
                dn20a.z
            )

            tess.addTriangle(
                dn20b,
                mid,
                dn20a,
                westFaceIdx,
                Direction.westVector,
                texPivotX = 0.0f,
                texRelY = rela,
                flipTexX = true,
                flipTexY = true,
                texRelHeight = 1.0f / run.length,
            )

            if (i > 0) {
                val up20a = lerp(run.from.pt2, upperLastPt0, rela)
                val up20b = lerp(run.from.pt2, upperLastPt0, relb)

                tess.addQuad(
                    dn20a,
                    mid,
                    up20a,
                    up20b,
                    westFaceIdx,
                    Direction.westVector,
                    texRelHeight = rela,
                )
            }
        }
    }
}
