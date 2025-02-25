package ch.digorydoo.titanium.game.core

import ch.digorydoo.kutils.string.snakeCase
import ch.digorydoo.titanium.engine.sound.SoundManager.SampleId

internal enum class GameSampleId(
    override val id: Int, // must be >= 1000 (ids below 1000 belong to EngineSampleId)
    override val relPitch: Float,
    override val randomDetune: Float,
    override val gain: Float,
): SampleId {
    JUMP(/* ........ */ id = 1000, relPitch = 0.0f, randomDetune = 0.5f, gain = 1.0f),
    TOUCHDOWN(/* ... */ id = 1001, relPitch = 0.0f, randomDetune = 1.1f, gain = 0.5f),
    WALK1(/* ....... */ id = 1002, relPitch = 0.0f, randomDetune = 1.0f, gain = 0.2f),
    ;

    override val fileName = snakeCase("$this") + ".wav"
}
