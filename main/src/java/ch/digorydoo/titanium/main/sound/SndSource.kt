package ch.digorydoo.titanium.main.sound

import ch.digorydoo.kutils.point.Point3f
import org.lwjgl.openal.AL10.*

class SndSource private constructor(private var src: Int) {
    /**
     * @param sampleBuf: AL buffer slot
     * @param relFreq: relative frequency; 1.0f=sample pitch; 2.0f=+1 octave; 0.5f=-1 octave
     * @param gain: sample gain when worldPt is in the screen centre; 1.0f=maximum gain
     * @param worldPt: panning and final volume is computed from these world coords; null=centre
     */
    fun play(sampleBuf: Int, relFreq: Float, gain: Float, worldPt: Point3f?) {
        alSourceStop(src)
        alSourcef(src, AL_PITCH, relFreq)

        val gain2: Float
        val pan: Float

        if (worldPt == null) {
            gain2 = 1.0f
            pan = 0.0f
        } else {
            gain2 = 1.0f
            pan = 0.0f
            /*
            TODO implement this once the world coords are in metres
            val d = worldPt.distanceTo(World.camera.targetPos)

            gain2 =
                if (d <= GAIN_NOCHANGE_AREA) {
                    1.0f
                } else {
                    1.0f / (1.0 + (d - GAIN_NOCHANGE_AREA) / GAIN_FACTOR).toFloat()
                }

            pan = clamp(distanceLR / PANNING_FACTOR, -1.0f, 1.0f)
            */
        }

        // When y is 0.0f, panning just flips full left or full right if x != 0.
        // With y = 0.5f, panning seems to work like it should.
        alSource3f(src, AL_POSITION, pan, 0.5f, 0.0f)

        // Unfortunately, with y=0.5f (above), the sound is much less in volume!
        // We can't use a gain higher than 1 here. Instead, we increase the listener's
        // gain a bit (see class SoundManager).
        alSourcef(src, AL_GAIN, gain * gain2)

        alSource3f(src, AL_VELOCITY, 0.0f, 0.0f, 0.0f)
        alSourcei(src, AL_LOOPING, AL_FALSE)
        alSourcei(src, AL_BUFFER, sampleBuf)
        alSourcePlay(src)
    }

    fun free() {
        if (src < 0) {
            return
        }

        alSourceStop(src)
        SoundManagerImpl.checkALError()

        alDeleteSources(src)
        src = -1
        SoundManagerImpl.checkALError()
    }

    companion object {
        //private const val GAIN_NOCHANGE_AREA = 200 // distance in world coords where gain is unchanged
        //private const val GAIN_FACTOR = 48 // distance in world coords until gain is 0.5
        //private const val PANNING_FACTOR = 600 // distance in world coords for sound to become fully L or R

        fun create(): SndSource {
            val tmpArr = intArrayOf(0)
            alGenSources(tmpArr)
            val src = tmpArr[0]
            return SndSource(src)
        }
    }
}
