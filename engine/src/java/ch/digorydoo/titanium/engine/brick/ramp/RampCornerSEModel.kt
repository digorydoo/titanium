package ch.digorydoo.titanium.engine.brick.ramp

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.utils.Direction

class RampCornerSEModel: AbstrBrickModel() {
    override fun heightAt(x: Float, y: Float): Float? {
        val rx = (x - upside.pt3.x) / (upside.pt1.x - upside.pt3.x)
        val ry = (y - upside.pt2.y) / (upside.pt3.y - upside.pt2.y)
        val diag = rx + ry
        if (diag < 1.0f) return null
        val rel = clamp(diag - 1.0f)
        return lerp(downside.pt0.z, upside.pt1.z, rel)
    }

    override fun tesselateUpFace(tess: Tesselator) {
        tess.addTriangle(
            downside.pt3,
            downside.pt0,
            upside.pt1,
            upFaceIdx,
            null,
            1.0f,
        )
    }

    override fun tesselateDownFace(tess: Tesselator) {
        if (isBrickBelowFullyCoveringMyDownFace()) {
            return
        }

        tess.addTriangle(
            downside.pt3,
            downside.pt1,
            downside.pt0,
            downFaceIdx,
            Direction.downVector,
            0.0f,
        )
    }

    override fun tesselateNorthFace(tess: Tesselator) {}

    override fun tesselateSouthFace(tess: Tesselator) {
        tess.addTriangle(
            upside.pt1,
            downside.pt0,
            downside.pt1,
            southFaceIdx,
            Direction.southVector,
            1.0f,
        )
    }

    override fun tesselateEastFace(tess: Tesselator) {
        tess.addTriangle(
            upside.pt1,
            downside.pt1,
            downside.pt3,
            eastFaceIdx,
            Direction.eastVector,
            0.0f,
        )
    }

    override fun tesselateWestFace(tess: Tesselator) {}
}
