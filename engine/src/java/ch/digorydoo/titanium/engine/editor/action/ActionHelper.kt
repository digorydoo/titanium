package ch.digorydoo.titanium.engine.editor.action

import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.Selection
import ch.digorydoo.titanium.engine.editor.UndoStack
import ch.digorydoo.titanium.engine.editor.statusbar.EditorStatusBar

class ActionHelper(
    private val selection: Selection,
    private val status: EditorStatusBar,
    private val undoStack: UndoStack,
) {
    fun applyToAllSelectedBricks(newShape: BrickShape?, newMaterial: BrickMaterial?) {
        applyToAllSelectedBricks { _, _, _, brick ->
            if (brick.shape != BrickShape.NONE || newShape != null) {
                if (newShape == BrickShape.NONE) {
                    brick.setInvalid()
                } else {
                    if (newShape != null) {
                        brick.shape = newShape
                    }

                    if (newMaterial != null) {
                        brick.material = newMaterial
                        brick.setFacesFromMaterialAndBrickCoords()
                    }
                }
            }
            true // always set brick
        }
    }

    fun applyToAllSelectedBricks(lambda: (x: Int, y: Int, z: Int, brick: Brick) -> Boolean): Int {
        val undoList = mutableListOf<Brick>()
        var numChanged = 0

        selection.forEachBrick { x, y, z ->
            val before = Brick().also { App.bricks.getAtBrickCoord(x, y, z, it) }
            undoList.add(before)
            val newBrick = Brick(before)

            if (lambda(x, y, z, newBrick)) {
                App.bricks.setAtBrickCoord(x, y, z, newBrick)
                numChanged++
            }
        }

        if (numChanged > 0) {
            val sel = selection.getUnreversed()
            undoStack.push(sel, undoList)
            App.bricks.updateBricks(sel, updateRunsAcross = true)
            status.updateStats()
        }

        return numChanged
    }
}
