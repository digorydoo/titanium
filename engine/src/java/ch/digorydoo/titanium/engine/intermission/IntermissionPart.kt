package ch.digorydoo.titanium.engine.intermission

import ch.digorydoo.titanium.engine.intermission.effect.IntermissionEffect
import ch.digorydoo.titanium.engine.intermission.effect.IntermissionEffectResult

typealias IntermissionBody<Arg, Result> = IntermissionEffectFactory.(arg: Arg) -> IntermissionEffect<Result>

class IntermissionPart<Arg, Result> internal constructor(private val body: IntermissionBody<Arg, Result>) {
    private var next: IntermissionPart<Result, *>? = null
    private var effect: IntermissionEffect<Result>? = null
    private var result: IntermissionEffectResult<Result>? = null

    fun <NextResult> then(lambda: IntermissionBody<Result, NextResult>): IntermissionPart<Result, NextResult> {
        if (next != null) throw NextAlreadyDefinedException()
        return IntermissionPart(lambda).also { next = it }
    }

    internal fun callBody(arg: Arg, factory: IntermissionEffectFactory) {
        if (effect != null) throw BodyAlreadyCalledException()
        effect = factory.body(arg).apply { start() }
    }

    internal fun checkResult(): Boolean {
        if (result == null) {
            val effect = effect ?: throw BodyWasNotCalledException()
            result = effect.checkResult()
        }
        return result != null
    }

    internal fun callNext(factory: IntermissionEffectFactory): IntermissionPart<Result, *>? {
        val result = result ?: throw StillWaitingForResultException()
        val next = next ?: return null // there is no next part
        next.callBody(result.result, factory)
        return next
    }
}
