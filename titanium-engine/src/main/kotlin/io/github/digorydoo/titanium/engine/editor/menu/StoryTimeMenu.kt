package io.github.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.kutils.string.lpad
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import io.github.digorydoo.titanium.engine.intermission.Intermission

internal class StoryTimeMenu(private val actions: EditorActions) {
    suspend fun Intermission.show() {
        showImpl(/* hasParentMenu = true */)
    }

    private suspend fun Intermission.showImpl() {
        val hours = arrayOf(0, 3, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 21)

        showDlg {
            hours.forEach { h ->
                item {
                    text = "Set to ${lpad(h, 2, '0')}:00}"
                    onSelect = { actions.setStoryTime(h, 0) }
                }
            }

            dismiss = item { textId = EngineTextId.BACK }
        }
    }
}
