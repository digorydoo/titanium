package ch.digorydoo.titanium.engine.editor.wizard

import ch.digorydoo.kutils.box.Boxi
import ch.digorydoo.kutils.string.trunc
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickShapeAndMaterial
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.ui.choice.TextChoice

internal class WizardMenu(private val actions: EditorActions) {
    private val drawingWizard = DrawingWizard()

    fun show(curSelection: Boxi, isTopLevel: Boolean, onCancel: () -> Unit) {
        show(curSelection, playSoundOnOpen = isTopLevel, playSoundOnDismiss = isTopLevel, onCancel)
    }

    private fun show(
        curSelection: Boxi,
        playSoundOnOpen: Boolean,
        playSoundOnDismiss: Boolean,
        onCancel: () -> Unit,
    ) {
        val suggestions = drawingWizard.getSuggestions(curSelection.centrei()).toMutableList()

        if (suggestions.size <= 4) {
            val centre = curSelection.centrei()
            val curBrick = Brick().also { App.bricks.getAtBrickCoord(centre, it) }

            if (curBrick.isValid()) {
                suggestions.add(BrickShapeAndMaterial(curBrick.shape, curBrick.material))
            }
        }

        if (suggestions.size <= 4) {
            suggestions.add(BrickShapeAndMaterial(BrickShape.BASIC_BLOCK, BrickMaterial.GREY_CONCRETE))
        }

        val choices = suggestions
            .distinctBy { "${it.shape}_${it.material}" }
            .map { suggest ->
                val text = trunc("${suggest.shape.displayText} (${suggest.material.displayText})", 34)
                TextChoice(text) {
                    actions.setActiveShape(suggest.shape)
                    actions.setActiveMaterial(suggest.material)
                }
            }
            .toMutableList()

        choices.add(TextChoice("Cancel", onCancel))

        App.dlg.showChoices(
            choices,
            initHilitedIdx = 0,
            lastItemIsDismiss = true,
            playSoundOnOpen = playSoundOnOpen,
            playSoundOnDismiss = playSoundOnDismiss,
        )
    }
}
