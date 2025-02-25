package ch.digorydoo.titanium.engine.brick.bar

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class BarFrameWEModel(
    private val relInsetEastFace: Float = 0.0f,
    private val relInsetWestFace: Float = 0.0f,
): AbstrBrickModel() {
    private val upperPt0 = MutablePoint3f()
    private val upperPt1 = MutablePoint3f()
    private val upperPt2 = MutablePoint3f()
    private val upperPt3 = MutablePoint3f()
    private val innerPt0 = MutablePoint3f()
    private val innerPt1 = MutablePoint3f()
    private val innerPt2 = MutablePoint3f()
    private val innerPt3 = MutablePoint3f()
    private val upperInnerPt0 = MutablePoint3f()
    private val upperInnerPt1 = MutablePoint3f()
    private val upperInnerPt2 = MutablePoint3f()
    private val upperInnerPt3 = MutablePoint3f()
    private val lowerPtN0 = MutablePoint3f()
    private val lowerPtN1 = MutablePoint3f()
    private val lowerPtN2 = MutablePoint3f()
    private val lowerPtN3 = MutablePoint3f()
    private val lowerPtS0 = MutablePoint3f()
    private val lowerPtS1 = MutablePoint3f()
    private val lowerPtS2 = MutablePoint3f()
    private val lowerPtS3 = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val innerNorthX = lerp(upside.pt2.x, upside.pt0.x, THIN_RELSIZE)
        val innerSouthX = lerp(upside.pt0.x, upside.pt2.x, THIN_RELSIZE)
        val westY = lerp(upside.pt0.y, upside.pt1.y, relInsetWestFace)
        val eastY = lerp(upside.pt1.y, upside.pt0.y, relInsetEastFace)
        val innerZ = upside.pt0.z - THIN_RELSIZE

        upperPt0.set(upside.pt0.x, westY, upside.pt0.z)
        upperPt1.set(upside.pt1.x, eastY, upside.pt1.z)
        upperPt2.set(upside.pt2.x, westY, upside.pt2.z)
        upperPt3.set(upside.pt3.x, eastY, upside.pt3.z)

        innerPt0.set(innerSouthX, westY, innerZ)
        innerPt1.set(innerSouthX, eastY, innerZ)
        innerPt2.set(innerNorthX, westY, innerZ)
        innerPt3.set(innerNorthX, eastY, innerZ)

        upperInnerPt0.set(innerSouthX, westY, upside.pt0.z)
        upperInnerPt1.set(innerSouthX, eastY, upside.pt1.z)
        upperInnerPt2.set(innerNorthX, westY, upside.pt2.z)
        upperInnerPt3.set(innerNorthX, eastY, upside.pt3.z)

        lowerPtN0.set(innerNorthX, westY, downside.pt3.z)
        lowerPtN1.set(innerNorthX, eastY, downside.pt3.z)
        lowerPtN2.set(downside.pt2.x, westY, downside.pt3.z)
        lowerPtN3.set(downside.pt3.x, eastY, downside.pt3.z)

        lowerPtS0.set(downside.pt0.x, westY, downside.pt1.z)
        lowerPtS1.set(downside.pt1.x, eastY, downside.pt1.z)
        lowerPtS2.set(innerSouthX, westY, downside.pt1.z)
        lowerPtS3.set(innerSouthX, eastY, downside.pt1.z)
    }

    override fun heightAt(x: Float, y: Float) = null // TODO

    override fun tesselateUpFace(tess: Tesselator) {
        if (isBrickAboveFullyCoveringMyUpFace()) {
            return
        }

        tess.addQuad(
            upperPt0,
            upperPt1,
            upperPt2,
            upperPt3,
            upFaceIdx,
            Direction.upVector,
            texRelX = relInsetWestFace,
            texRelWidth = 1.0f - relInsetWestFace - relInsetEastFace,
        )
    }

    override fun tesselateDownFace(tess: Tesselator) {
        tess.addQuad(
            innerPt1,
            innerPt0,
            innerPt3,
            innerPt2,
            downFaceIdx,
            Direction.downVector,
            texRelX = relInsetEastFace,
            texRelWidth = 1.0f - relInsetWestFace - relInsetEastFace,
            texRelY = THIN_RELSIZE,
            texRelHeight = 1.0f - 2 * THIN_RELSIZE,
        )

        if (isBrickBelowFullyCoveringMyDownFace()) {
            return
        }

        tess.addQuad(
            lowerPtN1,
            lowerPtN0,
            lowerPtN3,
            lowerPtN2,
            downFaceIdx,
            Direction.downVector,
            texRelX = relInsetEastFace,
            texRelWidth = 1.0f - relInsetWestFace - relInsetEastFace,
            texRelHeight = THIN_RELSIZE,
        )
        tess.addQuad(
            lowerPtS1,
            lowerPtS0,
            lowerPtS3,
            lowerPtS2,
            downFaceIdx,
            Direction.downVector,
            texRelX = relInsetEastFace,
            texRelWidth = 1.0f - relInsetWestFace - relInsetEastFace,
            texRelY = 1.0f - THIN_RELSIZE,
            texRelHeight = THIN_RELSIZE,
        )
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        tess.addQuad(
            lowerPtN3,
            lowerPtN2,
            upperPt3,
            upperPt2,
            northFaceIdx,
            Direction.northVector,
            texRelX = relInsetEastFace,
            texRelWidth = 1.0f - relInsetWestFace - relInsetEastFace,
        )
        tess.addQuad(
            lowerPtS3,
            lowerPtS2,
            innerPt1,
            innerPt0,
            northFaceIdx,
            Direction.northVector,
            texRelX = relInsetEastFace,
            texRelWidth = 1.0f - relInsetWestFace - relInsetEastFace,
            texRelY = THIN_RELSIZE,
            texRelHeight = 1.0f - THIN_RELSIZE,
        )
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        tess.addQuad(
            lowerPtS0,
            lowerPtS1,
            upperPt0,
            upperPt1,
            southFaceIdx,
            Direction.southVector,
            texRelX = relInsetWestFace,
            texRelWidth = 1.0f - relInsetWestFace - relInsetEastFace,
        )
        tess.addQuad(
            lowerPtN0,
            lowerPtN1,
            innerPt2,
            innerPt3,
            southFaceIdx,
            Direction.southVector,
            texRelX = relInsetWestFace,
            texRelWidth = 1.0f - relInsetWestFace - relInsetEastFace,
            texRelY = THIN_RELSIZE,
            texRelHeight = 1.0f - THIN_RELSIZE,
        )
    }

    override fun tesselateEastFace(tess: Tesselator) {
        tess.addQuad(
            lowerPtS1,
            lowerPtS3,
            upperPt1,
            upperInnerPt1,
            eastFaceIdx,
            Direction.eastVector,
            texRelWidth = THIN_RELSIZE,
        )
        tess.addQuad(
            lowerPtN1,
            lowerPtN3,
            upperInnerPt3,
            upperPt3,
            eastFaceIdx,
            Direction.eastVector,
            texRelX = 1.0f - THIN_RELSIZE,
            texRelWidth = THIN_RELSIZE,
        )
        tess.addQuad(
            innerPt1,
            innerPt3,
            upperInnerPt1,
            upperInnerPt3,
            eastFaceIdx,
            Direction.eastVector,
            texRelX = THIN_RELSIZE,
            texRelWidth = 1.0f - 2 * THIN_RELSIZE,
            texRelHeight = THIN_RELSIZE,
        )
    }

    override fun tesselateWestFace(tess: Tesselator) {
        tess.addQuad(
            lowerPtN2,
            lowerPtN0,
            upperPt2,
            upperInnerPt2,
            westFaceIdx,
            Direction.westVector,
            texRelWidth = THIN_RELSIZE,
        )
        tess.addQuad(
            lowerPtS2,
            lowerPtS0,
            upperInnerPt0,
            upperPt0,
            westFaceIdx,
            Direction.westVector,
            texRelX = 1.0f - THIN_RELSIZE,
            texRelWidth = THIN_RELSIZE,
        )
        tess.addQuad(
            innerPt2,
            innerPt0,
            upperInnerPt2,
            upperInnerPt0,
            westFaceIdx,
            Direction.westVector,
            texRelX = THIN_RELSIZE,
            texRelWidth = 1.0f - 2 * THIN_RELSIZE,
            texRelHeight = THIN_RELSIZE,
        )
    }
}
