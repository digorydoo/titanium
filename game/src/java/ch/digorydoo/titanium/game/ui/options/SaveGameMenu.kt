package ch.digorydoo.titanium.game.ui.options

import ch.digorydoo.kutils.utils.Moment
import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.behaviours.Align.Anchor.TOP_CENTRE
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.SaveGameFileWriter
import ch.digorydoo.titanium.engine.file.SaveGameFileWriter.Summary
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.texture.ImageData
import ch.digorydoo.titanium.engine.ui.SAVEGAME_THUMBNAIL_HEIGHT
import ch.digorydoo.titanium.engine.ui.SAVEGAME_THUMBNAIL_WIDTH
import ch.digorydoo.titanium.engine.ui.SUMMARY_BTN_HEIGHT
import ch.digorydoo.titanium.engine.ui.SUMMARY_BTN_MARGIN_TOP
import ch.digorydoo.titanium.engine.ui.SUMMARY_BTN_WIDTH
import ch.digorydoo.titanium.engine.ui.dialogue.DlgSavegameItemDef
import ch.digorydoo.titanium.engine.ui.dlg_item.DlgSavegameItemGel
import ch.digorydoo.titanium.game.i18n.GameTextId

class SaveGameMenu {
    fun show(onDidSave: () -> Unit, onCancel: () -> Unit) {
        val thumbnail = App.gameMenu.screenshotWhenOpened?.let { screenshot ->
            ImageData(ImageData.Type.RGB8, SAVEGAME_THUMBNAIL_WIDTH, SAVEGAME_THUMBNAIL_HEIGHT).apply {
                drawImageScaled(screenshot, 0, 0, width, height, antiAliasing = true)
            }
        }

        val summary = object: Summary() {
            override val fileName = SaveGameFileWriter.getNewFileName()
            override val sceneTitle = App.i18n.getString(App.scene.title)
            override val saveDate = Moment().formatRevDateTime()
            override val screenshot = thumbnail
        }

        val summaryBtn = DlgSavegameItemGel<Unit>(
            def = DlgSavegameItemDef.build {
                this.summary = summary
            },
            alignment = Align.Alignment(anchor = TOP_CENTRE, marginTop = SUMMARY_BTN_MARGIN_TOP),
            btnWidth = SUMMARY_BTN_WIDTH,
            btnHeight = SUMMARY_BTN_HEIGHT,
            precomputedTextTex = null,
        )
        summaryBtn.onCreate(LayerKind.UI_BELOW_DLG)

        App.dlg.showTwoWayDlg(
            "",
            confirm = GameTextId.SAVE_GAME,
            deny = EngineTextId.CANCEL,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
            onConfirm = {
                if (App.state.saveToFile(summary)) {
                    App.dlg.showSnackbar(EngineTextId.GAME_SAVED)
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
