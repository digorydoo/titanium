package ch.digorydoo.titanium.import_asset.brick_textures

import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.kutils.tty.Kokuban
import ch.digorydoo.kutils.tty.ShellCommandError
import ch.digorydoo.titanium.engine.image.ImageData
import ch.digorydoo.titanium.import_asset.Options
import ch.digorydoo.titanium.import_asset.Options.Verbosity
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.DataBufferByte
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.ceil

class BrickTextureImporter(private val options: Options) {
    private val kokuban = Kokuban()

    fun importFiles() {
        if (options.outFile.isEmpty()) {
            throw ShellCommandError("Please specify an output filename.")
        }

        val dstFile = File(options.outFile)

        if (dstFile.exists()) {
            if (dstFile.isDirectory) {
                throw ShellCommandError("Given out-file is a directory: ${options.outFile}")
            } else if (!dstFile.isFile) {
                throw ShellCommandError("Not a regular file: ${options.outFile}")
            } else if (!options.overwrite) {
                throw ShellCommandError("File already exists: ${options.outFile}")
            } else {
                dstFile.delete()
            }
        }

        val srcFileNames = options.extraArgs
            .filter { it.isNotEmpty() }
            .sorted()

        if (srcFileNames.isEmpty()) {
            throw ShellCommandError("The list of source files is empty.")
        }

        if (options.verbosity == Verbosity.VERBOSE) {
            kokuban.text("Processing ${srcFileNames.size} files").println()
        }

        val textures = mutableListOf<ImageData>()

        srcFileNames.forEach { src ->
            val srcFile = File(src)

            if (options.verbosity != Verbosity.QUIET) {
                kokuban.faint.text("${srcFile.path} ").plain.print()
            }

            if (options.verbosity == Verbosity.VERBOSE) {
                println()
            }

            textures.add(read(srcFile))

            if (options.verbosity == Verbosity.VERBOSE) {
                print("   ")
            }

            if (options.verbosity != Verbosity.QUIET) {
                kokuban.green.text("OK").plain.println()
            }
        }

        val combined = combine(textures)
        write(combined, dstFile)
    }

    private fun read(imgFile: File): ImageData {
        val img = ImageIO.read(imgFile)
        val cm: ColorModel? = img.colorModel

        if (options.verbosity == Verbosity.VERBOSE) {
            kokuban
                .text("   width=${img.width}")
                .text(", height=${img.height}")
                .text(", alpha=${cm?.hasAlpha()}")
                .text(", alphaPremult=${cm?.isAlphaPremultiplied()}")
                .text(", pxSize=${cm?.pixelSize}")
                .text(", numComponents=${cm?.numComponents}")
                .println()
        }

        require(img.width > 0) { "Bad width: ${img.width}" }
        require(img.height > 0) { "Bad height: ${img.height}" }
        require(cm != null) { "Color model is null" }
        require(!cm.hasAlpha()) { "Alpha not supported" }
        require(!cm.isAlphaPremultiplied()) { "Pre-multiplied alpha not supported" }
        require(cm.pixelSize == 24) { "Unsupported pixel size: ${cm.pixelSize}" }
        require(cm.numComponents == 3) { "Unsupported number of components: ${cm.numComponents}" }

        return img.toImageData()
    }

    private fun combine(parts: List<ImageData>): ImageData {
        val padding = options.padding
        require(padding >= 0)

        val arrangeAcross = options.arrangeAcross
        require(arrangeAcross > 1)

        require(parts.isNotEmpty())
        val numRows = ceil(parts.size.toFloat() / arrangeAcross).toInt()

        if (options.verbosity == Verbosity.VERBOSE) {
            println("Arranging ${parts.size} textures on a ${arrangeAcross}x${numRows} grid")
        }

        var partWidth = 0
        var partHeight = 0

        parts.forEach { part ->
            if (partWidth == 0) {
                partWidth = part.width
            } else {
                require(part.width == partWidth) { "Not all parts have the same width!" }
            }

            if (partHeight == 0) {
                partHeight = part.height
            } else {
                require(part.height == partHeight) { "Not all parts have the same height!" }
            }
        }

        require(partWidth > 0)
        require(partHeight > 0)

        val finalWidth = arrangeAcross * (partWidth + 2 * padding)
        val finalHeight = numRows * (partHeight + 2 * padding)

        if (options.verbosity == Verbosity.VERBOSE) {
            println("Final texture is ${finalWidth}x${finalHeight} pixels")
        }

        val dst = ImageData(ImageData.Type.RGB8, finalWidth, finalHeight)
        dst.clear()

        parts.forEachIndexed { i, part ->
            val x = padding + (i % arrangeAcross) * (partWidth + 2 * padding)
            val y = padding + (i / arrangeAcross).toInt() * (partHeight + 2 * padding)
            dst.drawImage(part, x, y)

            if (padding > 0) {
                dst.extendEdge(padding, Recti(x, y, x + part.width, y + part.height))
            }
        }

        return dst
    }

    private fun write(combined: ImageData, dstFile: File) {
        kokuban.text("Writing ${dstFile.extension.uppercase()} file: ${dstFile.path}").println()
        val bim = combined.toBufferedImage()
        ImageIO.write(bim, dstFile.extension, dstFile)
    }

    companion object {
        private fun BufferedImage.toImageData(): ImageData {
            require(type == BufferedImage.TYPE_3BYTE_BGR) { "Unsupported type: $type" }
            val imd = ImageData(ImageData.Type.RGB8, width, height)
            val dstData = imd.buf
            val srcData = (raster.dataBuffer as DataBufferByte).data
            val numPixels = width * height
            var j = 0

            dstData.position(0)

            (0 ..< numPixels).forEach {
                // srcData is BGR
                val b = srcData[j++]
                val g = srcData[j++]
                val r = srcData[j++]

                // dstData is RGB
                dstData.put(r)
                dstData.put(g)
                dstData.put(b)
            }

            return imd
        }

        private fun ImageData.toBufferedImage(): BufferedImage {
            require(type == ImageData.Type.RGB8) { "Unsupported type: $type" }
            val bim = BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
            val dstData = (bim.raster.dataBuffer as DataBufferByte).data
            val srcData = buf
            val numPixels = width * height
            var j = 0

            srcData.position(0)

            (0 ..< numPixels).forEach {
                // srcData is RGB
                val r = srcData.get()
                val g = srcData.get()
                val b = srcData.get()

                // dstData is BGR
                dstData[j++] = b
                dstData[j++] = g
                dstData[j++] = r
            }

            return bim
        }
    }
}
