package ch.digorydoo.titanium.engine.ui.dialogue

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.input.gamepad.GamepadBtn
import ch.digorydoo.titanium.engine.sound.EngineSampleId
import ch.digorydoo.titanium.engine.ui.ITEM_MARGIN_BOTTOM
import ch.digorydoo.titanium.engine.ui.ITEM_MARGIN_TOP
import ch.digorydoo.titanium.engine.ui.ITEM_SPACING
import ch.digorydoo.titanium.engine.ui.choice.BoolChoice
import ch.digorydoo.titanium.engine.ui.choice.Choice
import ch.digorydoo.titanium.engine.ui.icon.DlgInputIconGel
import kotlin.math.max

/**
 * Dialogues are created through DlgManager (showDlg, showChoice).
 * A message dialogue has dlgTextGel, icons.
 * A choice dialogue has items, lastItemIsDismiss, icons.
 */
class Dialogue(
    private val dlgTextGel: DlgTextGel?, // may or may not be null if this is a choice dlg
    private val icon: DlgInputIconGel?, // either the dialogue's or an item's dismiss action
    private val choices: List<Choice>?, // items of a choice dialogue
    initHilitedIdx: Int = 0,
    private val lastItemIsDismiss: Boolean,
    private val playSoundOnDismiss: Boolean,
) {
    private val dlgId = nextDlgId++ // for debugging purposes only
    private var hilitedIdx = initHilitedIdx
    private val hilitedChoice get() = choices?.getOrNull(hilitedIdx)
    private val numChoices get() = choices?.size ?: 0

    // Called by DlgManager
    fun onShow() {
        if (dlgTextGel != null) {
            App.content.add(dlgTextGel, LayerKind.UI_ABOVE_DLG)
        }

        if (choices != null) {
            choices.forEach { App.content.add(it.gel!!, LayerKind.UI_ABOVE_DLG) }
            choices.getOrNull(hilitedIdx)?.gel?.hilited = true
            updateScrollOffset()
        }

        icon?.let { App.content.add(it, LayerKind.UI_ABOVE_DLG) }
    }

    // Called by DlgManager
    fun onDismiss() {
        dlgTextGel?.setZombie()
        icon?.setZombie()
        choices?.forEach { it.gel?.setZombie() }
    }

    private fun dismiss() {
        App.dlg.dismiss(this)
    }

    fun handle() {
        App.input.apply {
            when {
                selectBtn.pressedOnce -> onSelectBtnPressed()
                dismissBtn.pressedOnce -> onDismissBtnPressed()
                choices != null -> when {
                    hatOrArrowUp.pressedWithRepeat -> hilitePrevItem()
                    hatOrArrowDown.pressedWithRepeat -> hiliteNextItem()
                    ljoyUp.pressedWithRepeat -> hilitePrevItem()
                    ljoyDown.pressedWithRepeat -> hiliteNextItem()

                    hatOrArrowLeft.pressedWithRepeat -> onDecrementBtnPressed(
                        smallStep = isPressed(GamepadBtn.REAR_UPPER_LEFT) || altPressed
                    )
                    hatOrArrowRight.pressedWithRepeat -> onIncrementBtnPressed(
                        smallStep = isPressed(GamepadBtn.REAR_UPPER_LEFT) || altPressed
                    )
                }
            }
        }
    }

    private fun onSelectBtnPressed() {
        if (choices == null) {
            playBtnSound(isDismiss = true)
            dismiss()
            return
        }

        val choice = hilitedChoice ?: return
        val gel = choice.gel ?: return

        if (!choice.canSelect) {
            if (choice is BoolChoice) {
                when {
                    choice.canIncrement -> onIncrementBtnPressed()
                    choice.canDecrement -> onDecrementBtnPressed()
                }
            }
            return
        }

        val isLastAndDismiss = choice == choices.lastOrNull() && lastItemIsDismiss
        playBtnSound(isLastAndDismiss)

        if (isLastAndDismiss || choice.autoDismiss) {
            choices.forEach {
                it.gel?.fadeOut()
            }
        }

        gel.selectAndCall {
            if (isLastAndDismiss || choice.autoDismiss) {
                dismiss()
            } else {
                gel.unselect()
            }

            choice.onSelect()
        }
    }

    private fun onIncrementBtnPressed(smallStep: Boolean = false) {
        val choice = hilitedChoice ?: return

        if (choice.canIncrement) {
            App.sound.play(EngineSampleId.MENU_INC_DEC)
            choice.onIncrement(smallStep)
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }

    private fun onDecrementBtnPressed(smallStep: Boolean = false) {
        val choice = hilitedChoice ?: return

        if (choice.canDecrement) {
            App.sound.play(EngineSampleId.MENU_INC_DEC)
            choice.onDecrement(smallStep)
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }

    private fun onDismissBtnPressed() {
        if (choices == null) {
            playBtnSound(isDismiss = true)
            dismiss()
            return
        }

        if (lastItemIsDismiss && choices.isNotEmpty()) {
            choices.last().let { choice ->
                playBtnSound(isDismiss = true)
                hilitedChoice?.gel?.hilited = false

                choices.forEach {
                    it.gel?.fadeOut()
                }

                choice.gel?.selectAndCall {
                    dismiss()
                    choice.onSelect()
                }
            }
        }
    }

    private fun hiliteNextItem() = hiliteItemBy(1)
    private fun hilitePrevItem() = hiliteItemBy(-1)

    private fun hiliteItemBy(step: Int) {
        val prevIdx = hilitedIdx

        hilitedChoice?.gel?.hilited = false
        hilitedIdx = (hilitedIdx + numChoices + step) % numChoices
        hilitedChoice?.gel?.hilited = true

        if (hilitedIdx != prevIdx) {
            App.sound.play(EngineSampleId.HILITE1)
            updateScrollOffset()
        }
    }

    private fun updateScrollOffset() {
        if (numChoices > 1) {
            val menuHeight = choices?.fold(0) { result, item ->
                result + (item.gel?.height ?: 0) + ITEM_SPACING
            } ?: 0

            val viewHeight = App.screenHeightDp - ITEM_MARGIN_TOP - ITEM_MARGIN_BOTTOM
            val topOffset = max(0, menuHeight - viewHeight).toFloat()
            val scrollOffset = lerp(topOffset, 0.0f, hilitedIdx.toFloat() / (numChoices - 1))

            choices?.forEach { it.gel?.scrollOffset = scrollOffset }
            icon?.scrollOffset = scrollOffset
        }
    }

    private fun playBtnSound(isDismiss: Boolean) {
        when {
            choices == null -> App.sound.play(EngineSampleId.MSG_DISMISS)
            isDismiss && playSoundOnDismiss -> App.sound.play(EngineSampleId.CHOICES_DISMISS)
            else -> App.sound.play(EngineSampleId.BUTTON1)
        }
    }

    override fun toString() =
        arrayOf(
            "dlgId=$dlgId",
            if (dlgTextGel == null) null else "dlgTextGel=$dlgTextGel",
            if (choices == null) null else "#choices=${choices.size}",
        )
            .filterNotNull()
            .joinToString(", ")
            .let { "Dialogue($it)" }

    companion object {
        private var nextDlgId = 1
    }
}
