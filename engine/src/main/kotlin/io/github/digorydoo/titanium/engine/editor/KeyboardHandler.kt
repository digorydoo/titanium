package io.github.digorydoo.titanium.engine.editor

import ch.digorydoo.kutils.math.normAngle
import ch.digorydoo.kutils.vector.MutableVector2i
import ch.digorydoo.kutils.vector.Vector2i
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.editor.EditorState.EditMode
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.editor.menu.EditorMenu
import io.github.digorydoo.titanium.engine.input.keyboard.KeyboardKey
import io.github.digorydoo.titanium.engine.sound.EngineSampleId
import kotlin.math.PI

internal class KeyboardHandler(
    private val actions: EditorActions,
    private val brickSelection: BrickSelection,
    private val clipboard: Clipboard,
    private val heightMapSelection: HeightMapSelection,
    private val menu: EditorMenu,
    private val state: EditorState,
    private val undoStack: UndoStack,
) {
    fun handleKeys() {
        when (state.editMode) {
            EditMode.BRICKS -> handleKeysInBricksMode()
            EditMode.HEIGHT_MAP -> handleKeysInHeightMapMode()
        }

        handleCommonKeys()
    }

    private fun handleKeysInBricksMode() {
        App.input.apply {
            when {
                altPressed -> when {
                    ctrlPressed -> Unit
                    shiftPressed -> Unit
                    // ALT
                    isPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> moveBrickSelection(0, -1, 0)
                    isPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> moveBrickSelection(0, 1, 0)
                    isPressedWithRepeat(KeyboardKey.ARROW_UP) -> moveBrickSelection(-1, 0, 0)
                    isPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> moveBrickSelection(1, 0, 0)
                    isPressedWithRepeat(KeyboardKey.KEYPAD_MINUS) -> moveBrickSelection(0, 0, -1)
                    isPressedWithRepeat(KeyboardKey.KEYPAD_PLUS) -> moveBrickSelection(0, 0, 1)
                    isPressedWithRepeat(KeyboardKey.PAGE_UP) -> moveBrickSelection(-20, 0, 0)
                    isPressedWithRepeat(KeyboardKey.PAGE_DOWN) -> moveBrickSelection(20, 0, 0)
                    isPressedWithRepeat(KeyboardKey.HOME) -> moveBrickSelection(0, -20, 0)
                    isPressedWithRepeat(KeyboardKey.END) -> moveBrickSelection(0, 20, 0)
                }
                ctrlPressed -> when {
                    shiftPressed -> Unit
                    else -> when {
                        // CTRL
                        isPressedOnce('i') -> actions.printInfo()
                        isPressedOnce('m') -> actions.applyMaterialToSelectedBricks()
                        isPressedOnce('n') -> actions.applyShapeToSelectedBricks()
                        isPressedOnce('r') -> actions.rotateSelection()
                    }
                }
                shiftPressed -> when {
                    // SHIFT
                    isPressedOnce('q') -> actions.addAnotherSpawnPt()
                    isPressedOnce(KeyboardKey.TAB) -> actions.jumpToPrevSpawnPt()
                    isPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> extendBrickSelection(0, -1, 0)
                    isPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> extendBrickSelection(0, 1, 0)
                    isPressedWithRepeat(KeyboardKey.ARROW_UP) -> extendBrickSelection(-1, 0, 0)
                    isPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> extendBrickSelection(1, 0, 0)
                    isPressedWithRepeat(KeyboardKey.KEYPAD_MINUS) -> extendBrickSelection(0, 0, -1)
                    isPressedWithRepeat(KeyboardKey.KEYPAD_PLUS) -> extendBrickSelection(0, 0, 1)
                    isPressedWithRepeat(KeyboardKey.PAGE_UP) -> extendBrickSelection(-20, 0, 0)
                    isPressedWithRepeat(KeyboardKey.PAGE_DOWN) -> extendBrickSelection(20, 0, 0)
                    isPressedWithRepeat(KeyboardKey.HOME) -> extendBrickSelection(0, -20, 0)
                    isPressedWithRepeat(KeyboardKey.END) -> extendBrickSelection(0, 20, 0)
                }
                // all modifiers up
                isPressedOnce(KeyboardKey.BACKSPACE) -> actions.removeSelectedBricks()
                isPressedOnce(KeyboardKey.ENTER) -> actions.movePlayerToCursorPos()
                isPressedOnce(KeyboardKey.ESCAPE) -> menu.showMainMenu()
                isPressedOnce(KeyboardKey.FWDDEL) -> actions.removeSelectedBricks()
                isPressedOnce(KeyboardKey.TAB) -> actions.jumpToNextSpawnPt()
                isPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> collapseBrickSelectionAndMove(0, -1, 0)
                isPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> collapseBrickSelectionAndMove(0, 1, 0)
                isPressedWithRepeat(KeyboardKey.ARROW_UP) -> collapseBrickSelectionAndMove(-1, 0, 0)
                isPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> collapseBrickSelectionAndMove(1, 0, 0)
                isPressedWithRepeat(KeyboardKey.KEYPAD_MINUS) -> collapseBrickSelectionAndMove(0, 0, -1)
                isPressedWithRepeat(KeyboardKey.KEYPAD_PLUS) -> collapseBrickSelectionAndMove(0, 0, 1)
                isPressedWithRepeat(KeyboardKey.PAGE_UP) -> collapseBrickSelectionAndMove(-20, 0, 0)
                isPressedWithRepeat(KeyboardKey.PAGE_DOWN) -> collapseBrickSelectionAndMove(20, 0, 0)
                isPressedWithRepeat(KeyboardKey.HOME) -> collapseBrickSelectionAndMove(0, -20, 0)
                isPressedWithRepeat(KeyboardKey.END) -> collapseBrickSelectionAndMove(0, 20, 0)
                isPressedOnce('m') -> menu.showMaterialMenu()
                isPressedOnce('n') -> menu.showShapeMenu()
                isPressedOnce('p') -> actions.pickShapeMaterial()
                isPressedOnce('q') -> actions.setShapeAndMaterialOfSelectedBricks()
                isPressedOnce('t') -> menu.showSpawnPtMenu()
                isPressedOnce('w') -> menu.showWizardMenu()
            }
        }
    }

    private fun handleKeysInHeightMapMode() {
        App.input.apply {
            when {
                altPressed -> when {
                    ctrlPressed -> Unit
                    shiftPressed -> Unit
                    // ALT
                    isPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> moveHeightMapSelection(0, -1)
                    isPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> moveHeightMapSelection(0, 1)
                    isPressedWithRepeat(KeyboardKey.ARROW_UP) -> moveHeightMapSelection(-1, 0)
                    isPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> moveHeightMapSelection(1, 0)
                    isPressedWithRepeat(KeyboardKey.PAGE_UP) -> moveHeightMapSelection(-20, 0)
                    isPressedWithRepeat(KeyboardKey.PAGE_DOWN) -> moveHeightMapSelection(20, 0)
                    isPressedWithRepeat(KeyboardKey.HOME) -> moveHeightMapSelection(0, -20)
                    isPressedWithRepeat(KeyboardKey.END) -> moveHeightMapSelection(0, 20)

                    isPressedWithRepeat(KeyboardKey.KEYPAD_MINUS)
                    -> actions.modifyHeightOfSelectedSamples(-HM_SMALL_STEP)

                    isPressedWithRepeat(KeyboardKey.KEYPAD_PLUS)
                    -> actions.modifyHeightOfSelectedSamples(HM_SMALL_STEP)
                }
                ctrlPressed -> Unit // CTRL, CTRL+SHIFT
                shiftPressed -> when {
                    // SHIFT
                    isPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> extendHeightMapSelection(0, -1)
                    isPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> extendHeightMapSelection(0, 1)
                    isPressedWithRepeat(KeyboardKey.ARROW_UP) -> extendHeightMapSelection(-1, 0)
                    isPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> extendHeightMapSelection(1, 0)
                    isPressedWithRepeat(KeyboardKey.PAGE_UP) -> extendHeightMapSelection(-20, 0)
                    isPressedWithRepeat(KeyboardKey.PAGE_DOWN) -> extendHeightMapSelection(20, 0)
                    isPressedWithRepeat(KeyboardKey.HOME) -> extendHeightMapSelection(0, -20)
                    isPressedWithRepeat(KeyboardKey.END) -> extendHeightMapSelection(0, 20)
                }
                // all modifiers up
                isPressedOnce(KeyboardKey.ESCAPE) -> state.heightMapSpawnPt?.let { menu.showEditSpawnPtMenu(it) }
                isPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> collapseHeightMapSelectionAndMove(0, -1)
                isPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> collapseHeightMapSelectionAndMove(0, 1)
                isPressedWithRepeat(KeyboardKey.ARROW_UP) -> collapseHeightMapSelectionAndMove(-1, 0)
                isPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> collapseHeightMapSelectionAndMove(1, 0)
                isPressedWithRepeat(KeyboardKey.KEYPAD_MINUS) -> actions.modifyHeightOfSelectedSamples(-HM_LARGE_STEP)
                isPressedWithRepeat(KeyboardKey.KEYPAD_PLUS) -> actions.modifyHeightOfSelectedSamples(HM_LARGE_STEP)
            }
        }
    }

    private fun handleCommonKeys() {
        App.input.apply {
            when {
                altPressed -> Unit // ALT, ALT+CTRL, ALT+SHIFT
                ctrlPressed -> when {
                    shiftPressed -> when {
                        // CTRL+SHIFT
                        isPressedOnce('z') -> undoStack.redo()
                    }
                    else -> when {
                        // CTRL
                        isPressedOnce('c') -> clipboard.copy()
                        isPressedOnce('s') -> actions.saveToFile()
                        isPressedOnce('v') -> clipboard.paste()
                        isPressedOnce('x') -> clipboard.cut()
                        isPressedOnce('z') -> undoStack.undo()
                    }
                }
                shiftPressed -> when {
                    // SHIFT
                    isPressedOnce('y') -> actions.switchCameraTarget(true)
                }
                // all modifiers up
                isPressedOnce('c') -> menu.showCameraModeMenu()
                isPressedOnce('y') -> actions.switchCameraTarget(false)
            }
        }
    }

    private fun moveBrickSelection(dx: Int, dy: Int, dz: Int) {
        App.sound.play(EngineSampleId.HILITE1)
        val dir = dirWithCameraCorrection(dx, dy)
        brickSelection.move(dir.x, dir.y, dz)
    }

    private fun extendBrickSelection(dx: Int, dy: Int, dz: Int) {
        App.sound.play(EngineSampleId.HILITE1)
        val dir = dirWithCameraCorrection(dx, dy)
        brickSelection.extend(dir.x, dir.y, dz)
    }

    private fun collapseBrickSelectionAndMove(dx: Int, dy: Int, dz: Int) {
        App.sound.play(EngineSampleId.HILITE1)
        val dir = dirWithCameraCorrection(dx, dy)
        brickSelection.collapseAndMove(dir.x, dir.y, dz)
    }

    private fun moveHeightMapSelection(dx: Int, dy: Int) {
        App.sound.play(EngineSampleId.HILITE1)
        val dir = dirWithCameraCorrection(dx, dy)
        heightMapSelection.move(dir.x, dir.y)
    }

    private fun extendHeightMapSelection(dx: Int, dy: Int) {
        App.sound.play(EngineSampleId.HILITE1)
        val dir = dirWithCameraCorrection(dx, dy)
        heightMapSelection.extend(dir.x, dir.y)
    }

    private fun collapseHeightMapSelectionAndMove(dx: Int, dy: Int) {
        App.sound.play(EngineSampleId.HILITE1)
        val dir = dirWithCameraCorrection(dx, dy)
        heightMapSelection.collapseAndMove(dir.x, dir.y)
    }

    private fun dirWithCameraCorrection(dx: Int, dy: Int): Vector2i {
        val dir = MutableVector2i()
        val phi = normAngle(App.camera.currentPhi) // -PI..+PI

        when {
            phi >= PI * (3.0 / 4.0) -> dir.set(-dy, dx)
            phi >= PI * (1 / 4.0) -> dir.set(-dx, -dy)
            phi >= -PI * (1 / 4.0) -> dir.set(dy, -dx)
            phi >= -PI * (3.0 / 4.0) -> dir.set(dx, dy)
            else -> dir.set(-dy, dx)
        }
        return dir
    }

    companion object {
        private const val HM_LARGE_STEP = 0.5f
        private const val HM_SMALL_STEP = 0.1f
    }
}
