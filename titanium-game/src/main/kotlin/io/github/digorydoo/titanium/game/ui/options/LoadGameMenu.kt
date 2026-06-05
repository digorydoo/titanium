package io.github.digorydoo.titanium.game.ui.options

import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.file.SaveGameFileReader
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import io.github.digorydoo.titanium.engine.intermission.Intermission
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgSavegameItemDef

// Note: There are two instances of this menu: One is in StartScene, and one is in OptionsPage.
class LoadGameMenu {
    enum class Result { DID_LOAD, CANCEL }

    suspend fun Intermission.show(): Result {
        val selected = showDlg {
            SaveGameFileReader.listSummaries()
                .reversed()
                .forEach { item(it) }

            focus = items.lastOrNull()
            dismiss = item { textId = EngineTextId.CANCEL }
        }

        return when {
            selected is DlgSavegameItemDef -> {
                App.state.loadFromFile(selected.summary.fileName)
                Result.DID_LOAD
            }
            else -> Result.CANCEL
        }
    }
}
