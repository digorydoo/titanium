package ch.digorydoo.titanium.engine.ui.game_status

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer
import kotlin.math.PI

class CompassGel: GraphicElement() {
    init {
        inDialog = Visibility.INVISIBLE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.ACTIVE
        visibleOnScreenshots = false
    }

    private val props = object: UISpriteRenderer.Delegate() {
        override val renderPos get() = this@CompassGel.pos
        override val frameSize = Point2f(TEX_WIDTH, TEX_HEIGHT)
        override val rotation get() = App.camera.currentPhi + (PI / 2).toFloat()
        override val scaleFactor = MutablePoint2f(0.33f, 0.33f)
        override var tex = App.textures.getOrCreateTexture("ui-compass.png")
    }

    override val renderer = App.factory.createUISpriteRenderer(props, antiAliasing = true)

    private val align = Align(
        object: Align.Delegate() {
            override val anchor = Align.Anchor.TOP_RIGHT
            override val marginTop = MARGIN_TOP
            override val marginRight = MARGIN_RIGHT
            override val width = (props.frameSize.x * props.scaleFactor.x).toInt()

            override fun setPos(x: Int, y: Int) {
                this@CompassGel.pos.set(x, y, 0)
            }
        }
    )

    override fun onAnimateActive() {
        align.animate()
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    companion object {
        private const val TEX_WIDTH = 64.0f // dp
        private const val TEX_HEIGHT = 64.0f // dp
        private const val MARGIN_RIGHT = 48 // dp
        private const val MARGIN_TOP = 64 // dp
    }
}
