package io.github.digorydoo.titanium.engine.editor.menu

import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.core.GameTime
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import io.github.digorydoo.titanium.engine.scene.Lighting
import io.github.digorydoo.titanium.engine.scene.MutableLighting
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgDef

internal class LightingMenu(private val actions: EditorActions) {
    fun show(onBack: () -> Unit) {
        val fineDay1000 = MutableLighting(Lighting.fineDay1200).apply { adaptToRelTime(GameTime.relTime(10, 0)) }

        App.dlg.showDlg {
            item("Follow story time", null)
            item("Fine day 02:00", Lighting.fineDay0200)
            item("Fine day 06:00", Lighting.fineDay0600)
            item("Fine day 10:00", fineDay1000)
            item("Fine day 12:00", Lighting.fineDay1200)
            item("Fine day 18:00", Lighting.fineDay1800)
            item("Fine day 22:00", Lighting.fineDay2200)

            dismiss = item {
                textId = EngineTextId.CANCEL
                onSelect = onBack
            }
        }
    }

    private fun DlgDef<Unit>.item(text: String, lighting: Lighting?) =
        item {
            this.text = text
            onSelect = { actions.setLighting(lighting) }
        }
}
