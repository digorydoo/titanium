package ch.digorydoo.titanium.engine.ui.game_hud

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer

class ProgressBarGel(posX: Int, posY: Int): GraphicElement(posX, posY, 0) {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    private val bgTex = App.textures.createTexture(4, 4).apply {
        drawInto { clear(Colour.white) }
    }

    override val renderer = App.factory.createUISpriteRenderer(
        object: UISpriteRenderer.Delegate() {
            override val renderPos get() = this@ProgressBarGel.pos
            override val frameSize get() = this@ProgressBarGel.frameSize
            override val scaleFactor = MutablePoint2f(1.0f, 1.0f)
            override val tex = bgTex
        }
    )

    private val frameSize = MutablePoint2f(0, BAR_HEIGHT)
    var progress = 0.0f

    override fun onAnimateActive() {
        frameSize.x = lerp(frameSize.x, progress * BAR_MAX_WIDTH, 0.2f)
    }

    override fun onRemoveZombie() {
        renderer.free()
        bgTex.freeRequireUnshared()
    }

    companion object {
        const val BAR_MAX_WIDTH = 320
        const val BAR_HEIGHT = 16
    }
}
