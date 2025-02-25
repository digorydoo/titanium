package ch.digorydoo.titanium.engine.brick.wall

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class WallTopNorthModel(private val relInset: Float): AbstrBrickModel() {
    private val ceilingPt0 = MutablePoint3f()
    private val ceilingPt1 = MutablePoint3f()
    private val ceilingPt2 = MutablePoint3f()
    private val ceilingPt3 = MutablePoint3f()
    private val ceilingInnerPt2 = MutablePoint3f()
    private val ceilingInnerPt3 = MutablePoint3f()
    private val lowerPt0 = MutablePoint3f()
    private val lowerPt1 = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val midZ = lerp(downside.pt0.z, upside.pt0.z, relInset)
        ceilingPt0.set(upside.pt0.x, upside.pt0.y, midZ)
        ceilingPt1.set(upside.pt1.x, upside.pt1.y, midZ)
        ceilingPt2.set(upside.pt2.x, upside.pt2.y, midZ)
        ceilingPt3.set(upside.pt3.x, upside.pt3.y, midZ)

        val midX = lerp(downside.pt0.x, downside.pt2.x, relInset)
        ceilingInnerPt2.set(midX, downside.pt0.y, midZ)
        ceilingInnerPt3.set(midX, downside.pt1.y, midZ)

        lowerPt0.set(midX, downside.pt0.y, downside.pt0.z)
        lowerPt1.set(midX, downside.pt1.y, downside.pt1.z)
    }

    override fun tesselateUpFace(tess: Tesselator) {
        if (isBrickAboveFullyCoveringMyUpFace()) {
            return
        }

        tess.addQuad(
            upside.pt0,
            upside.pt1,
            upside.pt2,
            upside.pt3,
            upFaceIdx,
            Direction.upVector,
        )
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (!isBrickBelowFullyCoveringMyDownFace()) {
            tess.addQuad(
                lowerPt1,
                lowerPt0,
                downside.pt3,
                downside.pt2,
                downFaceIdx,
                Direction.downVector,
                texRelY = 0.0f,
                texRelHeight = 1.0f - relInset,
            )
        }

        tess.addQuad(
            ceilingPt1,
            ceilingPt0,
            ceilingInnerPt3,
            ceilingInnerPt2,
            downFaceIdx,
            Direction.downVector,
            texRelY = 1.0f - relInset,
            texRelHeight = relInset,
        )
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        if (isBrickToNorthFullyCoveringMyNorthFace()) {
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
        if (!isBrickToSouthFullyCoveringMySouthFace()) {
            tess.addQuad(
                ceilingPt0,
                ceilingPt1,
                upside.pt0,
                upside.pt1,
                southFaceIdx,
                Direction.southVector,
                texRelY = 0.0f,
                texRelHeight = 1.0f - relInset,
            )
        }

        tess.addQuad(
            lowerPt0,
            lowerPt1,
            ceilingInnerPt2,
            ceilingInnerPt3,
            southFaceIdx,
            Direction.southVector,
            texRelY = 1.0f - relInset,
            texRelHeight = relInset,
        )
    }

    override fun tesselateEastFace(tess: Tesselator) {
        if (isBrickToEastFullyCoveringMyEastFace()) {
            return
        }

        tess.addQuad(
            ceilingPt1,
            ceilingPt3,
            upside.pt1,
            upside.pt3,
            eastFaceIdx,
            Direction.eastVector,
            texRelY = 0.0f,
            texRelHeight = 1.0f - relInset,
        )
        tess.addQuad(
            lowerPt1,
            downside.pt3,
            ceilingInnerPt3,
            ceilingPt3,
            eastFaceIdx,
            Direction.eastVector,
            texRelX = relInset,
            texRelWidth = 1.0f - relInset,
            texRelY = 1.0f - relInset,
            texRelHeight = relInset,
        )
    }

    override fun tesselateWestFace(tess: Tesselator) {
        if (isBrickToWestFullyCoveringMyWestFace()) {
            return
        }

        tess.addQuad(
            ceilingPt2,
            ceilingPt0,
            upside.pt2,
            upside.pt0,
            westFaceIdx,
            Direction.westVector,
            texRelY = 0.0f,
            texRelHeight = 1.0f - relInset,
        )
        tess.addQuad(
            downside.pt2,
            lowerPt0,
            ceilingPt2,
            ceilingInnerPt2,
            westFaceIdx,
            Direction.westVector,
            texRelX = 0.0f,
            texRelWidth = 1.0f - relInset,
            texRelY = 1.0f - relInset,
            texRelHeight = relInset,
        )
    }

    override fun heightAt(x: Float, y: Float) = upside.pt0.z // TODO
}
