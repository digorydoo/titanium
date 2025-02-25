package ch.digorydoo.titanium.engine.input

interface Input {
    enum class InputMode { KEYBOARD, GAMEPAD }

    enum class Key {
        ACTION_A, // e.g. accept, yes, use item nearby
        ACTION_B, // e.g. reject, no, leave menu, put away item, interrupt spoken text
        ACTION_X, // e.g. attack, draw sword
        ACTION_Y, // e.g. jump
        MENU_LEFT, // aka. "Back", open menu far left
        MENU_RIGHT, // aka. "Start", open menu far right
        REAR_LEFT_BTN, // e.g. activate rune, select top-level menu item left
        REAR_RIGHT_BTN, // e.g. throw sword, select top-level menu item right
        REAR_LEFT_THRUSH, // e.g. shield
        REAR_RIGHT_THRUSH, // e.g. use bow and arrows
        ENTER,
        ESCAPE,
        SHIFT,
        ALT,
        CTRL,
        LJOY_LEFT,
        LJOY_RIGHT,
        LJOY_UP,
        LJOY_DOWN,
        RJOY_LEFT,
        RJOY_RIGHT,
        RJOY_UP,
        RJOY_DOWN,
        HAT_LEFT,
        HAT_RIGHT,
        HAT_UP,
        HAT_DOWN,
        EDITOR_TOGGLE,
        EDITOR_CUT,
        EDITOR_COPY,
        EDITOR_PASTE,
        EDITOR_SAVE,
        EDITOR_UNDO,
        EDITOR_INFO,
        EDITOR_DRAW,
        EDITOR_WIZARD_MENU,
        EDITOR_POSZ_INC,
        EDITOR_POSZ_DEC,
        EDITOR_PAGE_DOWN,
        EDITOR_PAGE_UP,
        EDITOR_PAGE_LEFT,
        EDITOR_PAGE_RIGHT,
        EDITOR_BACKSPACE,
        EDITOR_CAMERA_MODE,
        EDITOR_SWITCH_CAMERA_TARGET,
        EDITOR_ROTATE,
        EDITOR_SHAPE_MENU,
        EDITOR_APPLY_SHAPE,
        EDITOR_MATERIAL_MENU,
        EDITOR_APPLY_MATERIAL,
        EDITOR_PICK_SHAPE_MATERIAL,
        EDITOR_NEXT_SPAWN_PT,
        EDITOR_SPAWN_PT_MENU,
    }

    val mode: InputMode
    val values: InputValues
}
