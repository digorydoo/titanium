package ch.digorydoo.titanium.game.ui.options

import ch.digorydoo.kutils.utils.Moment
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.behaviours.Align.Anchor.TOP_CENTRE
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.SaveGameFileWriter
import ch.digorydoo.titanium.engine.file.SaveGameFileWriter.Summary
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.ui.SUMMARY_BTN_MARGIN_TOP
import ch.digorydoo.titanium.engine.ui.button.ButtonBuilder
import ch.digorydoo.titanium.engine.ui.choice.SavegameChoice
import ch.digorydoo.titanium.game.i18n.GameTextId

class SaveGameMenu {
    fun show(onDidSave: () -> Unit, onCancel: () -> Unit) {
        var screenshot = App.gameMenu.screenshotWhenOpened

        val summary = object: Summary() {
            override val fileName = SaveGameFileWriter.getNewFileName()
            override val sceneTitle = App.i18n.getString(App.scene.title)
            override val saveDate = Moment().formatRevDateTime()
            override val screenshot get() = screenshot // a getter, because we're going to replace it (see below)
        }

        val choice = SavegameChoice(summary) {
            // Inside the SaveGameMenu, the summaryBtn is just a visual item with no action.
        }

        val alignment = Align.Alignment(anchor = TOP_CENTRE, marginTop = SUMMARY_BTN_MARGIN_TOP)
        val summaryBtn = ButtonBuilder.create(choice, alignment).also { App.content.add(it, LayerKind.UI_BELOW_DLG) }

        // The button has scaled the screenshot down to the required size. We want to store the scaled-down image in
        // the savegame, not the original one.

        summaryBtn.imageTex?.let { tex ->
            screenshot = tex.copyAsRGB8()
        }

        App.dlg.showTwoWayDlg(
            "",
            confirm = GameTextId.SAVE_GAME,
            deny = EngineTextId.CANCEL,
            playSoundOnOpen = true,
            playSoundOnDismiss = true,
            onConfirm = {
                if (App.state.saveToFile(summary)) {
                    App.dlg.showSnackbar("Game saved successfully") // TODO translate
                }
                summaryBtn.setZombie()
                onDidSave()
            },
            onDeny = {
                summaryBtn.setZombie()
                onCancel()
            }
        )
    }
}
