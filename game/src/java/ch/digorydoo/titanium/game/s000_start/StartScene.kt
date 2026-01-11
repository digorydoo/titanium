package ch.digorydoo.titanium.game.s000_start

import ch.digorydoo.titanium.engine.camera.CameraProps.Mode.FIXED_DISTANCE
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.SaveGameFileReader
import ch.digorydoo.titanium.engine.prefs.PrefsMenu
import ch.digorydoo.titanium.engine.scene.Lighting
import ch.digorydoo.titanium.engine.scene.Scene
import ch.digorydoo.titanium.engine.state.StateManager.RestoredState
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
        App.time.setStoryTime(5, 15)

        App.camera.apply {
            setTarget(27.0f, 27.0f, 6.0f, jump = true)
            setSourceRelativeToTarget(phi = -(0.8 * PI).toFloat(), rho = -1.2f, jump = true)
            mode = FIXED_DISTANCE
        }

        // Show the menu later to give the gels time to animate once, e.g. StreetLampGel needs to move its halo.
        // We set skip = 2 so that two frames will be skipped. After the scene has been loaded, spawn points will
        // spawn new gels in the first frame, and we want to wait until all of them have been animated once.
        App.process.runAtEndOfFrame(skip = 1) {
            showStartMenu()
        }
    }

    private fun showStartMenu() {
        val reopen = { showStartMenu() }

        App.dlg.showDlg<Unit> {
            suppressSoundsOnShowAndDismiss = true

            if (SaveGameFileReader.anyFiles()) {
                item {
                    textId = CONTINUE_GAME
                    onSelect = { loadGameMenu.show(onDidLoad = {}, onCancel = reopen) }
                }
            }

            item {
                textId = NEW_GAME
                onSelect = { startNewGame() }
            }
            item {
                textId = SETTINGS
                onSelect = { prefsMenu.show(reopen) }
            }
            item {
                textId = QUIT
                onSelect = { App.process.exit() }
            }
        }
    }

    private fun startNewGame() {
        App.time.setStoryTime(10, 30)
        App.sceneLoader.load(TownScene())
    }
}
