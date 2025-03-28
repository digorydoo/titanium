package ch.digorydoo.titanium.engine.editor

import ch.digorydoo.kutils.math.normAngle
import ch.digorydoo.kutils.point.MutablePoint2i
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point2i
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.worldToBrick
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.editor.cursor.CursorGelHolder
import ch.digorydoo.titanium.engine.editor.menu.EditorMenu
import ch.digorydoo.titanium.engine.editor.statusbar.EditorStatusBar
import ch.digorydoo.titanium.engine.input.keyboard.KeyboardKey
import ch.digorydoo.titanium.engine.sound.EngineSampleId

class Editor {
    private val cursor = CursorGelHolder()
    private val status = EditorStatusBar()
    private val selection = Selection(::selectionChanged)
    private val undoStack = UndoStack(status, selection)
    private val clipboard = Clipboard(status, selection, undoStack)
    private val actions = EditorActions(cursor, selection, status, undoStack)
    private val menu = EditorMenu(status, selection, actions)

    var isShown = false; private set

    fun animate() {
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

        input.apply {
            when {
                altPressed -> when {
                    ctrlPressed -> Unit
                    shiftPressed -> Unit
                    isPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> moveSelection(0, -1, 0)
                    isPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> moveSelection(0, 1, 0)
                    isPressedWithRepeat(KeyboardKey.ARROW_UP) -> moveSelection(-1, 0, 0)
                    isPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> moveSelection(1, 0, 0)
                    isPressedWithRepeat(KeyboardKey.KEYPAD_MINUS) -> moveSelection(0, 0, -1)
                    isPressedWithRepeat(KeyboardKey.KEYPAD_PLUS) -> moveSelection(0, 0, 1)
                    isPressedWithRepeat(KeyboardKey.PAGE_UP) -> moveSelection(-20, 0, 0)
                    isPressedWithRepeat(KeyboardKey.PAGE_DOWN) -> moveSelection(20, 0, 0)
                    isPressedWithRepeat(KeyboardKey.HOME) -> moveSelection(0, -20, 0)
                    isPressedWithRepeat(KeyboardKey.END) -> moveSelection(0, 20, 0)
                }
                ctrlPressed -> when {
                    shiftPressed -> when {
                        // CTRL+SHIFT
                        isPressedOnce('t') -> actions.jumpToPrevNextSpawnPt(false)
                        isPressedOnce('z') -> undoStack.redo()
                    }
                    else -> when {
                        // CTRL
                        isPressedOnce('c') -> clipboard.copy()
                        isPressedOnce('i') -> actions.printInfo()
                        isPressedOnce('m') -> actions.applyMaterialToSelectedBricks()
                        isPressedOnce('n') -> actions.applyShapeToSelectedBricks()
                        isPressedOnce('r') -> actions.rotateSelection()
                        isPressedOnce('s') -> actions.saveToFile()
                        isPressedOnce('t') -> actions.jumpToPrevNextSpawnPt(true)
                        isPressedOnce('v') -> clipboard.paste()
                        isPressedOnce('x') -> clipboard.cut()
                        isPressedOnce('z') -> undoStack.undo()
                    }
                }
                else -> when {
                    shiftPressed -> when {
                        // SHIFT
                        isPressedOnce('q') -> actions.addAnotherSpawnPt()
                        isPressedOnce('y') -> actions.switchCameraTarget(true)
                        isPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> extendSelection(0, -1, 0)
                        isPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> extendSelection(0, 1, 0)
                        isPressedWithRepeat(KeyboardKey.ARROW_UP) -> extendSelection(-1, 0, 0)
                        isPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> extendSelection(1, 0, 0)
                        isPressedWithRepeat(KeyboardKey.KEYPAD_MINUS) -> extendSelection(0, 0, -1)
                        isPressedWithRepeat(KeyboardKey.KEYPAD_PLUS) -> extendSelection(0, 0, 1)
                        isPressedWithRepeat(KeyboardKey.PAGE_UP) -> extendSelection(-20, 0, 0)
                        isPressedWithRepeat(KeyboardKey.PAGE_DOWN) -> extendSelection(20, 0, 0)
                        isPressedWithRepeat(KeyboardKey.HOME) -> extendSelection(0, -20, 0)
                        isPressedWithRepeat(KeyboardKey.END) -> extendSelection(0, 20, 0)
                    }
                    else -> when {
                        // all modifiers up
                        isPressedOnce(KeyboardKey.BACKSPACE) -> actions.removeSelectedBricks()
                        isPressedOnce(KeyboardKey.ENTER) -> actions.movePlayerToCursorPos()
                        isPressedOnce(KeyboardKey.ESCAPE) -> menu.showMainMenu()
                        isPressedOnce(KeyboardKey.FWDDEL) -> actions.removeSelectedBricks()
                        isPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> collapseSelectionAndMove(0, -1, 0)
                        isPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> collapseSelectionAndMove(0, 1, 0)
                        isPressedWithRepeat(KeyboardKey.ARROW_UP) -> collapseSelectionAndMove(-1, 0, 0)
                        isPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> collapseSelectionAndMove(1, 0, 0)
                        isPressedWithRepeat(KeyboardKey.KEYPAD_MINUS) -> collapseSelectionAndMove(0, 0, -1)
                        isPressedWithRepeat(KeyboardKey.KEYPAD_PLUS) -> collapseSelectionAndMove(0, 0, 1)
                        isPressedWithRepeat(KeyboardKey.PAGE_UP) -> collapseSelectionAndMove(-20, 0, 0)
                        isPressedWithRepeat(KeyboardKey.PAGE_DOWN) -> collapseSelectionAndMove(20, 0, 0)
                        isPressedWithRepeat(KeyboardKey.HOME) -> collapseSelectionAndMove(0, -20, 0)
                        isPressedWithRepeat(KeyboardKey.END) -> collapseSelectionAndMove(0, 20, 0)
                        isPressedOnce('c') -> menu.showCameraModeMenu()
                        isPressedOnce('m') -> menu.showMaterialMenu()
                        isPressedOnce('n') -> menu.showShapeMenu()
                        isPressedOnce('p') -> actions.pickShapeMaterial()
                        isPressedOnce('q') -> actions.setShapeAndMaterialOfSelectedBricks()
                        isPressedOnce('t') -> menu.showSpawnPtMenu()
                        isPressedOnce('w') -> menu.showWizardMenu()
                        isPressedOnce('y') -> actions.switchCameraTarget(false)
                    }
                }
            }
        }
    }

