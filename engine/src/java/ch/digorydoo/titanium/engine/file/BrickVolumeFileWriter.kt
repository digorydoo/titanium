package ch.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickVolume
import java.io.File

class BrickVolumeFileWriter private constructor(
    private val stream: MyDataOutputStream,
    private val bricks: BrickVolume,
) {
    private fun write() {
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
        fun writeFile(bricks: BrickVolume) {
            Log.info("PlayfieldFileWriter: Writing ${bricks.fileName}")

            // val path = Assets.pathToPlayfield(pf.fileName) -- NO, not into the build folder!
            val path = "/Users/pamberg/Develop/titanium/assets/playfields/${bricks.fileName}" // FIXME

            val file = File(path)
            MyDataOutputStream.use(file) {
                BrickVolumeFileWriter(it, bricks).write()
            }
        }
    }
}
