package ch.digorydoo.titanium.engine.brick.bevel

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class BevelWEModel: AbstrBrickModel() {
    private val inner20Upper2 = MutablePoint3f()
    private val inner20Upper0 = MutablePoint3f()
    private val inner31Upper1 = MutablePoint3f()
    private val inner31Upper3 = MutablePoint3f()
    private val outerMid0 = MutablePoint3f()
    private val outerMid1 = MutablePoint3f()
    private val outerMid2 = MutablePoint3f()
    private val outerMid3 = MutablePoint3f()
    private val outer31Mid3 = MutablePoint3f()
    private val outer31Mid1 = MutablePoint3f()
    private val outer20Mid2 = MutablePoint3f()
    private val outer20Mid0 = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val x31 = upside.pt3.x * (1.0f - BEVEL_CUT_SIZE) + upside.pt1.x * BEVEL_CUT_SIZE
        val x13 = upside.pt3.x * BEVEL_CUT_SIZE + upside.pt1.x * (1.0f - BEVEL_CUT_SIZE)
        val zUpper = upside.pt0.z
        val zMid = zUpper - BEVEL_Z_DELTA

        inner20Upper2.set(x31, upside.pt2.y, zUpper)
        inner20Upper0.set(x13, upside.pt0.y, zUpper)
        inner31Upper1.set(x13, upside.pt1.y, zUpper)
        inner31Upper3.set(x31, upside.pt3.y, zUpper)
        outerMid0.set(upside.pt0.x, upside.pt0.y, zMid)
        outerMid1.set(upside.pt1.x, upside.pt1.y, zMid)
        outerMid2.set(upside.pt2.x, upside.pt2.y, zMid)
        outerMid3.set(upside.pt3.x, upside.pt3.y, zMid)
        outer31Mid3.set(x31, upside.pt3.y, zMid)
        outer31Mid1.set(x13, upside.pt1.y, zMid)
        outer20Mid2.set(x31, upside.pt2.y, zMid)
        outer20Mid0.set(x13, upside.pt0.y, zMid)
    }

    override fun heightAt(x: Float, y: Float): Float {
        return upside.pt0.z // TODO
    }

    override fun tesselateUpFace(tess: Tesselator) {
        if (isBrickAboveFullyCoveringMyUpFace()) {
            return
        }

        tess.addQuad(
            inner20Upper0,
            inner31Upper1,
            inner20Upper2,
            inner31Upper3,
            upFaceIdx,
            Direction.upVector,
            texRelY = BEVEL_CUT_SIZE,
            texRelHeight = 1.0f - 2.0f * BEVEL_CUT_SIZE,
        )
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (isBrickBelowFullyCoveringMyDownFace()) {
            return
        }

        tess.addQuad(
            downside.pt1,
            downside.pt0,
            downside.pt3,
            downside.pt2,
            downFaceIdx,
            Direction.downVector,
        )
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        tess.addQuad(
            outerMid3,
            outerMid2,
            inner31Upper3,
            inner20Upper2,
            northFaceIdx,
            null,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        if (isBrickToNorthFullyCoveringMyNorthFace()) {
            return
        }

        tess.addQuad(
            downside.pt3,
            downside.pt2,
            outerMid3,
            outerMid2,
            northFaceIdx,
            Direction.northVector,
            texRelY = BEVEL_Z_RELDELTA,
            texRelHeight = 1.0f - BEVEL_Z_RELDELTA,
        )
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        tess.addQuad(
            outerMid0,
            outerMid1,
            inner20Upper0,
            inner31Upper1,
            southFaceIdx,
            null,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        if (isBrickToSouthFullyCoveringMySouthFace()) {
            return
        }

        tess.addQuad(
            downside.pt0,
            downside.pt1,
            outerMid0,
            outerMid1,
            southFaceIdx,
            Direction.southVector,
            texRelY = BEVEL_Z_RELDELTA,
            texRelHeight = 1.0f - BEVEL_Z_RELDELTA,
        )
    }

    override fun tesselateEastFace(tess: Tesselator) {
        if (isBrickToEastFullyCoveringMyEastFace()) {
            return
        }

        tess.addTriangle(
            inner31Upper1,
            outerMid1,
            outer31Mid1,
            eastFaceIdx,
            Direction.eastVector,
            texPivotX = 1.0f,
            texRelWidth = BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addQuad(
            outer31Mid1,
            outer31Mid3,
            inner31Upper1,
            inner31Upper3,
            eastFaceIdx,
            Direction.eastVector,
            texRelX = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - 2 * BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addTriangle(
            inner31Upper3,
            outer31Mid3,
            outerMid3,
            eastFaceIdx,
            Direction.eastVector,
            texPivotX = 0.0f,
            texRelX = 1.0f - BEVEL_CUT_SIZE,
            texRelWidth = BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addQuad(
            downside.pt1,
            downside.pt3,
            outerMid1,
            outerMid3,
            eastFaceIdx,
            Direction.eastVector,
            texRelY = BEVEL_Z_RELDELTA,
            texRelHeight = 1.0f - BEVEL_Z_RELDELTA,
        )
    }

    override fun tesselateWestFace(tess: Tesselator) {
        if (isBrickToWestFullyCoveringMyWestFace()) {
            return
        }

        tess.addTriangle(
            inner20Upper2,
            outerMid2,
            outer20Mid2,
            westFaceIdx,
            Direction.westVector,
            texPivotX = 1.0f,
            texRelWidth = BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addQuad(
            outer20Mid2,
            outer20Mid0,
            inner20Upper2,
            inner20Upper0,
            westFaceIdx,
            Direction.westVector,
            texRelX = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - 2 * BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addTriangle(
            inner20Upper0,
            outer20Mid0,
            outerMid0,
            westFaceIdx,
            Direction.westVector,
            texPivotX = 0.0f,
            texRelX = 1.0f - BEVEL_CUT_SIZE,
            texRelWidth = BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addQuad(
            downside.pt2,
            downside.pt0,
            outerMid2,
            outerMid0,
            westFaceIdx,
            Direction.westVector,
            texRelY = BEVEL_Z_RELDELTA,
            texRelHeight = 1.0f - BEVEL_Z_RELDELTA,
        )
    }
}
