package ch.digorydoo.titanium.engine.brick.bevel

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class BevelNSModel: AbstrBrickModel() {
    private val inner01Upper0 = MutablePoint3f()
    private val inner01Upper1 = MutablePoint3f()
    private val inner23Upper2 = MutablePoint3f()
    private val inner23Upper3 = MutablePoint3f()
    private val outerMid0 = MutablePoint3f()
    private val outerMid1 = MutablePoint3f()
    private val outerMid2 = MutablePoint3f()
    private val outerMid3 = MutablePoint3f()
    private val outer23Mid2 = MutablePoint3f()
    private val outer23Mid3 = MutablePoint3f()
    private val outer01Mid0 = MutablePoint3f()
    private val outer01Mid1 = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val y23 = upside.pt2.y * (1.0f - BEVEL_CUT_SIZE) + upside.pt3.y * BEVEL_CUT_SIZE
        val y32 = upside.pt3.y * (1.0f - BEVEL_CUT_SIZE) + upside.pt2.y * BEVEL_CUT_SIZE
        val zUpper = upside.pt0.z
        val zMid = zUpper - BEVEL_Z_DELTA

        inner01Upper0.set(upside.pt0.x, y23, zUpper)
        inner01Upper1.set(upside.pt1.x, y32, zUpper)
        inner23Upper2.set(upside.pt2.x, y23, zUpper)
        inner23Upper3.set(upside.pt3.x, y32, zUpper)
        outerMid0.set(upside.pt0.x, upside.pt0.y, zMid)
        outerMid1.set(upside.pt1.x, upside.pt1.y, zMid)
        outerMid2.set(upside.pt2.x, upside.pt2.y, zMid)
        outerMid3.set(upside.pt3.x, upside.pt3.y, zMid)
        outer23Mid2.set(upside.pt2.x, y23, zMid)
        outer23Mid3.set(upside.pt3.x, y32, zMid)
        outer01Mid0.set(upside.pt0.x, y23, zMid)
        outer01Mid1.set(upside.pt1.x, y32, zMid)
    }

    override fun heightAt(x: Float, y: Float): Float {
        return upside.pt0.z // TODO
    }

    override fun tesselateUpFace(tess: Tesselator) {
        if (isBrickAboveFullyCoveringMyUpFace()) {
            return
        }

        tess.addQuad(
            inner01Upper0,
            inner01Upper1,
            inner23Upper2,
            inner23Upper3,
            upFaceIdx,
            Direction.upVector,
            texRelX = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - 2.0f * BEVEL_CUT_SIZE,
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
        if (isBrickToNorthFullyCoveringMyNorthFace()) {
            return
        }

        tess.addTriangle(
            inner23Upper3,
            outerMid3,
            outer23Mid3,
            northFaceIdx,
            Direction.northVector,
            texPivotX = 1.0f,
            texRelWidth = BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addQuad(
            outer23Mid3,
            outer23Mid2,
            inner23Upper3,
            inner23Upper2,
            northFaceIdx,
            Direction.northVector,
            texRelX = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - 2 * BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addTriangle(
            inner23Upper2,
            outer23Mid2,
            outerMid2,
            northFaceIdx,
            Direction.northVector,
            texPivotX = 0.0f,
            texRelX = 1.0f - BEVEL_CUT_SIZE,
            texRelWidth = BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

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
            inner01Upper0,
            outerMid0,
            outer01Mid0,
            southFaceIdx,
            Direction.southVector,
            texPivotX = 1.0f,
            texRelWidth = BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addQuad(
            outer01Mid0,
            outer01Mid1,
            inner01Upper0,
            inner01Upper1,
            southFaceIdx,
            Direction.southVector,
            texRelX = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - 2 * BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addTriangle(
            inner01Upper1,
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
            outerMid3,
            inner01Upper1,
            inner23Upper3,
            eastFaceIdx,
            null,
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
        tess.addQuad(
            outerMid2,
            outerMid0,
            inner23Upper2,
            inner01Upper0,
            westFaceIdx,
            null,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        if (isBrickToWestFullyCoveringMyWestFace()) {
            return
        }

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
