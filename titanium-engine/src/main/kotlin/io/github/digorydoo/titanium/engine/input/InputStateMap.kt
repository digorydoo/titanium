package io.github.digorydoo.titanium.engine.input

class InputStateMap<K> {
    private val map = mutableMapOf<K, InputState>()

    private var numPressed = 0
    val anyPressed get() = numPressed > 0

    private fun getOrCreateState(key: K) =
        map[key] ?: InputState().also { map[key] = it }

    /**
     * @return true if and only if the key is down (even if it has been handled)
     */
    fun isDown(key: K) =
        map[key]?.isDown == true // getOrCreateState not needed since isDown should not have any side-effects

    /**
     * @return true if and only if the key is down and has not been handled
     */
    fun checkPressed(key: K) =
        getOrCreateState(key).checkPressed()

    /**
     * Same as checkPressed, but also sets the key to handled.
     * @return true if and only if the key is down and has not been handled
     */
    fun checkPressedOnce(key: K) =
        getOrCreateState(key).checkPressedOnce()

    /**
     * Similar to checkPressedOnce, it will set the key to handled, but it will fire repeatedly.
     * @return true if and only if the key is down and either has not been handled, or the handle time is older than
     *    the repeat interval.
     */
    fun checkPressedWithRepeat(key: K) =
        getOrCreateState(key).checkPressedWithRepeat()

    /**
     * Marks the key as handled, which prevents checkPressed or checkPressedOnce from returning true until the key is
     * released. It is an error to call this function while the key is not currently down. You normally don't need to
     * call this function at all, because you should rely on checkPressedOnce or checkPressedWithRepeat. It is only
     * needed in rare cases when isDown or checkPressed need to be checked before you actually know you're going to
     * handle the key.
     */
    fun didHandle(key: K) {
        getOrCreateState(key).didHandle()
    }

    /**
     * Called repeatedly by Gamepad and KeyboardImpl to update the state of their buttons/keys.
     */
    fun setPressed(key: K) {
        val s = getOrCreateState(key)

        if (!s.isDown) {
            numPressed++
            s.setPressed()
        }
    }

    /**
     * Called repeatedly by Gamepad and KeyboardImpl to update the state of their buttons/keys.
     */
    fun setReleased(key: K) {
        val s = getOrCreateState(key)

        if (s.isDown) {
            numPressed--
            s.setReleased()
        }
    }

    fun setAllReleased() {
        map.forEach { (key, _) -> setReleased(key) }
        numPressed = 0
    }

    fun set(key: K, pressed: Boolean) {
        when {
            pressed -> setPressed(key)
            !pressed -> setReleased(key)
        }
    }
}
