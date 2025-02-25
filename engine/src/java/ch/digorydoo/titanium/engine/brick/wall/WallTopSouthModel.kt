package ch.digorydoo.titanium.engine.brick.wall

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class WallTopSouthModel(private val relInset: Float): AbstrBrickModel() {
    private val ceilingPt0 = MutablePoint3f()
    private val ceilingPt1 = MutablePoint3f()
    private val ceilingPt2 = MutablePoint3f()
    private val ceilingPt3 = MutablePoint3f()
    private val ceilingInnerPt0 = MutablePoint3f()
    private val ceilingInnerPt1 = MutablePoint3f()
    private val lowerPt2 = MutablePoint3f()
    private val lowerPt3 = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val midZ = lerp(downside.pt0.z, upside.pt0.z, relInset)
        ceilingPt0.set(upside.pt0.x, upside.pt0.y, midZ)
        ceilingPt1.set(upside.pt1.x, upside.pt1.y, midZ)
        ceilingPt2.set(upside.pt2.x, upside.pt2.y, midZ)
        ceilingPt3.set(upside.pt3.x, upside.pt3.y, midZ)

        val midX = lerp(downside.pt2.x, downside.pt0.x, relInset)
        ceilingInnerPt0.set(midX, downside.pt0.y, midZ)
        ceilingInnerPt1.set(midX, downside.pt1.y, midZ)

        lowerPt2.set(midX, downside.pt2.y, downside.pt2.z)
        lowerPt3.set(midX, downside.pt3.y, downside.pt3.z)
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
                downside.pt0,
                lowerPt3,
                lowerPt2,
                downFaceIdx,
                Direction.downVector,
                texRelY = relInset,
                texRelHeight = 1.0f - relInset,
            )
        }

        tess.addQuad(
            ceilingInnerPt1,
            ceilingInnerPt0,
            ceilingPt3,
            ceilingPt2,
            downFaceIdx,
            Direction.downVector,
            texRelY = 0.0f,
            texRelHeight = relInset,
        )
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        if (!isBrickToNorthFullyCoveringMyNorthFace()) {
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
        }

        tess.addQuad(
            lowerPt3,
            lowerPt2,
            ceilingInnerPt1,
            ceilingInnerPt0,
            northFaceIdx,
            Direction.northVector,
            texRelY = 1.0f - relInset,
            texRelHeight = relInset,
        )
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        if (isBrickToSouthFullyCoveringMySouthFace()) {
            return
        }

        tess.addQuad(
            downside.pt0,
            downside.pt1,
            upside.pt0,
            upside.pt1,
            southFaceIdx,
            Direction.southVector,
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
            downside.pt1,
            lowerPt3,
            ceilingPt1,
            ceilingInnerPt1,
            eastFaceIdx,
            Direction.eastVector,
            texRelX = 0.0f,
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
            lowerPt2,
            downside.pt0,
            ceilingInnerPt0,
            ceilingPt0,
            westFaceIdx,
            Direction.westVector,
            texRelX = relInset,
            texRelWidth = 1.0f - relInset,
            texRelY = 1.0f - relInset,
            texRelHeight = relInset,
        )
    }

    override fun heightAt(x: Float, y: Float) = upside.pt0.z // TODO
}
