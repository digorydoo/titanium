package ch.digorydoo.titanium.main.input

import ch.digorydoo.titanium.engine.input.Input.Key
import org.lwjgl.glfw.GLFW.*

// Some combinations of keys may not work due to hardware limitations:
// https://gaming.stackexchange.com/questions/6669/how-do-i-remove-the-limit-on-pc-keyboard-button-presses
class KeyMappingInGame: KeyMapping() {
    override fun getSoloKey(glfwKey: Int) = when (glfwKey) {
        GLFW_KEY_COMMA -> Key.ACTION_A
        GLFW_KEY_PERIOD -> Key.ACTION_B
        GLFW_KEY_MINUS -> Key.ACTION_X
        GLFW_KEY_SPACE -> Key.ACTION_Y // cannot be combined with more than one arrow key on my keyboard
        GLFW_KEY_V -> Key.ACTION_Y // combination with two arrow keys works on my keyboard

        GLFW_KEY_HOME -> Key.MENU_LEFT
        GLFW_KEY_END -> Key.MENU_RIGHT

        GLFW_KEY_A -> Key.HAT_LEFT
        GLFW_KEY_D -> Key.HAT_RIGHT
        GLFW_KEY_S -> Key.HAT_DOWN
        GLFW_KEY_W -> Key.HAT_UP

        GLFW_KEY_LEFT -> Key.LJOY_LEFT
        GLFW_KEY_RIGHT -> Key.LJOY_RIGHT
        GLFW_KEY_DOWN -> Key.LJOY_DOWN
        GLFW_KEY_UP -> Key.LJOY_UP

        GLFW_KEY_H -> Key.RJOY_LEFT
        GLFW_KEY_K -> Key.RJOY_RIGHT
        GLFW_KEY_J -> Key.RJOY_DOWN
        GLFW_KEY_U -> Key.RJOY_UP

        GLFW_KEY_N -> Key.REAR_LEFT_BTN
        GLFW_KEY_M -> Key.REAR_RIGHT_BTN
        GLFW_KEY_Y -> Key.REAR_LEFT_THRUSH
        GLFW_KEY_I -> Key.REAR_RIGHT_THRUSH

        GLFW_KEY_ENTER -> Key.ENTER
        GLFW_KEY_KP_ENTER -> Key.ENTER
        GLFW_KEY_ESCAPE -> Key.ESCAPE
        else -> null
    }

    override fun getCtrlKey(c: Char) = when (c) {
        'e' -> Key.EDITOR_TOGGLE
        else -> null
    }
}
