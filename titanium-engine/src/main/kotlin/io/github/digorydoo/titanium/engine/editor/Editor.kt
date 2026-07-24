package io.github.digorydoo.titanium.engine.editor

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.vector.MutableVector3i
import io.github.digorydoo.titanium.BuildConfig
import io.github.digorydoo.titanium.engine.brick.BrickVolume.Companion.worldToBrick
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.core.GameLoop
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.editor.cursor.CursorGelHolder
import io.github.digorydoo.titanium.engine.editor.menu.EditorMenu
import io.github.digorydoo.titanium.engine.sound.EngineSampleId

class Editor: GameLoop.Tick {
    private val state = EditorState()
    private val hud = EditorHUD(state)
    private val cursor = CursorGelHolder(state)
    private val brickSelection = BrickSelection(cursor)
    private val heightMapSelection = HeightMapSelection(cursor, state)
    private val undoStack = UndoStack(hud, brickSelection)
    private val clipboard = Clipboard(hud, brickSelection, undoStack)
    private val actions = EditorActions(cursor, hud, brickSelection, heightMapSelection, state, undoStack)
    private val menu = EditorMenu(state, brickSelection, actions)

    private val keyboardHandler =
        KeyboardHandler(actions, brickSelection, clipboard, heightMapSelection, menu, state, undoStack)

    var isShown = false; private set

    override fun tick(token: GameLoop.Token) {
        if (BuildConfig.isProduction) {
            return
        }

        val input = App.input

        if (input.ctrlIsDown && input.checkPressedOnce('e')) {
            if (isShown) {
                hide()
            } else {
                App.dlg.cancelActiveDlg() // e.g. dismiss start menu when editor is activated in the StartScene
                App.gameMenu.dismiss()
                show()
            }
        }

        if (!isShown || App.dlg.isInDlgMode) {
            return
        }

        try {
            keyboardHandler.handleKeys()
        } catch (e: Throwable) {
            Log.error(TAG, "Exception: ${e.message}\n${e.stackTraceToString()}")
        }
    }

    private fun show() {
        if (isShown) {
            return
        }

        App.sound.play(EngineSampleId.MENU_ENTER)
        cursor.createGels()

        val player = App.player
        val brickCoords = MutableVector3i()
        worldToBrick(player?.pos ?: App.camera.targetPos, brickCoords)
        App.bricks.clampToSize(brickCoords)
        brickSelection.set(brickCoords.x, brickCoords.y, brickCoords.z + 1)

        hud.show()
        isShown = true
    }

    private fun hide() {
        if (!isShown) {
            return
        }

        App.sound.play(EngineSampleId.MENU_LEAVE)
        actions.movePlayerToCursorPos()
        cursor.destroyGels()
        App.camera.setTarget(App.player)

        hud.hide()
        isShown = false
        state.setBricksEditMode()
    }

    companion object {
        private val TAG = Log.Tag("Editor")
    }
}
