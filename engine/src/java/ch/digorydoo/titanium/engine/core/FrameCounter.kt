package ch.digorydoo.titanium.engine.core

import ch.digorydoo.titanium.engine.core.GameTime.Companion.TARGET_FPS
import kotlin.math.max

// Use this class whenever you need to update some things periodically, but not on each frame.
// The first cycle is NOT guaranteed to use the full number of frames for the given limit.
class FrameCounter private constructor(private val limit: Int) {
    // curCount is initialized to something between 0..limit, which has as a frame-load-balancing effect
    private var curCount = ++nextFrameCounterId % limit

    fun next(): Int {
        curCount = (curCount + 1) % limit
        return curCount
    }

    @Suppress("unused")
    companion object {
        private var nextFrameCounterId = 1

        fun everyNthFrame(n: Int) = FrameCounter(max(1, n))
        fun everyNthSecond(seconds: Int) = FrameCounter(max(1, seconds * TARGET_FPS))
        fun everyNthSecond(seconds: Float) = FrameCounter(max(1, (seconds * TARGET_FPS).toInt()))
    }
}
