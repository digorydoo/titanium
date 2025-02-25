package ch.digorydoo.titanium.engine.i18n

import ch.digorydoo.titanium.engine.core.App
import org.jetbrains.annotations.PropertyKey

enum class EngineTextId(
    // The annotation helps the IDE to understand which keys are being used.
    @PropertyKey(resourceBundle = BUNDLE_NAME)
    override val resId: String,
): ITextId {
    // Editor
    EDITOR_BRICKS_COPIED("EDITOR_BRICKS_COPIED"),
    EDITOR_BRICKS_CUT("EDITOR_BRICKS_CUT"),
    EDITOR_CANNOT_PASTE("EDITOR_CANNOT_PASTE"),
    EDITOR_CANNOT_PICK_UP("EDITOR_CANNOT_PICK_UP"),
    EDITOR_CANNOT_REDO("EDITOR_CANNOT_REDO"),
    EDITOR_CANNOT_UNDO("EDITOR_CANNOT_UNDO"),
    EDITOR_FILE_SAVED("EDITOR_FILE_SAVED"),
    EDITOR_PICKED_UP("EDITOR_PICKED_UP"),
    EDITOR_NO_SPAWN_PTS("EDITOR_NO_SPAWN_PTS"),
    EDITOR_NO_PREV_ADDED_SPAWN_PT("EDITOR_NO_PREV_ADDED_SPAWN_PT"),

    // Prefs
    PREFS_AUTO_PICK_MONITOR_AND_RESOLUTION("PREFS_AUTO_PICK_MONITOR_AND_RESOLUTION"),
    PREFS_CAMERA_FAST_SPEED("PREFS_CAMERA_FAST_SPEED"),
    PREFS_CAMERA_NORMAL_SPEED("PREFS_CAMERA_NORMAL_SPEED"),
    PREFS_CAMERA_SLOW_SPEED("PREFS_CAMERA_SLOW_SPEED"),
    PREFS_CAMERA_SPEED("PREFS_CAMERA_SPEED"),
    PREFS_MONITOR_AND_RESOLUTION("PREFS_MONITOR_AND_RESOLUTION"),
    PREFS_SCALE_UI("PREFS_SCALE_UI"),
    PREFS_STRETCH_VIEWPORT("PREFS_STRETCH_VIEWPORT"),
    PREFS_SWAP_CAMERA_X("PREFS_SWAP_CAMERA_X"),
    PREFS_SWAP_CAMERA_Y("PREFS_SWAP_CAMERA_Y"),
    PREFS_TEXT_LANGUAGE("PREFS_TEXT_LANGUAGE"),
    PREFS_WINDOW_MODE("PREFS_WINDOW_MODE"),

    // Menus and dialogues
    YES("YES"),
    NO("NO"),
    BACK("BACK"),
    CANCEL("CANCEL"),
    DONE("DONE"),

    // Miscellaneous
    LOADING("LOADING"),
    MORE("MORE"), // with ellipsis
    ;

    override val bundle get() = App.i18n.engine

    companion object {
        const val BUNDLE_NAME = "EngineText"
    }
}
