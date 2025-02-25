package ch.digorydoo.titanium.game.core

import ch.digorydoo.titanium.engine.scene.ISceneId
import ch.digorydoo.titanium.game.s000_start.StartScene
import ch.digorydoo.titanium.game.s999_town.TownScene

enum class SceneId(override val value: Int): ISceneId {
    AASTART(1), TOWN(2);

    override fun createScene() = when (this) {
        AASTART -> StartScene()
        TOWN -> TownScene()
    }.also { require(it.id == this) }

    companion object {
        fun fromIntOrNull(value: Int) =
            entries.find { it.value == value }
    }
}
