package ch.digorydoo.titanium.engine.ui.button.helper

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.rect.MutableRecti
import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.font.FontManager.FontName.DIALOG_FONT
import ch.digorydoo.titanium.engine.ui.*
import ch.digorydoo.titanium.engine.ui.choice.Choice
import ch.digorydoo.titanium.engine.ui.choice.SavegameChoice

object ButtonTextureFactory {
    fun makeBgTexture(width: Int, height: Int) =
        App.textures.createTexture(width, height).apply {
            drawInto {
                clear(Colour.transparent)
                val r = Recti(0, 0, width, height)
                fillRoundRect(r, ITEM_CORNER_SIZE, ITEM_CORNER_SIZE, dlgBgColour)
                drawRoundRect(r, ITEM_CORNER_SIZE, ITEM_CORNER_SIZE, Colour.black)
            }
        }

    fun makeOtlTexture(width: Int, height: Int) =
        App.textures.createTexture(width, height).apply {
            drawInto {
                clear(Colour.transparent)
                val r = MutableRecti(0, 0, width, height)
                drawRoundRect(r, ITEM_CORNER_SIZE, ITEM_CORNER_SIZE, itemOtlColour)
                r.inset(1, 1)
                drawRoundRect(r, ITEM_CORNER_SIZE - 1, ITEM_CORNER_SIZE - 1, itemOtlColour)
            }
        }

    fun makeTextTexture(choice: Choice) =
        App.textures.createTexture(
            choice.itemText,
            allowNewlines = choice is SavegameChoice,
            lineSpacing = if (choice is SavegameChoice) SUMMARY_LINE_SPACING else 0,
            font = DIALOG_FONT,
            padding = ITEM_TEXT_INNER_PADDING,
            fgColour = dlgTextColour,
            otlColour = null,
        )
}
