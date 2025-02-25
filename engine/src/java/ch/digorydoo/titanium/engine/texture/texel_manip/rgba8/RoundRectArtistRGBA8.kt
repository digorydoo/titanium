package ch.digorydoo.titanium.engine.texture.texel_manip.rgba8

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.titanium.engine.texture.texel_manip.RoundRectArtist
import kotlin.math.min

internal class RoundRectArtistRGBA8(
    private val lineArtist: LineArtistRGBA8,
    private val rectArtist: RectArtistRGBA8,
    private val ovalArtist: OvalArtistRGBA8,
): RoundRectArtist {
    override fun draw(r: Recti, xCornerSize: Int, yCornerSize: Int, c: Colour) {
        if (xCornerSize <= 0 || yCornerSize <= 0) {
            rectArtist.draw(r.left, r.top, r.right, r.bottom, c)
        } else {
            val xcorn: Int = min(r.width / 2, xCornerSize)
            val ycorn: Int = min(r.height / 2, yCornerSize)

            ovalArtist.drawTLQuarter(r.left, r.top, r.left + xcorn, r.top + ycorn, c)
            ovalArtist.drawTRQuarter(r.right - xcorn, r.top, r.right, r.top + ycorn, c)
            ovalArtist.drawBRQuarter(r.right - xcorn, r.bottom - ycorn, r.right, r.bottom, c)
            ovalArtist.drawBLQuarter(r.left, r.bottom - ycorn, r.left + xcorn, r.bottom, c)

            lineArtist.draw(r.left, r.top + ycorn, r.left, r.bottom - ycorn, c)
            lineArtist.draw(r.right - 1, r.top + ycorn, r.right - 1, r.bottom - ycorn, c)
            lineArtist.draw(r.left + xcorn, r.top, r.right - 1 - xcorn, r.top, c)
            lineArtist.draw(r.left + xcorn, r.bottom - 1, r.right - 1 - xcorn, r.bottom - 1, c)
        }
    }

    override fun fill(r: Recti, xCornerSize: Int, yCornerSize: Int, c: Colour) {
        if (xCornerSize <= 0 || yCornerSize <= 0) {
            rectArtist.fill(r.left, r.top, r.right, r.bottom, c)
        } else {
            val xcorn: Int = min(r.width / 2, xCornerSize)
            val ycorn: Int = min(r.height / 2, yCornerSize)

            ovalArtist.fillTLQuarter(r.left, r.top, r.left + xcorn, r.top + ycorn, c)
            ovalArtist.fillTRQuarter(r.right - xcorn, r.top, r.right, r.top + ycorn, c)
            ovalArtist.fillBRQuarter(r.right - xcorn, r.bottom - ycorn, r.right, r.bottom, c)
            ovalArtist.fillBLQuarter(r.left, r.bottom - ycorn, r.left + xcorn, r.bottom, c)

            rectArtist.fill(r.left, r.top + ycorn, r.right, r.bottom - ycorn, c)
            rectArtist.fill(r.left + xcorn, r.top, r.right - xcorn, r.top + ycorn, c)
            rectArtist.fill(r.left + xcorn, r.bottom - ycorn, r.right - xcorn, r.bottom, c)
        }
    }
}
