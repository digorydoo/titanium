package ch.digorydoo.titanium.main.input

import ch.digorydoo.titanium.engine.input.Input.Key
import org.lwjgl.glfw.GLFW.*

class KeyMappingInEditor: KeyMapping() {
    override fun getSoloKey(glfwKey: Int) = when (glfwKey) {
        GLFW_KEY_ENTER -> Key.ENTER
        GLFW_KEY_ESCAPE -> Key.ESCAPE
        GLFW_KEY_HOME -> Key.EDITOR_PAGE_LEFT
        GLFW_KEY_END -> Key.EDITOR_PAGE_RIGHT
        GLFW_KEY_PAGE_DOWN -> Key.EDITOR_PAGE_DOWN
        GLFW_KEY_PAGE_UP -> Key.EDITOR_PAGE_UP

        GLFW_KEY_LEFT -> Key.HAT_LEFT
        GLFW_KEY_RIGHT -> Key.HAT_RIGHT
        GLFW_KEY_UP -> Key.HAT_UP
        GLFW_KEY_DOWN -> Key.HAT_DOWN

        GLFW_KEY_BACKSPACE -> Key.EDITOR_BACKSPACE
        GLFW_KEY_DELETE -> Key.EDITOR_BACKSPACE

        GLFW_KEY_KP_ADD -> Key.EDITOR_POSZ_INC
        GLFW_KEY_KP_SUBTRACT -> Key.EDITOR_POSZ_DEC
        GLFW_KEY_LEFT_BRACKET -> Key.EDITOR_POSZ_INC // ü in DE-CH layout; provided for laptop without keypad
        GLFW_KEY_APOSTROPHE -> Key.EDITOR_POSZ_DEC // ä in DE-CH layout; provided for laptop without keypad

        GLFW_KEY_C -> Key.EDITOR_CAMERA_MODE
        GLFW_KEY_H -> Key.RJOY_LEFT
        GLFW_KEY_J -> Key.RJOY_DOWN
        GLFW_KEY_K -> Key.RJOY_RIGHT
        GLFW_KEY_M -> Key.EDITOR_MATERIAL_MENU
        GLFW_KEY_N -> Key.EDITOR_SHAPE_MENU
        GLFW_KEY_P -> Key.EDITOR_PICK_SHAPE_MATERIAL
        GLFW_KEY_Q -> Key.EDITOR_DRAW
        GLFW_KEY_T -> Key.EDITOR_SPAWN_PT_MENU
        GLFW_KEY_U -> Key.RJOY_UP
        GLFW_KEY_W -> Key.EDITOR_WIZARD_MENU
        GLFW_KEY_Z -> Key.EDITOR_SWITCH_CAMERA_TARGET // Y in DE-CH layout
        else -> null
    }

    override fun getCtrlKey(c: Char) = when (c) {
        'c' -> Key.EDITOR_COPY
        'e' -> Key.EDITOR_TOGGLE
        'i' -> Key.EDITOR_INFO
        'm' -> Key.EDITOR_APPLY_MATERIAL
        'n' -> Key.EDITOR_APPLY_SHAPE
        'r' -> Key.EDITOR_ROTATE
        's' -> Key.EDITOR_SAVE
        't' -> Key.EDITOR_NEXT_SPAWN_PT
        'v' -> Key.EDITOR_PASTE
        'x' -> Key.EDITOR_CUT
        'z' -> Key.EDITOR_UNDO
        else -> null
    }
}
