package ch.digorydoo.titanium.engine.ui.game_hud

import ch.digorydoo.kutils.point.MutablePoint4f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.texture.FrameCollection
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer
import ch.digorydoo.titanium.engine.ui.icon.Icon
import kotlin.math.abs
import kotlin.math.sin

class ActionTargetArrowGel: GraphicElement() {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
        visibleOnScreenshots = false
    }

    var target: GraphicElement? = null; private set
    private val pos4f = MutablePoint4f()

    private val frames = FrameCollection().apply {
        setTexture("ui-icons.png", 5, 6) // shared
        setFrame(Icon.FOCUS_TRIANGLE.frame)
    }

    private val props = object: UISpriteRenderer.Delegate() {
        override val renderPos get() = this@ActionTargetArrowGel.pos
        override val renderPosIsNormalised = true
        override val tex get() = frames.tex
        override val frameSize = frames.frameSize // shared mutable object
        override val texOffset = frames.texOffset // shared mutable object
        override val scaleFactor = Point2f(SCALE_FACTOR, SCALE_FACTOR)
    }

    override val renderer = App.factory.createUISpriteRenderer(props, antiAliasing = true)

    fun show(newTarget: GraphicElement) {
        target = newTarget
        setHiddenOnNextFrameTo = false
    }

    fun hide() {
        target = null
        setHiddenOnNextFrameTo = true
    }

    override fun onAnimateActive() {
        val target = target ?: return
        val targetZ = (target.body?.let { it.pos.z + it.enclosingRadius }) ?: (target.pos.z + DEFAULT_RADIUS)

        // We should probably do this from the vertex shader, but it's simpler doing it from here.
        pos4f.set(target.pos.x, target.pos.y, targetZ + Z_DISTANCE, 1.0f)
        pos4f.setMultiplied(App.camera.projMatrix, pos4f)
        val pulsating = PULSATING_DISTANCE * abs(sin(App.time.sessionTime * PULSATING_FREQ))

        moveTo(
            pos4f.x / pos4f.w - App.dpToGlX(SCALE_FACTOR * frames.frameSize.x / 2.0f),
            pos4f.y / pos4f.w + App.dpToGlY(SCALE_FACTOR * frames.frameSize.y / 2.0f - pulsating),
            0.0f
        )
    }

    override fun onRemoveZombie() {
        renderer.free()
        // frames.tex is a shared texture
    }

    companion object {
        private const val SCALE_FACTOR = 0.5f
        private const val Z_DISTANCE = 0.42f // distance from the top of the target to the arrow's origin
        private const val PULSATING_DISTANCE = 16.0f // dp
        private const val PULSATING_FREQ = 4.2f
        private const val DEFAULT_RADIUS = 0.5f // just a fallback when the target has no body
    }
}
