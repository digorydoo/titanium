package ch.digorydoo.titanium.engine.anim

import ch.digorydoo.titanium.engine.sound.SoundManager.SampleId

/**
 * Definition of a 2D frame animation cycle
 */
class AnimCycleDef(
    val firstFrame: Int,                    // index of first frame
    lastFrame: Int? = null,                 // index of last frame; null=same as firstFrame (don't cycle)
    val cycleDuration: Float = 0.0f,        // number of seconds of one cycle; 0=don't cycle
    val numCycles: Int = 0,                 // 0 = repeat forever, n = stop after n cycles
    val startCycleSound: SampleId? = null,  // sound played at start of cycle
    val halfCycleSound: SampleId? = null,   // sound played when cycle is half through
) {
    val lastFrame = lastFrame ?: firstFrame
    val halfFrame = firstFrame + (this.lastFrame + 1 - firstFrame) / 2
}
