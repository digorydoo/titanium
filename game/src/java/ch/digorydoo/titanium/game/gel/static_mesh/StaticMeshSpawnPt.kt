package ch.digorydoo.titanium.game.gel.static_mesh

import ch.digorydoo.titanium.engine.gel.SpawnPt

class StaticMeshSpawnPt(raw: Map<String, String>, val kind: Kind): SpawnPt(raw) {
    enum class Kind { STONE_1, BENCH_1, SIGN_1, ROBOT_POLICEMAN, RAILING_1, RAILING_2 }

    override fun createGel() =
        StaticMeshGel(this)
}

