package ch.digorydoo.titanium.engine.input

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.input.keyboard.DirectionalKeys
import ch.digorydoo.titanium.engine.input.keyboard.Keyboard

internal class SynthesizedJoy(private val keys: DirectionalKeys, private val keyboard: Keyboard) {
    val dir: Point2f get() = _dir
    private val _dir = MutablePoint2f()

    fun update() {
        _dir.apply {
            x = when {
                keyboard.isPressed(keys.xNegativeKey) -> -1.0f
                keyboard.isPressed(keys.xPositiveKey) -> 1.0f
                else -> 0.0f
            }
            y = when {
                keyboard.isPressed(keys.yNegativeKey) -> -1.0f
                keyboard.isPressed(keys.yPositiveKey) -> 1.0f
                else -> 0.0f
            }
            if (x != 0.0f && y != 0.0f) normalize()
        }
    }
}
