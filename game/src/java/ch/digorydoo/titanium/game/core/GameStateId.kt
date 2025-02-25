package ch.digorydoo.titanium.game.core

import ch.digorydoo.titanium.engine.state.IStateId

enum class GameStateId(override val value: UShort): IStateId {
    PLAYER_POS(1001u),
    ;
    // Use App.state.getStateId(intValue) to get either an EngineStateId or a GameStateId
}
