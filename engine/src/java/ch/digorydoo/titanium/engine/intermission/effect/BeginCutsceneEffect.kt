package ch.digorydoo.titanium.engine.intermission.effect

import ch.digorydoo.titanium.engine.intermission.EffectAlreadyStartedException

internal class BeginCutsceneEffect: IntermissionEffect<Unit>() {
    private var started = false

    override fun start() {
        // Narrows the view with black stripes, which then will stay beyond endCutscene!
        // Tells the IntermissionManager to enable skipping. Skipping will search forward to the next
        // EndCutsceneEffect.
        // The result is Unit and comes in immediately.
        // TODO

        if (started) throw EffectAlreadyStartedException()
        else started = true
    }

    override fun checkResult(): IntermissionEffectResult<Unit>? {
        return null
    }
}
