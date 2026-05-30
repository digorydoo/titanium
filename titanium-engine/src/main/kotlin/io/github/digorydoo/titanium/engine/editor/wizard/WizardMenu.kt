package io.github.digorydoo.titanium.engine.editor.wizard

import ch.digorydoo.kutils.box.Boxi
import ch.digorydoo.kutils.string.trunc
import io.github.digorydoo.titanium.engine.brick.Brick
import io.github.digorydoo.titanium.engine.brick.BrickMaterial
import io.github.digorydoo.titanium.engine.brick.BrickShape
import io.github.digorydoo.titanium.engine.brick.BrickShapeAndMaterial
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.i18n.EngineTextId

internal class WizardMenu(private val actions: EditorActions) {
    private val drawingWizard = DrawingWizard()

    fun show(curSelection: Boxi, onBack: (() -> Unit)?) {
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

        App.dlg.showDlg<Unit> {
            suggestions
                .distinctBy { "${it.shape}_${it.material}" }
                .forEach { suggest ->
                    item {
                        text = trunc("${suggest.shape.displayText} (${suggest.material.displayText})", 34)
                        onSelect = {
                            actions.setActiveShape(suggest.shape)
                            actions.setActiveMaterial(suggest.material)
                        }
                    }
                }

            dismiss = item {
                textId = if (onBack == null) EngineTextId.DONE else EngineTextId.BACK
                onSelect = onBack
            }
        }
    }
}
