package io.github.digorydoo.titanium.engine.ui.icon

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.math.accel
import ch.digorydoo.kutils.math.clamp
import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.behaviours.Glow
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.i18n.ITextId
import io.github.digorydoo.titanium.engine.input.InputAccessor.BtnOrKeyAccessor
import io.github.digorydoo.titanium.engine.input.gamepad.GamepadBtn
import io.github.digorydoo.titanium.engine.input.keyboard.KeyboardKey

class InputIconGel(
    private val input: BtnOrKeyAccessor,
    alignment: Align.Alignment,
    label: ITextId? = null,
    glowEnabled: Boolean = true,
    private val fadeWhenNoInput: Boolean = false,
    val secondsToHoldForSelect: Float = 0.0f,
    private val onSelect: (() -> Unit)? = null,
): GraphicElement() {
    private enum class HoldingState { INITIAL, HOLD, RELEASE }

    // NOTE: When prefs.swapGamepadBtnsABXY is true, gamepad will report ACTION_Y when the X button is pressed, and
    // InputIconFrames will draw a Y when we fetch Icon.X.
    private val iconWhenGamepad: Icon = when (input.btn) {
        GamepadBtn.ACTION_A -> Icon.A
        GamepadBtn.ACTION_B -> Icon.B
        GamepadBtn.ACTION_X -> Icon.X
        GamepadBtn.ACTION_Y -> Icon.Y
        GamepadBtn.OPEN_MENU_LEFT -> Icon.MENU_LEFT
        GamepadBtn.OPEN_MENU_RIGHT -> Icon.MENU_RIGHT
        GamepadBtn.REAR_UPPER_LEFT -> Icon.REAR_UPPER_LEFT
        GamepadBtn.REAR_UPPER_RIGHT -> Icon.REAR_UPPER_RIGHT
        GamepadBtn.REAR_LOWER_LEFT -> Icon.REAR_LOWER_LEFT
        GamepadBtn.REAR_LOWER_RIGHT -> Icon.REAR_LOWER_RIGHT
        GamepadBtn.HAT_DOWN, GamepadBtn.HAT_UP, GamepadBtn.HAT_LEFT, GamepadBtn.HAT_RIGHT -> {
            Log.warn(TAG, "Unspecific icon for hat-switch used, because specific icon not supported")
            Icon.HAT_SWITCH
        }
        else -> {
            Log.warn(TAG, "Unspecific icon for gamepad button used, because there is no icon for ${input.btn}")
            Icon.UNSPECIFIED_GAMEPAD_BTN
        }
    }

    private val iconWhenKeyboard: Icon = when (input.key) {
        KeyboardKey.ESCAPE -> Icon.ESC
        KeyboardKey.ENTER -> Icon.RETURN
        else -> {
            Log.warn(TAG, "Unspecific icon for keyboard key used, because there is no icon for ${input.key}")
            Icon.UNSPECIFIED_KEYBOARD_BTN
        }
    }

    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
        visibleOnScreenshots = false
    }

    var scrollOffset = 0.0f

    private val frames = InputIconFrames(iconWhenGamepad, iconWhenKeyboard)
    override val renderer = InputIconRenderer(this, frames, label)

    private val align = Align(
        this,
        object: Align.Alignment(alignment) {
            override val xOffset
                get() = alignment.xOffset - when (anchor) {
                    Align.Anchor.TOP_RIGHT, Align.Anchor.BOTTOM_RIGHT -> renderer.labelWidth.toInt()
                    else -> 0
                }
        },
        object: Align.Delegate {
            private val frameSize = frames.frameSize // shared mutable object
            override val width get() = (frameSize.x * ICON_SCALE_FACTOR).toInt()
            override val height get() = (frameSize.y * ICON_SCALE_FACTOR).toInt()
        }
    )

    private val glowProps = object: Glow.Delegate {
        override var brightness = 1.0f
    }

    val brightness get() = glowProps.brightness
    private val glow = Glow(glowProps, enabled = glowEnabled)

    var opacity = 1f; private set
    private var timeOfLastInput = App.time.sessionTime

    var circularProgress = 0.0f; private set // 0..1
    private var holdingState = HoldingState.INITIAL
    private var holdingStateUntil = 0f

    override fun onAnimateActive() {
        val now = App.time.sessionTime

        if (fadeWhenNoInput) {
            if (App.input.anyBtnOrKeyPressed) {
                timeOfLastInput = now
                opacity = 1f
            } else {
                val timePassed = now - timeOfLastInput

                if (timePassed >= AUTO_FADE_DELAY) {
                    opacity = 1.0f - accel(clamp((timePassed - AUTO_FADE_DELAY) / AUTO_FADE_DURATION), 1.3f)
                }
            }
        }

        if (secondsToHoldForSelect > 0f) {
            when (holdingState) {
                HoldingState.INITIAL -> {
                    if (input.isDown) {
                        circularProgress = 0f
                        holdingState = HoldingState.HOLD
                        holdingStateUntil = now + secondsToHoldForSelect
                    }
                }
                HoldingState.HOLD -> {
                    if (input.isDown) {
                        circularProgress = 1f - (holdingStateUntil - now) / secondsToHoldForSelect

                        if (circularProgress >= 1f) {
                            circularProgress = 0f
                            holdingState = HoldingState.INITIAL
                            holdingStateUntil = 0f
                            input.didHandle()
                            onSelect?.invoke()
                        }
                    } else {
                        holdingState = HoldingState.RELEASE
                        holdingStateUntil = now + circularProgress * secondsToHoldForSelect
                    }
                }
                HoldingState.RELEASE -> {
                    if (input.isDown) {
                        holdingState = HoldingState.HOLD
                        holdingStateUntil = now + (1.0f - circularProgress) * secondsToHoldForSelect
                    } else {
                        circularProgress = (holdingStateUntil - now) / secondsToHoldForSelect

                        if (circularProgress <= 0f) {
                            circularProgress = 0f
                            holdingState = HoldingState.INITIAL
                            holdingStateUntil = 0f
                        }
                    }
                }
            }
        } else {
            onSelect?.let {
                if (input.checkPressedOnce()) it()
            }
        }

        align.animate()
        glow.animate()
        frames.update()
    }

    override fun show() {
        super.show()

        if (fadeWhenNoInput) {
            opacity = 1f
            timeOfLastInput = App.time.sessionTime
        }
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    companion object {
        private val TAG = Log.Tag("InputIconGel")
        const val ICON_SCALE_FACTOR = 0.375f
        private const val AUTO_FADE_DELAY = 1f // seconds
        private const val AUTO_FADE_DURATION = 2f // seconds
    }
}
