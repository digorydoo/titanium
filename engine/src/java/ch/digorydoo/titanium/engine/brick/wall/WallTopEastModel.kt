package ch.digorydoo.titanium.engine.brick.wall

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class WallTopEastModel(private val relInset: Float): AbstrBrickModel() {
    private val ceilingPt0 = MutablePoint3f()
    private val ceilingPt1 = MutablePoint3f()
    private val ceilingPt2 = MutablePoint3f()
    private val ceilingPt3 = MutablePoint3f()
    private val ceilingInnerPt1 = MutablePoint3f()
    private val ceilingInnerPt3 = MutablePoint3f()
    private val lowerPt0 = MutablePoint3f()
    private val lowerPt2 = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val midZ = lerp(downside.pt0.z, upside.pt0.z, relInset)
        ceilingPt0.set(upside.pt0.x, upside.pt0.y, midZ)
        ceilingPt1.set(upside.pt1.x, upside.pt1.y, midZ)
        ceilingPt2.set(upside.pt2.x, upside.pt2.y, midZ)
        ceilingPt3.set(upside.pt3.x, upside.pt3.y, midZ)

        val midY = lerp(downside.pt0.y, downside.pt1.y, relInset)
        ceilingInnerPt1.set(downside.pt1.x, midY, midZ)
        ceilingInnerPt3.set(downside.pt3.x, midY, midZ)

        lowerPt0.set(downside.pt0.x, midY, downside.pt0.z)
        lowerPt2.set(downside.pt2.x, midY, downside.pt2.z)
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
                downside.pt1,
                lowerPt0,
                downside.pt3,
                lowerPt2,
                downFaceIdx,
                Direction.downVector,
                texRelX = 0.0f,
                texRelWidth = 1.0f - relInset,
            )
        }

        tess.addQuad(
            ceilingInnerPt1,
            ceilingPt0,
            ceilingInnerPt3,
            ceilingPt2,
            downFaceIdx,
            Direction.downVector,
            texRelX = 1.0f - relInset,
            texRelWidth = relInset,
        )
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        if (isBrickToNorthFullyCoveringMyNorthFace()) {
            return
        }

        tess.addQuad(
            ceilingPt3,
            ceilingPt2,
            upside.pt3,
            upside.pt2,
            northFaceIdx,
            Direction.northVector,
            texRelY = 0.0f,
            texRelHeight = 1.0f - relInset,
        )
        tess.addQuad(
            downside.pt3,
            lowerPt2,
            ceilingPt3,
            ceilingInnerPt3,
            northFaceIdx,
            Direction.northVector,
            texRelX = 0.0f,
            texRelWidth = 1.0f - relInset,
            texRelY = 1.0f - relInset,
            texRelHeight = relInset,
        )
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        if (isBrickToSouthFullyCoveringMySouthFace()) {
            return
        }

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
        tess.addQuad(
            lowerPt0,
            downside.pt1,
            ceilingInnerPt1,
            ceilingPt1,
            southFaceIdx,
            Direction.southVector,
            texRelX = relInset,
            texRelWidth = 1.0f - relInset,
            texRelY = 1.0f - relInset,
            texRelHeight = relInset,
        )
    }

    override fun tesselateEastFace(tess: Tesselator) {
        if (isBrickToEastFullyCoveringMyEastFace()) {
            return
        }

        tess.addQuad(
            downside.pt1,
            downside.pt3,
            upside.pt1,
            upside.pt3,
            eastFaceIdx,
            Direction.eastVector,
        )
    }

    override fun tesselateWestFace(tess: Tesselator) {
        if (!isBrickToWestFullyCoveringMyWestFace()) {
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
        }

        tess.addQuad(
            lowerPt2,
            lowerPt0,
            ceilingInnerPt3,
            ceilingInnerPt1,
            westFaceIdx,
            Direction.westVector,
            texRelY = 1.0f - relInset,
            texRelHeight = relInset,
        )
    }

    override fun heightAt(x: Float, y: Float) = upside.pt0.z // TODO
}
