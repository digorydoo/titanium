package io.github.digorydoo.titanium.engine.ui.dlg_item

import ch.digorydoo.kutils.math.clamp
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.sound.EngineSampleId
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgIntItemDef

class DlgIntItemGelImpl(
    private val def: DlgIntItemDef,
    private val onValueChanged: (Int) -> Unit,
): DlgItemGel.Impl {
    override val canSelect = false
    override val canToggle = false
    override val canIncrement get() = curValue < def.maxValue
    override val canDecrement get() = curValue > def.minValue

    private var curValue = def.initialValue
    override val curValueAsString get() = curValue.toString()

    override fun increment(smallStep: Boolean) {
        if (canIncrement) {
            val oldValue = curValue
            curValue += if (smallStep) def.smallStep else def.step
            keepConstraints()

            if (oldValue < def.initialValue && curValue > def.initialValue) {
                curValue = def.initialValue // startValue may be outside step grid or even outside range
            }

            App.sound.play(EngineSampleId.MENU_INC_DEC)
            onValueChanged(curValue)
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }

    override fun decrement(smallStep: Boolean) {
        if (canDecrement) {
            val oldValue = curValue
            curValue -= if (smallStep) def.smallStep else def.step
            keepConstraints()

            if (oldValue > def.initialValue && curValue < def.initialValue) {
                curValue = def.initialValue
            }

            App.sound.play(EngineSampleId.MENU_INC_DEC)
            onValueChanged(curValue)
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }

    private fun keepConstraints() {
        curValue = clamp(curValue, def.minValue, def.maxValue)
    }
}
