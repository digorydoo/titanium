package ch.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.BrickSelection
import ch.digorydoo.titanium.engine.editor.EditorState
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.editor.menu.material.BrickMaterialMenu
import ch.digorydoo.titanium.engine.editor.menu.shape.BrickShapeMenu
import ch.digorydoo.titanium.engine.editor.wizard.WizardMenu
import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.ui.choice.TextChoice

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

    fun showMainMenu() = showMainMenu(true)
    fun showCameraModeMenu() = showCameraModeMenu(true)
    fun showMaterialMenu() = showMaterialMenu(true)
    fun showShapeMenu() = showShapeMenu(true)
    fun showWizardMenu() = showWizardMenu(true)
    fun showSpawnPtMenu() = showSpawnPtMenu(true)
    fun showEditSpawnPtMenu(spawnPt: SpawnPt) = spawnPtMenu.showEditSpawnPtMenu(spawnPt) {}

    private fun showMainMenu(playSoundOnOpen: Boolean) {
        val choices = listOf(
            TextChoice("Brick shape...") { showShapeMenu(false) },
            TextChoice("Brick material...") { showMaterialMenu(false) },
            TextChoice("Brick wizard...") { showWizardMenu(false) },
            TextChoice("Camera mode...") { showCameraModeMenu(false) },
            TextChoice("Lighting...") { showLightingMenu() },
            TextChoice("Spawn points...") { showSpawnPtMenu(false) },
            TextChoice("Story time...") { showStoryTimeMenu() },
            TextChoice(EngineTextId.DONE) {},
        )
        App.dlg.showChoices(choices, 0, lastItemIsDismiss = true, playSoundOnOpen = playSoundOnOpen)
    }

    private fun showCameraModeMenu(isTopLevel: Boolean) {
        cameraModeMenu.show(
            isTopLevel = isTopLevel,
            onCancel = { if (!isTopLevel) showMainMenu(false) },
        )
    }

    private fun showShapeMenu(isTopLevel: Boolean) {
        shapeMenu.show(
            state.shape,
            isTopLevel = isTopLevel,
            onCancel = { if (!isTopLevel) showMainMenu(false) },
        )
    }

    private fun showMaterialMenu(isTopLevel: Boolean) {
        materialMenu.show(
            state.material,
            isTopLevel = isTopLevel,
            onCancel = { if (!isTopLevel) showMainMenu(false) },
        )
    }

    private fun showWizardMenu(isTopLevel: Boolean) {
        wizardMenu.show(
            brickSelection.getUnreversed(),
            isTopLevel = isTopLevel,
            onCancel = { if (!isTopLevel) showMainMenu(false) }
        )
    }

    private fun showSpawnPtMenu(isTopLevel: Boolean) {
        spawnPtMenu.show(
            brickSelection.getTipPosInWorldCoords(),
            isTopLevel = isTopLevel,
            onCancel = { if (!isTopLevel) showMainMenu(false) },
        )
    }

    private fun showStoryTimeMenu() {
        storyTimeMenu.show(
            isTopLevel = false,
            onCancel = { showMainMenu(false) },
        )
    }

    private fun showLightingMenu() {
        lightingMenu.show(
            isTopLevel = false,
            onCancel = { showMainMenu(false) }
        )
    }
}
