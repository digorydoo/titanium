package ch.digorydoo.titanium.game.gel.ball

import ch.digorydoo.titanium.engine.gel.SpawnPt

class BallSpawnPt(raw: Map<String, String>, val kind: Kind): SpawnPt(raw) {
    enum class Kind {
        BALL_R25CM, // a ball with radius 25cm
    }

    override fun createGel() =
        BallGel(this)
}
