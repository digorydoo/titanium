package io.github.digorydoo.titanium.game.ui.options

import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.behaviours.Align.Anchor.TOP_CENTRE
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.file.SaveGameFileWriter
import io.github.digorydoo.titanium.engine.file.SaveGameFileWriter.Summary
import io.github.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import io.github.digorydoo.titanium.engine.intermission.Intermission
import io.github.digorydoo.titanium.engine.texture.ImageData
import io.github.digorydoo.titanium.engine.ui.SAVEGAME_THUMBNAIL_HEIGHT
import io.github.digorydoo.titanium.engine.ui.SAVEGAME_THUMBNAIL_WIDTH
import io.github.digorydoo.titanium.engine.ui.SUMMARY_BTN_HEIGHT
import io.github.digorydoo.titanium.engine.ui.SUMMARY_BTN_MARGIN_TOP
import io.github.digorydoo.titanium.engine.ui.SUMMARY_BTN_WIDTH
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgSavegameItemDef
import io.github.digorydoo.titanium.engine.ui.dlg_item.DlgSavegameItemGel
import io.github.digorydoo.titanium.game.i18n.GameTextId

class SaveGameMenu {
    enum class Result { DID_SAVE, CANCEL }

    suspend fun Intermission.show(): Result {
        val summary = getSummary()
        val thumbnailGel = getThumbnailGel(summary)

        val selected = showDlg {
            suppressSoundsOnShowAndDismiss = true
            item { textId = GameTextId.SAVE_GAME }
            dismiss = item { textId = EngineTextId.CANCEL }
        }

        return when (selected.textId) {
            GameTextId.SAVE_GAME -> {
                if (App.state.saveToFile(summary)) {
                    App.dlg.showSnackbar(EngineTextId.GAME_SAVED)
                }
                thumbnailGel.setZombie()
                Result.DID_SAVE
            }
            else -> {
                thumbnailGel.setZombie()
                Result.CANCEL
            }
        }
    }

    private fun getSummary(): Summary {
        val thumbnail = App.gameMenu.screenshotWhenOpened?.let { screenshot ->
            ImageData(ImageData.Type.RGB8, SAVEGAME_THUMBNAIL_WIDTH, SAVEGAME_THUMBNAIL_HEIGHT).apply {
                drawImageScaled(screenshot, 0, 0, width, height, antiAliasing = true)
            }
        }

        return object: Summary() {
            override val fileName = SaveGameFileWriter.getNewFileName()
            override val sceneTitle = App.i18n.getString(App.scene.title)
            override val saveDate = Moment.now().formatAsZoneAgnosticDateTime()
            override val screenshot = thumbnail
        }
    }

    private fun getThumbnailGel(smry: Summary): DlgSavegameItemGel {
        return DlgSavegameItemGel(
            def = DlgSavegameItemDef.fromSummary(smry),
            alignment = Align.Alignment(anchor = TOP_CENTRE, marginTop = SUMMARY_BTN_MARGIN_TOP),
            btnWidth = SUMMARY_BTN_WIDTH,
            btnHeight = SUMMARY_BTN_HEIGHT,
            precomputedTextTex = null,
        ).apply {
            onCreate(LayerKind.UI_BELOW_DLG)
        }
    }
}