    private fun moveSelection(dx: Int, dy: Int, dz: Int) {
        App.sound.play(EngineSampleId.HILITE1)
        val dir = dirWithCameraCorrection(dx, dy)
        selection.move(dir.x, dir.y, dz)
    }

    private fun extendSelection(dx: Int, dy: Int, dz: Int) {
        App.sound.play(EngineSampleId.HILITE1)
        val dir = dirWithCameraCorrection(dx, dy)
        selection.extend(dir.x, dir.y, dz)
    }

    private fun collapseSelectionAndMove(dx: Int, dy: Int, dz: Int) {
        App.sound.play(EngineSampleId.HILITE1)
        val dir = dirWithCameraCorrection(dx, dy)
        selection.collapseAndMove(dir.x, dir.y, dz)
    }

    private fun dirWithCameraCorrection(dx: Int, dy: Int): Point2i {
        val dir = MutablePoint2i()
        val phi = normAngle(App.camera.currentPhi) // -PI..+PI

        when {
            phi >= Math.PI * (3.0 / 4.0) -> dir.set(-dy, dx)
            phi >= Math.PI * (1 / 4.0) -> dir.set(-dx, -dy)
            phi >= -Math.PI * (1 / 4.0) -> dir.set(dy, -dx)
            phi >= -Math.PI * (3.0 / 4.0) -> dir.set(dx, dy)
            else -> dir.set(-dy, dx)
        }
        return dir
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
        selection.set(brickCoords.x, brickCoords.y, brickCoords.z + 1)

        status.show()
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

        status.hide()
        isShown = false
    }

    private fun selectionChanged() {
        cursor.updateGels(selection.get())
        val p = selection.getPosCentreInWorldCoords()
        App.camera.setTarget(p)
    }
}
