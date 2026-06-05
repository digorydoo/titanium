package io.github.digorydoo.titanium.engine.editor.menu

import io.github.digorydoo.titanium.engine.core.GameTime
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import io.github.digorydoo.titanium.engine.intermission.Intermission
import io.github.digorydoo.titanium.engine.scene.Lighting
import io.github.digorydoo.titanium.engine.scene.MutableLighting

internal class LightingMenu(private val actions: EditorActions) {
    private val fineDay1000 = MutableLighting(Lighting.fineDay1200).apply { adaptToRelTime(GameTime.relTime(10, 0)) }

    suspend fun Intermission.show() {
        showImpl(/* hasParentMenu = true */)
    }

    private suspend fun Intermission.showImpl() {
        val lightings = arrayOf(
            Pair("Follow story time", null),
            Pair("Fine day 02:00", Lighting.fineDay0200),
            Pair("Fine day 06:00", Lighting.fineDay0600),
            Pair("Fine day 10:00", fineDay1000),
            Pair("Fine day 12:00", Lighting.fineDay1200),
            Pair("Fine day 18:00", Lighting.fineDay1800),
            Pair("Fine day 22:00", Lighting.fineDay2200),
        )

        do {
            var reopen = false

            showDlg {
                lightings.forEach { (t, l) ->
                    item {
                        text = t
                        onSelect = {
                            actions.setLighting(l)
                            reopen = true
                        }
                    }
                }
                dismiss = item { textId = EngineTextId.BACK }
            }
        } while (reopen)
    }
}
