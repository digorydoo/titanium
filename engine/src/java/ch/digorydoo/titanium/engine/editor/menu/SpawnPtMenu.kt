package ch.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.EditorState
import ch.digorydoo.titanium.engine.editor.EditorState.EditMode
import ch.digorydoo.titanium.engine.editor.action.EditHeightMapActions.Companion.HEIGHT_MAP_SPAWN_OBJ_TYPE
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.heightmap.HeightMapGel
import ch.digorydoo.titanium.engine.heightmap.HeightMapSpawnPt
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.ui.choice.Choice
import ch.digorydoo.titanium.engine.ui.choice.FloatChoice
import ch.digorydoo.titanium.engine.ui.choice.IntChoice
import ch.digorydoo.titanium.engine.ui.choice.TextChoice
import kotlin.math.roundToInt

internal class SpawnPtMenu(private val state: EditorState, private val actions: EditorActions) {
    fun show(cursor: Point3f, isTopLevel: Boolean, onCancel: () -> Unit) {
        show(cursor, playSoundOnOpen = isTopLevel, playSoundOnDismiss = isTopLevel, onCancel)
    }

    private fun show(cursor: Point3f, playSoundOnOpen: Boolean, playSoundOnDismiss: Boolean, onCancel: () -> Unit) {
        val reopen = { show(cursor, playSoundOnOpen = false, playSoundOnDismiss, onCancel) }

        val choices = App.spawnMgr.findClosestSpawnPts(cursor, MAX_NUM_SPAWN_PTS_IN_MENU)
            .map { pt ->
                val strDistance = pt.distance
                    .takeIf { it > 1.0 }
                    ?.let { " (${(it * 10).roundToInt() / 10.0f} m)" }
                    ?: ""
                val text = "${pt.spawnPt.spawnObjTypeAsString}$strDistance"
                TextChoice(text) {
                    actions.jumpToSpawnPt(pt.spawnPt)
                    showEditSpawnPtMenu(
                        pt.spawnPt,
                        playSoundOnOpen = false,
                        playSoundOnDismiss = false,
                        onBack = reopen
                    )
                }
            }
            .toMutableList()

        choices.add(0, TextChoice("New...") {
            showNewSpawnPtMenu(onBack = reopen)
        })

        choices.add(TextChoice(if (playSoundOnDismiss) EngineTextId.DONE else EngineTextId.BACK, onCancel))

        App.dlg.showChoices(
            choices,
            0,
            lastItemIsDismiss = true,
            playSoundOnOpen = playSoundOnOpen,
            playSoundOnDismiss = playSoundOnDismiss,
        )
    }

