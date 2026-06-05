package io.github.digorydoo.titanium.engine.editor.menu.material

import io.github.digorydoo.titanium.engine.brick.BrickMaterial
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.editor.menu.material.BrickMaterialGroup.Companion.findFirstInnermostGroup
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import io.github.digorydoo.titanium.engine.intermission.Intermission

internal class BrickMaterialMenu(private val actions: EditorActions) {
    fun showInIntermission(initial: BrickMaterial) {
        App.intermissions.begin {
            showImpl(initial, hasParentMenu = false)
        }
    }

    suspend fun Intermission.show(initial: BrickMaterial) {
        showImpl(initial, hasParentMenu = true)
    }

    private suspend fun Intermission.showImpl(initial: BrickMaterial, hasParentMenu: Boolean) {
        val groupsOfInitial = mutableListOf<BrickMaterialGroup>().also { groups ->
            var grp = initial.findFirstInnermostGroup()

            while (grp != null) {
                groups.add(grp)
                grp = grp.findParent()
            }
        }

        val path = mutableListOf(BrickMaterialGroup.ROOT)

        do {
            val currentGroup = path.first()
            showDlg {
                val dlgDef = this

                BrickMaterialGroup.entries
                    .filter { it.findParent() == currentGroup }
                    .sortedBy { it.displayText }
                    .forEach { grp ->
                        item {
                            text = grp.displayText + " >"
                            if (groupsOfInitial.contains(grp)) dlgDef.focus = this
                            onSelect = { path.add(0, grp) }
                        }
                    }

                currentGroup.materials()
                    .sortedBy { it.displayText }
                    .forEach { mat ->
                        item {
                            text = mat.displayText
                            if (mat == initial) dlgDef.focus = this
                            onSelect = {
                                actions.setActiveMaterial(mat)
                                path.clear() // force-close all hierarchies
                            }
                        }
                    }

                dismiss = item {
                    textId = if (path.size > 1 || hasParentMenu) EngineTextId.BACK else EngineTextId.DONE
                    onSelect = { path.removeAt(0) }
                }
            }
        } while (path.isNotEmpty())
    }
}
