package io.github.digorydoo.titanium.game.core

import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.scene.ISceneId
import io.github.digorydoo.titanium.engine.state.EngineStateId
import io.github.digorydoo.titanium.engine.state.IStateId
import io.github.digorydoo.titanium.engine.state.StateManager

class StateManagerImpl: StateManager() {
    class RestoredStateImpl: RestoredState() {
        var playerPos: Vector3f? = null
    }

    override fun clearGameSpecificState() {
        // Nothing to do yet, because currently all state information is owned by other objects.
        //
        // GameStateId.entries.forEach { id ->
        //     when (id) {
        //         GameStateId.PLAYER_POS -> Unit
        //     }
        // }
    }

    override fun createNewRestoredState(): RestoredState {
        return RestoredStateImpl()
    }

    override fun getStateId(value: UShort): IStateId? =
        GameStateId.entries.find { it.value == value }
            ?: EngineStateId.entries.find { it.value == value }

    override fun getSceneId(intId: Int): ISceneId =
        SceneId.fromIntOrNull(intId) ?: throw Exception("Bad scene id: $intId")

    override fun serializeGameSpecificValues(s: MutableSerializedState) {
        GameStateId.entries.forEach { id ->
            when (id) {
                GameStateId.PLAYER_POS -> App.player?.pos?.let { s.vector3fs[id] = Vector3f(it) }
            }
        }
    }

    override fun restoreGameSpecificValues(s: SerializedState, restoredState: RestoredState) {
        val r = restoredState as RestoredStateImpl
        GameStateId.entries.forEach { id ->
            when (id) {
                GameStateId.PLAYER_POS -> r.playerPos = s.vector3fs[id]
            }
        }
    }
}
