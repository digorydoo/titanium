package ch.digorydoo.titanium.engine.core

import ch.digorydoo.titanium.engine.core.App.Companion.FIXED_ASPECT_RATIO
import kotlin.math.abs

abstract class ResolutionManager {
    enum class ReasonOfPick(val asText: String) {
        SAME_AS_DESKTOP("Resolution is same as desktop"),
        HIGHEST_WITH_PERFECT_ASPECT_RATIO("Highest resolution with perfect aspect ratio"),
        HIGHEST_BUT_NONE_WITH_PERFECT_ASPECT_RATIO("Highest resolution, but none found with perfect aspect ratio"),
        ONLY_ONE_AVAILABLE_WITHIN_LIMITS("Resolution is the only one available within limits"),
        NO_RESOLUTION_AVAILABLE_WITHIN_LIMITS("Imperfect match as no resolution was available within limits"),
    }

    abstract class Monitor(val name: String)

    class Resolution(val numPixelsX: Int, val numPixelsY: Int) {
        val aspectRatio = numPixelsX.toFloat() / numPixelsY

        override fun toString() =
            "${numPixelsX}x${numPixelsY}"
    }

    class ResolutionWithReason(val res: Resolution, val reasonOfPick: ReasonOfPick)

    abstract fun setWindowModeAndUpdatePrefs()
    abstract fun setFullscreenAndUpdatePrefs()
    abstract fun setFullscreenAndUpdatePrefs(monitor: Monitor, resolution: Resolution)
    abstract fun setAutoPickMonitorAndResAndUpdatePrefs(auto: Boolean)
    abstract fun setScaleUIAndUpdatePrefs(scale: Boolean)
    abstract fun setStretchViewportAndUpdatePrefs(stretch: Boolean)
    abstract fun getAvailableMonitors(): List<Monitor>
    abstract fun getAvailableResolutions(monitor: Monitor): List<Resolution>
    abstract fun getRecommendedResolutions(monitor: Monitor): List<Resolution>

    protected fun List<Resolution>.indexOfFirst(numPixelsX: Int, numPixelsY: Int): Int {
        return indexOfFirst { it.numPixelsX == numPixelsX && it.numPixelsY == numPixelsY }
    }

    protected fun Resolution.isWithinRecommendedBounds() =
        getScore() == 0

    /**
     * @return 0 if resolution is within preferred bounds; a value > 0 if resolution is not ideal
     */
    private fun Resolution.getScore(): Int {
        val xScore = when {
            numPixelsX < PREFERRED_MIN_NUM_PIXELS_X -> PREFERRED_MIN_NUM_PIXELS_X - numPixelsX
            numPixelsX > PREFERRED_MAX_NUM_PIXELS_X -> numPixelsX - PREFERRED_MAX_NUM_PIXELS_X
            else -> 0
        }

        val yScore = when {
            numPixelsY < PREFERRED_MIN_NUM_PIXELS_Y -> PREFERRED_MIN_NUM_PIXELS_Y - numPixelsY
            numPixelsY > PREFERRED_MAX_NUM_PIXELS_Y -> numPixelsY - PREFERRED_MAX_NUM_PIXELS_Y
            else -> 0
        }

        val minArea = PREFERRED_MIN_NUM_PIXELS_X * PREFERRED_MIN_NUM_PIXELS_Y
        val maxArea = PREFERRED_MAX_NUM_PIXELS_X * PREFERRED_MAX_NUM_PIXELS_Y

        val areaScore = (numPixelsX * numPixelsY).let {
            when {
                it < minArea -> minArea - it
                it > maxArea -> it - maxArea
                else -> 0
            }
        }

        require(xScore >= 0)
        require(yScore >= 0)
        require(areaScore >= 0)

        return xScore + yScore + areaScore
    }

    protected fun List<Resolution>.pickBestMatch(resolutionOfDesktop: Int): ResolutionWithReason? {
        // Try resolution of desktop

        if (resolutionOfDesktop != -1) {
            val res = get(resolutionOfDesktop)

            if (res.isWithinRecommendedBounds()) {
                return ResolutionWithReason(res, ReasonOfPick.SAME_AS_DESKTOP)
            }
        }

        // Try a resolution that should be OK

        class Candidate(val res: Resolution, val score: Int)

        val withinLimits = mutableListOf<Resolution>()
        var bestOutsideLimits: Candidate? = null

        forEach { res ->
            val score = res.getScore()

            if (score == 0) {
                withinLimits.add(res)
            } else if (bestOutsideLimits == null || bestOutsideLimits.score < score) {
                bestOutsideLimits = Candidate(res, score)
            }
        }

        if (withinLimits.isEmpty()) {
            if (bestOutsideLimits == null) {
                return null
            } else {
                return ResolutionWithReason(bestOutsideLimits.res, ReasonOfPick.NO_RESOLUTION_AVAILABLE_WITHIN_LIMITS)
            }
        } else if (withinLimits.size == 1) {
            return ResolutionWithReason(withinLimits.first(), ReasonOfPick.ONLY_ONE_AVAILABLE_WITHIN_LIMITS)
        }

        // Try the highest resolution with perfect aspect ratio, otherwise just take the highest available

        withinLimits.sortBy { it.numPixelsX * it.numPixelsY }
        val perfectAspectRatio = withinLimits.filter { abs(it.aspectRatio - FIXED_ASPECT_RATIO) < 0.01f }

        if (perfectAspectRatio.isNotEmpty()) {
            return ResolutionWithReason(perfectAspectRatio.last(), ReasonOfPick.HIGHEST_WITH_PERFECT_ASPECT_RATIO)
        } else {
            return ResolutionWithReason(withinLimits.last(), ReasonOfPick.HIGHEST_BUT_NONE_WITH_PERFECT_ASPECT_RATIO)
        }
    }

    companion object {
        // Nintendo Switch console is           1280x720
        // Nintendo Switch TV output up to      1920x1080
        // My old EIZO 22inch monitor was       1680x1050
        // My current Samsung 27inch monitor is 2560x1440
        // 4K UHD is                            3840x2160

        private const val PREFERRED_MIN_NUM_PIXELS_X = 1280
        private const val PREFERRED_MIN_NUM_PIXELS_Y = 720

        private const val PREFERRED_MAX_NUM_PIXELS_X = 1920
        private const val PREFERRED_MAX_NUM_PIXELS_Y = 1080
    }
}
