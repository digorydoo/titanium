package ch.digorydoo.titanium.engine.brick.ramp

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class RampCornerNWModel: AbstrBrickModel() {
    override fun heightAt(x: Float, y: Float): Float? {
        val rx = (x - upside.pt3.x) / (upside.pt1.x - upside.pt3.x)
        val ry = (y - upside.pt2.y) / (upside.pt3.y - upside.pt2.y)
        val diag = (1.0f - rx) + (1.0f - ry)
        if (diag < 1.0f) return null
        val rel = clamp(diag - 1.0f)
        return lerp(downside.pt0.z, upside.pt2.z, rel)
    }

    override fun tesselateUpFace(tess: Tesselator) {
        tess.addTriangle(
            downside.pt0,
            downside.pt3,
            upside.pt2,
            upFaceIdx,
            null,
            1.0f,
            flipTexX = true,
            flipTexY = true,
        )
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (isBrickBelowFullyCoveringMyDownFace()) {
            return
        }

        tess.addTriangle(
            downside.pt0,
            downside.pt2,
            downside.pt3,
            downFaceIdx,
            Direction.downVector,
            0.0f,
            flipTexX = true,
            flipTexY = true,
        )
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        tess.addTriangle(
            upside.pt2,
            downside.pt3,
            downside.pt2,
            northFaceIdx,
            Direction.northVector,
            1.0f,
        )
    }

    override fun tesselateSouthFace(tess: Tesselator) {}

    override fun tesselateEastFace(tess: Tesselator) {}

    override fun tesselateWestFace(tess: Tesselator) {
        tess.addTriangle(
            upside.pt2,
            downside.pt2,
            downside.pt0,
            westFaceIdx,
            Direction.westVector,
            0.0f,
        )
    }
}
