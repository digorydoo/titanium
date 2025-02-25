package ch.digorydoo.titanium.engine.ui

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.behaviours.Glow
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.input.Input.InputMode
import ch.digorydoo.titanium.engine.texture.FrameCollection

class KeyBtnGel(
    private val kindWhenGamepad: Kind,
    private val kindWhenKeyboard: Kind,
    posX: Int,
    posY: Int,
    glowEnabled: Boolean,
): GraphicElement(posX, posY, 0) {
    @Suppress("unused")
    enum class Kind(val frame: Int) {
        A(0),
        B(1),
        X(2),
        Y(3),
        LT(4),
        RT(5),
        START(6),
        HAT_SWITCH(7),
        RETURN(8),
        ESC(9),
        SPACE(10),
        BACKSPACE(11),
        LEFT_ARROW(12),
        RIGHT_ARROW(13),
        UP_ARROW(14),
        DOWN_ARROW(15),
    }

    private inner class WatchInputMode: Behaviour {
        private var prevMode: InputMode? = null

        override fun animate() {
            if (App.input.mode != prevMode) {
                prevMode = App.input.mode

                val kind = when (App.input.mode) {
                    InputMode.GAMEPAD -> kindWhenGamepad
                    InputMode.KEYBOARD -> kindWhenKeyboard
                }

                frames.setFrame(kind.frame)
            }
        }
    }

    var scrollOffset = 0.0f

    private val frames = FrameCollection().apply {
        setTexture("ui-buttons.png", 4, 4)
    }

    private val glowProps = object: Glow.Delegate {
        override var brightness = 1.0f
    }

    private val glow = Glow(glowProps, enabled = glowEnabled)

    private val renderPos = MutablePoint3f()

    override val renderer = App.factory.createUISpriteRenderer(
        object: UISpriteRenderer.Delegate() {
            override val renderPos
                get() = this@KeyBtnGel.renderPos.set(this@KeyBtnGel.pos).add(0.0f, scrollOffset, 0.0f)

            override val frameSize get() = frames.frameSize
            override val tex get() = frames.tex
            override val texOffset get() = frames.texOffset
            override val brightness get() = glowProps.brightness
        }
    )

    override val inDialog = Visibility.ACTIVE
    override val inMenu = Visibility.ACTIVE
    override val inEditor = Visibility.ACTIVE

    private val watchInputMode = WatchInputMode()

    override fun onAnimateActive() {
        glow.animate()
        watchInputMode.animate()
    }

    override fun onRemoveZombie() {
        renderer.free()
    }
}
