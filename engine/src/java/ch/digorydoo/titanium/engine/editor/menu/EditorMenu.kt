package ch.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.Selection
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.editor.statusbar.EditorStatusBar
import ch.digorydoo.titanium.engine.editor.wizard.WizardMenu
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.ui.choice.TextChoice

class EditorMenu(
    private val status: EditorStatusBar,
    private val selection: Selection,
    actions: EditorActions,
) {
    private val cameraModeMenu = CameraModeMenu(actions)
    private val lightingMenu = LightingMenu(actions)
    private val materialMenu = BrickMaterialMenu(actions)
    private val shapeMenu = BrickShapeMenu(actions)
    private val spawnPtMenu = SpawnPtMenu(actions)
    private val storyTimeMenu = StoryTimeMenu(actions)
    private val wizardMenu = WizardMenu(actions)

    fun showMainMenu() = showMainMenu(true)
    fun showCameraModeMenu() = showCameraModeMenu(true)
    fun showMaterialMenu() = showMaterialMenu(true)
    fun showShapeMenu() = showShapeMenu(true)
    fun showSpawnPtMenu() = showSpawnPtMenu(true)
    fun showWizardMenu() = showWizardMenu(true)

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
            status.shape,
            isTopLevel = isTopLevel,
            onCancel = { if (!isTopLevel) showMainMenu(false) },
        )
    }

    private fun showMaterialMenu(isTopLevel: Boolean) {
        materialMenu.show(
            status.material,
            isTopLevel = isTopLevel,
            onCancel = { if (!isTopLevel) showMainMenu(false) },
        )
    }

    private fun showWizardMenu(isTopLevel: Boolean) {
        wizardMenu.show(
            selection.getUnreversed(),
            isTopLevel = isTopLevel,
            onCancel = { if (!isTopLevel) showMainMenu(false) }
        )
    }

    private fun showSpawnPtMenu(isTopLevel: Boolean) {
        spawnPtMenu.show(
            selection.getPosCentreInWorldCoords(),
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
