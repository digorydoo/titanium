package ch.digorydoo.titanium.engine.intermission

import ch.digorydoo.kutils.utils.Log

class NextAlreadyDefinedException: Exception("Next part is already defined")
class BodyAlreadyCalledException: Exception("Intermission body has already been called")
class BodyWasNotCalledException: Exception("Intermission body wasn't called")
class StillWaitingForResultException: Exception("Still waiting for result")
class ResultAlreadySetException: Exception("Result has already been set")
class EffectAlreadyStartedException: Exception("Intermission effect has already started")
class EffectWasNotStartedException: Exception("Intermission effect was not started")

class IntermissionManager {
    class IntermissionAlreadyRunning: Exception("An intermission is already running")

    private var running: IntermissionPart<*, *>? = null
    val anyRunning get() = running != null

    private val factory = IntermissionEffectFactory()

    fun <Result> begin(lambda: IntermissionBody<Unit, Result>): IntermissionPart<Unit, Result> {
        if (running != null) throw IntermissionAlreadyRunning()
        else return IntermissionPart(lambda).also {
            it.callBody(Unit, factory)
            running = it
        }
    }

    fun handle() {
        val part = running ?: return

        try {
            if (part.checkResult()) {
                running = part.callNext(factory)

                if (running == null) {
                    Log.info(TAG, "Intermission ended")
                }
            }
        } catch (e: Exception) {
            Log.error(TAG, "Intermission handler crashed: $e")
            running = null
        }
    }

    companion object {
        private val TAG = Log.Tag("IntermisionManager")
    }
}
