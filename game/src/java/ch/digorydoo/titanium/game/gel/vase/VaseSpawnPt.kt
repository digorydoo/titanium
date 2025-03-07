package ch.digorydoo.titanium.game.gel.vase

import ch.digorydoo.titanium.engine.gel.SpawnPt

class VaseSpawnPt(raw: Map<String, String>, val kind: Kind): SpawnPt(raw) {
    enum class Kind {
        VASE_H1M, // a vase with height=1m
    }

    override fun createGel() =
        VaseGel(this)
}
