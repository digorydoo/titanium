package ch.digorydoo.titanium.engine.state

enum class EngineStateId(override val value: UShort): IStateId {
    CURRENT_SCENE(1u),
    CAMERA_PHI(2u),
    CAMERA_RHO(3u),
    CAMERA_SOURCE_PT(4u),
    CAMERA_TARGET_PT(5u),
    CAMERA_MODE(6u),
    STORY_CLOCK_HOURS_HAND(7u),
    STORY_CLOCK_MINUTES_HAND(8u),
    STORY_CLOCK_SECONDS_HAND(9u),
    ;
    // Use App.state.getStateId(intValue) to get either an EngineStateId or a GameStateId
}
