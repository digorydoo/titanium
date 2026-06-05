package io.github.digorydoo.titanium.engine.editor.menu

import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.editor.BrickSelection
import io.github.digorydoo.titanium.engine.editor.EditorState
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.editor.menu.material.BrickMaterialMenu
import io.github.digorydoo.titanium.engine.editor.menu.shape.BrickShapeMenu
import io.github.digorydoo.titanium.engine.editor.wizard.WizardMenu
import io.github.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import io.github.digorydoo.titanium.engine.intermission.Intermission

internal class EditorMenu(
    private val state: EditorState,
    private val brickSelection: BrickSelection,
    actions: EditorActions,
) {
    private val cameraModeMenu = CameraModeMenu(actions)
    private val lightingMenu = LightingMenu(actions)
    private val materialMenu = BrickMaterialMenu(actions)
    private val shapeMenu = BrickShapeMenu(actions)
    private val editSpawnPtMenu = EditSpawnPtMenu(state, actions)
    private val spawnPtMenu = SpawnPtMenu(editSpawnPtMenu, actions)
    private val storyTimeMenu = StoryTimeMenu(actions)
    private val wizardMenu = WizardMenu(actions)

    fun showEditSpawnPtMenu(spawnPt: SpawnPt) = editSpawnPtMenu.showInIntermission(spawnPt)
    fun showCameraModeMenu() = cameraModeMenu.showInIntermission()
    fun showShapeMenu() = shapeMenu.showInIntermission(state.shape)
    fun showMaterialMenu() = materialMenu.showInIntermission(state.material)
    fun showWizardMenu() = wizardMenu.showInIntermission(brickSelection.getUnreversed())
    fun showSpawnPtMenu() = spawnPtMenu.showInIntermission(brickSelection.getTipPosInWorldCoords())

    fun showMainMenu() {
        App.intermissions.begin {
            showMainMenuImpl()
        }
    }

    private suspend fun Intermission.showMainMenuImpl() {
        var focusOnReopen = ""

        do {
            val selected = showDlg {
                item {
                    text = "Brick shape..."
                    if (text == focusOnReopen) this@showDlg.focus = this
                    onSelect = {
                        shapeMenu.run { show(state.shape) }
                    }
                }
                item {
                    text = "Brick material..."
                    if (text == focusOnReopen) this@showDlg.focus = this
                    onSelect = {
                        materialMenu.run { show(state.material) }
                    }
                }
                item {
                    text = "Brick wizard..."
                    if (text == focusOnReopen) this@showDlg.focus = this
                    onSelect = {
                        wizardMenu.run { show(brickSelection.getUnreversed()) }
                    }
                }
                item {
                    text = "Camera mode..."
                    if (text == focusOnReopen) this@showDlg.focus = this
                    onSelect = {
                        cameraModeMenu.run { show() }
                    }
                }
                item {
                    text = "Lighting..."
                    if (text == focusOnReopen) this@showDlg.focus = this
                    onSelect = {
                        lightingMenu.run { show() }
                    }
                }
                item {
                    text = "Spawn points..."
                    if (text == focusOnReopen) this@showDlg.focus = this
                    onSelect = {
                        spawnPtMenu.run { show(brickSelection.getTipPosInWorldCoords()) }
                    }
                }
                item {
                    text = "Story time..."
                    if (text == focusOnReopen) this@showDlg.focus = this
                    onSelect = {
                        storyTimeMenu.run { show() }
                    }
                }
                dismiss = item {
                    textId = EngineTextId.DONE
                }
            }

            focusOnReopen = selected.text
        } while (selected.textId != EngineTextId.DONE)
    }
}
