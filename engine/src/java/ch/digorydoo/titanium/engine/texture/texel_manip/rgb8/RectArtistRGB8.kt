package ch.digorydoo.titanium.engine.texture.texel_manip.rgb8

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.titanium.engine.texture.texel_manip.RectArtist
import java.nio.ByteBuffer

internal class RectArtistRGB8(
    private val imgRGB8: ByteBuffer,
    private val imgWidth: Int,
    private val imgHeight: Int,
): RectArtist {
    override fun clear(c: Colour) {
        val buf = imgRGB8
        var scan = 0
        val end = imgWidth * imgHeight * 3

        while (scan < end) {
            buf.put(scan++, c.redByte)
            buf.put(scan++, c.greenByte)
            buf.put(scan++, c.blueByte)
        }
    }

    override fun draw(left: Int, top: Int, right: Int, bottom: Int, c: Colour) {
        throw NotImplementedError()
    }

    override fun fill(left: Int, top: Int, right: Int, bottom: Int, c: Colour) {
        throw NotImplementedError()
    }
}
