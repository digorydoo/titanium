package io.github.digorydoo.titanium.game.ui.options

import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import io.github.digorydoo.titanium.engine.input.gamepad.GamepadBtn
import io.github.digorydoo.titanium.engine.prefs.PrefsMenu
import io.github.digorydoo.titanium.engine.ui.button.ButtonArea
import io.github.digorydoo.titanium.engine.ui.game_menu.GameMenu.Companion.TOP_AREA_HEIGHT
import io.github.digorydoo.titanium.engine.ui.tab.MenuTabPage
import io.github.digorydoo.titanium.game.i18n.GameTextId
import io.github.digorydoo.titanium.game.s000_start.StartScene

class OptionsPage: MenuTabPage {
    private val btnArea = ButtonArea(marginLeft = BTN_AREA_LEFT, marginTop = BTN_AREA_TOP)
    private val prefsMenu = PrefsMenu()
    private val saveGameMenu = SaveGameMenu()
    private val loadGameMenu = LoadGameMenu()

    fun makeGels() {
        btnArea.apply {
            addButton(GameTextId.SAVE_GAME, ::saveBtnClicked)
            addButton(GameTextId.LOAD_GAME, ::loadBtnClicked)
            addGap()
            addButton(GameTextId.SETTINGS, ::settingsBtnClicked)
            addGap()
            addButton(GameTextId.EXIT_TO_TITLE, ::exitBtnClicked)
        }
    }

    override fun removeGels() {
        btnArea.removeGels()
    }

    private fun saveBtnClicked() {
        btnArea.hideAll()

        App.intermissions.begin {
            val result = saveGameMenu.run { show() }

            when (result) {
                SaveGameMenu.Result.DID_SAVE -> App.gameMenu.dismiss()
                SaveGameMenu.Result.CANCEL -> btnArea.showAll()
            }
        }
    }

    private fun loadBtnClicked() {
        btnArea.hideAll()

        App.intermissions.begin {
            val result = loadGameMenu.run { show() }

            when (result) {
                LoadGameMenu.Result.DID_LOAD -> App.gameMenu.dismiss()
                LoadGameMenu.Result.CANCEL -> btnArea.showAll()
            }
        }
    }

    private fun settingsBtnClicked() {
        // FIXME If the user changes the language, we should inform the game menu that it needs to rebuild itself.
        btnArea.hideAll()

        App.intermissions.begin {
            prefsMenu.run { show() }
            btnArea.showAll()
        }
    }

    private fun exitBtnClicked() {
        btnArea.hideAll()

        App.intermissions.begin {
            val selected = showDlg {
                textId = GameTextId.CONFIRM_EXIT_TO_TITLE
                item { textId = GameTextId.EXIT_TO_TITLE }
                focus = item { textId = EngineTextId.CANCEL }
                dismiss = focus
            }

            when (selected.textId) {
                GameTextId.EXIT_TO_TITLE -> {
                    App.gameMenu.dismiss()
                    App.sceneLoader.load(StartScene())
                }
                else -> btnArea.showAll()
            }
        }
    }

    override fun show() {
        btnArea.showAll()
    }

    override fun hide() {
        btnArea.hideAll()
    }

    override fun animate() {
        if (App.dlg.isInDlgMode) return

        val input = App.input

        when {
            input.checkPressedOnce(GamepadBtn.LJOY_UP) -> btnArea.hilitePrev()
            input.checkPressedOnce(GamepadBtn.LJOY_DOWN) -> btnArea.hiliteNext()
            input.hatOrArrowUp.checkPressedWithRepeat() -> btnArea.hilitePrev()
            input.hatOrArrowDown.checkPressedWithRepeat() -> btnArea.hiliteNext()
            input.selectBtn.checkPressedOnce() -> btnArea.selectHilited()
        }
    }

    companion object {
        private const val BTN_AREA_LEFT = 64
        private const val BTN_AREA_TOP = TOP_AREA_HEIGHT + 64
    }
}
