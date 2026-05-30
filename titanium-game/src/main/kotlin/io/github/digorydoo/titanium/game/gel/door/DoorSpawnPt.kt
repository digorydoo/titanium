package io.github.digorydoo.titanium.game.gel.door

import io.github.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.kstruct.KstructBuilder
import io.github.digorydoo.kstruct.KstructMap

class DoorSpawnPt(raw: KstructMap, val kind: Kind): SpawnPt(raw) {
    enum class Kind {
        DOOR_WITH_WOODEN_FRAME
    }

    override fun serialiseSpecific(builder: KstructBuilder) {}
    override fun createGel() = DoorGel(this)
}
