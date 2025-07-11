package ch.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.file.FileMarker.*
import ch.digorydoo.titanium.engine.heightmap.HeightMap
import java.io.File

class HeightMapFileWriter private constructor(
    private val stream: MyDataOutputStream,
    private val heightMap: HeightMap,
) {
    private fun write() {
        stream.write(BEGIN_HEIGHT_MAP)
        stream.writeInt16(NUM_SAMPLES_X, heightMap.numSamplesX)
        stream.writeInt16(NUM_SAMPLES_Y, heightMap.numSamplesY)
        stream.write(X_SIZE, heightMap.xsize)
        stream.write(Y_SIZE, heightMap.ysize)

        stream.write(Z_VALUES)
        stream.write(heightMap.zvalues)

        stream.write(END_HEIGHT_MAP)
    }

    companion object {
        private val TAG = Log.Tag("HeightMapFileWriter")

        fun write(heightMap: HeightMap) {
            // val path = Assets.pathToHeightMap(heightMap.filename) -- NO, not into the build folder!
            val path = "/Users/pamberg/Develop/titanium/assets/heightmaps/${heightMap.filename}" // FIXME

            Log.info(TAG, "Writing $path")

            MyDataOutputStream.use(File(path)) {
                HeightMapFileWriter(it, heightMap).write()
            }
        }
    }
}
