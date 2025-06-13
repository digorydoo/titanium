package ch.digorydoo.titanium.engine.ui.icon

/**
 * Common icons of the UI, not just the ones for describing input keys and buttons. Having all icons in one enum has
 * the benefit that we can use any of them in a UI dialogue and do not need to combine several enums. But it also means
 * that all of them need to have the same resolution and size.
 */
enum class Icon(val frame: Int) {
    A(0),
    B(1),
    X(2),
    Y(3),
    RESERVED1(4),
    HAT_SWITCH(5),
    LJOY(6),
    RJOY(7),
    LJOY_BTN(8), // push left joystick
    RJOY_BTN(9), // push right joystick
    REAR_UPPER_LEFT(10), // LB
    REAR_UPPER_RIGHT(11), // RB
    REAR_LOWER_LEFT(12), // LT
    REAR_LOWER_RIGHT(13), // RT
    RESERVED2(14),
    MENU_LEFT(15), // back
    MENU_RIGHT(16), // start
    ESC(17),
    RETURN(18),
    RESERVED3(19),
    FOCUS_TRIANGLE(20),
}
