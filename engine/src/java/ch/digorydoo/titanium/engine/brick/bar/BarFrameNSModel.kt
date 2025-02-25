package ch.digorydoo.titanium.engine.brick.bar

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class BarFrameNSModel(
    private val relInsetNorthFace: Float = 0.0f,
    private val relInsetSouthFace: Float = 0.0f,
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
    private val lowerPtE0 = MutablePoint3f()
    private val lowerPtE1 = MutablePoint3f()
    private val lowerPtE2 = MutablePoint3f()
    private val lowerPtE3 = MutablePoint3f()
    private val lowerPtW0 = MutablePoint3f()
    private val lowerPtW1 = MutablePoint3f()
    private val lowerPtW2 = MutablePoint3f()
    private val lowerPtW3 = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val northX = lerp(upside.pt2.x, upside.pt0.x, relInsetNorthFace)
        val southX = lerp(upside.pt0.x, upside.pt2.x, relInsetSouthFace)
        val innerWestY = lerp(upside.pt0.y, upside.pt1.y, THIN_RELSIZE)
        val innerEastY = lerp(upside.pt1.y, upside.pt0.y, THIN_RELSIZE)
        val innerZ = upside.pt0.z - THIN_RELSIZE

        upperPt0.set(southX, upside.pt0.y, upside.pt0.z)
        upperPt1.set(southX, upside.pt1.y, upside.pt1.z)
        upperPt2.set(northX, upside.pt2.y, upside.pt2.z)
        upperPt3.set(northX, upside.pt3.y, upside.pt3.z)

        innerPt0.set(southX, innerWestY, innerZ)
        innerPt1.set(southX, innerEastY, innerZ)
        innerPt2.set(northX, innerWestY, innerZ)
        innerPt3.set(northX, innerEastY, innerZ)

        upperInnerPt0.set(southX, innerWestY, upside.pt0.z)
        upperInnerPt1.set(southX, innerEastY, upside.pt1.z)
        upperInnerPt2.set(northX, innerWestY, upside.pt2.z)
        upperInnerPt3.set(northX, innerEastY, upside.pt3.z)

        lowerPtE0.set(southX, innerEastY, downside.pt0.z)
        lowerPtE1.set(southX, downside.pt1.y, downside.pt1.z)
        lowerPtE2.set(northX, innerEastY, downside.pt2.z)
        lowerPtE3.set(northX, downside.pt3.y, downside.pt3.z)

        lowerPtW0.set(southX, downside.pt0.y, downside.pt0.z)
        lowerPtW1.set(southX, innerWestY, downside.pt1.z)
        lowerPtW2.set(northX, downside.pt2.y, downside.pt3.z)
        lowerPtW3.set(northX, innerWestY, downside.pt3.z)
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
            texRelY = relInsetNorthFace,
            texRelHeight = 1.0f - relInsetNorthFace - relInsetSouthFace,
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
            texRelX = THIN_RELSIZE,
            texRelWidth = 1.0f - 2 * THIN_RELSIZE,
            texRelY = relInsetNorthFace,
            texRelHeight = 1.0f - relInsetNorthFace - relInsetSouthFace
        )

        if (isBrickBelowFullyCoveringMyDownFace()) {
            return
        }

        tess.addQuad(
            lowerPtW1,
            lowerPtW0,
            lowerPtW3,
            lowerPtW2,
            downFaceIdx,
            Direction.downVector,
            texRelX = 1.0f - THIN_RELSIZE,
            texRelWidth = THIN_RELSIZE,
            texRelY = relInsetNorthFace,
            texRelHeight = 1.0f - relInsetNorthFace - relInsetSouthFace
        )
        tess.addQuad(
            lowerPtE1,
            lowerPtE0,
            lowerPtE3,
            lowerPtE2,
            downFaceIdx,
            Direction.downVector,
            texRelWidth = THIN_RELSIZE,
            texRelY = relInsetNorthFace,
            texRelHeight = 1.0f - relInsetNorthFace - relInsetSouthFace
        )
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        tess.addQuad(
            lowerPtE3,
            lowerPtE2,
            upperPt3,
            upperInnerPt3,
            northFaceIdx,
            Direction.northVector,
            texRelWidth = THIN_RELSIZE,
        )
        tess.addQuad(
            lowerPtW3,
            lowerPtW2,
            upperInnerPt2,
            upperPt2,
            northFaceIdx,
            Direction.northVector,
            texRelX = 1.0f - THIN_RELSIZE,
            texRelWidth = THIN_RELSIZE,
        )
        tess.addQuad(
            innerPt3,
            innerPt2,
            upperInnerPt3,
            upperInnerPt2,
            northFaceIdx,
            Direction.northVector,
            texRelX = THIN_RELSIZE,
            texRelWidth = 1.0f - 2 * THIN_RELSIZE,
            texRelHeight = THIN_RELSIZE,
        )
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        tess.addQuad(
            lowerPtE0,
            lowerPtE1,
            upperInnerPt1,
            upperPt1,
            southFaceIdx,
            Direction.southVector,
            texRelX = 1.0f - THIN_RELSIZE,
            texRelWidth = THIN_RELSIZE,
        )
        tess.addQuad(
            lowerPtW0,
            lowerPtW1,
            upperPt0,
            upperInnerPt0,
            southFaceIdx,
            Direction.southVector,
            texRelWidth = THIN_RELSIZE,
        )
        tess.addQuad(
            innerPt0,
            innerPt1,
            upperInnerPt0,
            upperInnerPt1,
            southFaceIdx,
            Direction.southVector,
            texRelX = THIN_RELSIZE,
            texRelWidth = 1.0f - 2 * THIN_RELSIZE,
            texRelHeight = THIN_RELSIZE,
        )
    }

    override fun tesselateEastFace(tess: Tesselator) {
        tess.addQuad(
            lowerPtE1,
            lowerPtE3,
            upperPt1,
            upperPt3,
            eastFaceIdx,
            Direction.eastVector,
            texRelX = relInsetSouthFace,
            texRelWidth = 1.0f - relInsetNorthFace - relInsetSouthFace
        )
        tess.addQuad(
            lowerPtW1,
            lowerPtW3,
            innerPt0,
            innerPt2,
            eastFaceIdx,
            Direction.eastVector,
            texRelX = relInsetSouthFace,
            texRelWidth = 1.0f - relInsetNorthFace - relInsetSouthFace,
            texRelY = THIN_RELSIZE,
            texRelHeight = 1.0f - THIN_RELSIZE,
        )
    }

    override fun tesselateWestFace(tess: Tesselator) {
        tess.addQuad(
            lowerPtW2,
            lowerPtW0,
            upperPt2,
            upperPt0,
            westFaceIdx,
            Direction.westVector,
            texRelX = relInsetNorthFace,
            texRelWidth = 1.0f - relInsetNorthFace - relInsetSouthFace
        )
        tess.addQuad(
            lowerPtE2,
            lowerPtE0,
            innerPt3,
            innerPt1,
            westFaceIdx,
            Direction.westVector,
            texRelX = relInsetNorthFace,
            texRelWidth = 1.0f - relInsetNorthFace - relInsetSouthFace,
            texRelY = THIN_RELSIZE,
            texRelHeight = 1.0f - THIN_RELSIZE,
        )
    }
}
