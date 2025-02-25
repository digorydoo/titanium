package ch.digorydoo.titanium.engine.brick.inverse_ramp

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickRun
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class InverseRampRunWestModel: AbstrBrickModel() {
    private val upperLastPt1 = MutablePoint3f()
    private val upperLastPt3 = MutablePoint3f()
    private val run = BrickRun()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)
        run.getWERun(ix, iy, iz, BrickShape.INVERSE_RAMP_RUN_WEST, subvolume, toDownside = true)
        upperLastPt1.set(run.to.pt1.x, run.to.pt1.y, upside.pt1.z)
        upperLastPt3.set(run.to.pt3.x, run.to.pt3.y, upside.pt3.z)
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

            val pt01a = lerp(run.from.pt0, upperLastPt1, a)
            val pt23a = lerp(run.from.pt2, upperLastPt3, a)
            val pt01b = lerp(run.from.pt0, upperLastPt1, b)
            val pt23b = lerp(run.from.pt2, upperLastPt3, b)

            tess.addQuad(
                pt01a,
                pt01b,
                pt23a,
                pt23b,
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

            val pt01a = lerp(run.from.pt0, run.to.pt1, a)
            val pt23a = lerp(run.from.pt2, run.to.pt3, a)
            val pt01b = lerp(run.from.pt0, run.to.pt1, b)
            val pt23b = lerp(run.from.pt2, run.to.pt3, b)

            tess.addQuad(
                pt23b,
                pt01b,
                pt23a,
                pt01a,
                downFaceIdx,
                null,
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

        for (i in 0 ..< run.length) {
            val rela = i.toFloat() / run.length
            val relb = (i + 1).toFloat() / run.length

            val dn23a = lerp(run.from.pt2, run.to.pt3, rela)
            val dn23b = lerp(run.from.pt2, run.to.pt3, relb)
            mid.set(upside.pt2.x, dn23b.y, dn23a.z)

            tess.addTriangle(
                dn23b,
                dn23a,
                mid,
                northFaceIdx,
                Direction.northVector,
                texPivotX = 1.0f,
                texRelY = rela,
                texRelHeight = 1.0f / run.length,
                flipTexX = true,
                flipTexY = true,
            )

            if (i > 0) {
                val up23a = lerp(upside.pt2, upperLastPt3, rela)
                val up23b = lerp(upside.pt2, upperLastPt3, relb)

                tess.addQuad(
                    mid,
                    dn23a,
                    up23b,
                    up23a,
                    northFaceIdx,
                    Direction.northVector,
                    texRelHeight = rela,
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

        for (i in 0 ..< run.length) {
            val rela = i.toFloat() / run.length
            val relb = (i + 1).toFloat() / run.length

            val dn01a = lerp(run.from.pt0, run.to.pt1, rela)
            val dn01b = lerp(run.from.pt0, run.to.pt1, relb)
            mid.set(upside.pt0.x, dn01b.y, dn01a.z)

            tess.addTriangle(
                dn01b,
                mid,
                dn01a,
                southFaceIdx,
                Direction.southVector,
                texPivotX = 0.0f,
                texRelY = rela,
                texRelHeight = 1.0f / run.length,
                flipTexX = true,
                flipTexY = true,
            )

            if (i > 0) {
                val up01a = lerp(upside.pt0, upperLastPt1, rela)
                val up01b = lerp(upside.pt0, upperLastPt1, relb)

                tess.addQuad(
                    dn01a,
                    mid,
                    up01a,
                    up01b,
                    southFaceIdx,
                    Direction.southVector,
                    texRelHeight = rela,
                )
            }
        }
    }

    override fun tesselateEastFace(tess: Tesselator) {
        if (!run.isFirstOfRun || isBrickToEastFullyCoveringMyEastFace()) {
            return
        }

        tess.addQuad(
            run.to.pt1,
            run.to.pt3,
            upperLastPt1,
            upperLastPt3,
            eastFaceIdx,
            Direction.eastVector,
        )
    }

    override fun tesselateWestFace(tess: Tesselator) {}
}
