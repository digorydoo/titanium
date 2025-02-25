package ch.digorydoo.titanium.main.input

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.input.Input.Key
import ch.digorydoo.titanium.main.input.KeyMapping.KeysWithModifiers
import org.lwjgl.glfw.GLFW.*

class Keymap {
    private val keyMappingInGame = KeyMappingInGame()
    private val keyMappingInEditor = KeyMappingInEditor()

    class Match(
        val keyOfEvent: Key?,
        private val gameKey: KeysWithModifiers,
        private val editorKey: KeysWithModifiers,
    ) {
        fun forEachPossibleKey(lambda: (key: Key) -> Unit) {
            keyOfEvent?.let { lambda(it) }
            gameKey.solo?.let { lambda(it) }
            gameKey.ctrl?.let { lambda(it) }
            editorKey.solo?.let { lambda(it) }
            editorKey.ctrl?.let { lambda(it) }
        }
    }

    fun getMatch(glfwKey: Int, isCtrlDown: Boolean): Match {
        val gameKey = keyMappingInGame.getKeysWithModifiers(glfwKey)
        val editorKey = keyMappingInEditor.getKeysWithModifiers(glfwKey)

        val keyOfEvent = when (glfwKey) {
            GLFW_KEY_LEFT_SHIFT -> Key.SHIFT
            GLFW_KEY_RIGHT_SHIFT -> Key.SHIFT
            GLFW_KEY_LEFT_ALT -> Key.ALT
            GLFW_KEY_RIGHT_ALT -> Key.ALT
            GLFW_KEY_LEFT_CONTROL -> Key.CTRL
            GLFW_KEY_RIGHT_CONTROL -> Key.CTRL
            else -> when {
                App.editor.isShown -> editorKey.withModifiers(isCtrlDown)
                else -> gameKey.withModifiers(isCtrlDown)
            }
        }

        return Match(keyOfEvent, gameKey, editorKey)
    }
}
