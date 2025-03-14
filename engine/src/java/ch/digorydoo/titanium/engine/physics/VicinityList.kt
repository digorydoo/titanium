package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.gel.DummyGel
import ch.digorydoo.titanium.engine.gel.GraphicElement

/**
 * This is a list of gels that are in the vicinity of another gel. It is used during collision handling and must be
 * efficient, that's why we use an Array of a fixed size here.
 */
internal class VicinityList {
    private val array = Array<GraphicElement>(CAPACITY) { DummyGel.instance }
    private var numSlotsUsed = 0
    private var ticket = -1L

    fun clearIfOutdated(collisionTicket: Long) {
        if (collisionTicket != ticket) {
            require(collisionTicket > ticket) { "Collision tickets must never decrease" }
            ticket = collisionTicket
            (0 ..< numSlotsUsed).forEach { array[it] = DummyGel.instance }
            numSlotsUsed = 0
        }
    }

    fun add(gel: GraphicElement) {
        if (numSlotsUsed >= CAPACITY) {
            Log.error("Dropping $gel from vicinity list since the capacity is exceeded")
            return
        }
        array[numSlotsUsed++] = gel
    }

    fun forEach(lambda: (gel: GraphicElement) -> Unit) {
        (0 ..< numSlotsUsed).forEach { i ->
            lambda(array[i])
        }
    }

    companion object {
        private const val CAPACITY = 10
    }
}
