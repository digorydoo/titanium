package ch.digorydoo.titanium.engine.ui.button

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.i18n.ITextId

class ButtonArea(marginLeft: Int, marginTop: Int) {
    private val buttons = mutableListOf<ButtonGel>()
    private val willAddAt = MutablePoint2f(marginLeft, marginTop)
    private var hilitedIdx = -1

    fun addButton(textId: ITextId, onSelect: () -> Unit) {
        val btn = ButtonBuilder.create(
            textId,
            posX = willAddAt.x.toInt(),
            posY = willAddAt.y.toInt(),
            onSelect,
            BTN_WIDTH,
        )
        App.content.add(btn, LayerKind.UI_BELOW_DLG)
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
        when {
            buttons.isEmpty() -> return
            hilitedIdx < 0 -> hilite(0)
            else -> hilite((hilitedIdx + 1) % buttons.size)
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
