package ch.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.titanium.engine.camera.CameraProps.Mode
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.i18n.EngineTextId

internal class CameraModeMenu(private val actions: EditorActions) {
    fun show(onBack: (() -> Unit)?) {
        App.dlg.showDlg<Unit> {
            Mode.entries.forEach { mode ->
                item {
                    text = mode.displayText
                    onSelect = { actions.setCameraMode(mode) }
                    if (mode == App.camera.mode) this@showDlg.focus = this
                }
            }

            item {
                text = "Top-down (fixed distance)"
                onSelect = { actions.setCameraModeTopDown() }
            }
            dismiss = item {
                textId = if (onBack == null) EngineTextId.DONE else EngineTextId.BACK
                onSelect = onBack
            }
        }
    }
}
