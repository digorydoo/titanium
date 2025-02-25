package ch.digorydoo.titanium.game.s999_town

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.scene.Lighting
import ch.digorydoo.titanium.engine.scene.Scene
import ch.digorydoo.titanium.engine.state.StateManager.RestoredState
import ch.digorydoo.titanium.game.core.SceneId
import ch.digorydoo.titanium.game.core.StateManagerImpl.RestoredStateImpl
import ch.digorydoo.titanium.game.i18n.GameTextId
import ch.digorydoo.titanium.game.player.PlayerGel

class TownScene: Scene(
    id = SceneId.TOWN,
    GameTextId.SCENE_TOWN,
    fileNameStem = "town",
    Lighting.fineDay1200,
    lightingFollowsStoryTime = true,
    hasSky = true,
    hasShadows = false,
) {
    override fun enter(restore: RestoredState?) {
        val restoredState = restore as? RestoredStateImpl?
        val playerPos = restoredState?.playerPos ?: App.bricks.getPtWithTopFloorZ(175.0f, 156.0f)
        val player = PlayerGel(initialPos = playerPos, initialRotationPhi = 0.0f)
        App.content.add(player, LayerKind.MAIN_COLLIDABLE)

        App.content.player = player
        App.camera.setTarget(player, jump = true)

        if (restore == null) {
            App.camera.setSourceRelativeToTarget(jump = true)
        }
    }
}
