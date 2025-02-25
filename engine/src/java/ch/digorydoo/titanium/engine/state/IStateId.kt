package ch.digorydoo.titanium.engine.state

interface IStateId {
    val value: UShort
    // Use App.state.getStateId(intValue) to get either an EngineStateId or a GameStateId
}
