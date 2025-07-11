package ch.digorydoo.titanium.engine.ui.game_hud

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.sprite.UISpriteRenderer
import ch.digorydoo.titanium.engine.texture.Texture
import kotlin.math.PI

class CompassGel: GraphicElement() {
    init {
        inDialog = Visibility.INVISIBLE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.ACTIVE
        visibleOnScreenshots = false
        callOnCreateConcurrently = true
    }

    private var tex: Texture? = null

    private val props = object: UISpriteRenderer.Delegate() {
        override val renderPos get() = this@CompassGel.pos
        override val frameSize = Point2f(TEX_WIDTH, TEX_HEIGHT)
        override val rotation get() = App.camera.currentPhi + (PI / 2).toFloat()
        override val scaleFactor = MutablePoint2f(0.33f, 0.33f)
        override val tex get() = this@CompassGel.tex
    }

    override val renderer = App.factory.createUISpriteRenderer(props, antiAliasing = true)

    private val align = Align(
        object: Align.Delegate() {
            override val anchor = Align.Anchor.TOP_RIGHT
            override val marginTop = MARGIN_TOP
            override val marginRight = MARGIN_RIGHT
            override val width = (props.frameSize.x * props.scaleFactor.x).toInt()

            override fun setPos(x: Int, y: Int) {
                this@CompassGel.moveTo(x, y, 0)
            }
        }
    )

    override suspend fun onCreateConcurrently(): () -> Unit {
        val img = App.textures.getOrLoadImageDataAsync("ui-compass.png")
        require(img.width == TEX_WIDTH)
        require(img.height == TEX_HEIGHT)

        return {
            // Back in main thread
            tex = App.textures.getOrCreateTexture(img)
        }
    }

    fun show() {
        setHiddenOnNextFrameTo = false
    }

    fun hide() {
        setHiddenOnNextFrameTo = true
    }

    override fun onAnimateActive() {
        align.animate()
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    companion object {
        private const val TEX_WIDTH = 64 // texels
        private const val TEX_HEIGHT = 64 // texels
        private const val MARGIN_RIGHT = 48 // dp
        private const val MARGIN_TOP = 64 // dp
    }
}
