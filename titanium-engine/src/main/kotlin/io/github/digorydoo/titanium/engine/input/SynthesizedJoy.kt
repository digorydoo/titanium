package io.github.digorydoo.titanium.engine.input

import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.input.keyboard.DirectionalKeys
import io.github.digorydoo.titanium.engine.input.keyboard.Keyboard

internal class SynthesizedJoy(private val keys: DirectionalKeys, private val keyboard: Keyboard) {
    val dir: Vector2f get() = _dir
    private val _dir = MutableVector2f()

    fun update() {
        _dir.apply {
            x = when {
                keyboard.checkPressed(keys.xNegativeKey) -> -1.0f
                keyboard.checkPressed(keys.xPositiveKey) -> 1.0f
                else -> 0.0f
            }
            y = when {
                keyboard.checkPressed(keys.yNegativeKey) -> -1.0f
                keyboard.checkPressed(keys.yPositiveKey) -> 1.0f
                else -> 0.0f
            }
            if (x != 0.0f && y != 0.0f) normalize()
        }
    }
}
