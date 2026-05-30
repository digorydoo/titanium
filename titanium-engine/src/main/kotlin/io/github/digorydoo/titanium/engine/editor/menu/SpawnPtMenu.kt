package io.github.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.vector.MutableVector3f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.editor.EditorState
import io.github.digorydoo.titanium.engine.editor.EditorState.EditMode
import io.github.digorydoo.titanium.engine.editor.action.EditHeightMapActions.Companion.HEIGHT_MAP_SPAWN_OBJ_TYPE
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.titanium.engine.heightmap.HeightMapGel
import io.github.digorydoo.titanium.engine.heightmap.HeightMapSpawnPt
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import kotlin.math.roundToInt

internal class SpawnPtMenu(private val state: EditorState, private val actions: EditorActions) {
    fun show(cursor: Vector3f, onBack: (() -> Unit)?) {
        val reopen = { show(cursor, onBack) }

        App.dlg.showDlg<Unit> {
            item {
                text = "New..."
                onSelect = { showNewSpawnPtMenu(onBack = reopen) }
            }

            App.spawnMgr.findClosestSpawnPts(cursor, MAX_NUM_SPAWN_PTS_IN_MENU).forEach { pt ->
                val strDistance = pt.distance
                    .takeIf { it > 1.0 }
                    ?.let { " (${(it * 10).roundToInt() / 10.0f} m)" }
                    ?: ""

                item {
                    text = "${pt.spawnPt.spawnObjTypeAsString}$strDistance"
                    onSelect = {
                        actions.jumpToSpawnPt(pt.spawnPt)
                        showEditSpawnPtMenu(pt.spawnPt, onBack = reopen)
                    }
                }
            }

            dismiss = item {
                textId = if (onBack == null) EngineTextId.DONE else EngineTextId.BACK
                onSelect = onBack
            }
        }
    }

    private fun showNewSpawnPtMenu(onBack: () -> Unit) {
        val reopen = { showNewSpawnPtMenu(onBack) }

        App.dlg.showDlg<Unit> {
            App.spawnMgr.spawnObjTypeList.forEach { type ->
                item {
                    text = type
                    onSelect = {
                        if (type == HEIGHT_MAP_SPAWN_OBJ_TYPE) {
                            showNewHeightMapMenu(reopen)
                        } else {
                            actions.addNewSpawnPt(type)
                        }
                    }
                }
            }

            dismiss = item {
                textId = EngineTextId.BACK
                onSelect = onBack
            }
        }
    }

    private fun showNewHeightMapMenu(onBack: () -> Unit) {
        var xsize = 5.0f
        var ysize = 5.0f
        var numSamplesX = 5
        var numSamplesY = 5

        App.dlg.showDlg<Unit> {
            itemWithIntValue {
                text = "Num samples X"
                initialValue = numSamplesX
                minValue = MIN_NUM_SAMPLES
                maxValue = MAX_NUM_SAMPLES
                onChange = { numSamplesX = it }
            }
            itemWithIntValue {
                text = "Num samples Y"
                initialValue = numSamplesY
                minValue = MIN_NUM_SAMPLES
                maxValue = MAX_NUM_SAMPLES
                onChange = { numSamplesY = it }
            }
            itemWithFloatValue {
                text = "X size"
                initialValue = xsize
                minValue = MIN_HEIGHT_MAP_SIZE
                maxValue = MAX_HEIGHT_MAP_SIZE
                step = 1.0f
                smallStep = 0.1f
                onChange = { xsize = it }
            }
            itemWithFloatValue {
                text = "Y size"
                initialValue = ysize
                minValue = MIN_HEIGHT_MAP_SIZE
                maxValue = MAX_HEIGHT_MAP_SIZE
                step = 1.0f
                smallStep = 0.1f
                onChange = { ysize = it }
            }
            focus = item {
                text = "Create"
                onSelect = {
                    actions.addNewHeightMap(
                        xsize = xsize,
                        ysize = ysize,
                        numSamplesX = numSamplesX,
                        numSamplesY = numSamplesY
                    )
                }
            }
            dismiss = item {
                textId = EngineTextId.BACK
                onSelect = onBack
            }
        }
    }

    fun showEditSpawnPtMenu(spawnPt: SpawnPt, onBack: (() -> Unit)?) {
        val reopen = { showEditSpawnPtMenu(spawnPt, onBack) }

        // This log line is useful for copying the id from console output.
        Log.info(TAG, "Showing menu for ${spawnPt.spawnObjTypeAsString} id=${spawnPt.id}")

        App.dlg.showDlg<Unit> {
            text = "${spawnPt.spawnObjTypeAsString}\n#${spawnPt.id}"

            if (spawnPt is HeightMapSpawnPt) {
                val heightMap = (App.content.find { it.spawnPt == spawnPt } as? HeightMapGel)?.heightMap

                if (heightMap != null) {
                    if (state.editMode != EditMode.HEIGHT_MAP) {
                        item {
                            text = "Enter edit mode"
                            onSelect = { actions.setHeightMapEditMode(heightMap, spawnPt) }
                        }
                    } else {
                        item {
                            text = "Exit edit mode"
                            onSelect = { actions.setBricksEditMode() }
                        }
                    }
                }

                item {
                    text = "Resize..."
                    onSelect = { showResizeHeightMapMenu(spawnPt, reopen) }
                }
                item {
                    text = "Resample..."
                    onSelect = { showResampleHeightMapMenu(spawnPt, reopen) }
                }
            }

            item {
                text = "Move..."
                onSelect = { showMoveRotateSpawnPtMenu(spawnPt, reopen) }
            }

            spawnPt.buildEditorItems(
                dlgDef = this,
                onChange = { actions.spawnPtChanged(spawnPt) }
            )

            item {
                text = "Delete"
                onSelect = { actions.deleteSpawnPt(spawnPt) }
            }
            dismiss = item {
                textId = if (onBack == null) EngineTextId.DONE else EngineTextId.BACK
                onSelect = onBack
            }
        }
    }

