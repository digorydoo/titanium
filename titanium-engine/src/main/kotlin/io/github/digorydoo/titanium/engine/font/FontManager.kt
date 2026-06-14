package io.github.digorydoo.titanium.engine.font

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.vector.MutableVector2f
import java.nio.ByteBuffer

interface FontManager {
    enum class FontName {
        DIALOG_FONT,
        SNACKBAR_FONT,
        TOPIC_FONT,
        LARGE_HUD_FONT, // e.g. ActionInputIconGel: action of nearby object)
        MEDIUM_HUD_FONT, // e.g. InputIconGel: footer of HUD
        SMALL_HUD_FONT, // e.g. TextGel: small text of GameHUD and editor
    }

    class FormattedText(
        val lines: List<String>,
        val fontName: FontName,
        val lineHeight: Int,
        val maxHeight: Int,
    )

    fun measureText(text: String, fontName: FontName): MutableVector2f

    fun formatText(text: String, fontName: FontName, maxWidth: Int): FormattedText

    fun drawText(
        text: String,
        dstBuffer: ByteBuffer,
        dstX: Int,
        dstY: Int,
        dstWidth: Int, // width of entire destination buffer
        dstHeight: Int, // height of entire destination buffer
        fontName: FontName,
        textColour: Colour,
        outlineColour: Colour?,
    )

    fun drawText(
        text: FormattedText,
        dstBuffer: ByteBuffer,
        dstX: Int,
        dstY: Int,
        dstWidth: Int, // width of entire destination buffer
        dstHeight: Int, // height of entire destination buffer
        textColour: Colour,
        outlineColour: Colour?,
    )
}
