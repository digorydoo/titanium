package ch.digorydoo.titanium.engine.core

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log

/**
 * This class manages the lamps in a scene. Gels that implement lamps must register (add) and unregister (remove) their
 * lamp structure. Lamps are sorted according to distance. Renderers will then assign the n closest lamps to their
 * internal slots (which may vary between shaders).
 */
class LampManager {
    interface Lamp {
        val pos: Point3f
        val colour: Colour
        val intensity: Float
        val radius: Float
    }

    private class LampWrapper(val lamp: Lamp) {
        var sqrDistance: Double = 0.0
    }

    private val lamps = mutableListOf<LampWrapper>()
    private val maintainCounter = FrameCounter.everyNthFrame(3)
    private var nextIdxToReview = 0

    fun add(lamp: Lamp) {
        val wrapper = LampWrapper(lamp)
        wrapper.sqrDistance = App.camera.sourcePos.sqrDistanceTo(lamp.pos)
        val idx = findRankOfLamp(wrapper)
        if (idx < 0) lamps.add(wrapper) else lamps.add(idx, wrapper)
        nextIdxToReview = idx + 1
        Log.info(TAG, "Lamp added. Got ${lamps.size} lamps.")
    }

    fun remove(lamp: Lamp) {
        lamps.removeAll { it.lamp == lamp }
        Log.info(TAG, "Lamp removed. Got ${lamps.size} lamps.")
    }

    fun getOrNull(idx: Int): Lamp? =
        lamps.getOrNull(idx)?.lamp

    private fun findRankOfLamp(wrapper: LampWrapper): Int =
        when {
            lamps.isEmpty() -> -1
            wrapper.lamp.intensity <= 0 -> -1
            else -> lamps.indexOfFirst { it.sqrDistance > wrapper.sqrDistance || it.lamp.intensity <= 0 }
        }

    fun maintain() {
        // Lamps do not change all the time. Just pick one lamp, re-evaluate its sqrDistance, and move it within the
        // list if necessary. The ordering of lamps will not be correct at all times, but the list of closest lamps
        // will be more stable, making shader updates less frequent.

        if (lamps.size <= 1) return
        if (maintainCounter.next() != 0) return

        nextIdxToReview = (nextIdxToReview % lamps.size)
        val reviewIdx = nextIdxToReview++
        val wrapper = lamps[reviewIdx]
        wrapper.sqrDistance = App.camera.targetPos.sqrDistanceTo(wrapper.lamp.pos)
        var shouldBeAtIdx = findRankOfLamp(wrapper)

        if (shouldBeAtIdx < 0) {
            // Either this lamp is farthest away, or its off. We leave it where it is; other lamps will gradually move
            // in front. This is better than constantly reordering the lamps that are off.
            return
        } else if (shouldBeAtIdx > reviewIdx) {
            // Since we're going to remove the lamp at reviewIdx, the target index needs to be decremented.
            shouldBeAtIdx--
        }

        if (shouldBeAtIdx != reviewIdx) {
            Log.info(TAG, "Lamp was at idx $reviewIdx, moving it to $shouldBeAtIdx")
            lamps.removeAt(reviewIdx)
            lamps.add(shouldBeAtIdx, wrapper)
        }
    }

    companion object {
        private val TAG = Log.Tag("LampManager")
    }
}
