package io.github.digorydoo.titanium.engine.ui.dialogue

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.rect.MutableRecti
import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.behaviours.Align.Anchor
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.font.FontManager.FontName.DIALOG_FONT
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.engine.ui.*
import io.github.digorydoo.titanium.engine.ui.button.ButtonTextureFactory
import io.github.digorydoo.titanium.engine.ui.dlg_item.*
import io.github.digorydoo.titanium.engine.ui.icon.DlgInputIconGel
import io.github.digorydoo.titanium.engine.ui.icon.Icon.*
import kotlin.math.ceil
import kotlin.math.max

internal object DlgFactory {
    private class ItemTextures(val list: List<Texture>, val minItemWidth: Int)

    fun <Id> create(def: DlgDef<Id>): Dialogue<Id> {
        var dlgTextGel: DlgTextGel? = null
        var dismissIcon: DlgInputIconGel? = null
        var itemWidth = 0
        val itemGels = mutableListOf<DlgItemGel<Id>>()
        var dismissItem: DlgItemGel<Id>? = null

        if (def.items.isNotEmpty()) {
            val textTextures = createTextTextures(def.items)
            itemWidth = textTextures.minItemWidth

            // The last item will get a yOffset of 0, the rest will be negative.
            var yOffset = 0

            for (idx in def.items.size - 1 downTo 0) {
                val item = def.items[idx]
                val textTex = textTextures.list[idx]
                val gel = createItemGel(item, yOffset, textTex, itemWidth)
                itemGels.add(gel)

                if (def.dismiss == item) {
                    dismissItem = gel
                    dismissIcon = createDismissIconForItem(yOffset)
                }

                yOffset -= ITEM_SPACING + when (item) {
                    is DlgSavegameItemDef -> SUMMARY_BTN_HEIGHT
                    else -> ITEM_DEFAULT_HEIGHT
                }
            }

            itemGels.reverse() // we've added them in reverse order
        }

        if (def.text.isNotEmpty()) {
            val textTex = createDlgTextTexture(def.text, itemWidth)

            val dlgWidth = textTex.width + 2 * DLG_TEXT_MARGIN_X
            val dlgHeight = textTex.height + 2 * DLG_TEXT_MARGIN_Y
            val bgTex = createDlgBgTexture(dlgWidth, dlgHeight)
            dlgTextGel = DlgTextGel(textTex, bgTex)

            val screenSizeDp = App.resolutionMgr.screenSizeDp
            val screenWidth = screenSizeDp.x.toFloat()
            val screenHeight = screenSizeDp.y.toFloat()

            val bgLeft = when {
                itemWidth <= 0 -> {
                    // The text of a dialogue with no items is centred in the screen.
                    screenWidth / 2.0f - bgTex.width / 2.0f
                }
                else -> {
                    // The text of a dialogue with items is placed to the left of the items.
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

            if (def.items.isEmpty()) {
                // When there are no items, it's a modal dialogue, and icon is its dismiss action.
                dismissIcon = createDismissIconForDlg((bgLeft + bgTex.width).toInt(), bgBtm.toInt())
            }
        }

        require(dlgTextGel != null || def.items.isNotEmpty()) { "Dialogue has neither a message nor items!" }
        val initHilitedIdx = def.focus?.let { def.items.indexOf(it) }?.takeIf { it >= 0 } ?: 0

        return Dialogue(
            dlgTextGel = dlgTextGel,
            items = itemGels,
            initHilitedIdx = initHilitedIdx,
            dismissItem = dismissItem,
            dismissIcon = dismissIcon,
            onClose = def.onClose,
        )
    }

    private fun createTextTextures(items: List<DlgItemDef<*>>): ItemTextures {
        var minWidthTotal = ITEM_MIN_WIDTH

        val textTextures = items.map { item ->
            val tex = ButtonTextureFactory.makeTextTexture(item)
            var minWidth: Int

            if (item is DlgSavegameItemDef) {
                minWidth = SUMMARY_BTN_WIDTH
            } else {
                minWidth = tex.width + 2 * ITEM_TEXT_OUTER_PADDING

                if (item is DlgBooleanItemDef || item is DlgIntItemDef || item is DlgFloatItemDef) {
                    minWidth += ITEM_VALUE_MAX_WIDTH + ITEM_INCDEC_MARGIN_LR
                }
            }

            if (minWidthTotal < minWidth) minWidthTotal = minWidth
            tex
        }

        return ItemTextures(textTextures, minWidthTotal)
    }

    private fun <Id> createItemGel(
        def: DlgItemDef<Id>,
        yOffset: Int,
        precomputedTextTex: Texture? = null, // null = create from item def
        itemWidth: Int? = null, // null = automatic
        btnHeight: Int? = null, // null = automatic
    ): DlgItemGel<Id> {
        val alignment = Align.Alignment(
            anchor = Anchor.BOTTOM_RIGHT,
            yOffset = yOffset,
            marginRight = ITEM_MARGIN_RIGHT,
            marginBottom = ITEM_MARGIN_BOTTOM,
        )
        return when (def) {
            is DlgTextItemDef -> DlgTextItemGel(
                def = def,
                alignment = alignment,
                btnWidth = itemWidth ?: ITEM_MIN_WIDTH,
                btnHeight = btnHeight ?: ITEM_DEFAULT_HEIGHT,
                precomputedTextTex = precomputedTextTex,
            )
            is DlgBooleanItemDef -> DlgBooleanItemGel(
                def = def,
                alignment = alignment,
                btnWidth = itemWidth ?: ITEM_MIN_WIDTH,
                btnHeight = btnHeight ?: ITEM_DEFAULT_HEIGHT,
                precomputedTextTex = precomputedTextTex,
            )
            is DlgIntItemDef -> DlgIntItemGel(
                def = def,
                alignment = alignment,
                btnWidth = itemWidth ?: ITEM_MIN_WIDTH,
                btnHeight = btnHeight ?: ITEM_DEFAULT_HEIGHT,
                precomputedTextTex = precomputedTextTex,
            )
            is DlgFloatItemDef -> DlgFloatItemGel(
                def = def,
                alignment = alignment,
                btnWidth = itemWidth ?: ITEM_MIN_WIDTH,
                btnHeight = btnHeight ?: ITEM_DEFAULT_HEIGHT,
                precomputedTextTex = precomputedTextTex,
            )
            is DlgSavegameItemDef -> DlgSavegameItemGel(
                def = def,
                alignment = alignment,
                btnWidth = itemWidth ?: SUMMARY_BTN_WIDTH,
                btnHeight = btnHeight ?: SUMMARY_BTN_HEIGHT,
                precomputedTextTex = precomputedTextTex,
            )
        }
    }

    private fun createDismissIconForItem(yOffset: Int): DlgInputIconGel {
        val screenSizeDp = App.resolutionMgr.screenSizeDp
        return DlgInputIconGel(
            iconWhenGamepad = B,
            iconWhenKeyboard = ESC,
            posX = screenSizeDp.x - ITEM_MARGIN_RIGHT - ITEM_ICON_OFFSET_X,
            posY = screenSizeDp.y - ITEM_MARGIN_BOTTOM - ITEM_DEFAULT_HEIGHT + ITEM_ICON_OFFSET_Y + yOffset,
            glowEnabled = false,
        )
    }

    private fun createDismissIconForDlg(xOffset: Int, yOffset: Int) =
        DlgInputIconGel(
            iconWhenGamepad = A,
            iconWhenKeyboard = RETURN,
            posX = xOffset - DLG_BTN_RIGHT_MARGIN,
            posY = yOffset - DLG_BTN_BOTTOM_MARGIN,
            glowEnabled = true,
        )

    private fun createDlgBgTexture(dlgWidth: Int, dlgHeight: Int) =
        App.textures.createTexture(dlgWidth, dlgHeight).also {
            it.drawInto {
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

    private fun createDlgTextTexture(text: String, itemWidth: Int): Texture {
        // The text will become shorter when the items become wider; but never go smaller than a certain minimum
        var texWidth = max(DLG_MIN_WIDTH, DLG_DEFAULT_WIDTH - 2 * DLG_TEXT_MARGIN_X - itemWidth)
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
