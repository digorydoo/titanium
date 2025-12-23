package ch.digorydoo.titanium.engine.editor

import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.BuildConfig
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.worldToBrick
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.editor.cursor.CursorGelHolder
import ch.digorydoo.titanium.engine.editor.menu.EditorMenu
import ch.digorydoo.titanium.engine.sound.EngineSampleId

class Editor {
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

    fun animate() {
        if (BuildConfig.isProduction) {
            return
        }

        val input = App.input

        if (input.ctrlPressed && input.isPressedOnce('e')) {
            if (isShown) {
                hide()
            } else {
                App.dlg.dismiss() // e.g. dismiss start menu when editor is activated in the StartScene
                App.gameMenu.dismiss()
                show()
            }
        }

        if (!isShown || App.dlg.hasActiveDlg) {
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
        val brickCoords = MutablePoint3i()
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