    private fun showResizeHeightMapMenu(spawnPt: HeightMapSpawnPt, onBack: () -> Unit) {
        val gel = App.content.find { it.spawnPt == spawnPt } as? HeightMapGel
        val heightMap = gel?.heightMap

        if (heightMap == null) {
            Log.error(TAG, "Cannot find gel of $spawnPt, or gel has no heightmap")
            return
        }

        App.dlg.showDlg<Unit> {
            text = "Resizing ${spawnPt.id}"

            itemWithFloatValue {
                text = "X size"
                initialValue = heightMap.xsize
                minValue = MIN_HEIGHT_MAP_SIZE
                maxValue = MAX_HEIGHT_MAP_SIZE
                step = 1.0f
                smallStep = 0.1f
                onChange = {
                    heightMap.xsize = it
                    gel.heightMapChanged()
                }
            }
            itemWithFloatValue {
                text = "Y size"
                initialValue = heightMap.ysize
                minValue = MIN_HEIGHT_MAP_SIZE
                maxValue = MAX_HEIGHT_MAP_SIZE
                step = 1.0f
                smallStep = 0.1f
                onChange = {
                    heightMap.ysize = it
                    gel.heightMapChanged()
                }
            }
            dismiss = item {
                textId = EngineTextId.BACK
                onSelect = onBack
            }
        }
    }

    private fun showResampleHeightMapMenu(spawnPt: HeightMapSpawnPt, onBack: () -> Unit) {
        val gel = App.content.find { it.spawnPt == spawnPt } as? HeightMapGel
        val heightMap = gel?.heightMap

        if (heightMap == null) {
            Log.error(TAG, "Cannot find gel of $spawnPt, or gel has no heightmap")
            return
        }

        var numSamplesX = heightMap.numSamplesX
        var numSamplesY = heightMap.numSamplesY

        App.dlg.showDlg<Unit> {
            text = "Resampling ${spawnPt.id}"

            itemWithIntValue {
                text = "Num samples X"
                initialValue = numSamplesX
                minValue = MIN_NUM_SAMPLES
                maxValue = MAX_NUM_SAMPLES
                onChange = { numSamplesX = it }
            }
            itemWithIntValue {
                text = "Y size"
                initialValue = numSamplesY
                minValue = MIN_NUM_SAMPLES
                maxValue = MAX_NUM_SAMPLES
                onChange = { numSamplesY = it }
            }
            item {
                text = "Resample"
                onSelect = { actions.resampleHeightMap(heightMap, gel, numSamplesX, numSamplesY) }
            }
            dismiss = item {
                textId = EngineTextId.BACK
                onSelect = onBack
            }
        }
    }

    private fun showMoveRotateSpawnPtMenu(spawnPt: SpawnPt, onBack: () -> Unit) {
        val pos = MutableVector3f(spawnPt.pos)

        App.dlg.showDlg<Unit> {
            text = "Moving ${spawnPt.spawnObjTypeAsString}"

            itemWithFloatValue {
                text = "X"
                initialValue = pos.x
                step = MOVE_STEP
                smallStep = MOVE_SMALL_STEP
                onChange = {
                    pos.x = it
                    actions.moveSpawnPt(spawnPt, pos)
                }
            }
            itemWithFloatValue {
                text = "Y"
                initialValue = pos.y
                step = MOVE_STEP
                smallStep = MOVE_SMALL_STEP
                onChange = {
                    pos.y = it
                    actions.moveSpawnPt(spawnPt, pos)
                }
            }
            itemWithFloatValue {
                text = "Z"
                initialValue = pos.z
                step = MOVE_STEP
                smallStep = MOVE_SMALL_STEP
                onChange = {
                    pos.z = it
                    actions.moveSpawnPt(spawnPt, pos)
                }
            }
            dismiss = item {
                textId = EngineTextId.BACK
                onSelect = onBack
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("SpawnPtMenu")

        private const val MAX_NUM_SPAWN_PTS_IN_MENU = 10
        private const val MOVE_STEP = 1.0f
        private const val MOVE_SMALL_STEP = 0.1f
        private const val MIN_NUM_SAMPLES = 3
        private const val MAX_NUM_SAMPLES = 100
        private const val MIN_HEIGHT_MAP_SIZE = 1.0f
        private const val MAX_HEIGHT_MAP_SIZE = 50.0f
    }
}
