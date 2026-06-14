package io.github.digorydoo.titanium.engine.ui.button

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.rect.MutableRecti
import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.file.SaveGameFileWriter
import io.github.digorydoo.titanium.engine.font.FontManager.FontName.DIALOG_FONT
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.engine.ui.*
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgItemDef
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgSavegameItemDef
import kotlin.math.ceil

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

    fun makeTextTexture(item: DlgItemDef): Texture {
        val textFromSavegame = (item as? DlgSavegameItemDef)?.summary?.let { summary ->
            Moment.parseZoneAgnosticOrNull(summary.saveDate)
                ?.formatLocalized(locale = App.prefs.textLanguage.locale)
                ?.let { "${summary.sceneTitle}\n${it}" }
        }

        // It is valid to set both a text and a textId, in which case text will be displayed to the user while textId
        // may be used by code to identify the item.

        val text = textFromSavegame?.takeIf { it.isNotEmpty() }
            ?: item.text.takeIf { it.isNotEmpty() }
            ?: item.textId?.let { App.i18n.getString(it) }
            ?: ""

        if (text.isEmpty()) {
            Log.error(TAG, "No text for item: $item")
        }

        return App.textures.createTexture(
            text = text,
            allowNewlines = item is DlgSavegameItemDef,
            lineSpacing = if (item is DlgSavegameItemDef) SUMMARY_LINE_SPACING else 0,
            font = DIALOG_FONT,
            padding = ITEM_TEXT_INNER_PADDING,
            fgColour = dlgTextColour,
            otlColour = null,
        )
    }

    fun makeStringValueTexture(value: String): Texture {
        val sz = App.fonts.measureText("0", DIALOG_FONT)
        val textWidth = ITEM_VALUE_MAX_WIDTH + 2 * ITEM_TEXT_INNER_PADDING
        val textHeight = ceil(sz.y).toInt() + 2 * ITEM_TEXT_INNER_PADDING
        return App.textures.createTexture(textWidth, textHeight).also {
            Log.info(TAG, "Item value texture created: w=${it.width}, h=${it.height}, value=$value")
            redrawStringValue(it, value)
        }
    }

    fun redrawStringValue(tex: Texture, value: String) {
        tex.drawInto {
            clear(Colour.transparent)
            drawTextCentred(value, tex.width / 2, ITEM_TEXT_INNER_PADDING, dlgTextColour, DIALOG_FONT)
        }
    }

    fun makeSavegameImageTexture(summary: SaveGameFileWriter.Summary) =
        App.textures.createTexture(SAVEGAME_THUMBNAIL_WIDTH, SAVEGAME_THUMBNAIL_HEIGHT).apply {
            drawInto {
                val src = summary.screenshot
                if (src == null) {
                    Log.warn(TAG, "Drawing an empty thumbnail, because summary.screenshot is null")
                    clear(Colour.black)
                } else if (src.width != SAVEGAME_THUMBNAIL_WIDTH || src.height != SAVEGAME_THUMBNAIL_HEIGHT) {
                    Log.warn(TAG, "Thumbnail does not have the expected size: ${src.width}x${src.height}")
                    clear(Colour.black)
                } else {
                    // We can't help redrawing the image here, because the thumbnail is RGB8, but we need RGBA8.
                    drawImage(src, 0, 0)
                }
            }
        }
}
