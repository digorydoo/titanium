package ch.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.kutils.string.lpad
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.ui.choice.TextChoice

class StoryTimeMenu(private val actions: EditorActions) {
    fun show(isTopLevel: Boolean, onCancel: () -> Unit) {
        show(playSoundOnOpen = isTopLevel, playSoundOnDismiss = isTopLevel, onCancel)
    }

    private fun show(playSoundOnOpen: Boolean, playSoundOnDismiss: Boolean, onCancel: () -> Unit) {
        fun choice(hours: Int, minutes: Int) =
            TextChoice("Set to ${lpad(hours, 2, '0')}:${lpad(minutes, 2, '0')}") {
                actions.setStoryTime(hours, minutes)
            }

        val choices = listOf(
            choice(0, 0),
            choice(3, 0),
            choice(6, 0),
            choice(7, 0),
            choice(8, 0),
            choice(9, 0),
            choice(10, 0),
            choice(11, 0),
            choice(12, 0),
            choice(13, 0),
            choice(14, 0),
            choice(15, 0),
            choice(16, 0),
            choice(17, 0),
            choice(18, 0),
            choice(21, 0),
            TextChoice(EngineTextId.CANCEL, onCancel),
        )

        App.dlg.showChoices(
            choices,
            0,
            lastItemIsDismiss = true,
            playSoundOnOpen = playSoundOnOpen,
            playSoundOnDismiss = playSoundOnDismiss,
        )
    }
}
