package ch.digorydoo.titanium.engine.state

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.camera.CameraProps
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.SaveGameFileReader
import ch.digorydoo.titanium.engine.file.SaveGameFileWriter
import ch.digorydoo.titanium.engine.scene.ISceneId

abstract class StateManager {
    open class SerializedState(
        open val ints: Map<IStateId, Int>,
        open val floats: Map<IStateId, Float>,
        open val point3fs: Map<IStateId, Point3f>,
    )

    class MutableSerializedState(
        override val ints: MutableMap<IStateId, Int>,
        override val floats: MutableMap<IStateId, Float>,
        override val point3fs: MutableMap<IStateId, Point3f>,
    ): SerializedState(ints, floats, point3fs) {
        constructor(): this(mutableMapOf(), mutableMapOf(), mutableMapOf())
    }

    // All the values that are delegated to other objects and need to be restored by SceneLoader.
    // State values whose source of truth is StateManager or StateManagerImpl are not included here.
    abstract class RestoredState {
        var sceneId: ISceneId? = null
        var cameraPhi: Float? = null
        var cameraRho: Float? = null
        var cameraSource: Point3f? = null
        var cameraTarget: Point3f? = null
        var cameraMode: CameraProps.Mode? = null
        var storyClockHoursHand: Int? = null
        var storyClockMinutesHand: Int? = null
        var storyClockSecondsHand: Int? = null
    }

    abstract fun getStateId(value: UShort): IStateId?
    protected abstract fun clearGameSpecificState()
    protected abstract fun getSceneId(intId: Int): ISceneId
    protected abstract fun createNewRestoredState(): RestoredState
    protected abstract fun serializeGameSpecificValues(s: MutableSerializedState)
    protected abstract fun restoreGameSpecificValues(s: SerializedState, restoredState: RestoredState)

    fun loadFromFile(fileName: String) {
        try {
            val state = SaveGameFileReader.readContent(fileName)
            val restoredState = restore(state)
            val sceneId = restoredState.sceneId!!
            val scene = sceneId.createScene()
            App.load(scene, restore = restoredState)
        } catch (e: Exception) {
            Log.error(TAG, "Exception: ${e.message}")
            return
        }
    }

    fun saveToFile(summary: SaveGameFileWriter.Summary): Boolean {
        try {
            val state = serialize()
            SaveGameFileWriter.write(summary, state)
            return true
        } catch (e: Exception) {
            Log.error(TAG, "Exception: ${e.message}")
            return false
        }
    }

    fun clearAllState() {
        Log.info(TAG, "clearAllState called")

        // Nothing to do yet, because currently all state information is owned by other objects.
        //
        // EngineStateId.entries.forEach { id ->
        //     when (id) {
        //         EngineStateId.CURRENT_SCENE -> Unit
        //         EngineStateId.CAMERA_PHI -> Unit
        //         EngineStateId.CAMERA_RHO -> Unit
        //         EngineStateId.CAMERA_SOURCE_PT -> Unit
        //         EngineStateId.CAMERA_TARGET_PT -> Unit
        //         EngineStateId.CAMERA_MODE -> Unit
        //         EngineStateId.STORY_CLOCK_HOURS_HAND -> Unit
        //         EngineStateId.STORY_CLOCK_MINUTES_HAND -> Unit
        //         EngineStateId.STORY_CLOCK_SECONDS_HAND -> Unit
        //     }
        // }

        clearGameSpecificState()
    }

    private fun serialize(): SerializedState {
        val s = MutableSerializedState()

        val scene = App.scene
        val camera = App.camera
        val time = App.time

        EngineStateId.entries.forEach { id ->
            when (id) {
                EngineStateId.CURRENT_SCENE -> s.ints[id] = scene.id?.value ?: -1
                EngineStateId.CAMERA_PHI -> s.floats[id] = camera.currentPhi
                EngineStateId.CAMERA_RHO -> s.floats[id] = camera.currentRho
                EngineStateId.CAMERA_SOURCE_PT -> s.point3fs[id] = Point3f(camera.sourcePos)
                EngineStateId.CAMERA_TARGET_PT -> s.point3fs[id] = Point3f(camera.targetPos)
                EngineStateId.CAMERA_MODE -> s.ints[id] = camera.mode.value
                EngineStateId.STORY_CLOCK_HOURS_HAND -> s.ints[id] = time.storyClockHoursHand
                EngineStateId.STORY_CLOCK_MINUTES_HAND -> s.ints[id] = time.storyClockMinutesHand
                EngineStateId.STORY_CLOCK_SECONDS_HAND -> s.ints[id] = time.storyClockSecondsHand
            }
        }

        serializeGameSpecificValues(s)
        return SerializedState(s.ints, s.floats, s.point3fs)
    }

    private fun restore(s: SerializedState): RestoredState {
        val r = createNewRestoredState()

        EngineStateId.entries.forEach { id ->
            when (id) {
                EngineStateId.CURRENT_SCENE -> r.sceneId = s.ints[id]?.let { getSceneId(it) }
                EngineStateId.CAMERA_MODE -> r.cameraMode = s.ints[id]?.let { CameraProps.Mode.fromIntOrNull(it) }
                EngineStateId.CAMERA_PHI -> r.cameraPhi = s.floats[id]
                EngineStateId.CAMERA_RHO -> r.cameraRho = s.floats[id]
                EngineStateId.CAMERA_SOURCE_PT -> r.cameraSource = s.point3fs[id]
                EngineStateId.CAMERA_TARGET_PT -> r.cameraTarget = s.point3fs[id]
                EngineStateId.STORY_CLOCK_HOURS_HAND -> r.storyClockHoursHand = s.ints[id]
                EngineStateId.STORY_CLOCK_MINUTES_HAND -> r.storyClockMinutesHand = s.ints[id]
                EngineStateId.STORY_CLOCK_SECONDS_HAND -> r.storyClockSecondsHand = s.ints[id]
            }
        }

        restoreGameSpecificValues(s, r)
        return r
    }

    companion object {
        private val TAG = Log.Tag("StateManager")
    }
}
