package ch.digorydoo.titanium.game.s000_start

import ch.digorydoo.titanium.engine.camera.CameraProps.Mode.FIXED_DISTANCE
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.SaveGameFileReader
import ch.digorydoo.titanium.engine.prefs.PrefsMenu
import ch.digorydoo.titanium.engine.scene.Lighting
import ch.digorydoo.titanium.engine.scene.Scene
import ch.digorydoo.titanium.engine.state.StateManager.RestoredState
import ch.digorydoo.titanium.engine.ui.choice.TextChoice
import ch.digorydoo.titanium.game.core.SceneId
import ch.digorydoo.titanium.game.i18n.GameTextId.*
import ch.digorydoo.titanium.game.s999_town.TownScene
import ch.digorydoo.titanium.game.ui.options.LoadGameMenu
import kotlin.math.PI

class StartScene: Scene(
    id = SceneId.AASTART,
    SCENE_AASTART,
    fileNameStem = "aastart",
    Lighting.fineDay1200,
    lightingFollowsStoryTime = true,
    hasSky = true,
    hasShadows = true,
) {
    override val brickTexFileName = "tiles-town.png" // shared with TownScene
    private val loadGameMenu = LoadGameMenu()
    private val prefsMenu = PrefsMenu()

    override fun enter(restore: RestoredState?) {
        App.state.clearAllState()
        App.time.setStoryTime(21, 15)

        App.camera.apply {
            setTarget(28.1f, 10.1f, 6.0f, jump = true)
            setSourceRelativeToTarget(phi = -(0.75 * PI).toFloat(), rho = -0.9f, jump = true)
            mode = FIXED_DISTANCE
        }

        // Show the menu later to give the gels time to animate once, e.g. StreetLampGel needs to move its halo.
        App.runAtEndOfFrame {
            showStartMenu()
        }
    }

    private fun showStartMenu() {
        val reopen = { showStartMenu() }

        App.dlg.showChoices(
            listOfNotNull(
                if (SaveGameFileReader.anyFiles()) {
                    TextChoice(CONTINUE_GAME) { loadGameMenu.show(onDidLoad = {}, onCancel = reopen) }
                } else {
                    null
                },
                TextChoice(NEW_GAME) { startNewGame() },
                TextChoice(SETTINGS) { prefsMenu.show(reopen) },
                TextChoice(QUIT) { App.exit() },
            ),
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
    }

    private fun startNewGame() {
        App.time.setStoryTime(10, 30)
        App.load(TownScene())
    }
}
