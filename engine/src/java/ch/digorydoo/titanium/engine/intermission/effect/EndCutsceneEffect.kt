package ch.digorydoo.titanium.engine.intermission.effect

import ch.digorydoo.titanium.engine.intermission.EffectAlreadyStartedException

internal class EndCutsceneEffect: IntermissionEffect<Unit>() {
    private var started = false

    override fun start() {
        // Tells the IntermissionManager to disable skipping. The black stripes remain, because it looks odd if
        // they keep appearing and disappearing when the cutscene should have multiple skip points. We do NOT
        // pass a Boolean to the lamda indicating whether the cutscene was skipped, because we can't tell what
        // portion was skipped, so the lambda will have to deal with all possible situations anyway.
        // TODO

        if (started) throw EffectAlreadyStartedException()
        else started = true
    }

    override fun checkResult(): IntermissionEffectResult<Unit>? {
        return null
    }
}
