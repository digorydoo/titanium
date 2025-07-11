package ch.digorydoo.titanium.engine.heightmap

import java.nio.FloatBuffer

class HeightMap(
    val filename: String,
    val numSamplesX: Int,
    val numSamplesY: Int,
    val xsize: Float,
    val ysize: Float,
    val zvalues: FloatBuffer,
) {
    init {
        require(zvalues.limit() == numSamplesX * numSamplesY)
    }
}
