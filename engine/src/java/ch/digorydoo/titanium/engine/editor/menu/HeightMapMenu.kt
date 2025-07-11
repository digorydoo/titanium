package ch.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.heightmap.HeightMapSpawnPt
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.ui.choice.Choice
import ch.digorydoo.titanium.engine.ui.choice.FloatChoice
import ch.digorydoo.titanium.engine.ui.choice.IntChoice
import ch.digorydoo.titanium.engine.ui.choice.TextChoice
import kotlin.math.roundToInt

internal class HeightMapMenu(private val mainMenu: EditorMenu, private val actions: EditorActions) {
    fun show(cursor: Point3f, isTopLevel: Boolean, onCancel: () -> Unit) {
        show(cursor, playSoundOnOpen = isTopLevel, playSoundOnDismiss = isTopLevel, onCancel)
    }

    private fun show(cursor: Point3f, playSoundOnOpen: Boolean, playSoundOnDismiss: Boolean, onCancel: () -> Unit) {
        val reopen = { show(cursor, playSoundOnOpen = false, playSoundOnDismiss, onCancel) }

        val choices = App.spawnMgr
            .findClosestSpawnPts(cursor, MAX_NUM_SPAWN_PTS_IN_MENU) { it is HeightMapSpawnPt }
            .map { pt ->
                val strDistance = pt.distance
                    .takeIf { it > 1.0 }
                    ?.let { " (${(it * 10).roundToInt() / 10.0f} m)" }
                    ?: ""
                val text = "${pt.spawnPt.id}$strDistance"
                TextChoice(text) {
                    actions.jumpToSpawnPt(pt.spawnPt)
                    showEditHeightMapMenu(pt.spawnPt, onBack = reopen)
                }
            }
            .toMutableList()

        choices.add(0, TextChoice("New...") {
            showNewHeightMapMenu(onBack = reopen)
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

    private fun showNewHeightMapMenu(onBack: () -> Unit) {
        var xsize = 5.0f
        var ysize = 5.0f
        var numSamplesX = 5
        var numSamplesY = 5

        val choices: MutableList<Choice> = mutableListOf(
            IntChoice("Num samples X", initialValue = numSamplesX, minValue = 3, maxValue = 100) { numSamplesX = it },
            IntChoice("Num samples Y", initialValue = numSamplesY, minValue = 3, maxValue = 100) { numSamplesY = it },
            FloatChoice(
                "X size",
                initialValue = xsize,
                minValue = 1.0f,
                maxValue = 50.0f,
                step = 1.0f,
                smallStep = 0.1f,
                onChange = { xsize = it }
            ),
            FloatChoice(
                "Y size",
                initialValue = ysize,
                minValue = 1.0f,
                maxValue = 50.0f,
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

    private fun showEditHeightMapMenu(spawnPt: SpawnPt, onBack: () -> Unit) {
        val reopen = { showEditHeightMapMenu(spawnPt, onBack) }

        val choices = mutableListOf<Choice>(
            TextChoice("Spawn point...") { mainMenu.showEditSpawnPtMenu(spawnPt, reopen) }
        )

        choices.add(TextChoice(EngineTextId.BACK, onBack))

        // This log line is useful for copying the id from console output.
        Log.info(TAG, "Showing menu for ${spawnPt.spawnObjTypeAsString} id=${spawnPt.id}")

        App.dlg.showChoices(
            choices,
            0,
            questionText = "${spawnPt.spawnObjTypeAsString}\n#${spawnPt.id}",
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
    }

    companion object {
        private val TAG = Log.Tag("HeightMapMenu")
        private const val MAX_NUM_SPAWN_PTS_IN_MENU = 10
    }
}
