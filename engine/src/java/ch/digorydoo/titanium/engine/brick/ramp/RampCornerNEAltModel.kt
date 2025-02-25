package ch.digorydoo.titanium.engine.brick.ramp

import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class RampCornerNEAltModel: AbstrBrickModel() {
    override fun heightAt(x: Float, y: Float) =
        upside.pt0.z // TODO

    override fun tesselateUpFace(tess: Tesselator) {
        tess.addTriangle(
            upside.pt1,
            upside.pt3,
            upside.pt2,
            upFaceIdx,
            Direction.upVector,
            texPivotX = 0.0f,
            flipTexX = true,
            flipTexY = true,
        )
        tess.addTriangle(
            downside.pt0,
            upside.pt1,
            upside.pt2,
            upFaceIdx,
            null,
            texPivotX = 0.5f,
            flipTexX = true,
            flipTexY = true,
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
            downside.pt3,
            downside.pt2,
            upside.pt3,
            upside.pt2,
            northFaceIdx,
            Direction.northVector,
        )
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        tess.addTriangle(
            upside.pt1,
            downside.pt0,
            downside.pt1,
            southFaceIdx,
            Direction.southVector,
            texPivotX = 1.0f,
        )
    }

    override fun tesselateEastFace(tess: Tesselator) {
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
        tess.addTriangle(
            upside.pt2,
            downside.pt2,
            downside.pt0,
            westFaceIdx,
            Direction.westVector,
            texPivotX = 0.0f,
        )
    }
}
