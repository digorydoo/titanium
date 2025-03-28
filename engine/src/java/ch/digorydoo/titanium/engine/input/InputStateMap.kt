package ch.digorydoo.titanium.engine.input

class InputStateMap<K> {
    private val map = mutableMapOf<K, InputState>()

    private fun getOrCreateState(key: K) =
        map[key] ?: InputState().also { map[key] = it }

    fun isPressed(key: K) =
        map[key]?.pressed == true

    fun isPressedOnce(key: K) =
        getOrCreateState(key).pressedOnce

    fun isPressedWithRepeat(key: K) =
        getOrCreateState(key).pressedWithRepeat

    fun setPressed(key: K) {
        val s = getOrCreateState(key)

        // This function is called repeatedly when Gamepad btn is held down,
        // so don't clear handleTime unless btn wasn't pressed before.

        if (!s.pressed) {
            s.pressed = true
            s.handleTime = null
        }
    }

    fun setReleased(key: K) {
        val s = getOrCreateState(key)
        s.pressed = false
        s.handleTime = null
    }

    fun setAllReleased() {
        map.forEach { (key, _) -> setReleased(key) }
    }

    fun set(key: K, pressed: Boolean) {
        when {
            pressed && !isPressed(key) -> setPressed(key)
            !pressed && isPressed(key) -> setReleased(key)
        }
    }
}
