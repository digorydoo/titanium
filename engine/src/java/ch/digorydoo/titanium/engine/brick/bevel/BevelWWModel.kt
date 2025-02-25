package ch.digorydoo.titanium.engine.brick.bevel

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class BevelWWModel: AbstrBrickModel() {
    private val innerUpper0 = MutablePoint3f()
    private val innerUpper2 = MutablePoint3f()
    private val inner13Upper1 = MutablePoint3f()
    private val inner13Upper3 = MutablePoint3f()
    private val outerMid0 = MutablePoint3f()
    private val outerMid1 = MutablePoint3f()
    private val outerMid2 = MutablePoint3f()
    private val outerMid3 = MutablePoint3f()
    private val outer02Mid0 = MutablePoint3f()
    private val outer02Mid2 = MutablePoint3f()
    private val outer13Mid1 = MutablePoint3f()
    private val outer13Mid3 = MutablePoint3f()
    private val outer10Mid0 = MutablePoint3f()
    private val outer32Mid2 = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val x02 = upside.pt0.x * (1.0f - BEVEL_CUT_SIZE) + upside.pt2.x * BEVEL_CUT_SIZE
        val x20 = upside.pt2.x * (1.0f - BEVEL_CUT_SIZE) + upside.pt0.x * BEVEL_CUT_SIZE
        val y23 = upside.pt2.y * (1.0f - BEVEL_CUT_SIZE) + upside.pt3.y * BEVEL_CUT_SIZE
        val zUpper = upside.pt0.z
        val zMid = zUpper - BEVEL_Z_DELTA

        innerUpper0.set(x02, y23, zUpper)
        innerUpper2.set(x20, y23, zUpper)
        inner13Upper1.set(x02, upside.pt1.y, zUpper)
        inner13Upper3.set(x20, upside.pt3.y, zUpper)
        outerMid0.set(upside.pt0.x, upside.pt0.y, zMid)
        outerMid1.set(upside.pt1.x, upside.pt1.y, zMid)
        outerMid2.set(upside.pt2.x, upside.pt2.y, zMid)
        outerMid3.set(upside.pt3.x, upside.pt3.y, zMid)
        outer02Mid0.set(x02, upside.pt0.y, zMid)
        outer02Mid2.set(x20, upside.pt2.y, zMid)
        outer13Mid1.set(x02, upside.pt1.y, zMid)
        outer13Mid3.set(x20, upside.pt3.y, zMid)
        outer10Mid0.set(upside.pt0.x, y23, zMid)
        outer32Mid2.set(upside.pt2.x, y23, zMid)
    }

    override fun heightAt(x: Float, y: Float): Float {
        return upside.pt0.z // TODO
    }

    override fun tesselateUpFace(tess: Tesselator) {
        if (isBrickAboveFullyCoveringMyUpFace()) {
            return
        }

        tess.addQuad(
            innerUpper0,
            inner13Upper1,
            innerUpper2,
            inner13Upper3,
            upFaceIdx,
            Direction.upVector,
            texRelX = BEVEL_CUT_SIZE,
            texRelY = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - BEVEL_CUT_SIZE,
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
            outer32Mid2,
            inner13Upper3,
            innerUpper2,
            northFaceIdx,
            null,
            texRelWidth = 1.0f - BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addTriangle(
            innerUpper2,
            outer32Mid2,
            outerMid2,
            northFaceIdx,
            null,
            texPivotX = 0.0f,
            texRelX = 1.0f - BEVEL_CUT_SIZE,
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
        tess.addQuad(
            outer10Mid0,
            outerMid1,
            innerUpper0,
            inner13Upper1,
            southFaceIdx,
            null,
            texRelX = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addTriangle(
            innerUpper0,
            outerMid0,
            outer10Mid0,
            southFaceIdx,
            null,
            texPivotX = 1.0f,
            texRelWidth = BEVEL_CUT_SIZE,
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
            inner13Upper1,
            outerMid1,
            outer13Mid1,
            eastFaceIdx,
            Direction.eastVector,
            texPivotX = 1.0f,
            texRelWidth = BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addQuad(
            outer13Mid1,
            outer13Mid3,
            inner13Upper1,
            inner13Upper3,
            eastFaceIdx,
            Direction.eastVector,
            texRelX = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - 2 * BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addTriangle(
            inner13Upper3,
            outer13Mid3,
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
        tess.addTriangle(
            innerUpper2,
            outerMid2,
            outer02Mid2,
            westFaceIdx,
            null,
            texPivotX = 1.0f,
            texRelWidth = BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addQuad(
            outer02Mid2,
            outer02Mid0,
            innerUpper2,
            innerUpper0,
            westFaceIdx,
            null,
            texRelX = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - 2 * BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addTriangle(
            innerUpper0,
            outer02Mid0,
            outerMid0,
            westFaceIdx,
            null,
            texPivotX = 0.0f,
            texRelX = 1.0f - BEVEL_CUT_SIZE,
            texRelWidth = BEVEL_CUT_SIZE,
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
