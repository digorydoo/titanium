package ch.digorydoo.titanium.engine.ui.game_status

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.BuildConfig
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.behaviours.Align.Anchor
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.gel.NumberGel

class GameStatusBar {
    private var progressBar: ProgressBarGel? = null
    private var fpsGel: NumberGel? = null // can't create instance right now, textureMgr is not available at this time
    private var compass: CompassGel? = null
    private var timeDisplay: TimeDisplayGel? = null
    var isLoadingScene = false; private set

    private val shouldShow get() = !App.gameMenu.isShown
    private var isShown = false

    fun onBeforeLoadScene() {
        Log.info(TAG, "onBeforeLoadScene")

        // SceneLoader is expected to throw away all gels
        require(progressBar?.zombie != false) { "progressBar still alive" }
        require(fpsGel?.zombie != false) { "fpsGel still alive" }
        require(compass?.zombie != false) { "compass still alive" }
        require(timeDisplay?.zombie != false) { "timeDisplay still alive" }

        // Create the new progressBar now, since it should be visible while the scene is being loaded.
        progressBar = ProgressBarGel(
            posX = (App.screenWidthDp / 2.0f - ProgressBarGel.BAR_MAX_WIDTH / 2.0f).toInt(),
            posY = (App.screenHeightDp - PROGRESS_BAR_BOTTOM_MARGIN - ProgressBarGel.BAR_HEIGHT).toInt(),
        ).also { App.content.add(it, LayerKind.UI_ABOVE_DLG) }

        // All other gels will be created once the scene has been loaded.
        fpsGel = null
        compass = null
        timeDisplay = null
        isLoadingScene = true
        isShown = false
    }

    fun setLoadingProgress(progress: Float) {
        progressBar?.progress = progress
    }

    fun onAfterLoadScene() {
        Log.info(TAG, "onAfterLoadScene")
        isLoadingScene = false

        progressBar?.setZombie()
        progressBar = null

        makeGels()
    }

    private fun makeGels() {
        if (!BuildConfig.isProduction()) {
            if (fpsGel == null) {
                fpsGel = NumberGel(
                    alignment = Align.Alignment(anchor = Anchor.BOTTOM_CENTRE, marginBottom = 8, xOffset = 64)
                ).also { App.content.add(it, LayerKind.UI_BELOW_DLG) }
            }
        }

        if (compass == null) {
            compass = CompassGel().also { App.content.add(it, LayerKind.UI_BELOW_DLG) }
        }

        if (timeDisplay == null) {
            timeDisplay = TimeDisplayGel().also { App.content.add(it, LayerKind.UI_BELOW_DLG) }
        }

        isShown = true
    }

    private fun removeGels() {
        fpsGel?.setZombie()
        fpsGel = null

        compass?.setZombie()
        compass = null

        timeDisplay?.setZombie()
        timeDisplay = null

        isShown = false
    }

    fun animate() {
        if (isLoadingScene) return

        if (isShown && !shouldShow) {
            removeGels()
            return
        } else if (!isShown && shouldShow) {
            makeGels()
        }

        if (!isShown) return

        fpsGel?.numberValue = App.time.fps
    }

    companion object {
        private val TAG = Log.Tag("GameStatusBar")
        private const val PROGRESS_BAR_BOTTOM_MARGIN = 48.0f // dp
    }
}
