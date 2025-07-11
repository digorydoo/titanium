package ch.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.FileMarker.*
import ch.digorydoo.titanium.engine.heightmap.HeightMap
import java.io.File
import java.nio.FloatBuffer

class HeightMapFileReader private constructor(private val stream: MyDataInputStream, private val filename: String) {
    private fun read(): HeightMap {
        stream.readExpected(BEGIN_HEIGHT_MAP)
        var finished = false

        var numSamplesX = 0
        var numSamplesY = 0
        var xsize = 0.0f
        var ysize = 0.0f
        var zvalues: FloatBuffer? = null

        while (!finished) {
            val marker = stream.readMarker()

            when (marker) {
                NUM_SAMPLES_X -> numSamplesX = stream.readInt16()
                NUM_SAMPLES_Y -> numSamplesY = stream.readInt16()
                X_SIZE -> xsize = stream.readFloat()
                Y_SIZE -> ysize = stream.readFloat()
                Z_VALUES -> zvalues = stream.readFloatBuffer()
                END_HEIGHT_MAP -> finished = true
                else -> throw Exception("Unexpected marker in height map file: $marker")
            }
        }

        require(numSamplesX > 0)
        require(numSamplesY > 0)
        require(xsize > 0.0f)
        require(ysize > 0.0f)
        require(zvalues != null)

        return HeightMap(filename, numSamplesX, numSamplesY, xsize, ysize, zvalues)
    }

    companion object {
        private val TAG = Log.Tag("HeightMapFileReader")

        fun read(filename: String): HeightMap {
            val path = App.assets.pathToHeightMap(filename)
            Log.info(TAG, "Reading $path")

            return MyDataInputStream.use(File(path)) {
                HeightMapFileReader(it, filename).read()
            }
        }
    }
}
