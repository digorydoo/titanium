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

internal class EditorMenu(
    private val state: EditorState,
    private val brickSelection: BrickSelection,
    actions: EditorActions,
) {
    private val cameraModeMenu = CameraModeMenu(actions)
    private val lightingMenu = LightingMenu(actions)
    private val materialMenu = BrickMaterialMenu(actions)
    private val shapeMenu = BrickShapeMenu(actions)
    private val spawnPtMenu = SpawnPtMenu(state, actions)
    private val storyTimeMenu = StoryTimeMenu(actions)
    private val wizardMenu = WizardMenu(actions)

    fun showMainMenu() = showMainMenu(null)
    fun showEditSpawnPtMenu(spawnPt: SpawnPt) = spawnPtMenu.showEditSpawnPtMenu(spawnPt, null)
    fun showCameraModeMenu() = cameraModeMenu.show(null)
    fun showShapeMenu() = shapeMenu.show(state.shape, null)
    fun showMaterialMenu() = materialMenu.show(state.material, null)
    fun showWizardMenu() = wizardMenu.show(brickSelection.getUnreversed(), null)
    fun showSpawnPtMenu() = spawnPtMenu.show(brickSelection.getTipPosInWorldCoords(), null)

    fun showMainMenu(focusText: String?) {
        App.dlg.showDlg<Unit> {
            val dlgDef = this
            item {
                text = "Brick shape..."
                if (text == focusText) dlgDef.focus = this
                onSelect = { shapeMenu.show(state.shape, onBack = { showMainMenu(text) }) }
            }
            item {
                text = "Brick material..."
                if (text == focusText) dlgDef.focus = this
                onSelect = { materialMenu.show(state.material, onBack = { showMainMenu(text) }) }
            }
            item {
                text = "Brick wizard..."
                if (text == focusText) dlgDef.focus = this
                onSelect = { wizardMenu.show(brickSelection.getUnreversed(), onBack = { showMainMenu(text) }) }
            }
            item {
                text = "Camera mode..."
                if (text == focusText) dlgDef.focus = this
                onSelect = { cameraModeMenu.show(onBack = { showMainMenu(text) }) }
            }
            item {
                text = "Lighting..."
                if (text == focusText) dlgDef.focus = this
                onSelect = { lightingMenu.show(onBack = { showMainMenu(text) }) }
            }
            item {
                text = "Spawn points..."
                if (text == focusText) dlgDef.focus = this
                onSelect = {
                    spawnPtMenu.show(brickSelection.getTipPosInWorldCoords(), onBack = { showMainMenu(text) })
                }
            }
            item {
                text = "Story time..."
                if (text == focusText) dlgDef.focus = this
                onSelect = { storyTimeMenu.show(onBack = { showMainMenu(text) }) }
            }
            dismiss = item {
                textId = EngineTextId.DONE
            }
        }
    }
}
