package io.github.digorydoo.titanium.engine.ui.game_hud

import ch.digorydoo.kutils.utils.Log
import io.github.digorydoo.titanium.BuildConfig
import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.behaviours.Align.Anchor
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.gel.NumberGel
import io.github.digorydoo.titanium.engine.i18n.ITextId
import io.github.digorydoo.titanium.engine.ui.icon.ActionInputIconGel

/**
 * HUD = Heads Up Display
 */
class GameHUD {
    private enum class Mode { HIDDEN, FULL, SKIPPABLE_CUTSCENE, NON_SKIPPABLE_CUTSCENE }

    private var mode = Mode.HIDDEN
    private var progressBar: ProgressBarGel? = null
    private var fpsGel: NumberGel? = null
    private var compass: CompassGel? = null
    private var timeDisplay: TimeDisplayGel? = null
    private var actionInputIcon: ActionInputIconGel? = null
    private var actionTargetArrow: ActionTargetArrowGel? = null
    private var cutsceneTopStrip: CutsceneStripGel? = null
    private var cutsceneBtmStrip: CutsceneStripGel? = null

    fun onBeforeLoadScene() {
        Log.info(TAG, "onBeforeLoadScene")

        // SceneLoader is expected to throw away all gels
        require(progressBar?.zombie != false)
        require(fpsGel?.zombie != false)
        require(compass?.zombie != false)
        require(timeDisplay?.zombie != false)
        require(actionInputIcon?.zombie != false)
        require(actionTargetArrow?.zombie != false)
        require(cutsceneTopStrip?.zombie != false)
        require(cutsceneBtmStrip?.zombie != false)

        // Can't assign this as a member variable, because GameHUD is created too early.
        val screenSizeDp = App.resolutionMgr.screenSizeDp

        // Create the new progressBar now, since it should be visible while the scene is being loaded.
        progressBar = ProgressBarGel(
            posX = (screenSizeDp.x / 2.0f - ProgressBarGel.BAR_MAX_WIDTH / 2.0f).toInt(),
            posY = (screenSizeDp.y - PROGRESS_BAR_BOTTOM_MARGIN - ProgressBarGel.BAR_HEIGHT).toInt(),
        ).also { it.onCreate(LayerKind.UI_ABOVE_DLG) }

        // All other gels will be created once the scene has been loaded.
        fpsGel = null
        compass = null
        timeDisplay = null
        actionInputIcon = null
        actionTargetArrow = null
        cutsceneTopStrip = null
        cutsceneBtmStrip = null
        mode = Mode.HIDDEN
    }

    fun setLoadingProgress(progress: Float) {
        progressBar?.progress = progress
    }

    fun onAfterLoadScene() {
        Log.info(TAG, "onAfterLoadScene")

        progressBar?.setZombie()
        progressBar = null

        require(mode == Mode.HIDDEN)
        require(fpsGel == null)
        require(compass == null)
        require(timeDisplay == null)

        if (!BuildConfig.isProduction) {
            fpsGel = NumberGel(
                alignment = Align.Alignment(anchor = Anchor.BOTTOM_CENTRE, marginBottom = 8, xOffset = 64)
            ).also {
                it.hide()
                it.onCreate(LayerKind.UI_BELOW_DLG)
            }
        }

        compass = CompassGel().also {
            it.hide()
            it.onCreate(LayerKind.UI_BELOW_DLG)
        }
        timeDisplay = TimeDisplayGel().also {
            it.hide()
            it.onCreate(LayerKind.UI_BELOW_DLG)
        }
        actionInputIcon = ActionInputIconGel().also {
            it.hide()
            it.onCreate(LayerKind.UI_ABOVE_DLG)
        }
        actionTargetArrow = ActionTargetArrowGel().also {
            it.hide()
            it.onCreate(LayerKind.UI_BELOW_DLG)
        }
        cutsceneTopStrip = CutsceneStripGel(CutsceneStripGel.Side.TOP).also {
            it.hide()
            it.onCreate(LayerKind.UI_BELOW_DLG)
        }
        cutsceneBtmStrip = CutsceneStripGel(CutsceneStripGel.Side.BOTTOM).also {
            it.hide()
            it.onCreate(LayerKind.UI_BELOW_DLG)
        }
    }

    private fun setMode(newMode: Mode) {
        mode = newMode

        // These will be dynamically shown, so just hide them here
        actionInputIcon?.hide()
        actionTargetArrow?.hide()

        fpsGel?.setShown(newMode != Mode.HIDDEN)
        compass?.setShown(newMode == Mode.FULL)
        timeDisplay?.setShown(newMode == Mode.FULL)
        cutsceneTopStrip?.setShown(newMode == Mode.SKIPPABLE_CUTSCENE || newMode == Mode.NON_SKIPPABLE_CUTSCENE)
        cutsceneBtmStrip?.setShown(newMode == Mode.SKIPPABLE_CUTSCENE || newMode == Mode.NON_SKIPPABLE_CUTSCENE)

        // TODO skip button
    }

    fun hideAction() {
        actionInputIcon?.hide()
        actionTargetArrow?.hide()
    }

    fun showAction(verb: ITextId, target: GraphicElement) {
        actionInputIcon?.show(verb)
        actionTargetArrow?.show(target)
    }

    fun animate() {
        val newMode = when {
            App.gameMenu.isShown || App.content.isLoading -> Mode.HIDDEN
            App.intermissions.anyRunning -> when {
                // The HUD remains hidden during an intermission until canSkipForward becomes true.
                // After that, the stripes will stay until the intermission stops.
                // This prevents turning the stripes on and off during the same intermission.
                App.intermissions.canCancel -> Mode.SKIPPABLE_CUTSCENE
                mode == Mode.SKIPPABLE_CUTSCENE || mode == Mode.NON_SKIPPABLE_CUTSCENE -> Mode.NON_SKIPPABLE_CUTSCENE
                else -> Mode.HIDDEN
            }
            else -> Mode.FULL
        }

        if (mode != newMode) {
            setMode(newMode)
        }

        if (mode == Mode.HIDDEN) return
        fpsGel?.numberValue = App.time.fps
    }

    companion object {
        private val TAG = Log.Tag("GameHUD")
        private const val PROGRESS_BAR_BOTTOM_MARGIN = 48.0f // dp
    }
}
