package ch.digorydoo.titanium.engine.ui.button

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.rect.MutableRecti
import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.kutils.string.toPrecision
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.anim.FrameCollection
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.SaveGameFileWriter.Summary
import ch.digorydoo.titanium.engine.font.FontManager.FontName.DIALOG_FONT
import ch.digorydoo.titanium.engine.i18n.ITextId
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.*
import ch.digorydoo.titanium.engine.ui.button.ButtonGel.ButtonIncDec
import ch.digorydoo.titanium.engine.ui.button.ButtonGel.ButtonValue
import ch.digorydoo.titanium.engine.ui.choice.BoolChoice
import ch.digorydoo.titanium.engine.ui.choice.Choice
import ch.digorydoo.titanium.engine.ui.choice.FloatChoice
import ch.digorydoo.titanium.engine.ui.choice.SavegameChoice
import ch.digorydoo.titanium.engine.ui.choice.TextChoice
import ch.digorydoo.titanium.engine.ui.game_menu.GameMenu.Companion.SAVEGAME_THUMBNAIL_HEIGHT
import ch.digorydoo.titanium.engine.ui.game_menu.GameMenu.Companion.SAVEGAME_THUMBNAIL_WIDTH
import kotlin.math.ceil

object ButtonBuilder {
    fun create(textId: ITextId, posX: Int, posY: Int, onSelect: () -> Unit, itemWidth: Int): ButtonGel {
        val text = App.i18n.getString(textId)
        val choice = TextChoice(text, autoDismiss = false, onSelect)
        val alignment = Align.Alignment(marginLeft = posX, marginTop = posY)
        return create(choice, alignment, textTex = null, itemWidth = itemWidth)
    }

    fun create(text: String, posX: Int, posY: Int, onSelect: () -> Unit, itemWidth: Int): ButtonGel {
        val choice = TextChoice(text, autoDismiss = false, onSelect)
        val alignment = Align.Alignment(marginLeft = posX, marginTop = posY)
        return create(choice, alignment, textTex = null, itemWidth = itemWidth)
    }

