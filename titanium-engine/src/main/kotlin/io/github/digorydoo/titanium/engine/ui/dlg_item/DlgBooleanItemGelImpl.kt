package io.github.digorydoo.titanium.engine.ui.dlg_item

import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.sound.EngineSampleId
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgBooleanItemDef

class DlgBooleanItemGelImpl(
    def: DlgBooleanItemDef,
    private val onValueChanged: (Boolean) -> Unit,
): DlgItemGel.Impl {
    override val canSelect = false
    override val canToggle = true
    override val canIncrement get() = !curValue
    override val canDecrement get() = curValue

    private var curValue = def.initialValue
    override val curValueAsString = "" // Boolean value not displayed as such

    override fun toggle() {
        if (!curValue) increment(true)
        else decrement(true)
    }

    override fun increment(smallStep: Boolean) {
        if (!curValue) {
            App.sound.play(EngineSampleId.MENU_INC_DEC)
            curValue = true
            onValueChanged(true)
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }

    override fun decrement(smallStep: Boolean) {
        if (curValue) {
            App.sound.play(EngineSampleId.MENU_INC_DEC)
            curValue = false
            onValueChanged(false)
        } else {
            App.sound.play(EngineSampleId.NO_ACTION)
        }
    }
}
