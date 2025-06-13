package ch.digorydoo.titanium.engine.ui.game_hud

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.BuildConfig
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.behaviours.Align.Anchor
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.gel.NumberGel
import ch.digorydoo.titanium.engine.i18n.ITextId
import ch.digorydoo.titanium.engine.ui.icon.ActionInputIconGel

/**
 * HUD = Heads Up Display
 */
class GameHUD {
    private var progressBar: ProgressBarGel? = null
    private var fpsGel: NumberGel? = null
    private var compass: CompassGel? = null
    private var timeDisplay: TimeDisplayGel? = null
    private var actionInputIcon: ActionInputIconGel? = null
    private var actionTargetArrow: ActionTargetArrowGel? = null

    private val shouldShow get() = !App.gameMenu.isShown && !App.content.isLoading
    private var isShown = false

    fun onBeforeLoadScene() {
        Log.info(TAG, "onBeforeLoadScene")

        // SceneLoader is expected to throw away all gels
        require(progressBar?.zombie != false)
        require(fpsGel?.zombie != false)
        require(compass?.zombie != false)
        require(timeDisplay?.zombie != false)
        require(actionInputIcon?.zombie != false)
        require(actionTargetArrow?.zombie != false)

        // Create the new progressBar now, since it should be visible while the scene is being loaded.
        progressBar = ProgressBarGel(
            posX = (App.screenWidthDp / 2.0f - ProgressBarGel.BAR_MAX_WIDTH / 2.0f).toInt(),
            posY = (App.screenHeightDp - PROGRESS_BAR_BOTTOM_MARGIN - ProgressBarGel.BAR_HEIGHT).toInt(),
        ).also { App.content.add(it, LayerKind.UI_ABOVE_DLG) }

        // All other gels will be created once the scene has been loaded.
        fpsGel = null
        compass = null
        timeDisplay = null
        actionInputIcon = null
        actionTargetArrow = null
        isShown = false
    }

    fun setLoadingProgress(progress: Float) {
        progressBar?.progress = progress
    }

    fun onAfterLoadScene() {
        Log.info(TAG, "onAfterLoadScene")

        progressBar?.setZombie()
        progressBar = null

        require(!isShown)
        require(fpsGel == null)
        require(compass == null)
        require(timeDisplay == null)

        if (!BuildConfig.isProduction()) {
            fpsGel = NumberGel(
                alignment = Align.Alignment(anchor = Anchor.BOTTOM_CENTRE, marginBottom = 8, xOffset = 64)
            ).also {
                it.hide()
                App.content.add(it, LayerKind.UI_BELOW_DLG)
            }
        }

        compass = CompassGel().also {
            it.hide()
            App.content.add(it, LayerKind.UI_BELOW_DLG)
        }
        timeDisplay = TimeDisplayGel().also {
            it.hide()
            App.content.add(it, LayerKind.UI_BELOW_DLG)
        }
        actionInputIcon = ActionInputIconGel().also {
            it.hide()
            App.content.add(it, LayerKind.UI_ABOVE_DLG)
        }
        actionTargetArrow = ActionTargetArrowGel().also {
            it.hide()
            App.content.add(it, LayerKind.UI_BELOW_DLG)
        }

        isShown = false
    }

    private fun hide() {
        fpsGel?.hide()
        compass?.hide()
        timeDisplay?.hide()
        actionInputIcon?.hide()
        actionTargetArrow?.hide()
        isShown = false
    }

    private fun show() {
        fpsGel?.show()
        compass?.show()
        timeDisplay?.show()
        // actionInputIcon will be dynamically shown
        // actionTargetArrow will be dynamically shown
        isShown = true
    }

    /**
     * This function is repeatedly called by ActionManager, so make sure it's efficient, esp. when there's no change
     */
    fun hideAction() {
        actionInputIcon?.let { if (!it.hidden) it.hide() }
        actionTargetArrow?.let { if (!it.hidden) it.hide() }
    }

    /**
     * This function is repeatedly called by ActionManager, so make sure it's efficient, esp. when there's no change
     */
    fun showAction(verb: ITextId, target: GraphicElement) {
        actionInputIcon?.let { icon ->
            if (icon.hidden || icon.verb != verb) {
                icon.show(verb)
            }
        }

        actionTargetArrow?.let { arrow ->
            if (arrow.hidden || arrow.target != target) {
                arrow.show(target)
            }
        }
    }

    fun animate() {
        if (!shouldShow) {
            if (isShown) hide()
            return
        }

        if (!isShown) {
            show()
        }

        fpsGel?.numberValue = App.time.fps
    }

    companion object {
        private val TAG = Log.Tag("GameStatusBar")
        private const val PROGRESS_BAR_BOTTOM_MARGIN = 48.0f // dp
    }
}
