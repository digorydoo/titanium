package ch.digorydoo.titanium.engine.editor.menu

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.action.EditorActions
import ch.digorydoo.titanium.engine.gel.SpawnManager.SpawnPtAndDistance
import ch.digorydoo.titanium.engine.i18n.EngineTextId
import ch.digorydoo.titanium.engine.ui.choice.Choice
import ch.digorydoo.titanium.engine.ui.choice.FloatChoice
import ch.digorydoo.titanium.engine.ui.choice.TextChoice
import kotlin.math.roundToInt

class SpawnPtMenu(private val actions: EditorActions) {
    fun show(cursor: Point3f, isTopLevel: Boolean, onCancel: () -> Unit) {
        show(cursor, playSoundOnOpen = isTopLevel, playSoundOnDismiss = isTopLevel, onCancel)
    }

    private fun show(cursor: Point3f, playSoundOnOpen: Boolean, playSoundOnDismiss: Boolean, onCancel: () -> Unit) {
        val reopen = { show(cursor, playSoundOnOpen = false, playSoundOnDismiss, onCancel) }

        val choices = App.spawnMgr.findClosestSpawnPts(cursor, MAX_NUM_SPAWN_PTS_IN_MENU).map { pt ->
            val strDistance = pt.distance
                .takeIf { it > 1.0 }
                ?.let { " (${(it * 10).roundToInt() / 10.0f} m)" }
                ?: ""
            val text = "${pt.spawnPt.spawnObjTypeAsString}$strDistance"
            TextChoice(text) {
                actions.jumpToSpawnPt(pt.spawnPt, pt.spawnPtIdx)
                showEditSpawnPtMenu(pt, onBack = reopen)
            }
        }.toMutableList()

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
        val choices = App.spawnMgr.spawnObjTypeList
            .map { TextChoice(it) { actions.addNewSpawnPt(it) } }
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

    private fun showEditSpawnPtMenu(pt: SpawnPtAndDistance, onBack: () -> Unit) {
        val reopen = { showEditSpawnPtMenu(pt, onBack) }

        val choices = mutableListOf<Choice>(
            TextChoice("Move...") {
                showMoveRotateSpawnPtMenu(pt, onBack = reopen)
            }
        )

        val onChange = { actions.spawnPtChanged(pt.spawnPt) }
        choices.addAll(pt.spawnPt.getEditorChoices(onChange)) // the spawn pt's individual properties

        choices.add(TextChoice("Delete") { actions.deleteSpawnPt(pt.spawnPt) })
        choices.add(TextChoice(EngineTextId.BACK, onBack))

        // This log line is useful for copying the id from console output.
        Log.info(TAG, "Showing menu for ${pt.spawnPt.spawnObjTypeAsString} id=${pt.spawnPt.id}")

        App.dlg.showChoices(
            choices,
            0,
            questionText = "${pt.spawnPt.spawnObjTypeAsString}\n#${pt.spawnPt.id}",
            lastItemIsDismiss = true,
            playSoundOnOpen = false,
            playSoundOnDismiss = false,
        )
    }

    private fun showMoveRotateSpawnPtMenu(pt: SpawnPtAndDistance, onBack: () -> Unit) {
        val pos = MutablePoint3f(pt.spawnPt.pos)

        val choices = listOf(
            FloatChoice("X", initialValue = pos.x, step = MOVE_STEP, smallStep = MOVE_SMALL_STEP) {
                pos.x = it
                actions.moveSpawnPt(pt.spawnPt, pos)
            },
            FloatChoice("Y", initialValue = pos.y, step = MOVE_STEP, smallStep = MOVE_SMALL_STEP) {
                pos.y = it
                actions.moveSpawnPt(pt.spawnPt, pos)
            },
            FloatChoice("Z", initialValue = pos.z, step = MOVE_STEP, smallStep = MOVE_SMALL_STEP) {
                pos.z = it
                actions.moveSpawnPt(pt.spawnPt, pos)
            },
            TextChoice(EngineTextId.BACK, onBack),
        )

        App.dlg.showChoices(
            choices,
            0,
            questionText = "Moving ${pt.spawnPt.spawnObjTypeAsString}",
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
    }
}
