package io.github.digorydoo.titanium.engine.state

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.camera.CameraDirectingMode
import io.github.digorydoo.titanium.engine.camera.CameraInertia
import io.github.digorydoo.titanium.engine.camera.CameraInputMode
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.file.SaveGameFileReader
import io.github.digorydoo.titanium.engine.file.SaveGameFileWriter
import io.github.digorydoo.titanium.engine.scene.ISceneId

abstract class StateManager {
    interface SerializedState {
        val ints: Map<IStateId, Int>
        val floats: Map<IStateId, Float>
        val vector3fs: Map<IStateId, Vector3f>
    }

    class MutableSerializedState(
        override val ints: MutableMap<IStateId, Int>,
        override val floats: MutableMap<IStateId, Float>,
        override val vector3fs: MutableMap<IStateId, Vector3f>,
    ): SerializedState {
        constructor(): this(mutableMapOf(), mutableMapOf(), mutableMapOf())
    }

    // Class that contains the values that are delegated to other objects and need to be restored by SceneLoader.
    // State values whose source of truth is StateManager or StateManagerImpl are not included here.
    abstract class RestoredState {
        var sceneId: ISceneId? = null
        var cameraPhi: Float? = null
        var cameraRho: Float? = null
        var cameraSource: Vector3f? = null
        var cameraTarget: Vector3f? = null
        var cameraDirectingMode: CameraDirectingMode? = null
        var cameraInertia: CameraInertia? = null
        var cameraInputMode: CameraInputMode? = null
        var storyClockHoursHand: Int? = null
        var storyClockMinutesHand: Int? = null
        var storyClockSecondsHand: Int? = null
    }

    protected var currentState = MutableSerializedState()

    abstract fun getStateId(value: UShort): IStateId?
    protected abstract fun getSceneId(value: Int): ISceneId
    protected abstract fun createNewRestoredState(): RestoredState
    protected abstract fun serializeGameSpecificValues()
    protected abstract fun restoreGameSpecificValues(restoredState: RestoredState)

    fun loadFromFile(fileName: String) {
        try {
            currentState = SaveGameFileReader.readContent(fileName)
            val restoredState = restore()
            val sceneId = restoredState.sceneId!!
            val scene = sceneId.createScene()
            App.sceneLoader.load(scene, restore = restoredState)
        } catch (e: Exception) {
            Log.error(TAG, "Exception: ${e.message}")
            return
        }
    }

    fun saveToFile(summary: SaveGameFileWriter.Summary): Boolean {
        try {
            serialize()
            SaveGameFileWriter.write(summary, currentState)
            return true
        } catch (e: Exception) {
            Log.error(TAG, "Exception: ${e.message}")
            return false
        }
    }

    fun clearAllState() {
        Log.info(TAG, "clearAllState called")
        currentState = MutableSerializedState()
    }

    private fun serialize() {
        val s = currentState

        // Update state information whose source of truth lies outside StateManager.

        val scene = App.scene
        val camera = App.camera
        val time = App.time

        EngineStateId.entries.forEach { id ->
            when (id) {
                EngineStateId.CAMERA_DIRECTING_MODE -> s.ints[id] = camera.directingMode.value
                EngineStateId.CAMERA_INERTIA -> s.ints[id] = camera.inertia.id
                EngineStateId.CAMERA_INPUT_MODE -> s.ints[id] = camera.inputMode.value
                EngineStateId.CAMERA_PHI -> s.floats[id] = camera.currentPhi
                EngineStateId.CAMERA_RHO -> s.floats[id] = camera.currentRho
                EngineStateId.CAMERA_SOURCE_PT -> s.vector3fs[id] = Vector3f(camera.sourcePos)
                EngineStateId.CAMERA_TARGET_PT -> s.vector3fs[id] = Vector3f(camera.targetPos)
                EngineStateId.CURRENT_SCENE -> s.ints[id] = scene.id?.value ?: -1
                EngineStateId.STORY_CLOCK_HOURS_HAND -> s.ints[id] = time.storyClockHoursHand
                EngineStateId.STORY_CLOCK_MINUTES_HAND -> s.ints[id] = time.storyClockMinutesHand
                EngineStateId.STORY_CLOCK_SECONDS_HAND -> s.ints[id] = time.storyClockSecondsHand
            }
        }

        serializeGameSpecificValues()
    }

    private fun restore(): RestoredState {
        val s = currentState

        // Wrap state values that need to be restored by SceneLoader in a RestoredState object.
        val r = createNewRestoredState()

        EngineStateId.entries.forEach { id ->
            when (id) {
                EngineStateId.CAMERA_DIRECTING_MODE -> r.cameraDirectingMode =
                    s.ints[id]?.let { CameraDirectingMode.fromIntOrNull(it) }
                EngineStateId.CAMERA_INERTIA -> r.cameraInertia =
                    s.ints[id]?.let { CameraInertia.fromIntOrNull(it) }
                EngineStateId.CAMERA_INPUT_MODE -> r.cameraInputMode =
                    s.ints[id]?.let { CameraInputMode.fromIntOrNull(it) }
                EngineStateId.CAMERA_PHI -> r.cameraPhi = s.floats[id]
                EngineStateId.CAMERA_RHO -> r.cameraRho = s.floats[id]
                EngineStateId.CAMERA_SOURCE_PT -> r.cameraSource = s.vector3fs[id]
                EngineStateId.CAMERA_TARGET_PT -> r.cameraTarget = s.vector3fs[id]
                EngineStateId.CURRENT_SCENE -> r.sceneId = s.ints[id]?.let { getSceneId(it) }
                EngineStateId.STORY_CLOCK_HOURS_HAND -> r.storyClockHoursHand = s.ints[id]
                EngineStateId.STORY_CLOCK_MINUTES_HAND -> r.storyClockMinutesHand = s.ints[id]
                EngineStateId.STORY_CLOCK_SECONDS_HAND -> r.storyClockSecondsHand = s.ints[id]
            }
        }

        restoreGameSpecificValues(r)
        return r
    }

    companion object {
        private val TAG = Log.Tag("StateManager")
    }
}