    fun create(
        choice: Choice,
        alignment: Align.Alignment,
        textTex: Texture? = null, // null = create from choice
        itemWidth: Int? = null, // null = automatic
        itemHeight: Int? = null, // null = automatic
    ): ButtonGel {
        val actualTextTex = textTex ?: makeTextTexture(choice)
        val actualItemWidth = itemWidth ?: if (choice is SavegameChoice) SUMMARY_BTN_WIDTH else ITEM_MIN_WIDTH
        val actualItemHeight = itemHeight ?: if (choice is SavegameChoice) SUMMARY_BTN_HEIGHT else ITEM_DEFAULT_HEIGHT

        val bgTex = makeBgTexture(actualItemWidth, actualItemHeight)
        val otlTex = makeOtlTexture(actualItemWidth, actualItemHeight)

        val hasIncDec: Boolean
        val buttonValue: ButtonValue?
        val imageTex: Texture?

        when (choice) {
            is FloatChoice -> {
                hasIncDec = true
                val valueTex = makeStringValueTexture(choice.curValue.toPrecision(2))
                val frameSize = MutablePoint2f(valueTex.width, valueTex.height)
                buttonValue = ButtonValue(valueTex, frameSize, texOffset = MutablePoint2f(), centreVertically = false)
                choice.addOnChangeListener { redrawStringValue(valueTex, choice.curValue.toPrecision(2)) }
                imageTex = null
            }
            is BoolChoice -> {
                hasIncDec = true
                imageTex = null

                val frames = FrameCollection()
                frames.setTexture("ui-switch.png", 2, 1)
                val valueTex = frames.tex

                if (valueTex == null) {
                    Log.warn("ButtonBuilder: BoolChoice buttonValue: valueTex is null")
                    buttonValue = null
                } else {
                    frames.setFrame(if (choice.curValue) 1 else 0)
                    buttonValue = ButtonValue(valueTex, frames.frameSize, frames.texOffset)
                }

                choice.addOnChangeListener {
                    // We're sharing frames.texOffset with ButtonGel through ButtonValue::texOffset. When setting the
                    // frame here, ButtonGel will immediately see the change.
                    frames.setFrame(if (it) 1 else 0)
                }
            }
            is SavegameChoice -> {
                hasIncDec = false
                buttonValue = null
                imageTex = makeSavegameImageTexture(choice.summary)
            }
            else -> {
                hasIncDec = false
                buttonValue = null
                imageTex = null
            }
        }

        // val incDecTex: Texture?
        // val incDecFrameSize = MutablePoint2f()
        // val incTexOffset = MutablePoint2f()
        // val decTexOffset = MutablePoint2f()
        val incDec: ButtonIncDec?

        if (hasIncDec) {
            val frames = FrameCollection()
            frames.setTexture("ui-incdec.png", 2, 1)
            val incDecTex = frames.tex

            if (incDecTex == null) {
                Log.warn("ButtonBuilder: incDecTex is null")
                incDec = null
            } else {
                frames.setFrame(0)
                val decTexOffset = Point2f(frames.texOffset)

                frames.setFrame(1)
                val incTexOffset = Point2f(frames.texOffset)

                incDec = ButtonIncDec(
                    incDecTex,
                    frames.frameSize,
                    incTexOffset = incTexOffset,
                    decTexOffset = decTexOffset,
                )
            }
        } else {
            incDec = null
        }

        return ButtonGel(
            choice = choice,
            alignment = alignment,
            textTex = actualTextTex,
            bgTex = bgTex,
            otlTex = otlTex,
            imageTex = imageTex,
            value = buttonValue,
            incDec = incDec,
        )
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

    private fun makeStringValueTexture(value: String): Texture {
        val sz = App.fonts.measureText("0", DIALOG_FONT)
        val textWidth = ITEM_VALUE_MAX_WIDTH + 2 * ITEM_TEXT_INNER_PADDING
        val textHeight = ceil(sz.y).toInt() + 2 * ITEM_TEXT_INNER_PADDING
        return App.textures.createTexture(textWidth, textHeight).also {
            Log.info("Item value texture created: w=${it.width}, h=${it.height}, value=$value")
            redrawStringValue(it, value)
        }
    }

    private fun redrawStringValue(tex: Texture, value: String) {
        tex.drawInto {
            clear(Colour.transparent)
            drawTextCentred(value, tex.width / 2, ITEM_TEXT_INNER_PADDING, dlgTextColour, DIALOG_FONT)
        }
    }

    private fun makeBgTexture(width: Int, height: Int) =
        App.textures.createTexture(width, height).apply {
            drawInto {
                clear(Colour.transparent)
                val r = Recti(0, 0, width, height)
                fillRoundRect(r, ITEM_CORNER_SIZE, ITEM_CORNER_SIZE, dlgBgColour)
                drawRoundRect(r, ITEM_CORNER_SIZE, ITEM_CORNER_SIZE, Colour.black)
            }
        }

    private fun makeOtlTexture(width: Int, height: Int) =
        App.textures.createTexture(width, height).apply {
            drawInto {
                clear(Colour.transparent)
                val r = MutableRecti(0, 0, width, height)
                drawRoundRect(r, ITEM_CORNER_SIZE, ITEM_CORNER_SIZE, itemOtlColour)
                r.inset(1, 1)
                drawRoundRect(r, ITEM_CORNER_SIZE - 1, ITEM_CORNER_SIZE - 1, itemOtlColour)
            }
        }

    private fun makeSavegameImageTexture(summary: Summary) =
        App.textures.createTexture(SAVEGAME_THUMBNAIL_WIDTH, SAVEGAME_THUMBNAIL_HEIGHT).apply {
            drawInto {
                val src = summary.screenshot
                if (src == null) {
                    Log.warn("Drawing an empty thumbnail, because screenshotWhenOpened is null")
                    clear(Colour.blue)
                } else {
                    drawImageScaled(
                        src,
                        dstX = 0,
                        dstY = 0,
                        SAVEGAME_THUMBNAIL_WIDTH,
                        SAVEGAME_THUMBNAIL_HEIGHT,
                        antiAliasing = true,
                    )
                }
            }
        }
}
