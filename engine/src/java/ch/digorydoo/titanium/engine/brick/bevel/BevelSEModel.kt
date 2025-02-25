package ch.digorydoo.titanium.engine.brick.bevel

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class BevelSEModel: AbstrBrickModel() {
    private val innerUpper1 = MutablePoint3f()
    private val inner02Upper0 = MutablePoint3f()
    private val inner32Upper3 = MutablePoint3f()
    private val outerMid0 = MutablePoint3f()
    private val outerMid1 = MutablePoint3f()
    private val outerMid2 = MutablePoint3f()
    private val outerMid3 = MutablePoint3f()
    private val outer10Mid1 = MutablePoint3f()
    private val outer20Mid0 = MutablePoint3f()
    private val outer31Mid1 = MutablePoint3f()
    private val outer32Mid3 = MutablePoint3f()

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val x02 = upside.pt0.x * (1.0f - BEVEL_CUT_SIZE) + upside.pt2.x * BEVEL_CUT_SIZE
        val y10 = upside.pt1.y * (1.0f - BEVEL_CUT_SIZE) + upside.pt0.y * BEVEL_CUT_SIZE
        val zUpper = upside.pt0.z
        val zMid = zUpper - BEVEL_Z_DELTA

        innerUpper1.set(x02, y10, zUpper)
        inner32Upper3.set(upside.pt3.x, y10, zUpper)
        inner02Upper0.set(x02, upside.pt0.y, zUpper)
        outerMid0.set(upside.pt0.x, upside.pt0.y, zMid)
        outerMid1.set(upside.pt1.x, upside.pt1.y, zMid)
        outerMid2.set(upside.pt2.x, upside.pt2.y, zMid)
        outerMid3.set(upside.pt3.x, upside.pt3.y, zMid)
        outer10Mid1.set(upside.pt1.x, y10, zMid)
        outer32Mid3.set(upside.pt3.x, y10, zMid)
        outer31Mid1.set(x02, upside.pt1.y, zMid)
        outer20Mid0.set(x02, upside.pt0.y, zMid)
    }

    override fun heightAt(x: Float, y: Float): Float {
        return upside.pt0.z // TODO
    }

    override fun tesselateUpFace(tess: Tesselator) {
        if (isBrickAboveFullyCoveringMyUpFace()) {
            return
        }

        tess.addQuad(
            inner02Upper0,
            innerUpper1,
            upside.pt2,
            inner32Upper3,
            upFaceIdx,
            Direction.upVector,
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
        if (isBrickToNorthFullyCoveringMyNorthFace()) {
            return
        }

        tess.addTriangle(
            inner32Upper3,
            outerMid3,
            outer32Mid3,
            northFaceIdx,
            Direction.northVector,
            texPivotX = 1.0f,
            texRelWidth = BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addQuad(
            outer32Mid3,
            outerMid2,
            inner32Upper3,
            upside.pt2,
            northFaceIdx,
            Direction.northVector,
            texRelX = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - BEVEL_CUT_SIZE,
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
        tess.addQuad(
            outerMid0,
            outer10Mid1,
            inner02Upper0,
            innerUpper1,
            southFaceIdx,
            null,
            texRelWidth = 1.0f - BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addTriangle(
            innerUpper1,
            outer10Mid1,
            outerMid1,
            southFaceIdx,
            null,
            texPivotX = 0.0f,
            texRelX = 1.0f - BEVEL_CUT_SIZE,
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
        tess.addQuad(
            outer31Mid1,
            outerMid3,
            innerUpper1,
            inner32Upper3,
            eastFaceIdx,
            null,
            texRelX = BEVEL_CUT_SIZE,
            texRelWidth = 1.0f - BEVEL_CUT_SIZE,
            texRelHeight = BEVEL_Z_RELDELTA,
        )

        tess.addTriangle(
            innerUpper1,
            outerMid1,
            outer31Mid1,
            eastFaceIdx,
            null,
            texPivotX = 1.0f,
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
            inner02Upper0,
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
            outerMid2,
            outer20Mid0,
            upside.pt2,
            inner02Upper0,
            westFaceIdx,
            Direction.westVector,
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
