package io.github.digorydoo.titanium.engine.editor.action

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.editor.BrickSelection
import io.github.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.titanium.engine.heightmap.HeightMapSpawnPt
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import io.github.digorydoo.kstruct.KstructBuilder

internal class EditSpawnPtActions(private val brickSelection: BrickSelection) {
    private val history = mutableListOf<SpawnPt>()
    private var historyIdx = -1
    private var typeOfLastAdd = ""
    private var rotationOfLastAdd = 0.0f

    fun jumpToNextSpawnPt() {
        if (history.isNotEmpty()) {
            val posOfSpawnPt = history[historyIdx].pos
            val posOfCursor = brickSelection.getTipPosInWorldCoords()

            if (posOfCursor.distanceTo(posOfSpawnPt) > MAX_DISTANCE_BEFORE_CLEAR_HISTORY) {
                // The cursor has moved too far from the spawnpt. The history may no longer be relevant.
                history.clear()
                historyIdx = -1
            }
        }

        if (historyIdx >= 0 && historyIdx < history.size - 1) {
            historyIdx++
            jumpToSpawnPt(history[historyIdx])
        } else {
            val spawnPt = App.spawnMgr.findClosestSpawnPt(brickSelection.getTipPosInWorldCoords()) {
                !history.contains(it)
            }

            if (spawnPt != null) {
                history.add(spawnPt)
                historyIdx = history.size - 1
                jumpToSpawnPt(spawnPt)
            } else if (history.isEmpty()) {
                App.dlg.showSnackbar(EngineTextId.EDITOR_NO_SPAWN_PTS)
            } else {
                historyIdx = 0
                jumpToSpawnPt(history[0])
            }
        }
    }

    fun jumpToPrevSpawnPt() {
        if (historyIdx >= 1) {
            historyIdx--
            jumpToSpawnPt(history[historyIdx])
        } else if (history.isNotEmpty()) {
            historyIdx = history.size - 1
            jumpToSpawnPt(history[historyIdx])
        }
    }

    fun addNewSpawnPt(spawnObjType: String, rotation: Float = 0.0f) {
        val id = App.spawnMgr.generateUniqueId(spawnObjType)
        val pos = brickSelection.getTipPosInWorldCoords().apply { z -= 0.5f }

        App.spawnMgr.add(
            KstructBuilder.build {
                set("id", id)
                set("type", spawnObjType)
                set("x", pos.x)
                set("y", pos.y)
                set("z", pos.z)
                set("rotation", rotation)
            }
        )

        typeOfLastAdd = spawnObjType
        rotationOfLastAdd = rotation

        history.clear()
        historyIdx = -1

        if (App.spawnMgr.findSpawnPt(id) == null) {
            // This should never happen and indicates a bug in spawnMgr.
            Log.error(TAG, "Failed to retrieve spawnpt after insertion! id=$id")
        }
    }

    fun addAnotherSpawnPt() {
        if (typeOfLastAdd.isEmpty()) {
            App.dlg.showSnackbar(EngineTextId.EDITOR_NO_PREV_ADDED_SPAWN_PT)
        } else {
            addNewSpawnPt(typeOfLastAdd, rotationOfLastAdd)
        }
    }

    fun jumpToSpawnPt(pt: SpawnPt) {
        brickSelection.set(pt.pos.x.toInt(), pt.pos.y.toInt(), pt.pos.z.toInt())
    }

    fun deleteSpawnPt(pt: SpawnPt) {
        App.spawnMgr.despawnAndRemove(pt)

        history.clear()
        historyIdx = -1
    }

    fun moveSpawnPt(pt: SpawnPt, newPos: Vector3f) {
        App.spawnMgr.despawn(pt)
        pt.pos.set(newPos)
        pt.spawn()
    }

    fun spawnPtChanged(pt: SpawnPt) {
        App.spawnMgr.despawn(pt)
        pt.spawn()
    }

    fun didAddNewHeightMap(spawnPt: HeightMapSpawnPt) {
        rotationOfLastAdd = spawnPt.rotation
        history.clear()
        historyIdx = -1
    }

    companion object {
        private val TAG = Log.Tag("EditSpawnPtActions")
        private const val MAX_DISTANCE_BEFORE_CLEAR_HISTORY = 10.0f // metres
    }
}
