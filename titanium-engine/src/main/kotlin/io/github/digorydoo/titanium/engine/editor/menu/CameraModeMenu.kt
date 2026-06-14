package io.github.digorydoo.titanium.engine.editor.menu

import io.github.digorydoo.titanium.engine.camera.CameraDirectingMode
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import io.github.digorydoo.titanium.engine.intermission.Intermission

internal class CameraModeMenu(private val actions: EditorActions) {
    fun showInIntermission() {
        App.intermissions.begin {
            showImpl(hasParentMenu = false)
        }
    }

    suspend fun Intermission.show() {
        showImpl(hasParentMenu = true)
    }

    private suspend fun Intermission.showImpl(hasParentMenu: Boolean) {
        showDlg {
            CameraDirectingMode.entries.forEach { mode ->
                item {
                    text = mode.displayText
                    if (mode == App.camera.directingMode) this@showDlg.focus = this
                    onSelect = { actions.setCameraMode(mode) }
                }
            }

            dismiss = item {
                textId = if (hasParentMenu) EngineTextId.BACK else EngineTextId.DONE
            }
        }
    }
}
