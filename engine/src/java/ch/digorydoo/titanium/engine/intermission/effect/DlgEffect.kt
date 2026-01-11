package ch.digorydoo.titanium.engine.intermission.effect

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.intermission.EffectAlreadyStartedException
import ch.digorydoo.titanium.engine.intermission.ResultAlreadySetException
import ch.digorydoo.titanium.engine.ui.dialogue.DlgDef

internal class DlgEffect<Id>(val def: DlgDef<Id>): IntermissionEffect<Id?>() {
    private var started = false
    private var result: IntermissionEffectResult<Id?>? = null

    override fun start() {
        if (started) throw EffectAlreadyStartedException()

        val origOnClose = def.onClose

        def.onClose = { selectedItem ->
            origOnClose?.invoke(selectedItem)
            if (result != null) throw ResultAlreadySetException()
            result = IntermissionEffectResult(selectedItem?.id)
        }

        App.dlg.showDlg(def)
        started = true
    }

    override fun checkResult() = result
}
