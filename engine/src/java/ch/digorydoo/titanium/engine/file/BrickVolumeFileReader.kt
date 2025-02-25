package ch.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.FileMarker.*
import java.io.File

class BrickVolumeFileReader private constructor(
    private val stream: MyDataInputStream,
    private val brickVolumeFileName: String,
    private val texFileName: String,
) {
    private fun read(): BrickVolume {
        stream.readExpected(BEGIN_BRICK_VOLUME)
        stream.readExpected(BEGIN_HEADER)
        val xsize = stream.readUInt16().toInt()
        val ysize = stream.readUInt16().toInt()
        val zsize = stream.readUInt16().toInt()
        stream.readExpected(END_HEADER)

        val tex = App.textures.getOrCreateTexture(texFileName)!!
        val volume = BrickVolume(xsize, ysize, zsize, brickVolumeFileName, tex)
        val brick = Brick()

        for (z in 0 ..< zsize) {
            for (y in 0 ..< ysize) {
                for (x in 0 ..< xsize) {
                    brick.shape = stream.readInt16().let { BrickShape.fromInt(it) }

                    if (brick.shape == BrickShape.NONE) {
                        brick.setInvalid()
                    } else {
                        brick.upFaceIdx = stream.readInt16()
                        brick.downFaceIdx = stream.readInt16()
                        brick.northFaceIdx = stream.readInt16()
                        brick.eastFaceIdx = stream.readInt16()
                        brick.southFaceIdx = stream.readInt16()
                        brick.westFaceIdx = stream.readInt16()
                        brick.material = stream.readInt16().let { BrickMaterial.fromInt(it) }

                        // Since the editor persists the faceIdx, the following needs to run whenever material faces
                        // have been re-shared among faces (BrickFaceAssigner)
                        // brick.brickCoords.set(x, y, z)
                        // brick.setFacesFromMaterialAndBrickCoords()
                    }

                    try {
                        volume.setAtBrickCoord(x, y, z, brick)
                    } catch (e: Exception) {
                        Log.error("BrickVolumeFileReader: setAtBrickCoord failed at $x, $y, $z")
                        throw e
                    }
                }
            }
        }

        stream.readExpected(END_BRICK_VOLUME)
        return volume
    }

    companion object {
        fun readFile(brickVolumeFileName: String, texFileName: String): BrickVolume {
            val path = App.assets.pathToPlayfield(brickVolumeFileName)
            val file = File(path)
            val brickVolume = MyDataInputStream.use(file) {
                BrickVolumeFileReader(it, brickVolumeFileName, texFileName).read()
            }
            val size = "${brickVolume.xsize}x${brickVolume.ysize}x${brickVolume.zsize}"
            Log.info("BrickVolumeFileReader: $brickVolumeFileName: $size")
            return brickVolume
        }
    }
}
