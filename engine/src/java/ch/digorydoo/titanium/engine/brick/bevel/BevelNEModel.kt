package ch.digorydoo.titanium.engine.brick.bevel

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class BevelNEModel: AbstrBrickModel() {
    private val innerUpper3 = MutablePoint3f()
    private val inner10Upper1 = MutablePoint3f()
    private val inner20Upper2 = MutablePoint3f()
    private val outerMid0 = MutablePoint3f()
    private val outerMid1 = MutablePoint3f()
    private val outerMid2 = MutablePoint3f()
    private val outerMid3 = MutablePoint3f()
    private val outer01Mid1 = MutablePoint3f()
    private val outer20Mid2 = MutablePoint3f()
    private val outer23Mid3 = MutablePoint3f()
    private val outer31Mid3 = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val x31 = upside.pt3.x * (1.0f - BEVEL_CUT_SIZE) + upside.pt1.x * BEVEL_CUT_SIZE
        val y10 = upside.pt1.y * (1.0f - BEVEL_CUT_SIZE) + upside.pt0.y * BEVEL_CUT_SIZE
        val zUpper = upside.pt0.z
        val zMid = zUpper - BEVEL_Z_DELTA

        innerUpper3.set(x31, y10, zUpper)
        inner20Upper2.set(x31, upside.pt2.y, zUpper)
        inner10Upper1.set(upside.pt1.x, y10, zUpper)
        outerMid0.set(upside.pt0.x, upside.pt0.y, zMid)
        outerMid1.set(upside.pt1.x, upside.pt1.y, zMid)
        outerMid2.set(upside.pt2.x, upside.pt2.y, zMid)
        outerMid3.set(upside.pt3.x, upside.pt3.y, zMid)
        outer31Mid3.set(x31, upside.pt3.y, zMid)
        outer20Mid2.set(x31, upside.pt2.y, zMid)
        outer23Mid3.set(upside.pt3.x, y10, zMid)
        outer01Mid1.set(upside.pt1.x, y10, zMid)
    }

    override fun heightAt(x: Float, y: Float): Float {
        return upside.pt0.z // TODO
    }

    override fun tesselateUpFace(tess: Tesselator) {
        if (isBrickAboveFullyCoveringMyUpFace()) {
            return
        }

        tess.addQuad(
            upside.pt0,
            inner10Upper1,
            inner20Upper2,
            innerUpper3,
            upFaceIdx,
            Direction.upVector,
            texRelY = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - BEVEL_CUT_SIZE,
            texRelHeight = 1.0f - BEVEL_CUT_SIZE,
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
            outer23Mid3,
            outerMid2,
            innerUpper3,
            inner20Upper2,
            northFaceIdx,
            null,
            texRelX = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addTriangle(
            innerUpper3,
            outerMid3,
            outer23Mid3,
            northFaceIdx,
            null,
            texPivotX = 1.0f,
            texRelWidth = BEVEL_CUT_SIZE,
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
        if (isBrickToSouthFullyCoveringMySouthFace()) {
            return
        }

        tess.addTriangle(
            inner10Upper1,
            outer01Mid1,
            outerMid1,
            southFaceIdx,
            Direction.southVector,
            texPivotX = 0.0f,
            texRelX = 1.0f - BEVEL_CUT_SIZE,
            texRelWidth = BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addQuad(
            outerMid0,
            outer01Mid1,
            upside.pt0,
            inner10Upper1,
            southFaceIdx,
            Direction.southVector,
            texRelWidth = 1.0f - BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

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
        tess.addQuad(
            outerMid1,
            outer31Mid3,
            inner10Upper1,
            innerUpper3,
            eastFaceIdx,
            null,
            texRelWidth = 1.0f - BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addTriangle(
            innerUpper3,
            outer31Mid3,
            outerMid3,
            eastFaceIdx,
            null,
            texPivotX = 0.0f,
            texRelX = 1.0f - BEVEL_CUT_SIZE,
            texRelWidth = BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        if (isBrickToEastFullyCoveringMyEastFace()) {
            return
        }

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
            outerMid0,
            inner20Upper2,
            upside.pt0,
            westFaceIdx,
            Direction.westVector,
            texRelX = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - BEVEL_CUT_SIZE,
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
