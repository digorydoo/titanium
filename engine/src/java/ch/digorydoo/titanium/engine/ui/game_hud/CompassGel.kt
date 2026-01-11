package ch.digorydoo.titanium.engine.ui.game_hud

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.behaviours.CreateConcurrently
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.sprite.UISpriteRenderer
import ch.digorydoo.titanium.engine.texture.ImageData
import ch.digorydoo.titanium.engine.texture.Texture
import kotlin.math.PI
import kotlin.reflect.KClass

class CompassGel: GraphicElement() {
    init {
        inDialog = Visibility.INVISIBLE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.ACTIVE
        visibleOnScreenshots = false
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

    private val createConcurrently = CreateConcurrently(
        this,
        object: CreateConcurrently.Delegate {
            private lateinit var tmpImg: ImageData

            override suspend fun onJobStart() {
                // Do not modify gel here! Store everything in temporary variables!
                tmpImg = App.textures.getOrLoadImageDataAsync("ui-compass.png")
                require(tmpImg.width == TEX_WIDTH)
                require(tmpImg.height == TEX_HEIGHT)
            }

            override fun onJobDone() {
                // Back in the main thread
                tex = App.textures.getOrCreateTexture(tmpImg)
            }
        }
    )

    override fun getBehaviour(klass: KClass<*>) = when (klass) {
        CreateConcurrently::class -> createConcurrently
        else -> null
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
