package io.github.digorydoo.titanium.engine.ui.button

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.rect.MutableRecti
import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.titanium.BuildConfig
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.font.FontManager.FontName.DIALOG_FONT
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.engine.ui.*
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgItemDef
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgSavegameItemDef

object ButtonTextureFactory {
    private val TAG = Log.Tag("ButtonTextureFactory")

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

    fun makeTextTexture(item: DlgItemDef<*>): Texture {
        val textFromSavegame = (item as? DlgSavegameItemDef)?.summary?.let { summary ->
            Moment.parseZoneAgnosticOrNull(summary.saveDate)
                ?.formatLocalized(locale = App.prefs.textLanguage.locale)
                ?.let { "${summary.sceneTitle}\n${it}" }
                ?: ""
        }

        val textFromId = item.textId?.let { App.i18n.getString(it) }
        val textFromString = item.text

        if (!BuildConfig.isProduction) {
            val check = listOfNotNull(textFromSavegame, textFromId, textFromString).filter { it.isNotEmpty() }

            if (check.size > 1) {
                Log.warn(TAG, "Item cannot have more than one text: ${check.joinToString("/")}")
            }
        }

        return App.textures.createTexture(
            text = textFromSavegame ?: textFromId ?: textFromString,
            allowNewlines = item is DlgSavegameItemDef,
            lineSpacing = if (item is DlgSavegameItemDef) SUMMARY_LINE_SPACING else 0,
            font = DIALOG_FONT,
            padding = ITEM_TEXT_INNER_PADDING,
            fgColour = dlgTextColour,
            otlColour = null,
        )
    }
}
