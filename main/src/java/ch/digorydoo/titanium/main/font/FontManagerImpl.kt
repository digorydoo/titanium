package ch.digorydoo.titanium.main.font

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.rect.MutableRecti
import ch.digorydoo.titanium.engine.font.FontManager
import ch.digorydoo.titanium.engine.font.FontManager.FontName
import ch.digorydoo.titanium.engine.font.FontManager.FormattedText
import ch.digorydoo.titanium.engine.image.GreyscaleImageBuffer
import org.lwjgl.stb.STBTruetype.stbtt_GetCodepointKernAdvance
import org.lwjgl.stb.STBTruetype.stbtt_MakeCodepointBitmapSubpixel
import java.nio.ByteBuffer
import kotlin.math.ceil
import kotlin.math.floor

class FontManagerImpl: FontManager {
    private val loadedFonts = mutableMapOf<FontName, Font>()
    private val drawBuffer = GreyscaleImageBuffer()

    override fun measureText(text: String, fontName: FontName) =
        measureText(text, 0, getOrLoadFont(fontName), null)

    private fun measureText(
        text: String,
        startIdx: Int,
        font: Font,
        lambda: ((index: Int, ch: Char, widthSoFar: Int) -> Boolean)?,
    ): MutablePoint2f {
        val size = MutablePoint2f(0.0f, (font.ascent - font.descent) * font.scale)
        var prevCodePoint = 0

        for (i in startIdx ..< text.length) {
            val ch = text[i]

            if (prevCodePoint > 0) {
                size.x += font.scale * stbtt_GetCodepointKernAdvance(font.info, prevCodePoint, ch.code) + font.charGap
            }

            val charMetrics = font.getOrMeasureChar(ch.code)
            size.x += charMetrics.advanceWidth * font.scale

            if (lambda != null && !lambda(i, ch, ceil(size.x).toInt())) {
                break
            }

            prevCodePoint = ch.code
        }

        return size
    }

    override fun formatText(text: String, fontName: FontName, maxWidth: Int): FormattedText {
        val font = getOrLoadFont(fontName)
        var startIdx = 0
        val lines = mutableListOf<String>()

        while (startIdx < text.length) {
            var shouldBreak = false
            var breakBefore = -1
            var skipBreakChar = true

            measureText(text, startIdx, font) { index, ch, widthSoFar ->
                if (ch == '\n') {
                    breakBefore = index
                    skipBreakChar = true
                    shouldBreak = true
                } else if (widthSoFar > maxWidth) {
                    if (breakBefore < 0) {
                        breakBefore = index // forced break!
                        skipBreakChar = false
                    }
                    shouldBreak = true
                } else {
                    when (ch) {
                        ' ', '\t' -> {
                            breakBefore = index
                            skipBreakChar = true
                        }
                        '(' -> {
                            breakBefore = index
                            skipBreakChar = false
                        }
                        '-' -> {
                            breakBefore = index + 1
                            skipBreakChar = false
                        }
                    }
                }

                // Continue measuring unless we should break the line here.
                !shouldBreak
            }

            if (shouldBreak) {
                // Add the line until the breaking point, and continue.
                lines.add(text.slice(startIdx ..< breakBefore))
                startIdx = breakBefore + if (skipBreakChar) 1 else 0
            } else {
                // Add the last line, and we're done.
                lines.add(text.slice(startIdx ..< text.length))
                break
            }
        }

        val lineHeight = (font.ascent - font.descent) * font.scale
        var maxHeight = lines.size * lineHeight

        if (lines.size > 1) {
            maxHeight += (lines.size - 1) * ceil(font.lineGap * font.scale)
        }

        // Characters may extend beyond the line height. The following is an approximation. If it turns out to be too
        // inaccurate, I should call getBoundingBoxOfChar during measurement instead.
        maxHeight -= font.descent * font.scale // descent is negative

        return FormattedText(lines, fontName, ceil(lineHeight).toInt(), ceil(maxHeight).toInt())
    }

