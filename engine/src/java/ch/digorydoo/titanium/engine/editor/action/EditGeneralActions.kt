package ch.digorydoo.titanium.engine.editor.action

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.camera.CameraProps.Mode
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.Selection
import ch.digorydoo.titanium.engine.editor.cursor.CursorGelHolder
import ch.digorydoo.titanium.engine.editor.statusbar.EditorStatusBar
import ch.digorydoo.titanium.engine.file.BrickVolumeFileWriter
import ch.digorydoo.titanium.engine.scene.Lighting

internal class EditGeneralActions(
    private val cursor: CursorGelHolder,
    private val selection: Selection,
    private val status: EditorStatusBar,
) {
    fun movePlayerToCursorPos() {
        selection.collapseSelection()
        val pt = selection.getPosCentreInWorldCoords()
        App.player?.moveTo(pt)
    }

    fun setCameraMode(mode: Mode) {
        val wasInTopDownMode = App.camera.isInTopDownMode
        App.camera.mode = mode
        status.cameraModeChanged()

        if (wasInTopDownMode) {
            App.scene.lighting.set(Lighting.fineDay1800)
            App.scene.lightingFollowsStoryTime = true
            cursor.show()
        }
    }

    fun setCameraModeTopDown() {
        App.camera.setTopDownMode()
        status.cameraModeChanged()

        App.scene.lightingFollowsStoryTime = false
        App.scene.lighting.set(Lighting.fineDay1800)
        App.scene.lighting.haziness = 0.0f
        App.scene.lighting.skyColour1.set(Colour.black)
        App.scene.lighting.skyColour2.set(Colour.black)

        selection.set(App.bricks.xsize / 2, App.bricks.ysize / 2, App.bricks.zsize - 1)
        cursor.hide()
    }

    fun switchCameraTarget(backwards: Boolean) {
        // We rotate pos around the selection, because the camera follows pos.

        val sel = selection.get()
        val x0 = sel.x0
        val x1 = sel.x1
        val y0 = sel.y0
        val y1 = sel.y1
        val z0 = sel.z0
        val z1 = sel.z1

        when {
            backwards -> when {
                y0 < y1 -> when {
                    x0 < x1 -> selection.set(x1, y0, z1, x0, y1, z0)
                    else -> selection.set(x0, y1, z0, x1, y0, z1)
                }
                else -> when {
                    x0 < x1 -> selection.set(x0, y1, z0, x1, y0, z1)
                    else -> selection.set(x1, y0, z0, x0, y1, z1)
                }
            }
            y0 < y1 -> when {
                x0 < x1 -> selection.set(x0, y1, z0, x1, y0, z1)
                else -> selection.set(x1, y0, z1, x0, y1, z0)
            }
            else -> when {
                x0 < x1 -> selection.set(x1, y0, z0, x0, y1, z1)
                else -> selection.set(x0, y1, z0, x1, y0, z1)
            }
        }
    }

    fun setLighting(lgt: Lighting?) {
        if (lgt == null) {
            App.scene.lightingFollowsStoryTime = true
            App.scene.lighting.adaptToStoryTime()
        } else {
            App.scene.lightingFollowsStoryTime = false
            App.scene.lighting.set(lgt)
        }
    }

    fun setStoryTime(hours: Int, minutes: Int) {
        App.time.setStoryTime(hours, minutes)
    }

    fun printInfo() {
        val sel = selection.getUnreversed()

        if (sel.xsize != 1 || sel.ysize != 1 || sel.zsize != 1) {
            App.dlg.showMessage(
                """
                    Selected: $sel
                    Size: (${sel.xsize}, ${sel.ysize}, ${sel.zsize})
                """.trimIndent()
            )
        } else {
            val br = Brick()
            val subRelCoords = MutablePoint3i()

            App.bricks.getAtBrickCoord(
                sel.x0,
                sel.y0,
                sel.z0,
                br,
                outWorldCoords = null,
                outSubRelativeCoords = subRelCoords
            )

            val faces = arrayOf(
                "up=${br.upFaceIdx}",
                "dn=${br.downFaceIdx}",
                "N=${br.northFaceIdx}",
                "E=${br.eastFaceIdx}",
                "S=${br.southFaceIdx}",
                "W=${br.westFaceIdx}",
            ).joinToString(", ").trim()

            App.dlg.showMessage(
                """
                   Shape: ${br.shape.displayText}
                   Material: ${br.material.displayText}
                   Brick coords: (${sel.x0}, ${sel.y0}, ${sel.z0})
                   Subvolume-relative brick coords: $subRelCoords
                   Face indices: $faces
                """.trimIndent()
            )
        }
    }

    fun saveToFile() {
        BrickVolumeFileWriter.writeFile(App.bricks)
        App.spawnMgr.save(App.scene.gelListFileName)
        status.didSave()
    }
}
