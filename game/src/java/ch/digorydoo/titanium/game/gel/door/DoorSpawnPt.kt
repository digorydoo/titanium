package ch.digorydoo.titanium.game.gel.door

import ch.digorydoo.titanium.engine.gel.SpawnPt

class DoorSpawnPt(raw: Map<String, String>, val kind: Kind): SpawnPt(raw) {
    enum class Kind {
        DOOR_WITH_WOODEN_FRAME
    }

    override fun createGel() =
        DoorGel(this)
}
