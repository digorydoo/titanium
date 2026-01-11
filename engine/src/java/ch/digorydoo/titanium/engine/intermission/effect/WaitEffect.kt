package ch.digorydoo.titanium.engine.intermission.effect

import ch.digorydoo.titanium.engine.intermission.EffectAlreadyStartedException
import ch.digorydoo.titanium.engine.intermission.EffectWasNotStartedException

internal class WaitEffect(val until: () -> Boolean): IntermissionEffect<Unit>() {
    private var started = false

    override fun start() {
        if (started) throw EffectAlreadyStartedException()
        else started = true
    }

    override fun checkResult() =
        when {
            !started -> throw EffectWasNotStartedException()
            !until() -> null
            else -> IntermissionEffectResult(Unit)
        }
}
