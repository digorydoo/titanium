package io.github.digorydoo.titanium.engine.ui.game_hud

import ch.digorydoo.kutils.vector.MutableVector4f
import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.behaviours.CreateConcurrently
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.sprite.FrameCollection
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer
import io.github.digorydoo.titanium.engine.texture.ImageData
import io.github.digorydoo.titanium.engine.ui.icon.Icon
import kotlin.math.abs
import kotlin.math.sin
import kotlin.reflect.KClass

class ActionTargetArrowGel: GraphicElement() {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
        visibleOnScreenshots = false
    }

    var target: GraphicElement? = null; private set
    private val pos4f = MutableVector4f()

    private val frames = FrameCollection()

    private val props = object: UISpriteRenderer.Delegate() {
        override val renderPos get() = this@ActionTargetArrowGel.pos
        override val renderPosIsNormalised = true
        override val tex get() = frames.tex
        override val frameSize = frames.frameSize // shared mutable object
        override val texOffset = frames.texOffset // shared mutable object
        override val scaleFactor = Vector2f(SCALE_FACTOR, SCALE_FACTOR)
    }

    override val renderer = App.factory.createUISpriteRenderer(props, antiAliasing = true)

    private val createConcurrently = CreateConcurrently(
        this,
        object: CreateConcurrently.Delegate {
            private lateinit var tmpImg: ImageData

            override suspend fun onJobStart() {
                // Do not modify gel here! Store everything in temporary variables!
                tmpImg = App.textures.getOrLoadImageDataAsync("ui-icons.png")
            }

            override fun onJobDone() {
                // Back in main thread
                frames.setTexture(tmpImg, 5, 6)
                frames.setFrame(Icon.FOCUS_TRIANGLE.frame)
            }
        }
    )

    override fun getBehaviour(klass: KClass<*>) = when (klass) {
        CreateConcurrently::class -> createConcurrently
        else -> null
    }

    fun show(newTarget: GraphicElement) {
        target = newTarget
        setHiddenOnNextFrameTo = false
    }

    override fun hide() {
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

        val resolutionMgr = App.resolutionMgr
        moveTo(
            pos4f.x / pos4f.w - resolutionMgr.dpToGlX(SCALE_FACTOR * frames.frameSize.x / 2.0f),
            pos4f.y / pos4f.w + resolutionMgr.dpToGlY(SCALE_FACTOR * frames.frameSize.y / 2.0f - pulsating),
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
