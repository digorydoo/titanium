package io.github.digorydoo.titanium.engine.ui.dialogue

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.math.lerp
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import io.github.digorydoo.titanium.engine.input.gamepad.GamepadBtn
import io.github.digorydoo.titanium.engine.sound.EngineSampleId
import io.github.digorydoo.titanium.engine.ui.ITEM_MARGIN_BOTTOM
import io.github.digorydoo.titanium.engine.ui.ITEM_MARGIN_TOP
import io.github.digorydoo.titanium.engine.ui.ITEM_SPACING
import io.github.digorydoo.titanium.engine.ui.dlg_item.DlgItemGel
import io.github.digorydoo.titanium.engine.ui.icon.InputIconGel
import kotlin.math.max

/**
 * Dialogues can be a dismissable message or a list of items to choose from. Create them through App.dlg.show().
 */
internal class Dialogue(
    private val dlgTextGel: DlgTextGel?, // must not be null if items is empty, otherwise we'd show nothing
    private val dismissIcon: InputIconGel?, // the icon sticks either to dismissItem or to the dlg
    private val items: List<DlgItemGel>,
    private val dismissItem: DlgItemGel?,
    private val onClose: ((DlgItemDef?) -> Unit)?,
    initHilitedIdx: Int = 0,
) {
    private val dlgId = nextDlgId++ // for debugging purposes only
    private var hilitedIdx = initHilitedIdx
    private val hilitedGel get() = items.getOrNull(hilitedIdx)
    private val screenSizeDp = App.resolutionMgr.screenSizeDp

    // Called by DlgManager
    fun onShow() {
        dlgTextGel?.onCreate(LayerKind.UI_ABOVE_DLG)

        if (items.isNotEmpty()) {
            items.forEach { it.onCreate(LayerKind.UI_ABOVE_DLG) }
            items.getOrNull(hilitedIdx)?.hilited = true
            updateScrollOffset()
        }

        dismissIcon?.onCreate(LayerKind.UI_ABOVE_DLG)
    }

    private fun close(selectedItem: DlgItemDef?) {
        dlgTextGel?.setZombie()
        dismissIcon?.setZombie()
        items.forEach { it.setZombie() }
        App.dlg.onClose(this)
        onClose?.invoke(selectedItem)
    }

    fun abort() {
        close(null)
    }

    fun handle() {
        App.input.apply {
            when {
                selectBtn.checkPressedOnce() -> onSelectBtnPressed()
                dismissBtn.checkPressedOnce() -> onDismissBtnPressed()
                items.isNotEmpty() -> when {
                    hatOrArrowUp.checkPressedWithRepeat() -> hilitePrevItem()
                    hatOrArrowDown.checkPressedWithRepeat() -> hiliteNextItem()
                    ljoyUp.checkPressedWithRepeat() -> hilitePrevItem()
                    ljoyDown.checkPressedWithRepeat() -> hiliteNextItem()

                    hatOrArrowLeft.checkPressedWithRepeat() -> onDecrementBtnPressed(
                        smallStep = checkPressed(GamepadBtn.REAR_UPPER_LEFT) || altIsDown
                    )
                    hatOrArrowRight.checkPressedWithRepeat() -> onIncrementBtnPressed(
                        smallStep = checkPressed(GamepadBtn.REAR_UPPER_LEFT) || altIsDown
                    )
                }
            }
        }
    }

    private fun onSelectBtnPressed() {
        if (items.isEmpty()) {
            App.sound.play(EngineSampleId.BUTTON1)
            close(null)
            return
        }

        val gel = hilitedGel ?: return

        when {
            gel.canSelect -> {
                if (gel == dismissItem || gel.autoDismiss) {
                    items.forEach {
                        it.fadeOut()
                    }
                }

                gel.animateSelectAndThen {
                    onSelectAnimEnded(gel)
                }
            }
            gel.canToggle -> gel.toggle()
        }
    }

    private fun onSelectAnimEnded(gel: DlgItemGel) {
        if (gel == dismissItem || gel.autoDismiss) {
            close(gel.def)
        }
    }

    private fun onIncrementBtnPressed(smallStep: Boolean = false) {
        val gel = hilitedGel ?: return
        gel.increment(smallStep)
    }

    private fun onDecrementBtnPressed(smallStep: Boolean = false) {
        val gel = hilitedGel ?: return
        gel.decrement(smallStep)
    }

    private fun onDismissBtnPressed() {
        if (items.isEmpty()) {
            App.sound.play(EngineSampleId.BUTTON1)
            close(null)
            return
        }

        dismissItem?.let { gel ->
            App.sound.play(EngineSampleId.BUTTON1)
            hilitedGel?.hilited = false

            items.forEach {
                it.fadeOut()
            }

            if (gel.canSelect) {
                gel.animateSelectAndThen {
                    onSelectAnimEnded(gel)
                }
            } else {
                Log.warn(TAG, "Dismiss item returned false from canSelect")
                onSelectAnimEnded(gel)
            }
        }
    }

    private fun hiliteNextItem() = hiliteItemBy(1)
    private fun hilitePrevItem() = hiliteItemBy(-1)

    private fun hiliteItemBy(step: Int) {
        val prevIdx = hilitedIdx

        hilitedGel?.hilited = false
        hilitedIdx = (hilitedIdx + items.size + step) % items.size
        hilitedGel?.hilited = true

        if (hilitedIdx != prevIdx) {
            App.sound.play(EngineSampleId.HILITE1)
            updateScrollOffset()
        }
    }

    private fun updateScrollOffset() {
        if (items.size > 1) {
            val menuHeight = items.fold(0) { result, item ->
                result + item.height + ITEM_SPACING
            }

            val viewHeight = screenSizeDp.y - ITEM_MARGIN_TOP - ITEM_MARGIN_BOTTOM
            val topOffset = max(0, menuHeight - viewHeight).toFloat()
            val scrollOffset = lerp(topOffset, 0.0f, hilitedIdx.toFloat() / (items.size - 1))

            items.forEach { it.scrollOffset = scrollOffset }
            dismissIcon?.scrollOffset = scrollOffset
        }
    }

    override fun toString() =
        arrayOf(
            "dlgId=$dlgId",
            if (dlgTextGel == null) null else "dlgTextGel=$dlgTextGel",
            if (items.isEmpty()) null else "#items=${items.size}",
        )
            .filterNotNull()
            .joinToString(", ")
            .let { "Dialogue($it)" }

    companion object {
        private val TAG = Log.Tag("Dialogue")
        private var nextDlgId = 1
    }
}
