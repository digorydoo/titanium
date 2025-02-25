package ch.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.titanium.engine.camera.CameraProps.Mode
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.ui.choice.TextChoice

class CameraModeMenu(private val actions: EditorActions) {
    fun show(isTopLevel: Boolean, onCancel: () -> Unit) {
        show(
            playSoundOnOpen = isTopLevel,
            playSoundOnDismiss = isTopLevel,
            onCancel
        )
    }

    private fun show(playSoundOnOpen: Boolean, playSoundOnDismiss: Boolean, onCancel: () -> Unit) {
        val choices = Mode.entries.map { mode ->
            TextChoice(mode.displayText) { actions.setCameraMode(mode) }
        }.toMutableList()

        val curIdx = Mode.entries.indexOfFirst { it == App.camera.mode }

        choices.add(TextChoice("Top-down (fixed distance)") { actions.setCameraModeTopDown() })
        choices.add(TextChoice(EngineTextId.CANCEL) { onCancel() })

        App.dlg.showChoices(
            choices,
            curIdx,
            lastItemIsDismiss = true,
            playSoundOnOpen = playSoundOnOpen,
            playSoundOnDismiss = playSoundOnDismiss,
        )
    }
}
