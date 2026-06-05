package io.github.digorydoo.titanium.engine.ui.button

import ch.digorydoo.kutils.vector.MutableVector2f
import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import io.github.digorydoo.titanium.engine.i18n.ITextId
import io.github.digorydoo.titanium.engine.sound.EngineSampleId
import io.github.digorydoo.titanium.engine.ui.ITEM_DEFAULT_HEIGHT
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgTextItemDef
import io.github.digorydoo.titanium.engine.ui.dlg_item.DlgTextItemGel

class ButtonArea(marginLeft: Int, marginTop: Int) {
    private class Button(val gel: DlgTextItemGel, val onSelect: () -> Unit)

    private val buttons = mutableListOf<Button>()
    private val willAddAt = MutableVector2f(marginLeft, marginTop)
    private var hilitedIdx = -1

    fun addButton(textId: ITextId, onSelect: () -> Unit) {
        val gel = DlgTextItemGel(
            def = DlgTextItemDef.build {
                text = App.i18n.getString(textId)
                autoDismiss = false
                // We're not setting the dlg item's onSelect, because that onSelect is suspending while ours is not.
                // Instead, we keep our onSelect outside the definition.
            },
            alignment = Align.Alignment(
                marginLeft = willAddAt.x.toInt(),
                marginTop = willAddAt.y.toInt()
            ),
            btnWidth = BTN_WIDTH,
            btnHeight = ITEM_DEFAULT_HEIGHT,
            precomputedTextTex = null,
        )

        gel.onCreate(LayerKind.UI_BELOW_DLG)
        gel.hide()
        buttons.add(Button(gel, onSelect))
        willAddAt.y += gel.height + BTN_SPACING
    }

    fun addGap() {
        willAddAt.y += BTN_GAP - BTN_SPACING
    }

    fun removeGels() {
        buttons.forEach { it.gel.setZombie() }
        buttons.clear()
        hilitedIdx = -1
    }

    fun showAll() {
        buttons.forEach { it.gel.show() }
        hilite(0)
    }

    fun hideAll() {
        buttons.forEach { it.gel.hide() }
        hilite(-1)
    }

    fun hiliteNext() {
        val prevIdx = hilitedIdx

        when {
            buttons.isEmpty() -> return
            hilitedIdx < 0 -> hilite(0)
            else -> hilite((hilitedIdx + 1) % buttons.size)
        }

        if (hilitedIdx != prevIdx) {
            App.sound.play(EngineSampleId.HILITE1)
        }
    }

    fun hilitePrev() {
        val prevIdx = hilitedIdx

        when {
            buttons.isEmpty() -> return
            hilitedIdx < 0 -> hilite(0)
            else -> hilite((hilitedIdx + buttons.size - 1) % buttons.size)
        }

        if (hilitedIdx != prevIdx) {
            App.sound.play(EngineSampleId.HILITE1)
        }
    }

    fun selectHilited() {
        if (hilitedIdx in buttons.indices) {
            App.sound.play(EngineSampleId.BUTTON1)
            val btn = buttons[hilitedIdx]
            btn.gel.animateSelectAndThen { btn.onSelect() }
        }
    }

    private fun hilite(newIdx: Int) {
        if (hilitedIdx in buttons.indices) {
            buttons[hilitedIdx].gel.hilited = false
        }

        if (newIdx in buttons.indices) {
            buttons[newIdx].gel.hilited = true
            hilitedIdx = newIdx
        } else {
            hilitedIdx = -1
        }
    }

    companion object {
        private const val BTN_SPACING = 2
        private const val BTN_GAP = 32
        private const val BTN_WIDTH = 320
    }
}
