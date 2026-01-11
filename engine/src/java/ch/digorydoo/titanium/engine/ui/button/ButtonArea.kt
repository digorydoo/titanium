package ch.digorydoo.titanium.engine.ui.button

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.i18n.ITextId
import ch.digorydoo.titanium.engine.sound.EngineSampleId
import ch.digorydoo.titanium.engine.ui.ITEM_DEFAULT_HEIGHT
import ch.digorydoo.titanium.engine.ui.dialogue.DlgTextItemDef
import ch.digorydoo.titanium.engine.ui.dlg_item.DlgItemGel
import ch.digorydoo.titanium.engine.ui.dlg_item.DlgTextItemGel

class ButtonArea<Id>(marginLeft: Int, marginTop: Int) {
    private val buttons = mutableListOf<DlgItemGel<Id>>()
    private val willAddAt = MutablePoint2f(marginLeft, marginTop)
    private var hilitedIdx = -1

    fun addButton(textId: ITextId, onSelect: () -> Unit) {
        val btn = DlgTextItemGel(
            def = DlgTextItemDef.build<Id> {
                text = App.i18n.getString(textId)
                autoDismiss = false
                this.onSelect = onSelect
            },
            alignment = Align.Alignment(
                marginLeft = willAddAt.x.toInt(),
                marginTop = willAddAt.y.toInt()
            ),
            btnWidth = BTN_WIDTH,
            btnHeight = ITEM_DEFAULT_HEIGHT,
            precomputedTextTex = null,
        )

        btn.onCreate(LayerKind.UI_BELOW_DLG)
        btn.hide()
        buttons.add(btn)
        willAddAt.y += btn.height + BTN_SPACING
    }

    fun addGap() {
        willAddAt.y += BTN_GAP - BTN_SPACING
    }

    fun removeGels() {
        buttons.forEach { it.setZombie() }
        buttons.clear()
        hilitedIdx = -1
    }

    fun showAll() {
        buttons.forEach { it.show() }
        hilite(0)
    }

    fun hideAll() {
        buttons.forEach { it.hide() }
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
        when {
            buttons.isEmpty() -> return
            hilitedIdx < 0 -> hilite(0)
            else -> hilite((hilitedIdx + buttons.size - 1) % buttons.size)
        }
    }

    fun selectHilited() {
        if (hilitedIdx in buttons.indices) {
            App.sound.play(EngineSampleId.BUTTON1)
            buttons[hilitedIdx].select()
        }
    }

    private fun hilite(newIdx: Int) {
        if (hilitedIdx in buttons.indices) {
            buttons[hilitedIdx].hilited = false
        }

        if (newIdx in buttons.indices) {
            buttons[newIdx].hilited = true
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
