package ch.digorydoo.titanium.engine.ui.dialogue

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.rect.MutableRecti
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.behaviours.Align.Anchor
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.font.FontManager.FontName.DIALOG_FONT
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.*
import ch.digorydoo.titanium.engine.ui.KeyBtnGel.Kind.*
import ch.digorydoo.titanium.engine.ui.button.ButtonBuilder
import ch.digorydoo.titanium.engine.ui.choice.BoolChoice
import ch.digorydoo.titanium.engine.ui.choice.Choice
import ch.digorydoo.titanium.engine.ui.choice.FloatChoice
import ch.digorydoo.titanium.engine.ui.choice.SavegameChoice
import kotlin.math.ceil
import kotlin.math.max

internal object DlgBuilder {
    fun makeDialogue(
        choices: List<Choice>? = null,
        initHilitedIdx: Int = 0,
        questionText: String, // may be combined with choices; must not be empty when there are no choices
        lastItemIsDismiss: Boolean = false,
        playSoundOnDismiss: Boolean = false,
    ): Dialogue {
        var dlgTextGel: DlgTextGel? = null
        var buttons: List<KeyBtnGel>? = null
        var itemWidth = 0

        if (!choices.isNullOrEmpty()) {
            itemWidth = ITEM_MIN_WIDTH

            val textTextures = choices.map { choice ->
                val tex = ButtonBuilder.makeTextTexture(choice)
                var requiredWidth: Int

                if (choice is SavegameChoice) {
                    requiredWidth = SUMMARY_BTN_WIDTH
                } else {
                    requiredWidth = tex.width + 2 * ITEM_TEXT_OUTER_PADDING

                    if (choice is FloatChoice || choice is BoolChoice) {
                        requiredWidth += ITEM_VALUE_MAX_WIDTH + ITEM_INCDEC_MARGIN_LR
                    }
                }

                if (itemWidth < requiredWidth) itemWidth = requiredWidth
                tex
            }

            // The last choice will get a yOffset of 0, the rest will be negative.
            var yOffset = 0

            for (idx in choices.size - 1 downTo 0) {
                val choice = choices[idx]
                val textTex = textTextures[idx]

                choice.gel = ButtonBuilder.create(
                    choice,
                    alignment = Align.Alignment(
                        anchor = Anchor.BOTTOM_RIGHT,
                        yOffset = yOffset,
                        marginRight = ITEM_MARGIN_RIGHT,
                        marginBottom = ITEM_MARGIN_BOTTOM,
                    ),
                    textTex = textTex,
                    itemWidth
                )

                yOffset -= ITEM_SPACING + when (choice) {
                    is SavegameChoice -> SUMMARY_BTN_HEIGHT
                    else -> ITEM_DEFAULT_HEIGHT
                }
            }

            if (lastItemIsDismiss) {
                val btn = KeyBtnGel(
                    kindWhenGamepad = B,
                    kindWhenKeyboard = ESC,
                    posX = App.screenWidthDp - ITEM_MARGIN_RIGHT - ITEM_KEY_OFFSET_X,
                    posY = App.screenHeightDp - ITEM_MARGIN_BOTTOM - ITEM_DEFAULT_HEIGHT + ITEM_KEY_OFFSET_Y,
                    glowEnabled = false,
                )
                buttons = listOf(btn)
            }
        }

        if (questionText.isNotEmpty()) {
            val textTex = makeDlgTextTexture(questionText, itemWidth)

            val dlgWidth = textTex.width + 2 * DLG_TEXT_MARGIN_X
            val dlgHeight = textTex.height + 2 * DLG_TEXT_MARGIN_Y
            val bgTex = makeDlgBgTexture(dlgWidth, dlgHeight)
            dlgTextGel = DlgTextGel(textTex, bgTex)

            val screenWidth = App.screenWidthDp.toFloat()
            val screenHeight = App.screenHeightDp.toFloat()

            val bgLeft = when {
                itemWidth <= 0 -> {
                    // A message dialogue with no choices is simply centred horizontally.
                    screenWidth / 2.0f - bgTex.width / 2.0f
                }
                else -> {
                    // A choice dialoge's text is placed to the left of choices.
                    max(
                        DLG_LR_MARGIN.toFloat(),
                        screenWidth - ITEM_MARGIN_RIGHT - (itemWidth + 2 * DLG_TEXT_MARGIN_X) - DLG_LR_MARGIN
                            - bgTex.width
                    )
                }
            }

            val bgTop = screenHeight - bgTex.height - DLG_BOTTOM_MARGIN
            val bgBtm = bgTop + bgTex.height

            dlgTextGel.moveTo(bgLeft, bgTop, 0.0f)

            if (choices.isNullOrEmpty()) {
                // When there are no choices, the buttons is the key symbol for the dialogue's dismiss action.
                buttons = listOf(
                    KeyBtnGel(
                        kindWhenGamepad = A,
                        kindWhenKeyboard = RETURN,
                        posX = (bgLeft + bgTex.width - DLG_BTN_RIGHT_MARGIN).toInt(),
                        posY = (bgBtm - DLG_BTN_BOTTOM_MARGIN).toInt(),
                        glowEnabled = true,
                    )
                )
            }
        }

        require(dlgTextGel != null || choices?.isNotEmpty() == true) { "Dialogue has neither a message nor choices!" }

        return Dialogue(
            dlgTextGel = dlgTextGel,
            buttons = buttons,
            choices = choices,
            initHilitedIdx = initHilitedIdx,
            lastItemIsDismiss = lastItemIsDismiss,
            playSoundOnDismiss = playSoundOnDismiss,
        )
    }

