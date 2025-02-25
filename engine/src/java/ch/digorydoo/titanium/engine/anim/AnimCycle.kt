package ch.digorydoo.titanium.engine.anim

import ch.digorydoo.titanium.engine.core.App
import kotlin.math.max

/**
 * This class is used to describe a cycle animation of a 2D (paper) gel.
 * This class is obsolete, because on the one hand, such code should be implemented with a Behaviour, while on the other
 * hand, we only need this, because our MeshRenderer cannot handle bones/rigged models yet.
 * FIXME remove this class once we support animations for bones/meshes
 */
class AnimCycle(private val delegate: Delegate) {
    interface Delegate {
        val cycleDef: AnimCycleDef
        fun setFrame(idx: Int)
        fun cycleEnded()
    }

    private val cycleDef get() = delegate.cycleDef

    val isEndless get() = cycleDef.numCycles == 0

    private var durationWithFactor = 1.0f  // duration / factor
    private var startTime = 0.0f
    private var curCycle = 1
    private var curFrame = -1
    private var fps = 0.0f
    private var ended = false

    fun setSpeedFactor(factor: Float) {
        durationWithFactor = cycleDef.cycleDuration / max(factor, 0.001f)

        if (!ended) {
            // Choose startTime such that animate() would choose the same frame.
            val relFrame = fps * (App.time.sessionTime - startTime)
            computeFps() // update fps for new speed factor
            startTime = App.time.sessionTime - relFrame / fps
        }
    }

    private fun computeFps() {
        fps = (cycleDef.lastFrame - cycleDef.firstFrame + 1).toFloat() / durationWithFactor
    }

    fun animate() {
        if (ended) {
            throw IllegalStateException("Animate called on AnimCycle that has already ended")
        }

        val prevFrame = curFrame

        if (curFrame < 0) {
            startTime = App.time.sessionTime
            curFrame = cycleDef.firstFrame
            computeFps()
        } else {
            val secondsPassed = App.time.sessionTime - startTime
            curFrame = cycleDef.firstFrame + (fps * secondsPassed).toInt()

            if (curFrame > cycleDef.lastFrame) {
                if (!isEndless) {
                    curCycle++

                    if (curCycle > cycleDef.numCycles) {
                        ended = true
                        delegate.setFrame(cycleDef.lastFrame)
                        delegate.cycleEnded()
                        return
                    }
                }

                curFrame = cycleDef.firstFrame
                startTime += durationWithFactor
            }
        }

        if (curFrame != prevFrame) {
            delegate.setFrame(curFrame)
        }

        if (curFrame != prevFrame || prevFrame == -1) {
            val s0 = cycleDef.startCycleSound
            val s1 = cycleDef.halfCycleSound

            if (curFrame == cycleDef.firstFrame && s0 != null) {
                App.sound.play(s0, null)
            } else if (curFrame == cycleDef.halfFrame && s1 != null) {
                App.sound.play(s1, null)
            }
        }
    }
}
