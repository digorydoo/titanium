package io.github.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.vector.MutableVector3f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.editor.EditorState
import io.github.digorydoo.titanium.engine.editor.EditorState.EditMode
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.editor.menu.SpawnPtMenu.Companion.MAX_HEIGHT_MAP_SIZE
import io.github.digorydoo.titanium.engine.editor.menu.SpawnPtMenu.Companion.MAX_NUM_SAMPLES
import io.github.digorydoo.titanium.engine.editor.menu.SpawnPtMenu.Companion.MIN_HEIGHT_MAP_SIZE
import io.github.digorydoo.titanium.engine.editor.menu.SpawnPtMenu.Companion.MIN_NUM_SAMPLES
import io.github.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.titanium.engine.heightmap.HeightMapGel
import io.github.digorydoo.titanium.engine.heightmap.HeightMapSpawnPt
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import io.github.digorydoo.titanium.engine.intermission.Intermission

internal class EditSpawnPtMenu(private val state: EditorState, private val actions: EditorActions) {
    fun showInIntermission(spawnPt: SpawnPt) {
        App.intermissions.begin {
            showImpl(spawnPt, hasParentMenu = false)
        }
    }

    suspend fun Intermission.show(spawnPt: SpawnPt) {
        showImpl(spawnPt, hasParentMenu = true)
    }

    private suspend fun Intermission.showImpl(spawnPt: SpawnPt, hasParentMenu: Boolean) {
        // This log line is useful for copying the id from console output.
        Log.info(TAG, "Showing menu for ${spawnPt.spawnObjTypeAsString} id=${spawnPt.id}")

        do {
            var reopen = false

            showDlg {
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
                        onSelect = {
                            showResizeHeightMapMenu(spawnPt)
                            reopen = true
                        }
                    }
                    item {
                        text = "Resample..."
                        onSelect = {
                            showResampleHeightMapMenu(spawnPt)
                            reopen = true
                        }
                    }
                }

                item {
                    text = "Move..."
                    onSelect = {
                        showMoveRotateSpawnPtMenu(spawnPt)
                        reopen = true
                    }
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
                    textId = if (hasParentMenu) EngineTextId.BACK else EngineTextId.DONE
                }
            }
        } while (reopen)
    }

    private suspend fun Intermission.showResizeHeightMapMenu(spawnPt: HeightMapSpawnPt) {
        val gel = App.content.find { it.spawnPt == spawnPt } as? HeightMapGel
        val heightMap = gel?.heightMap

        if (heightMap == null) {
            Log.error(TAG, "Cannot find gel of $spawnPt, or gel has no heightmap")
            return
        }

        showDlg {
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
            dismiss = item { textId = EngineTextId.BACK }
        }
    }

    private suspend fun Intermission.showResampleHeightMapMenu(spawnPt: HeightMapSpawnPt) {
        val gel = App.content.find { it.spawnPt == spawnPt } as? HeightMapGel
        val heightMap = gel?.heightMap

        if (heightMap == null) {
            Log.error(TAG, "Cannot find gel of $spawnPt, or gel has no heightmap")
            return
        }

        var numSamplesX = heightMap.numSamplesX
        var numSamplesY = heightMap.numSamplesY

        showDlg {
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
            dismiss = item { textId = EngineTextId.BACK }
        }
    }

    private suspend fun Intermission.showMoveRotateSpawnPtMenu(spawnPt: SpawnPt) {
        val pos = MutableVector3f(spawnPt.pos)

        showDlg {
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
            dismiss = item { textId = EngineTextId.BACK }
        }
    }

    companion object {
        private val TAG = Log.Tag("EditSpawnPtMenu")
        private const val MOVE_STEP = 1.0f
        private const val MOVE_SMALL_STEP = 0.1f
    }
}
