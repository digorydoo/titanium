package ch.digorydoo.titanium.engine.editor.menu.shape

import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.editor.menu.shape.BrickShapeGroup.Companion.findFirstInnermostGroup
import ch.digorydoo.titanium.engine.i18n.EngineTextId

internal class BrickShapeMenu(private val actions: EditorActions) {
    fun show(initialShape: BrickShape, onBack: (() -> Unit)?) {
        show(BrickShapeGroup.ROOT, initialShape, onBack)
    }

    private fun show(group: BrickShapeGroup, initialShape: BrickShape?, onBack: (() -> Unit)?) {
        val reopen = { show(group, null, onBack) }
        val groupsOfInitialShape = mutableListOf<BrickShapeGroup>()

        if (initialShape != null) {
            var grp = initialShape.findFirstInnermostGroup()

            while (grp != null) {
                groupsOfInitialShape.add(grp)
                grp = grp.findParent()
            }
        }

        App.dlg.showDlg<Unit> {
            val dlgDef = this

            BrickShapeGroup.entries
                .filter { it.findParent() == group }
                .sortedBy { it.displayText }
                .forEach { grp ->
                    item {
                        text = grp.displayText + " >"
                        onSelect = { show(grp, initialShape, onBack = reopen) }
                        if (groupsOfInitialShape.contains(grp)) {
                            dlgDef.focus = this
                        }
                    }
                }

            group.shapes()
                .sortedBy { it.displayText }
                .forEach { shape ->
                    item {
                        text = shape.displayText
                        onSelect = { actions.setActiveShape(shape) }
                        if (shape == initialShape) dlgDef.focus = this
                    }
                }

            dismiss = item {
                textId = if (onBack == null) EngineTextId.DONE else EngineTextId.BACK
                onSelect = onBack
            }
        }
    }
}
