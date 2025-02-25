package ch.digorydoo.titanium.game.gel.static_paper

import ch.digorydoo.titanium.engine.gel.SpawnPt

class StaticPaperSpawnPt(raw: Map<String, String>, val kind: Kind): SpawnPt(raw) {
    enum class Kind { GNARLED_TREE_LARGE, GNARLED_TREE_MEDIUM, GNARLED_TREE_SMALL, ROUND_TREE }

    override fun createGel() =
        StaticPaperGel(this)
}
