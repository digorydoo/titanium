package ch.digorydoo.titanium.main.sound

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.toByteBuffer
import ch.digorydoo.titanium.engine.sound.SoundManager.SampleId
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL10.AL_FORMAT_MONO16
import org.lwjgl.openal.AL10.alDeleteBuffers
import kotlin.math.pow

class Sample private constructor(
    private var buf: Int,
    private val relPitch: Float, // 0=sample pitch; +12=+1 octave; -12=-1 octave
    private val randomDetune: Float, // 0=don't; 1=+1 semitone at most
    private val gain: Float, // 1=no change
) {
    fun play(source: SndSource, worldPt: Point3f?, volume: Float) {
        val detune = randomDetune * Math.random()
        val relFreq = 2.0.pow((relPitch + detune) / 12.0).toFloat() // 2 pow 0 == 1
        source.play(buf, relFreq, gain * volume, worldPt)
    }

    fun free() {
        if (buf < 0) {
            return
        }

        alDeleteBuffers(buf)
        buf = -1
        SoundManagerImpl.checkALError()
    }

    companion object {
        private val TAG = Log.Tag("Sample")

        private const val SAMPLING_FREQ = 44100

        fun load(smp: SampleId): Sample {
            Log.info(TAG, "Loading $smp")

            val tmpArr = intArrayOf(0)
            AL10.alGenBuffers(tmpArr)
            val buf = tmpArr[0]
            val wave = WAVFileReader.read(smp.fileName)
            val data = wave.arr.toByteBuffer()

            when {
                wave.samplingFreq == SAMPLING_FREQ && wave.numChannels == 1 && wave.bytesPerSample == 2 -> {
                    AL10.alBufferData(buf, AL_FORMAT_MONO16, data, SAMPLING_FREQ)
                    SoundManagerImpl.checkALError()
                }
                else -> {
                    throw Exception(
                        "Unsupported WAV format! " +
                            "samplingFreq=${wave.samplingFreq}, " +
                            "numChannels=${wave.numChannels}, " +
                            "bytesPerSample=${wave.bytesPerSample}"
                    )
                }
            }

            return Sample(buf, relPitch = smp.relPitch, randomDetune = smp.randomDetune, gain = smp.gain)
        }
    }
}
