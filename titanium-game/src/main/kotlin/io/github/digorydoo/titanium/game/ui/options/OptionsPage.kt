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
    private val btnArea = ButtonArea<Unit>(marginLeft = BTN_AREA_LEFT, marginTop = BTN_AREA_TOP)
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
        saveGameMenu.show(onDidSave = { App.gameMenu.dismiss() }, onCancel = { btnArea.showAll() })
    }

    private fun loadBtnClicked() {
        btnArea.hideAll()
        loadGameMenu.show(onDidLoad = { App.gameMenu.dismiss() }, onCancel = { btnArea.showAll() })
    }

    private fun settingsBtnClicked() {
        // We close the GameMenu first, because the menu needs to be rebuilt when the user changes the language.
        btnArea.hideAll()
        prefsMenu.show(onDone = { btnArea.showAll() })
    }

    private fun exitBtnClicked() {
        btnArea.hideAll()

        App.dlg.showTwoWayDlg(
            GameTextId.CONFIRM_EXIT_TO_TITLE,
            confirm = GameTextId.EXIT_TO_TITLE,
            deny = EngineTextId.CANCEL,
            onConfirm = {
                App.gameMenu.dismiss()
                App.sceneLoader.load(StartScene())
            },
            onDeny = {
                btnArea.showAll()
            }
        )
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
            input.isPressedOnce(GamepadBtn.LJOY_UP) -> btnArea.hilitePrev()
            input.isPressedOnce(GamepadBtn.LJOY_DOWN) -> btnArea.hiliteNext()
            input.hatOrArrowUp.pressedWithRepeat -> btnArea.hilitePrev()
            input.hatOrArrowDown.pressedWithRepeat -> btnArea.hiliteNext()
            input.selectBtn.pressedOnce -> btnArea.selectHilited()
        }
    }

    companion object {
        private const val BTN_AREA_LEFT = 64
        private const val BTN_AREA_TOP = TOP_AREA_HEIGHT + 64
    }
}
