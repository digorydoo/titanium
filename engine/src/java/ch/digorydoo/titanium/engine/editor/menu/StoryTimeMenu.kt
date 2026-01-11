package ch.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.kutils.string.lpad
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.ui.dialogue.DlgDef

internal class StoryTimeMenu(private val actions: EditorActions) {
    fun show(onBack: () -> Unit) {
        App.dlg.showDlg {
            item(0, 0)
            item(3, 0)
            item(6, 0)
            item(7, 0)
            item(8, 0)
            item(9, 0)
            item(10, 0)
            item(11, 0)
            item(12, 0)
            item(13, 0)
            item(14, 0)
            item(15, 0)
            item(16, 0)
            item(17, 0)
            item(18, 0)
            item(21, 0)

            dismiss = item {
                textId = EngineTextId.CANCEL
                onSelect = onBack
            }
        }
    }

    private fun DlgDef<Unit>.item(hours: Int, minutes: Int) =
        item {
            text = "Set to ${lpad(hours, 2, '0')}:${lpad(minutes, 2, '0')}"
            onSelect = { actions.setStoryTime(hours, minutes) }
        }
}
