package ch.digorydoo.titanium.engine.input

import ch.digorydoo.titanium.engine.input.Input.Key

class KeyStateMap {
    private val map = mutableMapOf<Key, KeyState>()

    private fun getOrCreateState(key: Key) =
        map[key] ?: KeyState().also { map[key] = it }

    fun isPressed(key: Key) =
        map[key]?.pressed == true

    fun isPressedOnce(key: Key) =
        getOrCreateState(key).pressedOnce

    fun isPressedWithRepeat(key: Key) =
        getOrCreateState(key).pressedWithRepeat

    fun setPressed(key: Key) {
        val s = getOrCreateState(key)

        // This method is called repeatedly when Gamepad btn is held down,
        // so don't clear handleTime unless btn wasn't pressed before.

        if (!s.pressed) {
            s.pressed = true
            s.handleTime = null
        }
    }

    fun setReleased(key: Key) {
        val s = getOrCreateState(key)
        s.pressed = false
        s.handleTime = null
    }

    fun setPressedOrReleased(key: Key, pressed: Boolean) {
        when (pressed) {
            true -> setPressed(key)
            false -> setReleased(key)
        }
    }
}
