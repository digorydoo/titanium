package io.github.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.file.KDataOutputStream
import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.BuildConfig
import io.github.digorydoo.titanium.engine.brick.Brick
import io.github.digorydoo.titanium.engine.brick.BrickShape
import io.github.digorydoo.titanium.engine.brick.BrickVolume
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.utils.NotForProductionException
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File

class BrickVolumeFileWriter private constructor(private val stream: KDataOutputStream<FileMarker>) {
    private fun write(bricks: BrickVolume) {
        stream.write(FileMarker.BEGIN_BRICK_VOLUME)
        stream.write(FileMarker.BEGIN_HEADER)
        stream.writeUInt16(bricks.xsize)
        stream.writeUInt16(bricks.ysize)
        stream.writeUInt16(bricks.zsize)
        stream.write(FileMarker.END_HEADER)

        // TODO RLE encoding

        for (z in 0 ..< bricks.zsize) {
            for (y in 0 ..< bricks.ysize) {
                for (x in 0 ..< bricks.xsize) {
                    val brick = Brick().also { bricks.getAtBrickCoord(x, y, z, it) }

                    stream.writeInt16(brick.shape.id)

                    if (brick.shape != BrickShape.NONE) {
                        stream.writeInt16(brick.upFaceIdx)
                        stream.writeInt16(brick.downFaceIdx)
                        stream.writeInt16(brick.northFaceIdx)
                        stream.writeInt16(brick.eastFaceIdx)
                        stream.writeInt16(brick.southFaceIdx)
                        stream.writeInt16(brick.westFaceIdx)
                        stream.writeInt16(brick.material.value)
                    }
                }
            }
        }

        stream.write(FileMarker.END_BRICK_VOLUME)
    }

    companion object {
        private val TAG = Log.Tag("BrickVolumeFileWriter")

        fun writeFile(bricks: BrickVolume) {
            when {
                BuildConfig.isProduction -> throw NotForProductionException()
                bricks.filename.isEmpty() -> throw Exception("File name is empty!")
                else -> {
                    arrayOf(
                        App.assets.pathToPlayfield(bricks.filename),
                        App.assets.pathToDeveloperPlayfield(bricks.filename),
                    ).forEach { path ->
                        Log.info(TAG, "Writing $path")
                        File(path).outputStream()
                            .let { BufferedOutputStream(it) }
                            .let { DataOutputStream(it) }
                            .use { BrickVolumeFileWriter(KDataOutputStream(it)).write(bricks) }
                    }
                }
            }
        }
    }
}
