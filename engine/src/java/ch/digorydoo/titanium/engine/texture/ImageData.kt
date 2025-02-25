package ch.digorydoo.titanium.engine.texture

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.font.FontManager
import ch.digorydoo.titanium.engine.texture.texel_manip.Blitter
import ch.digorydoo.titanium.engine.texture.texel_manip.LineArtist
import ch.digorydoo.titanium.engine.texture.texel_manip.OvalArtist
import ch.digorydoo.titanium.engine.texture.texel_manip.RectArtist
import ch.digorydoo.titanium.engine.texture.texel_manip.RoundRectArtist
import ch.digorydoo.titanium.engine.texture.texel_manip.rgb8.BlitterRGB8
import ch.digorydoo.titanium.engine.texture.texel_manip.rgb8.RectArtistRGB8
import ch.digorydoo.titanium.engine.texture.texel_manip.rgba8.BlitterRGBA8
import ch.digorydoo.titanium.engine.texture.texel_manip.rgba8.LineArtistRGBA8
import ch.digorydoo.titanium.engine.texture.texel_manip.rgba8.OvalArtistRGBA8
import ch.digorydoo.titanium.engine.texture.texel_manip.rgba8.RectArtistRGBA8
import ch.digorydoo.titanium.engine.texture.texel_manip.rgba8.RoundRectArtistRGBA8
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * CPU texture buffer that can be used to draw simple stuff and rendering fonts. Don't use this for complex stuff, as
 * it's quite slow.
 *
 * I'm avoiding BufferedImage, because it's over-engineered and hard to use. Especially avoid createGraphics(), because
 * it causes another java process to be launched (see git history).
 */
class ImageData(val buf: ByteBuffer, val type: Type, val width: Int, val height: Int) {
    constructor(type: Type, width: Int, height: Int): this(alloc(type, width, height), type, width, height)

    enum class Type { RGBA8, RGB8 }

    inner class NotImplForType: Exception("Method not implemented for buffer type $type")

    private val blitter: Blitter
    private val lineArtist: LineArtist?
    private val rectArtist: RectArtist
    private val ovalArtist: OvalArtist?
    private val roundRectArtist: RoundRectArtist?

    init {
        when (type) {
            Type.RGBA8 -> {
                blitter = BlitterRGBA8(buf, width, height)
                lineArtist = LineArtistRGBA8(buf, width, height)
                rectArtist = RectArtistRGBA8(buf, width, height, lineArtist)
                ovalArtist = OvalArtistRGBA8(buf, width, height)
                roundRectArtist = RoundRectArtistRGBA8(lineArtist, rectArtist, ovalArtist)
            }
            Type.RGB8 -> {
                blitter = BlitterRGB8(buf, width, height)
                lineArtist = null
                rectArtist = RectArtistRGB8(buf, width, height)
                ovalArtist = null
                roundRectArtist = null
            }
        }
    }

    fun clear(c: Colour = Colour.Companion.black) {
        rectArtist.clear(c)
    }

    @Suppress("unused")
    fun drawLine(a: Point2f, b: Point2f, c: Colour) {
        val lineArtist = lineArtist ?: throw NotImplForType()
        lineArtist.draw(a.x.toInt(), a.y.toInt(), b.x.toInt(), b.y.toInt(), c)
    }

    @Suppress("unused")
    fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, c: Colour) {
        val lineArtist = lineArtist ?: throw NotImplForType()
        lineArtist.draw(x1, y1, x2, y2, c)
    }

    @Suppress("unused")
    fun drawRect(rect: Recti, c: Colour) {
        val lineArtist = lineArtist ?: throw NotImplForType()
        lineArtist.draw(rect.left, rect.top, rect.right - 1, rect.top, c)
        lineArtist.draw(rect.right - 1, rect.top, rect.right - 1, rect.bottom - 1, c)
        lineArtist.draw(rect.right - 1, rect.bottom - 1, rect.left, rect.bottom - 1, c)
        lineArtist.draw(rect.left, rect.bottom - 1, rect.left, rect.top, c)
    }

    @Suppress("unused")
    fun fillRect(rect: Recti, c: Colour) {
        rectArtist.fill(rect.left, rect.top, rect.right, rect.bottom, c)
    }

    fun drawRoundRect(rect: Recti, xCornerSize: Int, yCornerSize: Int, c: Colour) {
        val roundRectArtist = roundRectArtist ?: throw NotImplForType()
        roundRectArtist.draw(rect, xCornerSize, yCornerSize, c)
    }

    fun fillRoundRect(rect: Recti, xCornerSize: Int, yCornerSize: Int, c: Colour) {
        val roundRectArtist = roundRectArtist ?: throw NotImplForType()
        roundRectArtist.fill(rect, xCornerSize, yCornerSize, c)
    }

    fun fillOval(rect: Recti, c: Colour) {
        val ovalArtist = ovalArtist ?: throw NotImplForType()
        ovalArtist.fill(rect, c)
    }

    @Suppress("unused")
    fun drawOval(rect: Recti, c: Colour) {
        val ovalArtist = ovalArtist ?: throw NotImplForType()
        ovalArtist.draw(rect, c)
    }

    fun drawText(text: String, x: Int, y: Int, c: Colour, fontName: FontManager.FontName, otlColour: Colour? = null) {
        if (type != Type.RGBA8) throw NotImplForType()
        App.Companion.fonts.drawText(text, buf, x, y, width, height, fontName, c, otlColour)
    }

    fun drawTextCentred(
        text: String,
        centreX: Int,
        y: Int,
        c: Colour,
        fontName: FontManager.FontName,
        otlColour: Colour? = null,
    ) {
        if (type != Type.RGBA8) throw NotImplForType()
        val m = App.Companion.fonts.measureText(text, fontName)
        val x = (centreX - (m.x / 2.0f)).toInt()
        App.Companion.fonts.drawText(text, buf, x, y, width, height, fontName, c, otlColour)
    }

    fun drawText(text: FontManager.FormattedText, x: Int, y: Int, c: Colour, otlColour: Colour? = null) {
        if (type != Type.RGBA8) throw NotImplForType()
        App.Companion.fonts.drawText(text, buf, x, y, width, height, c, otlColour)
    }

    fun drawImage(src: ImageData, dstX: Int, dstY: Int) {
        blitter.blit(src, dstX, dstY)
    }

    fun drawImage(src: ImageData, dstX: Int, dstY: Int, colourMultiplier: Float) {
        blitter.blit(src, dstX, dstY, colourMultiplier)
    }

    fun drawImageScaled(
        src: ImageData,
        dstX: Int,
        dstY: Int,
        dstDrawWidth: Int,
        dstDrawHeight: Int,
        antiAliasing: Boolean = true,
    ) {
        blitter.blitScaled(src, dstX, dstY, dstDrawWidth, dstDrawHeight, antiAliasing)
    }

    fun blur3x3() {
        blitter.blur3x3()
    }

    fun extendEdge(edgeSize: Int, innerRect: Recti) {
        blitter.extendEdge(edgeSize, innerRect)
    }

    companion object {
        private fun alloc(type: Type, width: Int, height: Int): ByteBuffer {
            require(width > 0) { "width must be positive!" }
            require(height > 0) { "height must be positive!" }

            val bytesPerPixel = when (type) {
                Type.RGBA8 -> 4
                Type.RGB8 -> 3
            }

            val numBytes = width * height * bytesPerPixel
            return ByteBuffer.allocateDirect(numBytes).apply {
                order(ByteOrder.nativeOrder())
            }
        }
    }
}