    private fun showNewSpawnPtMenu(onBack: () -> Unit) {
        val reopen = { showNewSpawnPtMenu(onBack) }

        val choices = App.spawnMgr.spawnObjTypeList
            .map {
                TextChoice(it) {
                    if (it == HEIGHT_MAP_SPAWN_OBJ_TYPE) {
                        showNewHeightMapMenu(reopen)
                    } else {
                        actions.addNewSpawnPt(it)
                    }
                }
            }
            .toMutableList()

        choices.add(TextChoice(EngineTextId.BACK, onBack))

        App.dlg.showChoices(
            choices,
            0,
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
    }

    private fun showNewHeightMapMenu(onBack: () -> Unit) {
        var xsize = 5.0f
        var ysize = 5.0f
        var numSamplesX = 5
        var numSamplesY = 5

        val choices: MutableList<Choice> = mutableListOf(
            IntChoice(
                "Num samples X",
                initialValue = numSamplesX,
                minValue = MIN_NUM_SAMPLES,
                maxValue = MAX_NUM_SAMPLES,
                onChange = { numSamplesX = it },
            ),
            IntChoice(
                "Num samples Y",
                initialValue = numSamplesY,
                minValue = MIN_NUM_SAMPLES,
                maxValue = MAX_NUM_SAMPLES,
                onChange = { numSamplesY = it },
            ),
            FloatChoice(
                "X size",
                initialValue = xsize,
                minValue = MIN_HEIGHT_MAP_SIZE,
                maxValue = MAX_HEIGHT_MAP_SIZE,
                step = 1.0f,
                smallStep = 0.1f,
                onChange = { xsize = it }
            ),
            FloatChoice(
                "Y size",
                initialValue = ysize,
                minValue = MIN_HEIGHT_MAP_SIZE,
                maxValue = MAX_HEIGHT_MAP_SIZE,
                step = 1.0f,
                smallStep = 0.1f,
                onChange = { ysize = it }
            ),
        )

        val initialIdx = choices.size
        choices.add(
            TextChoice("Create") {
                actions.addNewHeightMap(
                    xsize = xsize,
                    ysize = ysize,
                    numSamplesX = numSamplesX,
                    numSamplesY = numSamplesY
                )
            }
        )
        choices.add(TextChoice(EngineTextId.BACK, onBack))

        App.dlg.showChoices(
            choices,
            initialIdx,
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
    }

    fun showEditSpawnPtMenu(spawnPt: SpawnPt, onBack: () -> Unit) {
        showEditSpawnPtMenu(spawnPt, playSoundOnOpen = true, playSoundOnDismiss = true, onBack)
    }

    fun showEditSpawnPtMenu(
        spawnPt: SpawnPt,
        playSoundOnOpen: Boolean,
        playSoundOnDismiss: Boolean,
        onBack: () -> Unit,
    ) {
        val reopen = {
            showEditSpawnPtMenu(spawnPt, playSoundOnOpen = false, playSoundOnDismiss = playSoundOnDismiss, onBack)
        }

        val choices = mutableListOf<Choice>()

        if (spawnPt is HeightMapSpawnPt) {
            val heightMap = (App.content.find { it.spawnPt == spawnPt } as? HeightMapGel)?.heightMap

            if (heightMap != null) {
                if (state.editMode != EditMode.HEIGHT_MAP) {
                    choices.add(TextChoice("Enter edit mode") { actions.setHeightMapEditMode(heightMap, spawnPt) })
                } else {
                    choices.add(TextChoice("Exit edit mode") { actions.setBricksEditMode() })
                }
            }

            choices.add(TextChoice("Resize...") { showResizeHeightMapMenu(spawnPt, reopen) })
            choices.add(TextChoice("Resample...") { showResampleHeightMapMenu(spawnPt, reopen) })
        }

        choices.add(TextChoice("Move...") { showMoveRotateSpawnPtMenu(spawnPt, reopen) })

        val onChange = { actions.spawnPtChanged(spawnPt) }
        choices.addAll(spawnPt.getEditorChoices(onChange)) // the spawn pt's individual properties

        choices.add(TextChoice("Delete") { actions.deleteSpawnPt(spawnPt) })
        choices.add(TextChoice(EngineTextId.BACK, onBack))

        // This log line is useful for copying the id from console output.
        Log.info(TAG, "Showing menu for ${spawnPt.spawnObjTypeAsString} id=${spawnPt.id}")

        App.dlg.showChoices(
            choices,
            0,
            questionText = "${spawnPt.spawnObjTypeAsString}\n#${spawnPt.id}",
            lastItemIsDismiss = true,
            playSoundOnOpen = playSoundOnOpen,
            playSoundOnDismiss = playSoundOnDismiss,
        )
    }

    private fun showResizeHeightMapMenu(spawnPt: HeightMapSpawnPt, onBack: () -> Unit) {
        val gel = App.content.find { it.spawnPt == spawnPt } as? HeightMapGel
        val heightMap = gel?.heightMap

        if (heightMap == null) {
            Log.error(TAG, "Cannot find gel of $spawnPt, or gel has no heightmap")
            return
        }

        val choices = listOf(
            FloatChoice(
                "X size",
                initialValue = heightMap.xsize,
                minValue = MIN_HEIGHT_MAP_SIZE,
                maxValue = MAX_HEIGHT_MAP_SIZE,
                step = 1.0f,
                smallStep = 0.1f,
                onChange = {
                    heightMap.xsize = it
                    gel.heightMapChanged()
                }
            ),
            FloatChoice(
                "Y size",
                initialValue = heightMap.ysize,
                minValue = MIN_HEIGHT_MAP_SIZE,
                maxValue = MAX_HEIGHT_MAP_SIZE,
                step = 1.0f,
                smallStep = 0.1f,
                onChange = {
                    heightMap.ysize = it
                    gel.heightMapChanged()
                }
            ),
            TextChoice(EngineTextId.BACK, onBack),
        )

        App.dlg.showChoices(
            choices,
            0,
            questionText = "Resizing ${spawnPt.id}",
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
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

        val choices = listOf(
            IntChoice(
                "Num samples X",
                initialValue = numSamplesX,
                minValue = MIN_NUM_SAMPLES,
                maxValue = MAX_NUM_SAMPLES,
                onChange = { numSamplesX = it }
            ),
            IntChoice(
                "Y size",
                initialValue = numSamplesY,
                minValue = MIN_NUM_SAMPLES,
                maxValue = MAX_NUM_SAMPLES,
                onChange = { numSamplesY = it }
            ),
            TextChoice("Resample") { actions.resampleHeightMap(heightMap, gel, numSamplesX, numSamplesY) },
            TextChoice(EngineTextId.BACK, onBack),
        )

        App.dlg.showChoices(
            choices,
            0,
            questionText = "Resampling ${spawnPt.id}",
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
    }

    private fun showMoveRotateSpawnPtMenu(spawnPt: SpawnPt, onBack: () -> Unit) {
        val pos = MutablePoint3f(spawnPt.pos)

        val choices = listOf(
            FloatChoice("X", initialValue = pos.x, step = MOVE_STEP, smallStep = MOVE_SMALL_STEP) {
                pos.x = it
                actions.moveSpawnPt(spawnPt, pos)
            },
            FloatChoice("Y", initialValue = pos.y, step = MOVE_STEP, smallStep = MOVE_SMALL_STEP) {
                pos.y = it
                actions.moveSpawnPt(spawnPt, pos)
            },
            FloatChoice("Z", initialValue = pos.z, step = MOVE_STEP, smallStep = MOVE_SMALL_STEP) {
                pos.z = it
                actions.moveSpawnPt(spawnPt, pos)
            },
            TextChoice(EngineTextId.BACK, onBack),
        )

        App.dlg.showChoices(
            choices,
            0,
            questionText = "Moving ${spawnPt.spawnObjTypeAsString}",
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
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
