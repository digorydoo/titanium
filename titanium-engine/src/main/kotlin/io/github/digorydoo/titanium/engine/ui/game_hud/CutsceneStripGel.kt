package io.github.digorydoo.titanium.engine.ui.game_hud

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.math.decel
import ch.digorydoo.kutils.vector.MutableVector2f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.sprite.UISolidRenderer

class CutsceneStripGel(private val side: Side): GraphicElement() {
    enum class Side { TOP, BOTTOM }
    private enum class Mode { SHOWN, HIDDEN, FADING_IN, FADING_OUT }

    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    private val frameSize = MutableVector2f()
    private val screenSizeDp = App.resolutionMgr.screenSizeDp
    private var mode = Mode.HIDDEN
    private var modeSince = App.time.sessionTime
    private var opacity = 0.0f

    override val renderer = App.factory.createUISolidRenderer(
        object: UISolidRenderer.Delegate() {
            override val frameSize = this@CutsceneStripGel.frameSize
            override val renderPos = this@CutsceneStripGel.pos
            override val colour = Colour.black
        },
    )

    override fun show() {
        if (mode != Mode.SHOWN && mode != Mode.FADING_IN) {
            mode = Mode.FADING_IN
            modeSince = App.time.sessionTime
            setHiddenOnNextFrameTo = false
        }
    }

    override fun hide() {
        if (mode != Mode.HIDDEN && mode != Mode.FADING_OUT) {
            mode = Mode.FADING_OUT
            modeSince = App.time.sessionTime
        }
    }

    override fun onAnimateActive() {
        val delta: Float

        when (mode) {
            Mode.SHOWN -> {
                delta = 0f
            }
            Mode.HIDDEN -> {
                // Once the gel is hidden, onAnimateActive will no longer be called.
                // Therefore, we expect this to be called in the very first frame only.
                delta = HEIGHT.toFloat()
                setHiddenOnNextFrameTo = true
            }
            Mode.FADING_IN -> {
                val now = App.time.sessionTime
                val rel = (now - modeSince) / FADE_DURATION

                if (rel < 1.0f) {
                    opacity = rel
                    delta = (1f - decel(rel, DELTA_DECEL)) * HEIGHT
                } else {
                    opacity = 1.0f
                    delta = 0f
                    mode = Mode.SHOWN
                    modeSince = now
                }
            }
            Mode.FADING_OUT -> {
                val now = App.time.sessionTime
                val rel = (now - modeSince) / FADE_DURATION

                if (rel < 1.0f) {
                    opacity = 1.0f - rel
                    delta = decel(rel, DELTA_DECEL) * HEIGHT
                } else {
                    opacity = 0.0f
                    delta = HEIGHT.toFloat()
                    mode = Mode.HIDDEN
                    modeSince = now
                    setHiddenOnNextFrameTo = true
                }
            }
        }

        val screenWidthDp = screenSizeDp.x
        val screenHeightDp = screenSizeDp.y

        val top = when (side) {
            Side.TOP -> -delta
            Side.BOTTOM -> screenHeightDp - HEIGHT + delta
        }

        moveTo(0f, top, 0f)
        frameSize.set(screenWidthDp, HEIGHT)
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    companion object {
        private const val HEIGHT = 80 // dp
        private const val FADE_DURATION = 0.42f // seconds
        private const val DELTA_DECEL = 1.1f // 1..oo
    }
}
