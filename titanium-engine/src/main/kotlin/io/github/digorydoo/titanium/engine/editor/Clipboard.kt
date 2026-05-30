package io.github.digorydoo.titanium.engine.editor

import io.github.digorydoo.titanium.engine.brick.Brick
import io.github.digorydoo.titanium.engine.brick.BrickFaceAssigner
import io.github.digorydoo.titanium.engine.brick.BrickShape
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import kotlin.math.min

internal class Clipboard(
    private val hud: EditorHUD,
    private val brickSelection: BrickSelection,
    private val undoStack: UndoStack,
) {
    private var copiedXsize = 0
    private var copiedYsize = 0
    private var copiedZsize = 0
    private var copiedBricks = listOf<Brick>()

    fun cut() = cutCopy(cut = true)
    fun copy() = cutCopy(cut = false)

    private fun cutCopy(cut: Boolean) {
        val list = mutableListOf<Brick>()
        val empty = Brick()
        var anyCopied = false

        brickSelection.forEachBrick { x, y, z ->
            val brick = Brick().also { App.bricks.getAtBrickCoord(x, y, z, it) }
            list.add(brick)
            anyCopied = anyCopied || brick.shape != BrickShape.NONE

            if (cut) {
                App.bricks.setAtBrickCoord(x, y, z, empty)
            }
        }

        val textId = if (cut) EngineTextId.EDITOR_BRICKS_CUT else EngineTextId.EDITOR_BRICKS_COPIED

        if (!anyCopied) {
            App.dlg.showSnackbar(App.i18n.choose(textId, 0))
            return
        }

        val box = brickSelection.getUnreversed()
        copiedXsize = box.xsize
        copiedYsize = box.ysize
        copiedZsize = box.zsize
        copiedBricks = list

        if (cut) {
            undoStack.push(box, list)
            App.bricks.updateBricks(box, evenAdjacient = true)
            hud.updateStats()
        }

        App.dlg.showSnackbar(App.i18n.choose(textId, list.size))
    }

    fun paste() {
        if (copiedXsize <= 0 || copiedYsize <= 0 || copiedZsize <= 0) {
            hud.didFailToPaste()
            return
        }

        val sel = brickSelection.getUnreversed()

        val x1 = min(sel.x0 + copiedXsize, App.bricks.xsize)
        val y1 = min(sel.y0 + copiedYsize, App.bricks.ysize)
        val z1 = min(sel.z0 + copiedZsize, App.bricks.zsize)

        if (sel.xsize != x1 - sel.x0 || sel.ysize != y1 - sel.y0 || sel.zsize != z1 - sel.z0) {
            brickSelection.set(sel.x0, sel.y0, sel.z0, x1, y1, z1)
        } else {
            val undoList = mutableListOf<Brick>()
            var i = 0
            val brick = Brick()
            val bfa = BrickFaceAssigner()

            for (iz in 0 ..< copiedZsize) {
                for (iy in 0 ..< copiedYsize) {
                    for (ix in 0 ..< copiedXsize) {
                        val x = ix + sel.x0
                        val y = iy + sel.y0
                        val z = iz + sel.z0

                        if (x < App.bricks.xsize && y < App.bricks.ysize && z < App.bricks.zsize) {
                            undoList.add(Brick().also { App.bricks.getAtBrickCoord(x, y, z, it) })

                            brick.set(copiedBricks[i])
                            bfa.setFacesFromMaterialAndBrickCoords(brick, x, y, z)
                            App.bricks.setAtBrickCoord(x, y, z, brick)
                        }

                        i++
                    }
                }
            }

            val box = brickSelection.getUnreversed()
            undoStack.push(box, undoList)
            App.bricks.updateBricks(box, evenAdjacient = true)
            hud.updateStats()
        }
    }
}
