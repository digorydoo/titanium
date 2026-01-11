package ch.digorydoo.titanium.game.ui.options

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.SaveGameFileReader
import ch.digorydoo.titanium.engine.i18n.EngineTextId

// Note: There are two instances of this menu: One is in StartScene, and one is in OptionsPage.
class LoadGameMenu {
    fun show(onDidLoad: () -> Unit, onCancel: () -> Unit) {
        App.dlg.showDlg<Unit> {
            SaveGameFileReader.listSummaries()
                .reversed()
                .forEach { summary ->
                    itemForSavegame {
                        this.summary = summary
                        onSelect = {
                            App.state.loadFromFile(summary.fileName)
                            onDidLoad()
                        }
                    }
                }

            focus = items.lastOrNull()

            dismiss = item {
                textId = EngineTextId.CANCEL
                onSelect = onCancel
            }
        }
    }
}
