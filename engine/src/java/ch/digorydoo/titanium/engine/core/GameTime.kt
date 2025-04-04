package ch.digorydoo.titanium.engine.core

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.scene.ActiveSceneContent.Companion.STORY_TIME_REAL_TIME_RATIO
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.max

/**
 * This class deals with various time-related things.
 * The session time refers to the time since the game was launched. It may be used to base animations upon, etc.
 * The story time refers to the time of day of the game story. The lighting may change based on the story time.
 * The total playing time refers to the real time passed in all sessions for a particular savegame. It is not currently
 * implemented.
 */
class GameTime {
    var sessionTime = 0.0f; private set // number of seconds since session start (corrected by sessionTimeOffset)
    private var sessionStartNanos = 0L // nanoTime of session start
    private var sessionTimeOffset = 0.0f // seconds added to sessionTime to even out lost frames

    var storyClockHoursHand = 0; private set
    var storyClockMinutesHand = 0; private set
    var storyClockSecondsHand = 0; private set
    private var storyMidnightNanos = 0L // nanoTime when story passed midnight
    private var secondsSinceStoryMidnight = 0 // will be updated on each frame unless storyTime is paused

    // 0 is midnight today; 1 is midnight next day (will wrap around)
    val storyTimeProgress get() = clamp(secondsSinceStoryMidnight.toFloat() / SECONDS_PER_DAY)

    var fps = 0; private set
    private var fpsMeasureNanos = 0L // nanoTime of last measure
    private var fpsMeasureCounter = 0 // frame count

    private var prevFrameNanos = 0L // nanoTime of previous frame
    private var sleepOverhead = 0L // nanoseconds; estimated overhead of sleep() function

    fun setStoryTime(hours: Int, minutes: Int, seconds: Int = 0) {
        val now = System.nanoTime()
        storyMidnightNanos = now - ((hours * 3600.0f + minutes * 60.0f + seconds) *
            NANOS_PER_SECOND / STORY_TIME_REAL_TIME_RATIO).toLong()
        updateStoryTime(now)
        Log.info(TAG, "setStoryTime: $storyClockHoursHand:$storyClockMinutesHand:$storyClockSecondsHand")

        if (App.scene.lightingFollowsStoryTime) {
            App.scene.lighting.adaptToStoryTime()
        }
    }

    val isDaylightStoryTime
        get() = isStoryTimeLaterThan(6, 0) && isStoryTimeEarlierThan(18, 0)

    @Suppress("MemberVisibilityCanBePrivate")
    fun isStoryTimeEarlierThan(hours: Int, minutes: Int) = when {
        storyClockHoursHand < hours -> true
        storyClockHoursHand > hours -> false
        storyClockMinutesHand < minutes -> true
        else -> false
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun isStoryTimeLaterThan(hours: Int, minutes: Int) = when {
        storyClockHoursHand < hours -> false
        storyClockHoursHand > hours -> true
        storyClockMinutesHand < minutes -> false
        else -> true
    }

    fun maintain() {
        var now = System.nanoTime()

        if (sessionStartNanos == 0L) {
            // Apparently the game has just started.
            sessionStartNanos = now
            prevFrameNanos = sessionStartNanos
        }

        updateSessionTime(now)
        updateStoryTime(now)
        now = sleepUntilEndOfFrame(now)
        updateFps(now)
        prevFrameNanos = now
    }

    private fun updateSessionTime(now: Long) {
        val prevSessionTime = sessionTime
        sessionTime = sessionTimeOffset + (now - sessionStartNanos).toFloat() / NANOS_PER_SECOND

        val dtNanos = now - prevFrameNanos // nanoseconds

        if (dtNanos > MAX_DELTA_TIME_NANOS) {
            // This happens when rendering was blocked, e.g. when we loaded a lot of data, or when some user interaction
            // such as the menu bar blocked our main thread.

            val dtSeconds = dtNanos.toFloat() / NANOS_PER_SECOND // seconds
            Log.info(TAG, "Hiccup detected! The frame took $dtSeconds seconds. Session time is $prevSessionTime.")

            // Move timeOffset such that sessionTime will look as if the frame took exactly MAX_DELTA_TIME_NANOS.
            val maxDeltaTimeSeconds = MAX_DELTA_TIME_NANOS.toFloat() / NANOS_PER_SECOND
            sessionTimeOffset += dtSeconds - maxDeltaTimeSeconds
            sessionTime += maxDeltaTimeSeconds - dtSeconds

            Log.info(TAG, "New session time is $sessionTime (timeOffset=$sessionTimeOffset)")
        }
    }

    private fun sleepUntilEndOfFrame(inNow: Long): Long {
        var now = inNow

        if (now - prevFrameNanos < MIN_DELTA_TIME_NANOS) {
            // Even though we use glfwSwapInterval to enable syncing to the screen refresh rate, the resulting frame
            // rate may vary depending on the monitor. To simplify physics computations, we limit the fps.
            val t1 = now
            val sleepNanos = MIN_DELTA_TIME_NANOS - (t1 - prevFrameNanos) - sleepOverhead
            sleepOverhead = (sleepOverhead * 0.9).toLong()

            if (sleepNanos > 0) {
                TimeUnit.NANOSECONDS.sleep(sleepNanos)
                now = System.nanoTime()
                val actualSleepNanos = now - t1

                if (actualSleepNanos > sleepNanos) {
                    // The sleep function overslept! Adjust sleepOverhead to avoid this in the next frame.
                    sleepOverhead = (0.5 * (actualSleepNanos - sleepNanos)).toLong()
                }
            }
        }

        return now
    }

    private fun updateFps(now: Long) {
        fpsMeasureCounter++

        if (fpsMeasureNanos == 0L || now - fpsMeasureNanos > NANOS_PER_SECOND / 2) {
            fps = fpsMeasureCounter * 2
            fpsMeasureCounter = 0
            fpsMeasureNanos = now
            // Log.info(TAG, "FPS $fps dtn $dtNanos sleep ${MIN_DELTA_TIME_NANOS - dtNanos} overslept $sleepOverhead")
        }
    }

    private fun updateStoryTime(now: Long) {
        val realSecondsSinceMidnight = max(0, now - storyMidnightNanos).toDouble() / NANOS_PER_SECOND
        var t = (realSecondsSinceMidnight * STORY_TIME_REAL_TIME_RATIO).toInt() % SECONDS_PER_DAY
        secondsSinceStoryMidnight = t
        storyClockSecondsHand = t % 60
        t /= 60
        storyClockMinutesHand = t % 60
        storyClockHoursHand = t / 60
    }

    companion object {
        private val TAG = Log.Tag("GameTime")

        const val RELTIME0200 = 2.0f / 24
        const val RELTIME0600 = 6.0f / 24
        const val RELTIME1200 = 12.0f / 24
        const val RELTIME1800 = 18.0f / 24
        const val RELTIME2200 = 22.0f / 24

        const val TARGET_FPS = 60
        const val DELTA_TIME = 1.0f / TARGET_FPS

        private const val NANOS_PER_SECOND = 1000000000
        private const val SECONDS_PER_DAY = 24 * 60 * 60

        private val MIN_DELTA_TIME_NANOS = ceil(NANOS_PER_SECOND.toDouble() / TARGET_FPS).toLong()
        private const val MAX_DELTA_TIME_NANOS = (0.5f * NANOS_PER_SECOND).toLong()

        fun relTime(hours: Int, minutes: Int) =
            (hours.toFloat() + minutes.toFloat() / 60.0f) / 24.0f
    }
}
