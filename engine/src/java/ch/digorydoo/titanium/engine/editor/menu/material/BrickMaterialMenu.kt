package ch.digorydoo.titanium.engine.editor.menu.material

import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.editor.menu.material.BrickMaterialGroup.Companion.findFirstInnermostGroup
import ch.digorydoo.titanium.engine.i18n.EngineTextId

internal class BrickMaterialMenu(private val actions: EditorActions) {
    fun show(initial: BrickMaterial, onBack: (() -> Unit)?) {
        show(BrickMaterialGroup.ROOT, initial, onBack)
    }

    private fun show(group: BrickMaterialGroup, initialMat: BrickMaterial?, onBack: (() -> Unit)?) {
        val reopen = { show(group, null, onBack) }
        val groupsOfInitialMat = mutableListOf<BrickMaterialGroup>()

        if (initialMat != null) {
            var grp = initialMat.findFirstInnermostGroup()

            while (grp != null) {
                groupsOfInitialMat.add(grp)
                grp = grp.findParent()
            }
        }

        App.dlg.showDlg<Unit> {
            val dlgDef = this

            BrickMaterialGroup.entries
                .filter { it.findParent() == group }
                .sortedBy { it.displayText }
                .forEach { grp ->
                    item {
                        text = grp.displayText + " >"
                        onSelect = { show(grp, initialMat, onBack = reopen) }

                        if (groupsOfInitialMat.contains(grp)) {
                            dlgDef.focus = this
                        }
                    }
                }

            group.materials()
                .sortedBy { it.displayText }
                .forEach { mat ->
                    item {
                        text = mat.displayText
                        onSelect = { actions.setActiveMaterial(mat) }
                        if (mat == initialMat) dlgDef.focus = this
                    }
                }

            dismiss = item {
                textId = if (onBack == null) EngineTextId.DONE else EngineTextId.BACK
                onSelect = onBack
            }
        }
    }
}
