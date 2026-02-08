package io.github.digorydoo.titanium.game.gel.static_paper

import io.github.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.kstruct.KstructBuilder
import io.github.digorydoo.kstruct.KstructMap

class StaticPaperSpawnPt(raw: KstructMap, val kind: Kind): SpawnPt(raw) {
    enum class Kind { GNARLED_TREE_LARGE, GNARLED_TREE_MEDIUM, GNARLED_TREE_SMALL, ROUND_TREE }

    override fun serialiseSpecific(builder: KstructBuilder) {}
    override fun createGel() = StaticPaperGel(this)
}
