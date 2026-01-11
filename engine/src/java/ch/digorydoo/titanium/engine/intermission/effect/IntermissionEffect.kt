package ch.digorydoo.titanium.engine.intermission.effect

// Result itself may be nullable, so we need to wrap it to know whether we've got a result
internal class IntermissionEffectResult<Result>(val result: Result)

sealed class IntermissionEffect<Result> {
    internal abstract fun start()
    internal abstract fun checkResult(): IntermissionEffectResult<Result>?
}
