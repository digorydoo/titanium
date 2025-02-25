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

class RampRunEastModel: AbstrBrickModel() {
    private val lowerLastPt1 = MutablePoint3f()
    private val lowerLastPt3 = MutablePoint3f()
    private val run = BrickRun()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)
        run.getWERun(ix, iy, iz, BrickShape.RAMP_RUN_EAST, subvolume, fromDownside = true)
        lowerLastPt1.set(run.to.pt1.x, run.to.pt1.y, downside.pt1.z)
        lowerLastPt3.set(run.to.pt3.x, run.to.pt3.y, downside.pt3.z)
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

        run.forEachBrick { i, brick ->
            val j = run.length - 1 - i
            val a = j.toFloat() / run.length
            val b = (j + 1).toFloat() / run.length

            val pt10a = lerp(run.to.pt1, run.from.pt0, a)
            val pt32a = lerp(run.to.pt3, run.from.pt2, a)
            val pt10b = lerp(run.to.pt1, run.from.pt0, b)
            val pt32b = lerp(run.to.pt3, run.from.pt2, b)

            tess.addQuad(
                pt10b,
                pt10a,
                pt32b,
                pt32a,
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

            val pt10a = lerp(lowerLastPt1, downside.pt0, a)
            val pt32a = lerp(lowerLastPt3, downside.pt2, a)
            val pt10b = lerp(lowerLastPt1, downside.pt0, b)
            val pt32b = lerp(lowerLastPt3, downside.pt2, b)

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

            val up23a = lerp(downside.pt2, run.to.pt3, rela)
            val up23b = lerp(downside.pt2, run.to.pt3, relb)
            mid.set(downside.pt2.x, up23b.y, up23a.z)

            tess.addTriangle(
                up23b,
                mid,
                up23a,
                brick.northFaceIdx,
                Direction.northVector,
                texPivotX = 0.0f,
                texRelY = 1.0f - relb,
                texRelHeight = 1.0f / run.length,
            )

            if (i > 0) {
                val dn23a = lerp(downside.pt2, lowerLastPt3, rela)
                val dn23b = lerp(downside.pt2, lowerLastPt3, relb)

                tess.addQuad(
                    dn23b,
                    dn23a,
                    mid,
                    up23a,
                    brick.northFaceIdx,
                    Direction.northVector,
                    texRelY = 1.0f - rela,
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

        run.forEachBrick { i, brick ->
            val rela = i.toFloat() / run.length
            val relb = (i + 1).toFloat() / run.length

            val up01a = lerp(downside.pt0, run.to.pt1, rela)
            val up01b = lerp(downside.pt0, run.to.pt1, relb)
            mid.set(downside.pt0.x, up01b.y, up01a.z)

            tess.addTriangle(
                up01b,
                up01a,
                mid,
                brick.southFaceIdx,
                Direction.southVector,
                texPivotX = 1.0f,
                texRelY = 1.0f - relb,
                texRelHeight = 1.0f / run.length,
            )

            if (i > 0) {
                val dn01a = lerp(downside.pt0, lowerLastPt1, rela)
                val dn01b = lerp(downside.pt0, lowerLastPt1, relb)

                tess.addQuad(
                    dn01a,
                    dn01b,
                    up01a,
                    mid,
                    brick.southFaceIdx,
                    Direction.southVector,
                    texRelY = 1.0f - rela,
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
            lowerLastPt1,
            lowerLastPt3,
            run.to.pt1,
            run.to.pt3,
            run.to.brick.eastFaceIdx,
            Direction.eastVector,
        )
    }

    override fun tesselateWestFace(tess: Tesselator) {}
}
