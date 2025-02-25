package ch.digorydoo.titanium.main.input

import ch.digorydoo.titanium.engine.input.Input.Key
import org.lwjgl.glfw.GLFW

abstract class KeyMapping {
    class KeysWithModifiers(val solo: Key?, val ctrl: Key?) {
        fun withModifiers(isCtrlDown: Boolean) =
            when {
                isCtrlDown -> ctrl
                else -> solo
            }
    }

    protected abstract fun getSoloKey(glfwKey: Int): Key?
    protected abstract fun getCtrlKey(c: Char): Key?

    fun getKeysWithModifiers(glfwKey: Int): KeysWithModifiers {
        val keyName = GLFW.glfwGetKeyName(glfwKey, GLFW.glfwGetKeyScancode(glfwKey)) ?: ""
        val c = if (keyName.isNotEmpty()) keyName[0] else '\u0000'
        return KeysWithModifiers(
            solo = getSoloKey(glfwKey),
            ctrl = getCtrlKey(c),
        )
    }
}
