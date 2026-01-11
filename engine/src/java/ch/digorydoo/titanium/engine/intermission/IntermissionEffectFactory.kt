package ch.digorydoo.titanium.engine.intermission

import ch.digorydoo.titanium.engine.intermission.effect.*
import ch.digorydoo.titanium.engine.ui.dialogue.DlgDef

class IntermissionEffectFactory internal constructor() {
    fun <Id> showDlg(lambda: DlgDef<Id>.() -> Unit): IntermissionEffect<Id?> {
        val def = DlgDef.build(lambda)
        return DlgEffect(def)
    }

    fun sleep(seconds: Float): IntermissionEffect<Unit> {
        return SleepEffect(seconds)
    }

    fun waitFor(until: () -> Boolean): IntermissionEffect<Unit> {
        return WaitEffect(until)
    }

    fun beginCutscene(): IntermissionEffect<Unit> {
        return BeginCutsceneEffect()
    }

    fun endCutscene(): IntermissionEffect<Unit> {
        return EndCutsceneEffect()
    }
}
