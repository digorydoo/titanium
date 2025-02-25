package ch.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.core.GameTime
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.scene.Lighting
import ch.digorydoo.titanium.engine.scene.MutableLighting
import ch.digorydoo.titanium.engine.ui.choice.TextChoice

class LightingMenu(private val actions: EditorActions) {
    fun show(isTopLevel: Boolean, onCancel: () -> Unit) {
        show(playSoundOnOpen = isTopLevel, playSoundOnDismiss = isTopLevel, onCancel)
    }

    private fun show(playSoundOnOpen: Boolean, playSoundOnDismiss: Boolean, onCancel: () -> Unit) {
        fun choice(text: String, lgt: Lighting?) =
            TextChoice(text, autoDismiss = false) { actions.setLighting(lgt) }

        val fineDay1000 = MutableLighting(Lighting.fineDay1200).apply { adaptToRelTime(GameTime.relTime(10, 0)) }

        val choices = listOf(
            choice("Follow story time", null),
            choice("Fine day 02:00", Lighting.fineDay0200),
            choice("Fine day 06:00", Lighting.fineDay0600),
            choice("Fine day 10:00", fineDay1000),
            choice("Fine day 12:00", Lighting.fineDay1200),
            choice("Fine day 18:00", Lighting.fineDay1800),
            choice("Fine day 22:00", Lighting.fineDay2200),
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
