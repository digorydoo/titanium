package io.github.digorydoo.titanium.engine.editor.menu

import io.github.digorydoo.titanium.engine.camera.CameraProps.Mode
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.i18n.EngineTextId

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
