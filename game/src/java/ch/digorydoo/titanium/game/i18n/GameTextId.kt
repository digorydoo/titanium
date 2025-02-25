package ch.digorydoo.titanium.game.i18n

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.i18n.ITextId
import org.jetbrains.annotations.PropertyKey

internal enum class GameTextId(
    // The annotation helps the IDE to understand which keys are being used.
    @PropertyKey(resourceBundle = BUNDLE_NAME)
    override val resId: String,
): ITextId {
    // Start menu
    CONTINUE_GAME("CONTINUE_GAME"),
    LOAD_GAME("LOAD_GAME"),
    NEW_GAME("NEW_GAME"),
    SETTINGS("SETTINGS"),
    QUIT("QUIT"),

    // Game menu
    MAP("MAP"),
    QUESTS("QUESTS"),
    PROFILE("PROFILE"),
    INVENTORY("INVENTORY"),
    ACHIEVEMENTS("ACHIEVEMENTS"),
    OPTIONS("OPTIONS"),

    // Game menu options
    SAVE_GAME("SAVE_GAME"),
    EXIT_TO_TITLE("EXIT_TO_TITLE"),
    CONFIRM_EXIT_TO_TITLE("CONFIRM_EXIT_TO_TITLE"),

    // Scene titles
    SCENE_AASTART("SCENE_AASTART"),
    SCENE_TOWN("SCENE_TOWN"),
    ;

    override val bundle get() = (App.i18n as I18nManagerImpl).game

    companion object {
        const val BUNDLE_NAME = "GameText"
    }
}
