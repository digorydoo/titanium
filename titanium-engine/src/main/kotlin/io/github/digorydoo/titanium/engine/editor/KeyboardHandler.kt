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
                altIsDown -> when {
                    ctrlIsDown -> Unit
                    shiftIsDown -> Unit
                    // ALT
                    checkPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> moveBrickSelection(0, -1, 0)
                    checkPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> moveBrickSelection(0, 1, 0)
                    checkPressedWithRepeat(KeyboardKey.ARROW_UP) -> moveBrickSelection(-1, 0, 0)
                    checkPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> moveBrickSelection(1, 0, 0)
                    checkPressedWithRepeat(KeyboardKey.KEYPAD_MINUS) -> moveBrickSelection(0, 0, -1)
                    checkPressedWithRepeat(KeyboardKey.KEYPAD_PLUS) -> moveBrickSelection(0, 0, 1)
                    checkPressedWithRepeat(KeyboardKey.PAGE_UP) -> moveBrickSelection(-20, 0, 0)
                    checkPressedWithRepeat(KeyboardKey.PAGE_DOWN) -> moveBrickSelection(20, 0, 0)
                    checkPressedWithRepeat(KeyboardKey.HOME) -> moveBrickSelection(0, -20, 0)
                    checkPressedWithRepeat(KeyboardKey.END) -> moveBrickSelection(0, 20, 0)
                }
                ctrlIsDown -> when {
                    shiftIsDown -> Unit
                    else -> when {
                        // CTRL
                        checkPressedOnce('i') -> actions.printInfo()
                        checkPressedOnce('m') -> actions.applyMaterialToSelectedBricks()
                        checkPressedOnce('n') -> actions.applyShapeToSelectedBricks()
                        checkPressedOnce('r') -> actions.rotateSelection()
                    }
                }
                shiftIsDown -> when {
                    // SHIFT
                    checkPressedOnce('q') -> actions.addAnotherSpawnPt()
                    checkPressedOnce(KeyboardKey.TAB) -> actions.jumpToPrevSpawnPt()
                    checkPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> extendBrickSelection(0, -1, 0)
                    checkPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> extendBrickSelection(0, 1, 0)
                    checkPressedWithRepeat(KeyboardKey.ARROW_UP) -> extendBrickSelection(-1, 0, 0)
                    checkPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> extendBrickSelection(1, 0, 0)
                    checkPressedWithRepeat(KeyboardKey.KEYPAD_MINUS) -> extendBrickSelection(0, 0, -1)
                    checkPressedWithRepeat(KeyboardKey.KEYPAD_PLUS) -> extendBrickSelection(0, 0, 1)
                    checkPressedWithRepeat(KeyboardKey.PAGE_UP) -> extendBrickSelection(-20, 0, 0)
                    checkPressedWithRepeat(KeyboardKey.PAGE_DOWN) -> extendBrickSelection(20, 0, 0)
                    checkPressedWithRepeat(KeyboardKey.HOME) -> extendBrickSelection(0, -20, 0)
                    checkPressedWithRepeat(KeyboardKey.END) -> extendBrickSelection(0, 20, 0)
                }
                // all modifiers up
                checkPressedOnce(KeyboardKey.BACKSPACE) -> actions.removeSelectedBricks()
                checkPressedOnce(KeyboardKey.ENTER) -> actions.movePlayerToCursorPos()
                checkPressedOnce(KeyboardKey.ESCAPE) -> menu.showMainMenu()
                checkPressedOnce(KeyboardKey.FWDDEL) -> actions.removeSelectedBricks()
                checkPressedOnce(KeyboardKey.TAB) -> actions.jumpToNextSpawnPt()
                checkPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> collapseBrickSelectionAndMove(0, -1, 0)
                checkPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> collapseBrickSelectionAndMove(0, 1, 0)
                checkPressedWithRepeat(KeyboardKey.ARROW_UP) -> collapseBrickSelectionAndMove(-1, 0, 0)
                checkPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> collapseBrickSelectionAndMove(1, 0, 0)
                checkPressedWithRepeat(KeyboardKey.KEYPAD_MINUS) -> collapseBrickSelectionAndMove(0, 0, -1)
                checkPressedWithRepeat(KeyboardKey.KEYPAD_PLUS) -> collapseBrickSelectionAndMove(0, 0, 1)
                checkPressedWithRepeat(KeyboardKey.PAGE_UP) -> collapseBrickSelectionAndMove(-20, 0, 0)
                checkPressedWithRepeat(KeyboardKey.PAGE_DOWN) -> collapseBrickSelectionAndMove(20, 0, 0)
                checkPressedWithRepeat(KeyboardKey.HOME) -> collapseBrickSelectionAndMove(0, -20, 0)
                checkPressedWithRepeat(KeyboardKey.END) -> collapseBrickSelectionAndMove(0, 20, 0)
                checkPressedOnce('m') -> menu.showMaterialMenu()
                checkPressedOnce('n') -> menu.showShapeMenu()
                checkPressedOnce('p') -> actions.pickShapeMaterial()
                checkPressedOnce('q') -> actions.setShapeAndMaterialOfSelectedBricks()
                checkPressedOnce('t') -> menu.showSpawnPtMenu()
                checkPressedOnce('w') -> menu.showWizardMenu()
            }
        }
    }

    private fun handleKeysInHeightMapMode() {
        App.input.apply {
            when {
                altIsDown -> when {
                    ctrlIsDown -> Unit
                    shiftIsDown -> Unit
                    // ALT
                    checkPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> moveHeightMapSelection(0, -1)
                    checkPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> moveHeightMapSelection(0, 1)
                    checkPressedWithRepeat(KeyboardKey.ARROW_UP) -> moveHeightMapSelection(-1, 0)
                    checkPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> moveHeightMapSelection(1, 0)
                    checkPressedWithRepeat(KeyboardKey.PAGE_UP) -> moveHeightMapSelection(-20, 0)
                    checkPressedWithRepeat(KeyboardKey.PAGE_DOWN) -> moveHeightMapSelection(20, 0)
                    checkPressedWithRepeat(KeyboardKey.HOME) -> moveHeightMapSelection(0, -20)
                    checkPressedWithRepeat(KeyboardKey.END) -> moveHeightMapSelection(0, 20)

                    checkPressedWithRepeat(KeyboardKey.KEYPAD_MINUS)
                    -> actions.modifyHeightOfSelectedSamples(-HM_SMALL_STEP)

                    checkPressedWithRepeat(KeyboardKey.KEYPAD_PLUS)
                    -> actions.modifyHeightOfSelectedSamples(HM_SMALL_STEP)
                }
                ctrlIsDown -> Unit // CTRL, CTRL+SHIFT
                shiftIsDown -> when {
                    // SHIFT
                    checkPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> extendHeightMapSelection(0, -1)
                    checkPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> extendHeightMapSelection(0, 1)
                    checkPressedWithRepeat(KeyboardKey.ARROW_UP) -> extendHeightMapSelection(-1, 0)
                    checkPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> extendHeightMapSelection(1, 0)
                    checkPressedWithRepeat(KeyboardKey.PAGE_UP) -> extendHeightMapSelection(-20, 0)
                    checkPressedWithRepeat(KeyboardKey.PAGE_DOWN) -> extendHeightMapSelection(20, 0)
                    checkPressedWithRepeat(KeyboardKey.HOME) -> extendHeightMapSelection(0, -20)
                    checkPressedWithRepeat(KeyboardKey.END) -> extendHeightMapSelection(0, 20)
                }
                // all modifiers up
                checkPressedOnce(KeyboardKey.ESCAPE) -> state.heightMapSpawnPt?.let { menu.showEditSpawnPtMenu(it) }
                checkPressedWithRepeat(KeyboardKey.ARROW_LEFT) -> collapseHeightMapSelectionAndMove(0, -1)
                checkPressedWithRepeat(KeyboardKey.ARROW_RIGHT) -> collapseHeightMapSelectionAndMove(0, 1)
                checkPressedWithRepeat(KeyboardKey.ARROW_UP) -> collapseHeightMapSelectionAndMove(-1, 0)
                checkPressedWithRepeat(KeyboardKey.ARROW_DOWN) -> collapseHeightMapSelectionAndMove(1, 0)
                checkPressedWithRepeat(KeyboardKey.KEYPAD_MINUS) -> actions.modifyHeightOfSelectedSamples(-HM_LARGE_STEP)
                checkPressedWithRepeat(KeyboardKey.KEYPAD_PLUS) -> actions.modifyHeightOfSelectedSamples(HM_LARGE_STEP)
            }
        }
    }

    private fun handleCommonKeys() {
        App.input.apply {
            when {
                altIsDown -> Unit // ALT, ALT+CTRL, ALT+SHIFT
                ctrlIsDown -> when {
                    shiftIsDown -> when {
                        // CTRL+SHIFT
                        checkPressedOnce('z') -> undoStack.redo()
                    }
                    else -> when {
                        // CTRL
                        checkPressedOnce('c') -> clipboard.copy()
                        checkPressedOnce('s') -> actions.saveToFile()
                        checkPressedOnce('v') -> clipboard.paste()
                        checkPressedOnce('x') -> clipboard.cut()
                        checkPressedOnce('z') -> undoStack.undo()
                    }
                }
                shiftIsDown -> when {
                    // SHIFT
                    checkPressedOnce('y') -> actions.switchCameraTarget(true)
                }
                // all modifiers up
                checkPressedOnce('c') -> menu.showCameraModeMenu()
                checkPressedOnce('y') -> actions.switchCameraTarget(false)
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
