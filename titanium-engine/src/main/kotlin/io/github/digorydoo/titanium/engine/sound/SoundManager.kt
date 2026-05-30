package io.github.digorydoo.titanium.engine.sound

import ch.digorydoo.kutils.vector.Vector3f

interface SoundManager {
    interface SampleId {
        val id: Int
        val fileName: String
        val relPitch: Float
        val randomDetune: Float
        val gain: Float
    }

    fun initialize()
    fun tearDown()

    fun play(smp: SampleId, worldPt: Vector3f? = null, volume: Float = 1.0f)
}
