package ch.digorydoo.titanium.engine.sound

import ch.digorydoo.kutils.string.snakeCase
import ch.digorydoo.titanium.engine.sound.SoundManager.SampleId

internal enum class EngineSampleId(
    override val id: Int, // must be in 0..< 1000 (GameSampleId's ids start at 1000)
    override val relPitch: Float,
    override val randomDetune: Float,
    override val gain: Float,
): SampleId {
    BUTTON1(/* .......... */ id = 10, relPitch = 0.0f, randomDetune = 0.1f, gain = 1.0f),
    CHOICES_DISMISS(/* .. */ id = 11, relPitch = 0.0f, randomDetune = 0.1f, gain = 1.0f),
    CHOICES_SHOW(/* ..... */ id = 12, relPitch = 0.0f, randomDetune = 0.1f, gain = 1.0f),
    HILITE1(/* .......... */ id = 13, relPitch = 0.0f, randomDetune = 0.1f, gain = 0.9f),
    MENU_ENTER(/* ....... */ id = 14, relPitch = 0.0f, randomDetune = 0.1f, gain = 1.0f),
    MENU_INC_DEC(/* ..... */ id = 15, relPitch = 0.0f, randomDetune = 0.0f, gain = 0.6f),
    MENU_LEAVE(/* ....... */ id = 16, relPitch = 0.0f, randomDetune = 0.1f, gain = 1.0f),
    MSG_DISMISS(/* ...... */ id = 17, relPitch = 0.0f, randomDetune = 0.1f, gain = 1.0f),
    MSG_SHOW(/* ......... */ id = 18, relPitch = 0.0f, randomDetune = 0.1f, gain = 1.0f),
    NO_ACTION(/* ........ */ id = 19, relPitch = 0.0f, randomDetune = 0.0f, gain = 0.9f),
    SCENE_LOADED(/* ..... */ id = 20, relPitch = 0.0f, randomDetune = 0.0f, gain = 0.1f),
    ;

    override val fileName = snakeCase("$this") + ".wav"
}
