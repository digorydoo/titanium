package ch.digorydoo.titanium.engine.texture.texel_manip.rgb8

import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.titanium.engine.texture.ImageData
import ch.digorydoo.titanium.engine.texture.texel_manip.Blitter
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

internal class BlitterRGB8(
    private val imgRGB8: ByteBuffer,
    private val imgWidth: Int,
    private val imgHeight: Int,
): Blitter {
    override fun blit(src: ImageData, dstX: Int, dstY: Int) {
        if (src.type != ImageData.Type.RGB8) throw NotImplementedError()

        val dstLeft = max(0, dstX)
        val dstRight = min(imgWidth, dstX + src.width)
        val drawWidth = dstRight - dstLeft
        if (drawWidth <= 0) return

        val dstTop = max(0, dstY)
        val dstBottom = min(imgHeight, dstY + src.height)
        val drawHeight = dstBottom - dstTop
        if (drawHeight <= 0) return

        val dstBuf = imgRGB8
        val srcBuf = src.buf

        val dstRowBytes = imgWidth * 3
        val srcRowBytes = src.width * 3

        var dstIdx = (dstTop * imgWidth + dstLeft) * 3
        var srcIdx = ((dstTop - dstY) * src.width + (dstLeft - dstX)) * 3

        (0 ..< drawHeight).forEach {
            dstBuf.position(dstIdx)
            srcBuf.position(srcIdx)

            (0 ..< drawWidth).forEach {
                dstBuf.put(srcBuf.get()) // R
                dstBuf.put(srcBuf.get()) // G
                dstBuf.put(srcBuf.get()) // B
            }

            dstIdx += dstRowBytes
            srcIdx += srcRowBytes
        }
    }

    override fun blit(src: ImageData, dstX: Int, dstY: Int, colourMultiplier: Float) {
        throw NotImplementedError()
    }

    override fun blitScaled(
        src: ImageData,
        dstX: Int,
        dstY: Int,
        dstDrawWidth: Int,
        dstDrawHeight: Int,
        antiAliasing: Boolean,
    ) {
        require(!antiAliasing) { "Anti-aliasing not implemented" }

        val dstBuf = imgRGB8
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

        var dstIdx = (dstY * dstBufWidth + dstX) * 3
        val dstRowOffset = dstBufWidth * 3

        srcBuf.position(0)
        val scaleX = dstDrawWidth.toFloat() / srcBufWidth
        val scaleY = dstDrawHeight.toFloat() / srcBufHeight
        var prevSrcY = 0

        for (scanY in 0 ..< dstDrawHeight) {
            dstBuf.position(dstIdx)
            val srcY = (scanY / scaleY).toInt()

            for (scanX in 0 ..< dstDrawWidth) {
                val srcX = (scanX / scaleX).toInt()

                srcBuf.position((srcY * srcBufWidth + srcX) * 3)
                dstBuf.put(srcBuf.get()) // R
                dstBuf.put(srcBuf.get()) // G
                dstBuf.put(srcBuf.get()) // B
            }

            prevSrcY = srcY
            dstIdx += dstRowOffset
        }
    }

    override fun blur3x3() {
        throw NotImplementedError()
    }

    // Draws the extended edge around the parts of brick textures to simulate clamp-to-edge behaviour
    override fun extendEdge(edgeSize: Int, innerRect: Recti) {
        if (edgeSize <= 0) return

        val innerWidth = innerRect.width
        val innerHeight = innerRect.height
        require(innerWidth > 0)
        require(innerHeight > 0)

        val buf = imgRGB8
        val rowBytes = imgWidth * 3

        // Top edge

        var srcIdx = innerRect.top * rowBytes + innerRect.left * 3
        var dstIdx = srcIdx - rowBytes
        var dstOffset = edgeSize * rowBytes + 3
        var r: Byte
        var g: Byte
        var b: Byte

        (0 ..< innerWidth).forEach {
            buf.position(srcIdx)
            r = buf.get()
            g = buf.get()
            b = buf.get()
            srcIdx += 3

            (0 ..< edgeSize).forEach {
                buf.position(dstIdx)
                buf.put(r)
                buf.put(g)
                buf.put(b)
                dstIdx -= rowBytes
            }

            dstIdx += dstOffset
        }

        // Bottom edge

        srcIdx = (innerRect.bottom - 1) * rowBytes + innerRect.left * 3
        dstIdx = srcIdx + rowBytes
        dstOffset = -edgeSize * rowBytes + 3

        (0 ..< innerWidth).forEach {
            buf.position(srcIdx)
            r = buf.get()
            g = buf.get()
            b = buf.get()
            srcIdx += 3

            (0 ..< edgeSize).forEach {
                buf.position(dstIdx)
                buf.put(r)
                buf.put(g)
                buf.put(b)
                dstIdx += rowBytes
            }

            dstIdx += dstOffset
        }

        // Left edge

        srcIdx = innerRect.top * rowBytes + innerRect.left * 3
        dstIdx = srcIdx - edgeSize * 3

        (0 ..< innerHeight).forEach {
            buf.position(srcIdx)
            r = buf.get()
            g = buf.get()
            b = buf.get()
            srcIdx += rowBytes

            buf.position(dstIdx)

            (0 ..< edgeSize).forEach {
                buf.put(r)
                buf.put(g)
                buf.put(b)
            }

            dstIdx += rowBytes
        }

        // Right edge

        srcIdx = innerRect.top * rowBytes + (innerRect.right - 1) * 3
        dstIdx = srcIdx + 3

        (0 ..< innerHeight).forEach {
            buf.position(srcIdx)
            r = buf.get()
            g = buf.get()
            b = buf.get()
            srcIdx += rowBytes

            buf.position(dstIdx)

            (0 ..< edgeSize).forEach {
                buf.put(r)
                buf.put(g)
                buf.put(b)
            }

            dstIdx += rowBytes
        }

        // Top-left corner

        buf.position(innerRect.top * rowBytes + innerRect.left * 3)
        r = buf.get()
        g = buf.get()
        b = buf.get()

        dstIdx = (innerRect.top - edgeSize) * rowBytes + (innerRect.left - edgeSize) * 3

        (0 ..< edgeSize).forEach {
            buf.position(dstIdx)

            (0 ..< edgeSize).forEach {
                buf.put(r)
                buf.put(g)
                buf.put(b)
            }

            dstIdx += rowBytes
        }

        // Top-right corner

        buf.position(innerRect.top * rowBytes + (innerRect.right - 1) * 3)
        r = buf.get()
        g = buf.get()
        b = buf.get()

        dstIdx = (innerRect.top - edgeSize) * rowBytes + innerRect.right * 3

        (0 ..< edgeSize).forEach {
            buf.position(dstIdx)

            (0 ..< edgeSize).forEach {
                buf.put(r)
                buf.put(g)
                buf.put(b)
            }

            dstIdx += rowBytes
        }

        // Bottom-left corner

        buf.position((innerRect.bottom - 1) * rowBytes + innerRect.left * 3)
        r = buf.get()
        g = buf.get()
        b = buf.get()

        dstIdx = innerRect.bottom * rowBytes + (innerRect.left - edgeSize) * 3

        (0 ..< edgeSize).forEach {
            buf.position(dstIdx)

            (0 ..< edgeSize).forEach {
                buf.put(r)
                buf.put(g)
                buf.put(b)
            }

            dstIdx += rowBytes
        }

        // Bottom-right corner

        buf.position((innerRect.bottom - 1) * rowBytes + (innerRect.right - 1) * 3)
        r = buf.get()
        g = buf.get()
        b = buf.get()

        dstIdx = innerRect.bottom * rowBytes + innerRect.right * 3

        (0 ..< edgeSize).forEach {
            buf.position(dstIdx)

            (0 ..< edgeSize).forEach {
                buf.put(r)
                buf.put(g)
                buf.put(b)
            }

            dstIdx += rowBytes
        }
    }
}
