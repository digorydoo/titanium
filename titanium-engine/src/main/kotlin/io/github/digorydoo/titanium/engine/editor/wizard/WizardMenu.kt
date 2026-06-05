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
import io.github.digorydoo.titanium.engine.intermission.Intermission

internal class WizardMenu(private val actions: EditorActions) {
    private val drawingWizard = DrawingWizard()

    fun showInIntermission(curSelection: Boxi) {
        App.intermissions.begin {
            showImpl(curSelection, hasParentMenu = false)
        }
    }

    suspend fun Intermission.show(curSelection: Boxi) {
        showImpl(curSelection, hasParentMenu = true)
    }

    private suspend fun Intermission.showImpl(curSelection: Boxi, hasParentMenu: Boolean) {
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

        showDlg {
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

            dismiss = item { textId = if (hasParentMenu) EngineTextId.BACK else EngineTextId.DONE }
        }
    }
}
