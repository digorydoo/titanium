package ch.digorydoo.titanium.engine.brick.zz_various

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class UprightTubeModel(private val numSides: Int, private val radius: Float): AbstrBrickModel() {
    private val upperMid = MutablePoint3f()
    private val lowerMid = MutablePoint3f()
    private val upperCirc = Array(numSides) { MutablePoint3f() }
    private val lowerCirc = Array(numSides) { MutablePoint3f() }
    private val texMid = Point2f(0.5f, 0.5f)
    private val texCirc = Array(numSides) { MutablePoint2f() }

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)

        val cx = (upside.pt0.x + upside.pt2.x) / 2.0f
        val cy = (upside.pt0.y + upside.pt1.y) / 2.0f
        val zu = upside.pt0.z
        val zl = downside.pt0.z

        upperMid.set(cx, cy, zu)
        lowerMid.set(cx, cy, zl)

        for (i in upperCirc.indices) {
            val reli = i.toDouble() / numSides
            val alpha = reli * 2.0 * PI
            val x = (cx + radius * cos(alpha)).toFloat()
            val y = (cy + radius * sin(alpha)).toFloat()
            upperCirc[i].set(x, y, zu)
            lowerCirc[i].set(x, y, zl)

            val tx = (0.5 + radius * sin(alpha)).toFloat()
            val ty = (0.5 + radius * cos(alpha)).toFloat()
            texCirc[i].set(tx, ty)
        }
    }

    override fun heightAt(x: Float, y: Float) =
        upperMid.z // TODO

    override fun tesselateUpFace(tess: Tesselator) {
        val hidden = when (shapeAbove) {
            BrickShape.OCTAGONAL_UPRIGHT_PILLAR -> true
            else -> isBrickAboveFullyCoveringMyUpFace()
        }

        if (hidden) return
        tess.addFan(upperMid, upperCirc, texMid, texCirc, upFaceIdx, ccw = true)
    }

    override fun tesselateDownFace(tess: Tesselator) {
        val hidden = when (shapeBelow) {
            BrickShape.OCTAGONAL_UPRIGHT_PILLAR -> true
            else -> isBrickBelowFullyCoveringMyDownFace()
        }

        if (hidden) return
        tess.addFan(lowerMid, lowerCirc, texMid, texCirc, downFaceIdx, ccw = false, flipTexX = true)
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        // For efficiency reasons, all the sides of the tube are rendered with the northFaceIdx.
        tess.addStrip(upperCirc, lowerCirc, northFaceIdx, close = true, texWrapCount = 2)
    }

    override fun tesselateSouthFace(tess: Tesselator) {}
    override fun tesselateEastFace(tess: Tesselator) {}
    override fun tesselateWestFace(tess: Tesselator) {}
}
