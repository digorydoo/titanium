package io.github.digorydoo.titanium.engine.ui.icon

import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.input.InputManager.InputMode
import io.github.digorydoo.titanium.engine.sprite.FrameCollection

class InputIconFrames(private val iconWhenGamepad: Icon, private val iconWhenKeyboard: Icon) {
    private val frames = FrameCollection().apply {
        val img = App.textures.getOrLoadImageDataSync("ui-icons.png")
        setTexture(img, 5, 6) // shared
    }

    val frameSize: Vector2f = frames.frameSize // shared mutable object
    val texOffset: Vector2f = frames.texOffset // shared mutable object
    val tex get() = frames.tex

    private var prevMode: InputMode? = null
    private var prevSwapGamepadBtnsABXY = false

    fun update() {
        if (App.input.mode != prevMode || App.prefs.swapGamepadBtnsABXY != prevSwapGamepadBtnsABXY) {
            prevMode = App.input.mode
            prevSwapGamepadBtnsABXY = App.prefs.swapGamepadBtnsABXY

            val icon = when (App.input.mode) {
                InputMode.GAMEPAD -> iconWhenGamepad
                InputMode.KEYBOARD -> iconWhenKeyboard
            }

            val frame = when {
                App.prefs.swapGamepadBtnsABXY -> when (icon) {
                    Icon.A -> Icon.B.frame
                    Icon.B -> Icon.A.frame
                    Icon.X -> Icon.Y.frame
                    Icon.Y -> Icon.X.frame
                    else -> icon.frame
                }
                else -> icon.frame
            }

            frames.setFrame(frame)
        }
    }
}
