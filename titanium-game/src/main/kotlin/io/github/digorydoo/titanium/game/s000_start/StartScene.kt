package io.github.digorydoo.titanium.game.s000_start

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.camera.CameraDirectingMode
import io.github.digorydoo.titanium.engine.camera.CameraInputMode
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.file.SaveGameFileReader
import io.github.digorydoo.titanium.engine.intermission.Intermission
import io.github.digorydoo.titanium.engine.intermission.Intermission.DlgCancelledException
import io.github.digorydoo.titanium.engine.prefs.PrefsMenu
import io.github.digorydoo.titanium.engine.scene.Lighting
import io.github.digorydoo.titanium.engine.scene.Scene
import io.github.digorydoo.titanium.engine.state.StateManager.RestoredState
import io.github.digorydoo.titanium.game.core.SceneId
import io.github.digorydoo.titanium.game.i18n.GameTextId.*
import io.github.digorydoo.titanium.game.s999_town.TownScene
import io.github.digorydoo.titanium.game.ui.options.LoadGameMenu
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
            directingMode = CameraDirectingMode.FIXED_DISTANCE
            inputMode = CameraInputMode.CONSTRAINED
        }

        // Show the menu later to give the gels time to animate once, e.g. StreetLampGel needs to move its halo.
        // We set skip = 2 so that two frames will be skipped. After the scene has been loaded, spawn points will
        // spawn new gels in the first frame, and we want to wait until all of them have been animated once.
        App.process.runAtEndOfFrame(skip = 1) {
            App.intermissions.begin {
                showStartMenu()
            }
        }
    }

    private suspend fun Intermission.showStartMenu() {
        try {
            do {
                var reopen = false

                val selected = showDlg {
                    suppressSoundsOnShowAndDismiss = true

                    if (SaveGameFileReader.anyFiles()) {
                        item { textId = CONTINUE_GAME }
                    }

                    item { textId = NEW_GAME }
                    item { textId = SETTINGS }
                    item { textId = QUIT }
                }

                when (selected.textId) {
                    CONTINUE_GAME -> {
                        val result = loadGameMenu.run { show() }

                        when (result) {
                            LoadGameMenu.Result.DID_LOAD -> Unit
                            LoadGameMenu.Result.CANCEL -> reopen = true
                        }
                    }
                    NEW_GAME -> startNewGame()
                    SETTINGS -> {
                        prefsMenu.run { show() }
                        reopen = true
                    }
                    QUIT -> App.process.exit() // initiates termination at end of frame, not immediately
                    else -> Unit // We come here if dlg is aborted, e.g. when the editor mode is enabled.
                }
            } while (reopen)
        } catch (_: DlgCancelledException) {
            // This happens when switching to editor while the start menu is being shown.
            Log.info(TAG, "Start menu was cancelled")
        }
    }

    private fun startNewGame() {
        App.time.setStoryTime(10, 30)
        App.sceneLoader.load(TownScene())
    }

    companion object {
        private val TAG = Log.Tag("StartScene")
    }
}
