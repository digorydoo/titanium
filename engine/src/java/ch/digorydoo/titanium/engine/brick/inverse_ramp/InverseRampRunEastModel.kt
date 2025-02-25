package ch.digorydoo.titanium.engine.brick.inverse_ramp

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickRun
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class InverseRampRunEastModel: AbstrBrickModel() {
    private val run = BrickRun()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)
        run.getWERun(ix, iy, iz, BrickShape.INVERSE_RAMP_RUN_EAST, subvolume, fromDownside = true)
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

            val pt10a = lerp(run.to.pt1, upside.pt0, a)
            val pt32a = lerp(run.to.pt3, upside.pt2, a)
            val pt10b = lerp(run.to.pt1, upside.pt0, b)
            val pt32b = lerp(run.to.pt3, upside.pt2, b)

            tess.addQuad(
                pt10b,
                pt10a,
                pt32b,
                pt32a,
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

            val pt10a = lerp(run.to.pt1, run.from.pt0, a)
            val pt32a = lerp(run.to.pt3, run.from.pt2, a)
            val pt10b = lerp(run.to.pt1, run.from.pt0, b)
            val pt32b = lerp(run.to.pt3, run.from.pt2, b)

            tess.addQuad(
                pt10b,
                pt32b,
                pt10a,
                pt32a,
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

            mid.set(
                run.from.pt2.x,
                dn23a.y,
                dn23b.z
            )

            tess.addTriangle(
                dn23a,
                mid,
                dn23b,
                northFaceIdx,
                Direction.northVector,
                texPivotX = 0.0f,
                texRelY = 1.0f - relb,
                texRelHeight = 1.0f / run.length,
                flipTexX = true,
                flipTexY = true,
            )

            if (i < run.length - 1) {
                val up23a = lerp(upside.pt2, run.to.pt3, rela)
                val up23b = lerp(upside.pt2, run.to.pt3, relb)

                tess.addQuad(
                    dn23b,
                    mid,
                    up23b,
                    up23a,
                    northFaceIdx,
                    Direction.northVector,
                    texRelHeight = 1.0f - relb,
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

            mid.set(
                run.from.pt0.x,
                dn01a.y,
                dn01b.z
            )

            tess.addTriangle(
                dn01a,
                dn01b,
                mid,
                southFaceIdx,
                Direction.southVector,
                texPivotX = 1.0f,
                texRelY = 1.0f - relb,
                texRelHeight = 1.0f / run.length,
                flipTexX = true,
                flipTexY = true,
            )

            if (i < run.length - 1) {
                val up01a = lerp(upside.pt0, run.to.pt1, rela)
                val up01b = lerp(upside.pt0, run.to.pt1, relb)

                tess.addQuad(
                    mid,
                    dn01b,
                    up01a,
                    up01b,
                    southFaceIdx,
                    Direction.southVector,
                    texRelHeight = 1.0f - relb,
                )
            }
        }
    }

    override fun tesselateEastFace(tess: Tesselator) {
    }

    override fun tesselateWestFace(tess: Tesselator) {
        if (!run.isFirstOfRun || isBrickToWestFullyCoveringMyWestFace()) {
            return
        }

        tess.addQuad(
            run.from.pt2,
            run.from.pt0,
            upside.pt2,
            upside.pt0,
            westFaceIdx,
            Direction.westVector,
        )
    }
}
