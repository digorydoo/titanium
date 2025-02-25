package ch.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.editor.items.BrickShapeGroup
import ch.digorydoo.titanium.engine.editor.items.BrickShapeGroup.Companion.findFirstInnermostGroup
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.ui.choice.Choice
import ch.digorydoo.titanium.engine.ui.choice.TextChoice

class BrickShapeMenu(private val actions: EditorActions) {
    fun show(initialShape: BrickShape, isTopLevel: Boolean, onCancel: () -> Unit) {
        show(
            BrickShapeGroup.ROOT,
            initialShape,
            playSoundOnOpen = isTopLevel,
            playSoundOnDismiss = isTopLevel,
            onCancel
        )
    }

    private fun show(
        group: BrickShapeGroup,
        initialShape: BrickShape?,
        playSoundOnOpen: Boolean,
        playSoundOnDismiss: Boolean,
        onCancel: () -> Unit,
    ) {
        val reopen = {
            show(group, null, playSoundOnOpen = false, playSoundOnDismiss, onCancel)
        }

        val groupsOfInitialShape = mutableListOf<BrickShapeGroup>()

        if (initialShape != null) {
            var grp = initialShape.findFirstInnermostGroup()

            while (grp != null) {
                groupsOfInitialShape.add(grp)
                grp = grp.findParent()
            }
        }

        var textOfInitiallySelected = ""

        val groupChoices = BrickShapeGroup.entries
            .filter { it.findParent() == group }
            .map { grp ->
                val choiceText = grp.displayText + " >"

                if (groupsOfInitialShape.contains(grp)) {
                    textOfInitiallySelected = choiceText
                }

                TextChoice(choiceText) {
                    show(
                        grp,
                        initialShape,
                        playSoundOnOpen = false,
                        playSoundOnDismiss = false,
                        onCancel = reopen
                    )
                }
            }
            .sortedBy { it.itemText }

        val shapeChoices = group.shapes()
            .map { shape ->
                val choiceText = shape.displayText
                if (shape == initialShape) textOfInitiallySelected = choiceText

                TextChoice(choiceText) {
                    actions.setActiveShape(shape)
                }
            }
            .sortedBy { it.itemText }

        val choices = mutableListOf<Choice>().apply {
            addAll(groupChoices)
            addAll(shapeChoices)
        }

        val curIdx = choices.indexOfFirst { it.itemText == textOfInitiallySelected }
        choices.add(TextChoice(EngineTextId.BACK) { onCancel() })

        App.dlg.showChoices(
            choices,
            curIdx,
            lastItemIsDismiss = true,
            playSoundOnOpen = playSoundOnOpen,
            playSoundOnDismiss = playSoundOnDismiss,
        )
    }
}
