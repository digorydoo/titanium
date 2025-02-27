package ch.digorydoo.titanium.engine.editor.action

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.IdHash
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.Moment
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.camera.CameraProps.Mode
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.Selection
import ch.digorydoo.titanium.engine.editor.UndoStack
import ch.digorydoo.titanium.engine.editor.cursor.CursorGelHolder
import ch.digorydoo.titanium.engine.editor.statusbar.EditorStatusBar
import ch.digorydoo.titanium.engine.file.BrickVolumeFileWriter
import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.scene.Lighting
import kotlin.math.roundToInt

class EditorActions(
    private val cursor: CursorGelHolder,
    private val selection: Selection,
    private val status: EditorStatusBar,
    undoStack: UndoStack,
) {
    private var spawnPtIndex = -1
    private var lastSpawnPtType = ""
    private var lastRotation = 0.0f
    private val helper = ActionHelper(selection, status, undoStack)
    private val rotateBricksHandler = RotateBricksHandler(helper, selection)

    fun setActiveShape(shape: BrickShape) {
        status.setActiveShape(shape)
    }

    fun setActiveMaterial(mat: BrickMaterial) {
        status.setActiveMaterial(mat)
    }

    fun setShapeAndMaterialOfSelectedBricks() {
        helper.applyToAllSelectedBricks(status.shape, status.material)
    }

    fun removeSelectedBricks() {
        helper.applyToAllSelectedBricks(BrickShape.NONE, null)
    }

    fun applyShapeToSelectedBricks() {
        helper.applyToAllSelectedBricks(status.shape, null)
    }

    fun applyMaterialToSelectedBricks() {
        helper.applyToAllSelectedBricks(null, status.material)
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

    fun rotateSelection() {
        rotateBricksHandler.rotateSelection()
    }

    fun pickShapeMaterial() {
        val pos = MutablePoint3i().also { selection.getPosCentreInBrickCoords(it) }
        val brick = Brick().also { App.bricks.getAtBrickCoord(pos.x, pos.y, pos.z, it) }

        if (brick.shape == BrickShape.NONE) {
            App.dlg.showSnackbar(EngineTextId.EDITOR_CANNOT_PICK_UP)
        } else {
            status.setActiveShape(brick.shape)
            status.setActiveMaterial(brick.material)
            App.dlg.showSnackbar(App.i18n.format(EngineTextId.EDITOR_PICKED_UP, brick.shape, brick.material))
        }
    }

    fun movePlayerToCursorPos() {
        selection.collapseSelection()
        val pt = selection.getPosCentreInWorldCoords()
        App.player?.pos?.set(pt)
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

    fun jumpToPrevNextSpawnPt(forward: Boolean) {
        val spawnPt = getPrevNextSpawnPt(forward)

        if (spawnPt == null) {
            App.dlg.showSnackbar(EngineTextId.EDITOR_NO_SPAWN_PTS)
            return
        }

        lastSpawnPtType = spawnPt.spawnObjTypeAsString
        lastRotation = spawnPt.rotation
        val x = spawnPt.pos.x.roundToInt()
        val y = spawnPt.pos.y.roundToInt()
        val z = spawnPt.pos.z.roundToInt()
        selection.set(x, y, z)
    }

    private fun getPrevNextSpawnPt(forward: Boolean): SpawnPt? {
        val numSpawnPts = App.spawnMgr.numSpawnPts
        if (numSpawnPts <= 0) return null

        if (spawnPtIndex < 0) {
            // spawnPtIndex is uninitialized. Pick the one closest to the selection centre!
            spawnPtIndex = App.spawnMgr.indexOfClosestSpawnPt(selection.getPosCentreInWorldCoords())
            if (spawnPtIndex < 0) return null
        } else {
            // Pick the next one in the list, and graceously handle the case when spawn pts have been added or removed.
            spawnPtIndex = when (forward) {
                true -> (spawnPtIndex + 1 + numSpawnPts) % numSpawnPts
                false -> (spawnPtIndex - 1 + numSpawnPts) % numSpawnPts
            }
        }

        return App.spawnMgr.spawnPtAt(spawnPtIndex)
    }

    fun addNewSpawnPt(spawnObjType: String, rotation: Float = 0.0f) {
        val pt = selection.getPosCentreInWorldCoords().apply { z -= 0.5f }
        val id = IdHash().encode(Moment().toLong().toULong())

        if (App.spawnMgr.findSpawnPt(id) != null) {
            // Since all ids are created from Moment, this should not happen unless you manually assign ids.
            Log.error("Id not unique ($id), did you manually assign spawn pt ids?")
            return
        }

        val raw = mutableMapOf<String, String>()
        raw["id"] = id
        raw["spawnObjType"] = spawnObjType
        raw["x"] = pt.x.toString()
        raw["y"] = pt.y.toString()
        raw["z"] = pt.z.toString()
        raw["rotation"] = rotation.toString()
        App.spawnMgr.add(raw)
        lastSpawnPtType = spawnObjType
        lastRotation = rotation

        if (App.spawnMgr.findSpawnPt(id) == null) {
            // This should never happen and indicates a bug in spawnMgr.
            Log.error("Failed to retrieve spawn pt after insertion! id=$id")
        }
    }

    fun addAnotherSpawnPt() {
        if (lastSpawnPtType.isEmpty()) {
            App.dlg.showSnackbar(EngineTextId.EDITOR_NO_PREV_ADDED_SPAWN_PT)
        } else {
            addNewSpawnPt(lastSpawnPtType, lastRotation)
        }
    }

    fun jumpToSpawnPt(pt: SpawnPt, index: Int) {
        selection.set(pt.pos.x.toInt(), pt.pos.y.toInt(), pt.pos.z.toInt())
        spawnPtIndex = index
    }

    fun deleteSpawnPt(pt: SpawnPt) {
        App.spawnMgr.despawnAndRemove(pt)
    }

    fun moveSpawnPt(pt: SpawnPt, newPos: Point3f) {
        App.spawnMgr.despawn(pt)
        pt.pos.set(newPos)
        pt.spawn()
    }

    fun spawnPtChanged(pt: SpawnPt) {
        App.spawnMgr.despawn(pt)
        pt.spawn()
    }
}
