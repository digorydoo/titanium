package io.github.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.file.KDataOutputStream
import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.BuildConfig
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.file.FileMarker.*
import io.github.digorydoo.titanium.engine.heightmap.HeightMap
import io.github.digorydoo.titanium.engine.utils.NotForProductionException
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File

class HeightMapFileWriter private constructor(private val stream: KDataOutputStream<FileMarker>) {
    private fun write(heightMap: HeightMap) {
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
            when {
                BuildConfig.isProduction -> throw NotForProductionException()
                heightMap.filename.isEmpty() -> throw Exception("File name is empty!")
                else -> {
                    arrayOf(
                        App.assets.pathToHeightMap(heightMap.filename),
                        App.assets.pathToDeveloperHeightMap(heightMap.filename),
                    ).forEach { path ->
                        Log.info(TAG, "Writing $path")
                        File(path).outputStream()
                            .let { BufferedOutputStream(it) }
                            .let { DataOutputStream(it) }
                            .use { HeightMapFileWriter(KDataOutputStream(it)).write(heightMap) }
                    }
                }
            }
        }
    }
}
