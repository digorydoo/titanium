package io.github.digorydoo.titanium.engine.texture.texel_manip.rgba8

import ch.digorydoo.kutils.colour.Colour
import io.github.digorydoo.titanium.engine.texture.texel_manip.RectArtist
import java.nio.ByteBuffer

internal class RectArtistRGBA8(
    private val imgRGBA8: ByteBuffer,
    private val imgWidth: Int,
    private val imgHeight: Int,
    private val lineArtist: LineArtistRGBA8,
): RectArtist {
    override fun clear(c: Colour) {
        val buf = imgRGBA8
        var scan = 0
        val end = imgWidth * imgHeight * 4

        while (scan < end) {
            buf.put(scan++, c.redByte)
            buf.put(scan++, c.greenByte)
            buf.put(scan++, c.blueByte)
            buf.put(scan++, c.alphaByte)
        }
    }

    override fun draw(left: Int, top: Int, right: Int, bottom: Int, c: Colour) {
        lineArtist.draw(left, top, right - 1, top, c)
        lineArtist.draw(right - 1, top, right - 1, bottom - 1, c)
        lineArtist.draw(right - 1, bottom - 1, left, bottom - 1, c)
        lineArtist.draw(left, bottom - 1, left, top, c)
    }

    override fun fill(left: Int, top: Int, right: Int, bottom: Int, c: Colour) {
        val r = c.redByte
        val g = c.greenByte
        val b = c.blueByte
        val a = c.alphaByte

        val buf = imgRGBA8
        var scan = (top * imgWidth + left) * 4
        val nextRow = (imgWidth - (right - left)) * 4

        (top ..< bottom).forEach {
            (left ..< right).forEach {
                buf.put(scan++, r)
                buf.put(scan++, g)
                buf.put(scan++, b)
                buf.put(scan++, a)
            }
            scan += nextRow
        }
    }

    override fun overlay(left: Int, top: Int, right: Int, bottom: Int, c: Colour) {
        val cr = c.red * 255.0f
        val cg = c.green * 255.0f
        val cb = c.blue * 255.0f
        val alpha = c.alpha
        val beta = 1.0f - alpha

        val buf = imgRGBA8
        var scan = (top * imgWidth + left) * 4
        val nextRow = (imgWidth - (right - left)) * 4
        buf.position(scan)

        (top ..< bottom).forEach {
            (left ..< right).forEach {
                val dr = buf.get().toUByte().toFloat()
                val dg = buf.get().toUByte().toFloat()
                val db = buf.get().toUByte().toFloat()

                buf.position(scan)
                buf.put(((dr * beta + cr * alpha).toInt() and 0xFF).toByte())
                buf.put(((dg * beta + cg * alpha).toInt() and 0xFF).toByte())
                buf.put(((db * beta + cb * alpha).toInt() and 0xFF).toByte())
                buf.put(0xFF.toByte())
                scan += 4
            }
            scan += nextRow
            buf.position(scan)
        }
    }
}
