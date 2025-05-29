package ch.digorydoo.titanium.engine.gel

/**
 * Note that kutils has a MutableFixedCapacitySet, but we no longer use it, because it's unclear if that's actually
 * more efficient since native MutableSet is probably optimised.
 */
class MutableGelSet {
    val gels = mutableSetOf<GraphicElement>()
    private var ticket = -1L // external ticket, see clearIfOutdated

    fun isEmpty() = gels.isEmpty()
    fun isNotEmpty() = gels.isNotEmpty()
    fun contains(gel: GraphicElement) = gels.contains(gel)

    fun clear() {
        gels.clear()
    }

    fun add(gel: GraphicElement) {
        gels.add(gel)
    }

    fun addAll(collection: MutableGelSet) {
        collection.forEach { add(it) }
    }

    fun forEach(lambda: (gel: GraphicElement) -> Unit) {
        gels.forEach(lambda)
    }

    fun clearIfOutdated(newTicket: Long) {
        if (newTicket != ticket) {
            require(newTicket > ticket) { "Tickets must never decrease" }
            ticket = newTicket
            clear()
        }
    }
}
