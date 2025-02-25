package ch.digorydoo.titanium.game.gel.pickup

import ch.digorydoo.titanium.engine.gel.SpawnPt

class PickupSpawnPt(raw: Map<String, String>, val kind: Kind): SpawnPt(raw) {
    enum class Kind { VASE }

    override fun createGel() =
        PickupGel(this)
}