    private fun makeDlgBgTexture(dlgWidth: Int, dlgHeight: Int) =
        App.textures.createTexture(dlgWidth, dlgHeight).also {
            it.drawInto {
                // clear(Colour.transparent)
                // fillRoundRect(MutableRecti(0, 0, dlgWidth, dlgHeight), CORNER_SIZE, CORNER_SIZE, bgColour)

                clear(dlgBgColour)

                val off = DLG_CORNER_SIZE / 2
                val r = MutableRecti(0, 0, DLG_CORNER_SIZE, DLG_CORNER_SIZE).offset(-off, -off)
                fillOval(r, Colour.transparent)

                r.set(0, 0, DLG_CORNER_SIZE, DLG_CORNER_SIZE).offset(dlgWidth - 1 - off, -off)
                fillOval(r, Colour.transparent)

                r.set(0, 0, DLG_CORNER_SIZE, DLG_CORNER_SIZE).offset(-off, dlgHeight - 1 - off)
                fillOval(r, Colour.transparent)

                r.set(0, 0, DLG_CORNER_SIZE, DLG_CORNER_SIZE).offset(dlgWidth - 1 - off, dlgHeight - 1 - off)
                fillOval(r, Colour.transparent)
            }
        }

    private fun makeDlgTextTexture(text: String, choiceItemWidth: Int): Texture {
        // The text will become shorter when the choice items become wider; but never go smaller than a certain minimum!
        var texWidth = max(DLG_MIN_WIDTH, DLG_DEFAULT_WIDTH - 2 * DLG_TEXT_MARGIN_X - choiceItemWidth)
        val texHeight: Int

        val ftxt = App.fonts.formatText(text, DIALOG_FONT, texWidth - 2 * DLG_TEXT_PADDING)
        val shrink = ftxt.lines.size <= 1

        if (shrink) {
            val sz = App.fonts.measureText(text, DIALOG_FONT)
            texWidth = ceil(sz.x).toInt() + 2 * DLG_TEXT_PADDING
            texHeight = ceil(sz.y).toInt() + 2 * DLG_TEXT_PADDING
        } else {
            texHeight = ftxt.maxHeight + 2 * DLG_TEXT_PADDING
        }

        return App.textures.createTexture(texWidth, texHeight).also {
            it.drawInto {
                clear(Colour.transparent)

                if (shrink) {
                    drawText(text, DLG_TEXT_PADDING, DLG_TEXT_PADDING, dlgTextColour, DIALOG_FONT)
                } else {
                    drawText(ftxt, DLG_TEXT_PADDING, DLG_TEXT_PADDING, dlgTextColour)
                }
            }
        }
    }
}