    override fun drawText(
        text: FormattedText,
        dstBuffer: ByteBuffer,
        dstX: Int,
        dstY: Int,
        dstWidth: Int, // width of the entire destination buffer
        dstHeight: Int, // height of the entire destination buffer
        textColour: Colour,
        outlineColour: Colour?,
    ) {
        val font = getOrLoadFont(text.fontName)
        var y = dstY

        if (outlineColour != null) {
            text.lines.forEach { line ->
                doDrawText(
                    line,
                    dstBuffer,
                    dstX,
                    y,
                    dstWidth,
                    dstHeight,
                    text.fontName,
                    outlineColour,
                    asOutline = true
                )
                y += text.lineHeight + ceil(font.lineGap * font.scale).toInt()
            }

            y = dstY
        }

        text.lines.forEach { line ->
            doDrawText(line, dstBuffer, dstX, y, dstWidth, dstHeight, text.fontName, textColour, asOutline = false)
            y += text.lineHeight + ceil(font.lineGap * font.scale).toInt()
        }
    }

    override fun drawText(
        text: String,
        dstBuffer: ByteBuffer,
        dstX: Int,
        dstY: Int,
        dstWidth: Int, // width of the entire destination buffer
        dstHeight: Int, // height of the entire destination buffer
        fontName: FontName,
        textColour: Colour,
        outlineColour: Colour?,
    ) {
        if (outlineColour != null) {
            doDrawText(text, dstBuffer, dstX, dstY, dstWidth, dstHeight, fontName, outlineColour, asOutline = true)
        }

        doDrawText(text, dstBuffer, dstX, dstY, dstWidth, dstHeight, fontName, textColour, asOutline = false)
    }

    private fun doDrawText(
        text: String,
        dstBuffer: ByteBuffer,
        dstX: Int,
        dstY: Int,
        dstWidth: Int, // width of the entire destination buffer
        dstHeight: Int, // height of the entire destination buffer
        fontName: FontName,
        colour: Colour,
        asOutline: Boolean,
    ) {
        val font = getOrLoadFont(fontName)
        var xpos = dstX.toFloat()
        var prevCodePoint = 0
        val box = MutableRecti()

        for (ch in text) {
            if (prevCodePoint > 0) {
                xpos += font.scale * stbtt_GetCodepointKernAdvance(font.info, prevCodePoint, ch.code) + font.charGap
            }

            val xshift = xpos - floor(xpos)
            val charMetrics = font.getOrMeasureChar(ch.code)

            font.getBoundingBoxOfChar(ch.code, xshift, 0.0f, box)

            val charWidth = box.right - box.left
            val charHeight = box.bottom - box.top

            if (charWidth > 0 && charHeight > 0) {
                // Reallocate buffer for drawing this character.

                drawBuffer.setSize(charWidth, charHeight)

                // Draw the character into our buffer.

                stbtt_MakeCodepointBitmapSubpixel(
                    font.info,
                    drawBuffer.bytes,
                    charWidth,
                    charHeight,
                    charWidth, // row stride in bytes
                    font.scale,
                    font.scale,
                    xshift,
                    0.0f, // yshift
                    ch.code,
                )

                // Draw the buffer into the destination.

                if (asOutline) {
                    drawBuffer.blendOutlineOver(
                        dstBuffer,
                        xpos.toInt() + box.left,
                        font.baseLine + box.top + dstY,
                        dstWidth,
                        dstHeight,
                        colour,
                    )
                } else {
                    // Apply a filter for improved appearance.
                    drawBuffer.applyToEachPixel(font.filter)

                    drawBuffer.blendOver(
                        dstBuffer,
                        xpos.toInt() + box.left,
                        font.baseLine + box.top + dstY,
                        dstWidth,
                        dstHeight,
                        colour,
                    )
                }
            }

            xpos += charMetrics.advanceWidth * font.scale
            prevCodePoint = ch.code
        }
    }

    private fun getOrLoadFont(fontName: FontName): Font {
        val entry = loadedFonts[fontName]

        if (entry != null) {
            return entry
        }

        return Font.load(fontName).also { loadedFonts[fontName] = it }
    }
}
