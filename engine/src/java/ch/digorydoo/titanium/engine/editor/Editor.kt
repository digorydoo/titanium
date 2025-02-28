package ch.digorydoo.titanium.engine.editor

import ch.digorydoo.kutils.math.normAngle
import ch.digorydoo.kutils.point.MutablePoint2i
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.worldToBrick
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.editor.cursor.CursorGelHolder
import ch.digorydoo.titanium.engine.editor.menu.EditorMenu
import ch.digorydoo.titanium.engine.editor.statusbar.EditorStatusBar
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
        val input = App.input.values

        if (input.editorToggle.pressedOnce) {
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

        var cursorDx = 0
        var cursorDy = 0
        var cursorDz = 0

        when {
            input.enter.pressedOnce -> actions.movePlayerToCursorPos()

            input.hatLeft.pressedWithRepeat -> cursorDy = -1 // west
            input.hatRight.pressedWithRepeat -> cursorDy = 1 // east
            input.hatUp.pressedWithRepeat -> cursorDx = -1  // north
            input.hatDown.pressedWithRepeat -> cursorDx = 1 // south

            input.editorPosZDec.pressedWithRepeat -> cursorDz = -1 // down
            input.editorPosZInc.pressedWithRepeat -> cursorDz = 1 // up

            input.editorPageUp.pressedWithRepeat -> cursorDx = -10 // page north
            input.editorPageDown.pressedWithRepeat -> cursorDx = 10 // page south
            input.editorPageLeft.pressedWithRepeat -> cursorDy = -10 // page west
            input.editorPageRight.pressedWithRepeat -> cursorDy = 10 // page east

            input.editorUndo.pressedOnce -> if (input.shift.pressed) undoStack.redo() else undoStack.undo()
            input.editorCut.pressedOnce -> clipboard.cut()
            input.editorCopy.pressedOnce -> clipboard.copy()
            input.editorPaste.pressedOnce -> clipboard.paste()
            input.editorBackspace.pressedOnce -> actions.removeSelectedBricks()

            input.editorDraw.pressedOnce -> {
                if (input.shift.pressed) {
                    actions.addAnotherSpawnPt()
                } else {
                    actions.setShapeAndMaterialOfSelectedBricks()
                }
            }

            input.editorSave.pressedOnce -> actions.saveToFile()
            input.editorSwitchCameraTarget.pressedOnce -> actions.switchCameraTarget(input.shift.pressed)
            input.editorInfo.pressedOnce -> actions.printInfo()
            input.editorRotate.pressedOnce -> actions.rotateSelection()

            input.editorNextSpawnPt.pressedOnce -> actions.jumpToPrevNextSpawnPt(!input.shift.pressed)

            input.escape.pressedOnce -> menu.showMainMenu()
            input.editorCameraMode.pressedOnce -> menu.showCameraModeMenu()
            input.editorShapeMenu.pressedOnce -> menu.showShapeMenu()
            input.editorMaterialMenu.pressedOnce -> menu.showMaterialMenu()
            input.editorSpawnPtMenu.pressedOnce -> menu.showSpawnPtMenu()
            input.editorWizardMenu.pressedOnce -> menu.showWizardMenu()

            input.editorApplyShape.pressedOnce -> actions.applyShapeToSelectedBricks()
            input.editorApplyMaterial.pressedOnce -> actions.applyMaterialToSelectedBricks()
            input.editorPickShapeMaterial.pressedOnce -> actions.pickShapeMaterial()
        }

        if (cursorDx != 0 || cursorDy != 0 || cursorDz != 0) {
            // Apply camera correction to direction (dx, dy)

            App.sound.play(EngineSampleId.HILITE1)
            val dr = MutablePoint2i()
            val phi = normAngle(App.camera.currentPhi) // -PI..+PI

            when {
                phi >= Math.PI * (3.0 / 4.0) -> dr.set(-cursorDy, cursorDx)
                phi >= Math.PI * (1 / 4.0) -> dr.set(-cursorDx, -cursorDy)
                phi >= -Math.PI * (1 / 4.0) -> dr.set(cursorDy, -cursorDx)
                phi >= -Math.PI * (3.0 / 4.0) -> dr.set(cursorDx, cursorDy)
                else -> dr.set(-cursorDy, cursorDx)
            }

            when {
                input.alt.pressed -> selection.move(dr.x, dr.y, cursorDz)
                input.shift.pressed -> selection.extend(dr.x, dr.y, cursorDz)
                else -> selection.collapseAndMove(dr.x, dr.y, cursorDz)
            }
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
