package io.github.digorydoo.titanium.engine.editor.menu.shape

import io.github.digorydoo.titanium.engine.brick.BrickShape
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.editor.menu.shape.BrickShapeGroup.Companion.findFirstInnermostGroup
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import io.github.digorydoo.titanium.engine.intermission.Intermission

internal class BrickShapeMenu(private val actions: EditorActions) {
    fun showInIntermission(initial: BrickShape) {
        App.intermissions.begin {
            showImpl(initial, hasParentMenu = false)
        }
    }

    suspend fun Intermission.show(initial: BrickShape) {
        showImpl(initial, hasParentMenu = true)
    }

    private suspend fun Intermission.showImpl(initial: BrickShape, hasParentMenu: Boolean) {
        val groupsOfInitial = mutableListOf<BrickShapeGroup>().also { groups ->
            var grp = initial.findFirstInnermostGroup()

            while (grp != null) {
                groups.add(grp)
                grp = grp.findParent()
            }
        }

        val path = mutableListOf(BrickShapeGroup.ROOT)

        do {
            val currentGroup = path.first()
            showDlg {
                val dlgDef = this

                BrickShapeGroup.entries
                    .filter { it.findParent() == currentGroup }
                    .sortedBy { it.displayText }
                    .forEach { grp ->
                        item {
                            text = grp.displayText + " >"
                            if (groupsOfInitial.contains(grp)) dlgDef.focus = this
                            onSelect = { path.add(0, grp) }
                        }
                    }

                currentGroup.shapes()
                    .sortedBy { it.displayText }
                    .forEach { shape ->
                        item {
                            text = shape.displayText
                            if (shape == initial) dlgDef.focus = this
                            onSelect = {
                                actions.setActiveShape(shape)
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
