package io.github.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.editor.action.EditHeightMapActions.Companion.HEIGHT_MAP_SPAWN_OBJ_TYPE
import io.github.digorydoo.titanium.engine.editor.action.EditorActions
import io.github.digorydoo.titanium.engine.i18n.EngineTextId
import io.github.digorydoo.titanium.engine.intermission.Intermission
import kotlin.math.roundToInt

internal class SpawnPtMenu(private val editSpawnPtMenu: EditSpawnPtMenu, private val actions: EditorActions) {
    fun showInIntermission(cursor: Vector3f) {
        App.intermissions.begin {
            showImpl(cursor, hasParentMenu = false)
        }
    }

    suspend fun Intermission.show(cursor: Vector3f) {
        showImpl(cursor, hasParentMenu = true)
    }

    private suspend fun Intermission.showImpl(cursor: Vector3f, hasParentMenu: Boolean) {
        do {
            var reopen = false
            showDlg {
                item {
                    text = "New..."
                    onSelect = {
                        showNewSpawnPtMenu()
                        reopen = true
                    }
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
                            editSpawnPtMenu.run { show(pt.spawnPt) }
                            reopen = true
                        }
                    }
                }

                dismiss = item { textId = if (hasParentMenu) EngineTextId.BACK else EngineTextId.DONE }
            }
        } while (reopen)
    }

    private suspend fun Intermission.showNewSpawnPtMenu() {
        do {
            var reopen = false
            showDlg {
                App.spawnMgr.spawnObjTypeList.forEach { type ->
                    item {
                        text = type
                        onSelect = {
                            if (type == HEIGHT_MAP_SPAWN_OBJ_TYPE) {
                                showNewHeightMapMenu()
                                reopen = true
                            } else {
                                actions.addNewSpawnPt(type)
                            }
                        }
                    }
                }

                dismiss = item { textId = EngineTextId.BACK }
            }
        } while (reopen)
    }

    private suspend fun Intermission.showNewHeightMapMenu() {
        var xsize = 5.0f
        var ysize = 5.0f
        var numSamplesX = 5
        var numSamplesY = 5

        showDlg {
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
            }
        }
    }

    companion object {
        private const val MAX_NUM_SPAWN_PTS_IN_MENU = 10
        const val MIN_NUM_SAMPLES = 3
        const val MAX_NUM_SAMPLES = 100
        const val MIN_HEIGHT_MAP_SIZE = 1.0f
        const val MAX_HEIGHT_MAP_SIZE = 50.0f
    }
}
