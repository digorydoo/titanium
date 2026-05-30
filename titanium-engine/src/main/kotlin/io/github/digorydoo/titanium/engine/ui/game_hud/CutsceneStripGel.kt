package io.github.digorydoo.titanium.engine.ui.game_hud

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.vector.MutableVector2f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer

class CutsceneStripGel(private val side: Side): GraphicElement() {
    enum class Side { TOP, BOTTOM }

    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    private val texture = makeTexture(Colour(0.0f, 0.0f, 0.0f)) // FIXME should not use a tex; see UIAreaGel
    private val frameSize = MutableVector2f()
    private val texScaleFactor = MutableVector2f(1.0f, 1.0f)

    override val renderer = App.factory.createUISpriteRenderer(
        object: UISpriteRenderer.Delegate() {
            override val tex = this@CutsceneStripGel.texture
            override val frameSize = this@CutsceneStripGel.frameSize
            override val renderPos = this@CutsceneStripGel.pos
            override val texScaleFactor = this@CutsceneStripGel.texScaleFactor
        },
        antiAliasing = false
    )

    private val screenSizeDp = App.resolutionMgr.screenSizeDp

    override fun onAnimateActive() {
        val screenWidthDp = screenSizeDp.x
        val screenHeightDp = screenSizeDp.y

        val top = when (side) {
            Side.TOP -> 0
            Side.BOTTOM -> screenHeightDp - HEIGHT
        }

        moveTo(0, top, 0)
        frameSize.set(screenWidthDp, HEIGHT)
    }

    override fun onRemoveZombie() {
        renderer.free()
        texture.freeRequireUnshared()
    }

    companion object {
        private const val HEIGHT = 80 // dp

        @Deprecated("Use Colour directly")
        fun makeTexture(bgColour: Colour) =
            App.textures.createTexture(2, 2).apply {
                drawInto { clear(bgColour) }
            }
    }
}
