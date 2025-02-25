package ch.digorydoo.titanium.engine.brick.ramp

import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class RampCornerSEAltModel: AbstrBrickModel() {
    override fun heightAt(x: Float, y: Float) =
        upside.pt0.z // TODO

    override fun tesselateUpFace(tess: Tesselator) {
        tess.addTriangle(
            upside.pt3,
            upside.pt0,
            upside.pt1,
            upFaceIdx,
            Direction.upVector,
            texPivotX = 1.0f,
        )
        tess.addTriangle(
            downside.pt2,
            upside.pt0,
            upside.pt3,
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
        tess.addTriangle(
            upside.pt3,
            downside.pt3,
            downside.pt2,
            northFaceIdx,
            Direction.northVector,
            texPivotX = 0.0f,
        )
    }

    override fun tesselateSouthFace(tess: Tesselator) {
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
            upside.pt0,
            downside.pt2,
            downside.pt0,
            westFaceIdx,
            Direction.westVector,
            texPivotX = 1.0f,
        )
    }
}
