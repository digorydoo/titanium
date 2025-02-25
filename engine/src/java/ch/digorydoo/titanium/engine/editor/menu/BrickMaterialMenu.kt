package ch.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.editor.items.BrickMaterialGroup
import ch.digorydoo.titanium.engine.editor.items.BrickMaterialGroup.Companion.findFirstInnermostGroup
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.ui.choice.Choice
import ch.digorydoo.titanium.engine.ui.choice.TextChoice

class BrickMaterialMenu(private val actions: EditorActions) {
    fun show(initial: BrickMaterial, isTopLevel: Boolean, onCancel: () -> Unit) {
        show(
            BrickMaterialGroup.ROOT,
            initial,
            playSoundOnOpen = isTopLevel,
            playSoundOnDismiss = isTopLevel,
            onCancel
        )
    }

    private fun show(
        group: BrickMaterialGroup,
        initialMat: BrickMaterial?,
        playSoundOnOpen: Boolean,
        playSoundOnDismiss: Boolean,
        onCancel: () -> Unit,
    ) {
        val reopen = {
            show(group, null, playSoundOnOpen = false, playSoundOnDismiss, onCancel)
        }

        val groupsOfInitialMat = mutableListOf<BrickMaterialGroup>()

        if (initialMat != null) {
            var grp = initialMat.findFirstInnermostGroup()

            while (grp != null) {
                groupsOfInitialMat.add(grp)
                grp = grp.findParent()
            }
        }

        var textOfInitiallySelected = ""

        val groupChoices = BrickMaterialGroup.entries
            .filter { it.findParent() == group }
            .map { grp ->
                val choiceText = grp.displayText + " >"

                if (groupsOfInitialMat.contains(grp)) {
                    textOfInitiallySelected = choiceText
                }

                TextChoice(choiceText) {
                    show(
                        grp,
                        initialMat,
                        playSoundOnOpen = false,
                        playSoundOnDismiss = false,
                        onCancel = reopen
                    )
                }
            }
            .sortedBy { it.itemText }

        val matChoices = group.materials()
            .map { mat ->
                val choiceText = mat.displayText
                if (mat == initialMat) textOfInitiallySelected = choiceText

                TextChoice(choiceText) {
                    actions.setActiveMaterial(mat)
                }
            }
            .sortedBy { it.itemText }

        val choices = mutableListOf<Choice>().apply {
            addAll(groupChoices)
            addAll(matChoices)
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
