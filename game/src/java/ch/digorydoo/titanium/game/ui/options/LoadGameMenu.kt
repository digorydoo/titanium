package ch.digorydoo.titanium.game.ui.options

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.SaveGameFileReader
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.ui.choice.Choice
import ch.digorydoo.titanium.engine.ui.choice.SavegameChoice
import ch.digorydoo.titanium.engine.ui.choice.TextChoice
import kotlin.math.max

// Note: There are two instances of this menu: One is in StartScene, and one is in OptionsPage.
class LoadGameMenu {
    fun show(onDidLoad: () -> Unit, onCancel: () -> Unit) {
        val choices: MutableList<Choice> = SaveGameFileReader.listSummaries()
            .reversed()
            .map { summary ->
                SavegameChoice(summary) {
                    App.state.loadFromFile(summary.fileName)
                    onDidLoad()
                }
            }
            .toMutableList()

        choices.add(TextChoice(EngineTextId.CANCEL, onCancel))

        App.dlg.showChoices(
            choices,
            initHilitedIdx = max(0, choices.size - 2),
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
    }
}
