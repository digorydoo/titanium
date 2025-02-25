package ch.digorydoo.titanium.game.ui.options

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.prefs.PrefsMenu
import ch.digorydoo.titanium.engine.ui.button.ButtonArea
import ch.digorydoo.titanium.engine.ui.tab.MenuTabPage
import ch.digorydoo.titanium.game.i18n.GameTextId
import ch.digorydoo.titanium.game.s000_start.StartScene

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
                App.load(StartScene())
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
        if (App.dlg.hasActiveDlg) return

        val input = App.input.values

        when {
            input.hatUp.pressedOnce -> btnArea.hilitePrev()
            input.hatDown.pressedOnce -> btnArea.hiliteNext()
            input.ljoyUp.pressedOnce -> btnArea.hilitePrev()
            input.ljoyDown.pressedOnce -> btnArea.hiliteNext()
            input.enter.pressedOnce -> btnArea.selectHilited()
            input.actionA.pressedOnce -> btnArea.selectHilited()
        }
    }

    companion object {
        private const val BTN_AREA_LEFT = 64
        private const val BTN_AREA_TOP = 128
    }
}
