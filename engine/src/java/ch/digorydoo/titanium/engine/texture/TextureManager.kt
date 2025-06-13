package ch.digorydoo.titanium.engine.texture

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.titanium.engine.font.FontManager.FontName

interface TextureManager {
    fun getOrCreateTexture(fname: String): Texture?
    fun createTexture(width: Int, height: Int): Texture

    fun createTexture(
        text: String,
        width: Int? = null,
        height: Int? = null,
        allowNewlines: Boolean = false,
        lineSpacing: Int = 4, // only applies when allowNewlines is true
        font: FontName = FontName.SMALL_HUD_FONT,
        padding: Int = GreyscaleImageBuffer.OUTLINE_RANGE,
        fgColour: Colour = Colour.white,
        bgColour: Colour = Colour.transparent,
        otlColour: Colour? = Colour.grey900,
    ): Texture
}
