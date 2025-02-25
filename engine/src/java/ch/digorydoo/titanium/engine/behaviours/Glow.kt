package ch.digorydoo.titanium.engine.behaviours

import ch.digorydoo.kutils.waveforms.triang
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement

class Glow(
    private val delegate: Delegate,
    var enabled: Boolean = true,
    var minBrite: Float = 0.8f,
    var maxBrite: Float = 1.2f,
    private var phase: Float = 0.0f,
): GraphicElement.Behaviour {
    interface Delegate {
        var brightness: Float
    }

    private val period = 1.8f
    private var startTime = 0.0f

    fun reset(newPhase: Float? = null) {
        if (newPhase != null) {
            phase = newPhase
        }

        startTime = App.time.sessionTime
    }

    override fun animate() {
        if (!enabled) {
            return
        }

        val now = App.time.sessionTime

        if (startTime == 0.0f) {
            startTime = now
        }

        val t = now - startTime + phase
        val s = triang(t / period) // -1..+1
        delegate.brightness = minBrite + (maxBrite - minBrite) * (0.5f + 0.5f * s)
    }
}
