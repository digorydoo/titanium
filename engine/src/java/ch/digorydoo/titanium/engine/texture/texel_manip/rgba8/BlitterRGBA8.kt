package ch.digorydoo.titanium.engine.texture.texel_manip.rgba8

import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.titanium.engine.texture.ImageData
import ch.digorydoo.titanium.engine.texture.texel_manip.Blitter
import java.nio.ByteBuffer

internal class BlitterRGBA8(
    private val imgRGBA8: ByteBuffer,
    private val imgWidth: Int,
    private val imgHeight: Int,
): Blitter {
    override fun blit(src: ImageData, dstX: Int, dstY: Int) {
        val dstBuf = imgRGBA8
        val dstWidth = imgWidth
        val dstHeight = imgHeight

        require(src.type == ImageData.Type.RGB8) { "Unsupported type: ${src.type}" }
        val srcBuf = src.buf
        val srcWidth = src.width
        val srcHeight = src.height

        // TODO implement proper clipping
        require(dstX in 0 ..< dstWidth)
        require(dstX + srcWidth in 0 .. dstWidth)
        require(dstY in 0 ..< dstHeight)
        require(dstY + srcHeight in 0 .. dstHeight)

        var dstIdx = (dstY * dstWidth + dstX) * 4
        val dstRowOffset = dstWidth * 4

        var srcIdx = 0
        var srcRowOffset = srcWidth * 3

        (dstY ..< dstY + srcHeight).forEach {
            dstBuf.position(dstIdx)
            srcBuf.position(srcIdx)

            (dstX ..< dstX + srcWidth).forEach {
                val r = srcBuf.get()
                val g = srcBuf.get()
                val b = srcBuf.get()

                // FIXME Is this correct? Currently untested!
                val newR = r.toUByte().toInt().toByte()
                val newG = g.toUByte().toInt().toByte()
                val newB = b.toUByte().toInt().toByte()

                dstBuf.put(newR)
                dstBuf.put(newG)
                dstBuf.put(newB)
                dstBuf.put(0xFF.toByte())
            }

            dstIdx += dstRowOffset
            srcIdx += srcRowOffset
        }
    }

    override fun blit(src: ImageData, dstX: Int, dstY: Int, colourMultiplier: Float) {
        val dstBuf = imgRGBA8
        val dstWidth = imgWidth
        val dstHeight = imgHeight

        require(src.type == ImageData.Type.RGB8) { "Unsupported type: ${src.type}" }
        val srcBuf = src.buf
        val srcWidth = src.width
        val srcHeight = src.height

        // TODO implement proper clipping
        require(dstX in 0 ..< dstWidth)
        require(dstX + srcWidth in 0 .. dstWidth)
        require(dstY in 0 ..< dstHeight)
        require(dstY + srcHeight in 0 .. dstHeight)

        var dstIdx = (dstY * dstWidth + dstX) * 4
        val dstRowOffset = dstWidth * 4

        var srcIdx = 0
        var srcRowOffset = srcWidth * 3

        (dstY ..< dstY + srcHeight).forEach {
            dstBuf.position(dstIdx)
            srcBuf.position(srcIdx)

            (dstX ..< dstX + srcWidth).forEach {
                val r = srcBuf.get()
                val g = srcBuf.get()
                val b = srcBuf.get()

                val newR = (r.toUByte().toFloat() * colourMultiplier).toInt().toByte()
                val newG = (g.toUByte().toFloat() * colourMultiplier).toInt().toByte()
                val newB = (b.toUByte().toFloat() * colourMultiplier).toInt().toByte()

                dstBuf.put(newR)
                dstBuf.put(newG)
                dstBuf.put(newB)
                dstBuf.put(0xFF.toByte())
            }

            dstIdx += dstRowOffset
            srcIdx += srcRowOffset
        }
    }

    override fun blitScaled(
        src: ImageData,
        dstX: Int,
        dstY: Int,
        dstDrawWidth: Int,
        dstDrawHeight: Int,
        antiAliasing: Boolean, // only implemented when scaling down; no effect when scaling up
    ) {
        val dst = imgRGBA8
        val dstBufWidth = imgWidth
        val dstBufHeight = imgHeight

        require(src.type == ImageData.Type.RGB8) { "Unsupported type: ${src.type}" }
        val srcBuf = src.buf
        val srcBufWidth = src.width
        val srcBufHeight = src.height

        // TODO implement proper clipping
        require(dstX in 0 ..< dstBufWidth)
        require(dstX + dstDrawWidth in 0 .. dstBufWidth)
        require(dstY in 0 ..< dstBufHeight)
        require(dstY + dstDrawHeight in 0 .. dstBufHeight)

        var dstIdx = (dstY * dstBufWidth + dstX) * 4
        val dstRowOffset = dstBufWidth * 4
        srcBuf.position(0)
        val scaleX = dstDrawWidth.toFloat() / srcBufWidth
        val scaleY = dstDrawHeight.toFloat() / srcBufHeight
        var prevSrcY = 0

        for (scanY in 0 ..< dstDrawHeight) {
            dst.position(dstIdx)
            val srcY = (scanY / scaleY).toInt()
            var prevSrcX = 0

            for (scanX in 0 ..< dstDrawWidth) {
                val srcX = (scanX / scaleX).toInt()

                if (!antiAliasing) {
                    srcBuf.position((srcY * srcBufWidth + srcX) * 3)
                    dst.put(srcBuf.get()) // R
                    dst.put(srcBuf.get()) // G
                    dst.put(srcBuf.get()) // B
                } else {
                    var rsum = 0.0f
                    var gsum = 0.0f
                    var bsum = 0.0f
                    var weight = 0

                    for (slurY in prevSrcY .. srcY) {
                        for (slurX in prevSrcX .. srcX) {
                            weight++
                            srcBuf.position((slurY * srcBufWidth + slurX) * 3)
                            rsum += srcBuf.get().toUByte().toFloat()
                            gsum += srcBuf.get().toUByte().toFloat()
                            bsum += srcBuf.get().toUByte().toFloat()
                        }
                    }

                    dst.put((rsum / weight).toInt().toByte()) // R
                    dst.put((gsum / weight).toInt().toByte()) // G
                    dst.put((bsum / weight).toInt().toByte()) // B
                    prevSrcX = srcX
                }

                dst.put(0xFF.toByte()) // A
            }

            prevSrcY = srcY
            dstIdx += dstRowOffset
        }
    }

    override fun blur3x3() {
        imgRGBA8.position(0)
        val srcBuf = imgRGBA8.duplicate() // copy all pixels
        blur3x3From(srcBuf, imgWidth, imgHeight)
    }

    private fun blur3x3From(srcBuf: ByteBuffer, width: Int, height: Int) {
        val dstBuf = imgRGBA8
        dstBuf.position(0)

        for (dstY in 0 ..< height) {
            for (dstX in 0 ..< width) {
                var rsum = 0.0f
                var gsum = 0.0f
                var bsum = 0.0f
                var wsum = 0.0f
                var srcPos = ((dstY - 1) * width + dstX - 1) * 4

                // Row above

                if (dstY < 1) {
                    srcPos += width * 4
                } else {
                    if (dstX >= 1) {
                        srcBuf.position(srcPos)
                        rsum += 0.7f * srcBuf.get().toUByte().toFloat()
                        gsum += 0.7f * srcBuf.get().toUByte().toFloat()
                        bsum += 0.7f * srcBuf.get().toUByte().toFloat()
                        wsum += 0.7f
                    }

                    srcPos += 4

                    srcBuf.position(srcPos)
                    rsum += 0.9f * srcBuf.get().toUByte().toFloat()
                    gsum += 0.9f * srcBuf.get().toUByte().toFloat()
                    bsum += 0.9f * srcBuf.get().toUByte().toFloat()
                    wsum += 0.9f

                    srcPos += 4

                    if (dstX < width - 1) {
                        srcBuf.position(srcPos)
                        rsum += 0.7f * srcBuf.get().toUByte().toFloat()
                        gsum += 0.7f * srcBuf.get().toUByte().toFloat()
                        bsum += 0.7f * srcBuf.get().toUByte().toFloat()
                        wsum += 0.7f
                    }

                    srcPos += (width - 2) * 4
                }

                // Middle row

                if (dstX >= 1) {
                    srcBuf.position(srcPos)
                    rsum += 0.9f * srcBuf.get().toUByte().toFloat()
                    gsum += 0.9f * srcBuf.get().toUByte().toFloat()
                    bsum += 0.9f * srcBuf.get().toUByte().toFloat()
                    wsum += 0.9f
                }

                srcPos += 4

                srcBuf.position(srcPos)
                rsum += srcBuf.get().toUByte().toFloat()
                gsum += srcBuf.get().toUByte().toFloat()
                bsum += srcBuf.get().toUByte().toFloat()
                val alpha = srcBuf.get()
                wsum += 1.0f

                srcPos += 4

                if (dstX < width - 1) {
                    srcBuf.position(srcPos)
                    rsum += 0.9f * srcBuf.get().toUByte().toFloat()
                    gsum += 0.9f * srcBuf.get().toUByte().toFloat()
                    bsum += 0.9f * srcBuf.get().toUByte().toFloat()
                    wsum += 0.9f
                }

                srcPos += (width - 2) * 4

                // Row below

                if (dstY < height - 1) {
                    if (dstX >= 1) {
                        srcBuf.position(srcPos)
                        rsum += 0.7f * srcBuf.get().toUByte().toFloat()
                        gsum += 0.7f * srcBuf.get().toUByte().toFloat()
                        bsum += 0.7f * srcBuf.get().toUByte().toFloat()
                        wsum += 0.7f
                    }

                    srcPos += 4

                    srcBuf.position(srcPos)
                    rsum += 0.9f * srcBuf.get().toUByte().toFloat()
                    gsum += 0.9f * srcBuf.get().toUByte().toFloat()
                    bsum += 0.9f * srcBuf.get().toUByte().toFloat()
                    wsum += 0.9f

                    srcPos += 4

                    if (dstX < width - 1) {
                        srcBuf.position(srcPos)
                        rsum += 0.7f * srcBuf.get().toUByte().toFloat()
                        gsum += 0.7f * srcBuf.get().toUByte().toFloat()
                        bsum += 0.7f * srcBuf.get().toUByte().toFloat()
                        wsum += 0.7f
                    }
                }

                // Set result
                dstBuf.put((rsum / wsum).toInt().toByte())
                dstBuf.put((gsum / wsum).toInt().toByte())
                dstBuf.put((bsum / wsum).toInt().toByte())
                dstBuf.put(alpha)
            }
        }
    }

    override fun extendEdge(edgeSize: Int, innerRect: Recti) {
        throw NotImplementedError()
    }
}
