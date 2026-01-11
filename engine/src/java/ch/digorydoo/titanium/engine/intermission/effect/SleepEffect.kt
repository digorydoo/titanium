package ch.digorydoo.titanium.engine.intermission.effect

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.intermission.EffectAlreadyStartedException
import ch.digorydoo.titanium.engine.intermission.EffectWasNotStartedException

internal class SleepEffect(val seconds: Float): IntermissionEffect<Unit>() {
    private var wakeupTime = 0.0f
    private var started = false

    override fun start() {
        if (started) throw EffectAlreadyStartedException()
        started = true
        wakeupTime = App.time.sessionTime + seconds
    }

    override fun checkResult() =
        when {
            !started -> throw EffectWasNotStartedException()
            App.time.sessionTime < wakeupTime -> null
            else -> IntermissionEffectResult(Unit)
        }
}
