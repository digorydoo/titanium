package ch.digorydoo.titanium.game.gel.static_mesh

import ch.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.kstruct.KstructBuilder
import io.github.digorydoo.kstruct.KstructMap

class StaticMeshSpawnPt(raw: KstructMap, val kind: Kind): SpawnPt(raw) {
    enum class Kind { STONE_1, BENCH_1, SIGN_1, ROBOT_POLICEMAN, RAILING_1, RAILING_2 }

    override fun serialiseSpecific(builder: KstructBuilder) {}
    override fun createGel() = StaticMeshGel(this)
}

