package ch.digorydoo.titanium.engine.editor

import ch.digorydoo.kutils.box.Boxi
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.statusbar.EditorStatusBar

class UndoStack(private val status: EditorStatusBar, private val selection: Selection) {
    class Entry(val sel: Boxi, val bricks: List<Brick>)

    private val stack = mutableListOf<Entry>()
    private var nextIdx = 0

    fun push(sel: Boxi, bricks: List<Brick>) {
        while (stack.size > nextIdx) {
            stack.removeLast() // drop entries that were undone
        }

        if (stack.size >= MAX_SIZE) {
            stack.removeFirst() // forget about entries long ago
        }

        stack.add(Entry(sel, bricks))
        nextIdx = stack.size
    }

    fun undo() {
        if (nextIdx <= 0) {
            status.didFailToUndo()
            return
        }

        val entry = stack[nextIdx - 1]
        val sel = selection.getUnreversed()

        if (!sel.hasSameValues(entry.sel)) {
            selection.set(entry.sel)
        } else {
            nextIdx--
            restore(entry)
        }
    }

    fun redo() {
        if (nextIdx >= stack.size) {
            status.didFailToRedo()
            return
        }

        val entry = stack[nextIdx]
        val sel = selection.getUnreversed()

        if (!sel.hasSameValues(entry.sel)) {
            selection.set(entry.sel)
        } else {
            nextIdx++
            restore(entry)
        }
    }

    private fun restore(entry: Entry) {
        var i = 0

        selection.set(entry.sel)

        selection.forEachBrick { x, y, z ->
            val cur = Brick().also { App.bricks.getAtBrickCoord(x, y, z, it) }
            App.bricks.setAtBrickCoord(x, y, z, entry.bricks[i])
            entry.bricks[i].set(cur) // replace the brick in the entry with the one we've overwritten
            i++
        }

        App.bricks.updateBricks(entry.sel, updateRunsAcross = true)
        status.updateStats()
    }

    companion object {
        private const val MAX_SIZE = 1000
    }
}
