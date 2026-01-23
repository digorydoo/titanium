package ch.digorydoo.titanium.game.gel.vase

import ch.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.kstruct.KstructBuilder
import io.github.digorydoo.kstruct.KstructMap

class VaseSpawnPt(raw: KstructMap, val kind: Kind): SpawnPt(raw) {
    enum class Kind {
        VASE_H1M, // a vase with height=1m
    }

    override fun serialiseSpecific(builder: KstructBuilder) {}
    override fun createGel() = VaseGel(this)
}
