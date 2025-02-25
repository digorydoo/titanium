package ch.digorydoo.titanium.engine.behaviours

import ch.digorydoo.kutils.math.decel
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement

class Rotate(
    private val delegate: Delegate,
    val duration: Float = 0.0f, // 0 = perpetual
    val speed: Float = 1.0f, // ignored when duration > 0
    val phase: Float = 0.0f,
    var isRotating: Boolean = true,
): GraphicElement.Behaviour {
    interface Delegate {
        var rotation: Float
    }

    private var rotateStarted = 0.0f

    fun rotate() {
        rotateStarted = App.time.sessionTime
        isRotating = true
    }

    override fun animate() {
        if (!isRotating) {
            return
        }

        val sessionTime = App.time.sessionTime

        if (duration <= 0.0f) {
            delegate.rotation = speed * (sessionTime - rotateStarted) + phase
        } else {
            val relTime = (sessionTime - rotateStarted) / duration

            if (relTime > 1.0f) {
                delegate.rotation = phase
                isRotating = false
            } else {
                delegate.rotation = phase + decel(relTime, 1.3f) * 2.0f * Math.PI.toFloat()
            }
        }
    }
}
