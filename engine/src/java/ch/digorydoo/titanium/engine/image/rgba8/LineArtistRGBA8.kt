package ch.digorydoo.titanium.engine.image.rgba8

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.math.sign
import ch.digorydoo.titanium.engine.image.LineArtist
import java.nio.ByteBuffer
import kotlin.math.abs

internal class LineArtistRGBA8(
    private val imgRGBA8: ByteBuffer,
    private val imgWidth: Int,
    @Suppress("unused") private val imgHeight: Int,
): LineArtist {
    override fun draw(x1: Int, y1: Int, x2: Int, y2: Int, c: Colour) {
        // Algorithm from ZX81 pocket book!

        val r = c.redByte
        val g = c.greenByte
        val b = c.blueByte
        val a = c.alphaByte
        val imgWidth = imgWidth
        val buf = imgRGBA8

        val u = x2 - x1
        val v = y2 - y1
        val d1x = sign(u)
        val d1y = sign(v)
        var d2x = sign(u)
        var d2y = 0
        var m = abs(u)
        var n = abs(v)

        if (m <= n) {
            d2x = 0
            d2y = sign(v)
            m = abs(v)
            n = abs(u)
        }

        var s = m / 2
        var x = x1
        var y = y1

        (0 .. m).forEach {
            var scan = (y * imgWidth + x) * 4
            buf.put(scan++, r)
            buf.put(scan++, g)
            buf.put(scan++, b)
            buf.put(scan, a)

            s += n

            if (s >= m) {
                // A diagonal step.
                s -= m
                x += d1x
                y += d1y
            } else {
                // An up-down or left-right step.
                x += d2x
                y += d2y
            }
        }
    }
}
