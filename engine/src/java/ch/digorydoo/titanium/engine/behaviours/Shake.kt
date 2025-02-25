package ch.digorydoo.titanium.engine.behaviours

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import kotlin.math.sin

class Shake(private val delegate: Delegate): GraphicElement.Behaviour {
    interface Delegate {
        var shakeValue: Float
    }

    private val duration = 0.7f
    private val distance = 6.0f
    private val count = 8
    private var shakeStarted = 0.0f
    private var isShaking = false
    private var startValue = 0.0f

    fun shake() {
        shakeStarted = App.time.sessionTime
        startValue = delegate.shakeValue
        isShaking = true
    }

    override fun animate() {
        if (isShaking) {
            val relTime = (App.time.sessionTime - shakeStarted) / duration

            if (relTime > 1.0f) {
                delegate.shakeValue = startValue
                isShaking = false
            } else {
                val phi = relTime * count * Math.PI
                delegate.shakeValue = startValue + (1.0f - relTime) * distance * sin(phi).toFloat()
            }
        }
    }
}
