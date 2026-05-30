package io.github.digorydoo.titanium.engine.ui.icon

import ch.digorydoo.kutils.vector.MutableVector3f
import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.behaviours.Glow
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer

class DlgInputIconGel(
    iconWhenGamepad: Icon,
    iconWhenKeyboard: Icon,
    posX: Int,
    posY: Int,
    glowEnabled: Boolean,
): GraphicElement(posX, posY, 0) {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
        visibleOnScreenshots = false
    }

    var scrollOffset = 0.0f

    private val frames = InputIconFrames(iconWhenGamepad, iconWhenKeyboard)

    private val glowProps = object: Glow.Delegate {
        override var brightness = 1.0f
    }

    private val glow = Glow(glowProps, enabled = glowEnabled)
    private val renderPos = MutableVector3f()

    override val renderer = App.factory.createUISpriteRenderer(
        object: UISpriteRenderer.Delegate() {
            override val renderPos
                get() = this@DlgInputIconGel.renderPos.set(this@DlgInputIconGel.pos).add(0.0f, scrollOffset, 0.0f)

            override val frameSize = frames.frameSize // shared mutable object
            override val tex get() = frames.tex
            override val texOffset = frames.texOffset // shared mutable object
            override val brightness get() = glowProps.brightness
            override val scaleFactor = Vector2f(ICON_SCALE_FACTOR, ICON_SCALE_FACTOR)
        }
    )

    override fun onAnimateActive() {
        glow.animate()
        frames.update()
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    companion object {
        private const val ICON_SCALE_FACTOR = 0.375f
    }
}
