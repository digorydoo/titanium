package ch.digorydoo.titanium.main.sound

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.sound.SoundManager
import ch.digorydoo.titanium.engine.sound.SoundManager.SampleId
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10.*
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10.*
import org.lwjgl.openal.ALCapabilities
import java.nio.ByteBuffer
import java.nio.IntBuffer

class SoundManagerImpl: SoundManager {
    private var alcDevice = 0L
    private var alcContext = 0L
    private var alCaps: ALCapabilities? = null
    private var fxSources = mutableListOf<SndSource>()
    private var nextFxSource = 0
    private val samples = mutableMapOf<Int, Sample>()

    override fun initialize() {
        require(alCaps == null) { "Do not call initialize twice!" }

        alcDevice = alcOpenDevice(null as ByteBuffer?)

        if (alcDevice == 0L) {
            Log.error("SoundManager: Failed to open device! Sound will be disabled!")
            return
        }

        alcContext = alcCreateContext(alcDevice, null as IntBuffer?)

        if (!alcMakeContextCurrent(alcContext)) {
            Log.error("SoundManager: Failed to make context current! Sound will be disabled!")
            return
        }

        val alcCaps = ALC.createCapabilities(alcDevice)
        alCaps = AL.createCapabilities(alcCaps)

        Log.info("OpenAL version: " + alGetString(AL_VERSION))
        checkALError()

        for (i in 0 ..< NUM_FX_SOURCES) {
            fxSources.add(SndSource.create())
        }

        alListener3f(AL_POSITION, 0.0f, 0.0f, 0.0f)
        checkALError()

        alListener3f(AL_VELOCITY, 0.0f, 0.0f, 0.0f)
        checkALError()

        // The volume is too low with a gain of 1.0f, so let's increase it a bit.
        alListenerf(AL_GAIN, 2.0f)
    }

    override fun tearDown() {
        Log.info("SoundManager.tearDown")

        if (alCaps == null) {
            Log.warn("SoundManager wasn't properly initialized!")
            return
        }

        checkALError()

        fxSources.forEach { source -> source.free() }
        samples.forEach { (_, sample) -> sample.free() }

        // NOTE: checkALError will fail after clearing the context, so don't check it after this point!

        alcMakeContextCurrent(0L)
        alcDestroyContext(alcContext)
        alcCloseDevice(alcDevice)

        alcContext = 0L
        alcDevice = 0L
        alCaps = null
    }

    override fun play(smp: SampleId, worldPt: Point3f?, volume: Float) {
        if (alCaps == null) {
            return // OpenAL failed to initialize
        }

        val source = fxSources[nextFxSource++]

        if (nextFxSource >= NUM_FX_SOURCES) {
            nextFxSource = 0
        }

        try {
            val sample = samples[smp.id] ?: Sample.load(smp).also { samples[smp.id] = it }
            sample.play(source, worldPt, volume)
        } catch (e: Exception) {
            Log.error("SoundManagerImpl: Play sound failed: $smp: ${e.message}")
        }
    }

    companion object {
        private const val NUM_FX_SOURCES = 4

        fun checkALError(ctx: String? = null) {
            val err = alGetError()

            if (err != AL_NO_ERROR) {
                val strErr = when (err) {
                    AL_INVALID_NAME -> "Error: AL_INVALID_NAME"
                    AL_INVALID_ENUM -> "Error: AL_INVALID_ENUM"
                    AL_INVALID_VALUE -> "Error: AL_INVALID_VALUE"
                    AL_INVALID_OPERATION -> "Error: AL_INVALID_OPERATION"
                    AL_OUT_OF_MEMORY -> "Error: AL_OUT_OF_MEMORY"
                    else -> "Unknown AL error $err"
                }
                throw Exception("$strErr ${if (ctx == null) "" else " ($ctx)"}")
            }
        }
    }
}
