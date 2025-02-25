package ch.digorydoo.titanium.engine.texture

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.colour.MutableColour
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.utils.Log
import java.nio.ByteBuffer
import kotlin.math.pow

// Maybe this should somehow be merged with ImageData
class GreyscaleImageBuffer {
    var bytes: ByteBuffer = ByteBuffer.allocateDirect(512) // will be reallocated when too small
    var width = 0
    var height = 0

    fun setSize(w: Int, h: Int) {
        width = w
        height = h

        val numBytes = w * h

        if (bytes.capacity() < numBytes) {
            bytes = ByteBuffer.allocateDirect(numBytes)
            Log.info("GreyscaleImageBuffer: Reallocated buffer to $numBytes bytes")
        }
    }

    fun applyToEachPixel(lambda: (value: Float) -> Float) {
        for (y in 0 ..< height) {
            for (x in 0 ..< width) {
                val index = x + width * y
                val value = lambda(((bytes.get(index)).toUInt() and 255u).toFloat() / 255.0f)
                bytes.put(index, (value * 255.0f).toUInt().toByte())
            }
        }
    }

    private fun unsafeGetAt(x: Int, y: Int): Float {
        val index = x + width * y
        return ((bytes.get(index)).toUInt() and 255u).toFloat() / 255.0f
    }

    private fun getAt(x: Int, y: Int): Float {
        return if (x in 0 ..< width && y in 0 ..< height) {
            unsafeGetAt(x, y)
        } else {
            0.0f
        }
    }

    fun blendOver(
        dstBuffer: ByteBuffer,
        dstX: Int,
        dstY: Int,
        dstTotalWidth: Int, // width of the entire destination buffer
        dstTotalHeight: Int, // height of the entire destination buffer
        colour: Colour,
    ) {
        // Clip the range against the boundaries of the dstBuffer.

        var srcX1 = 0
        var dstX1 = dstX
        var srcWidth = width

        if (dstX1 < 0) {
            srcX1 = -dstX1 // positive
            srcWidth -= srcX1
            dstX1 = 0
        }

        if (dstX1 > dstTotalWidth) return

        if (dstX1 + srcWidth > dstTotalWidth) {
            srcWidth = dstTotalWidth - dstX1
        }

        if (srcWidth <= 0) return

        var srcY1 = 0
        var dstY1 = dstY
        var srcHeight = height

        if (dstY1 < 0) {
            srcY1 = -dstY1 // positive
            if (srcY1 >= height) return
            srcHeight -= srcY1
            dstY1 = 0
        }

        if (dstY1 > dstTotalHeight) return

        if (dstY1 + srcHeight > dstTotalHeight) {
            srcHeight = dstTotalHeight - dstY1
        }

        if (srcHeight <= 0) return

        // Now draw the pixels.

        val srcX2 = srcX1 + srcWidth
        val srcY2 = srcY1 + srcHeight
        val dstRowSkip = (dstTotalWidth - srcWidth) * 4
        var dstIndex = (dstX1 + dstY1 * dstTotalWidth) * 4
        val c = MutableColour()

        for (y in srcY1 ..< srcY2) {
            for (x in srcX1 ..< srcX2) {
                // We treat a srcValue of 0 as transparent, and a srcValue of 1.0f as opaque.

                val srcValue = unsafeGetAt(x, y)

                if (srcValue <= 0.0f) {
                    dstIndex += 4 // skip this pixel
                } else {
                    c.set(
                        dstBuffer.get(dstIndex),
                        dstBuffer.get(dstIndex + 1),
                        dstBuffer.get(dstIndex + 2),
                        dstBuffer.get(dstIndex + 3),
                    )

                    c.mix(colour, srcValue)

                    dstBuffer.put(dstIndex++, c.redByte)
                    dstBuffer.put(dstIndex++, c.greenByte)
                    dstBuffer.put(dstIndex++, c.blueByte)
                    dstBuffer.put(dstIndex++, c.alphaByte)
                }
            }

            dstIndex += dstRowSkip
        }
    }

    fun blendOutlineOver(
        dstBuffer: ByteBuffer,
        dstX: Int,
        dstY: Int,
        dstTotalWidth: Int, // width of the entire destination buffer
        dstTotalHeight: Int, // height of the entire destination buffer
        colour: Colour,
    ) {
        // Clip the range against the boundaries of the dstBuffer.

        var srcX1 = -OUTLINE_RANGE
        var dstX1 = dstX - OUTLINE_RANGE
        var srcWidth = width + 2 * OUTLINE_RANGE

        if (dstX1 < 0) {
            srcX1 = -dstX1 // positive
            srcWidth -= srcX1
            dstX1 = 0
        }

        if (dstX1 > dstTotalWidth) return

        if (dstX1 + srcWidth > dstTotalWidth) {
            srcWidth = dstTotalWidth - dstX1
        }

        if (srcWidth <= 0) return

        var srcY1 = -OUTLINE_RANGE
        var dstY1 = dstY - OUTLINE_RANGE
        var srcHeight = height + 2 * OUTLINE_RANGE

        if (dstY1 < 0) {
            srcY1 = -dstY1 // positive
            if (srcY1 >= height) return
            srcHeight -= srcY1
            dstY1 = 0
        }

        if (dstY1 > dstTotalHeight) return

        if (dstY1 + srcHeight > dstTotalHeight) {
            srcHeight = dstTotalHeight - dstY1
        }

        if (srcHeight <= 0) return

        // Now draw the pixels.

        val srcX2 = srcX1 + srcWidth
        val srcY2 = srcY1 + srcHeight
        val dstRowSkip = (dstTotalWidth - srcWidth) * 4
        var dstIndex = (dstX1 + dstY1 * dstTotalWidth) * 4
        val c = MutableColour()

        for (y in srcY1 ..< srcY2) {
            for (x in srcX1 ..< srcX2) {
                // (x, y) may be outside of the source buffer. getAt will return 0 for these pixels.

                if (getAt(x, y) > 0) {
                    dstBuffer.put(dstIndex++, colour.redByte)
                    dstBuffer.put(dstIndex++, colour.greenByte)
                    dstBuffer.put(dstIndex++, colour.blueByte)
                    dstBuffer.put(dstIndex++, colour.alphaByte)
                } else {
                    var sum = 0.0f
                    var wsum = 0.0f
                    var wi = 0

                    for (wy in -OUTLINE_RANGE .. OUTLINE_RANGE) {
                        for (wx in -OUTLINE_RANGE .. OUTLINE_RANGE) {
                            val weight = outlineWeights[wi++]
                            wsum += weight
                            sum += weight * getAt(x + wx, y + wy)
                        }
                    }

                    val value = 1.0f - (1.0f - clamp(sum / wsum)).pow(20.0f)

                    if (value < 0.1f) {
                        // All neighbouring pixels are either transparent or almost transparent.
                        // Skip this pixel, since we're too far from the outline.
                        dstIndex += 4
                    } else {
                        c.set(
                            dstBuffer.get(dstIndex),
                            dstBuffer.get(dstIndex + 1),
                            dstBuffer.get(dstIndex + 2),
                            dstBuffer.get(dstIndex + 3),
                        )

                        c.mix(colour, value)

                        dstBuffer.put(dstIndex++, c.redByte)
                        dstBuffer.put(dstIndex++, c.greenByte)
                        dstBuffer.put(dstIndex++, c.blueByte)
                        dstBuffer.put(dstIndex++, c.alphaByte)
                    }
                }
            }

            dstIndex += dstRowSkip
        }
    }

    companion object {
        const val OUTLINE_RANGE = 3
        private val outlineWeights = arrayOf(
            0.15f, 0.28f, 0.37f, 0.40f, 0.37f, 0.28f, 0.15f,
            0.28f, 0.43f, 0.55f, 0.60f, 0.55f, 0.43f, 0.28f,
            0.37f, 0.55f, 0.72f, 0.80f, 0.72f, 0.55f, 0.37f,
            0.40f, 0.60f, 0.80f, 1.00f, 0.80f, 0.60f, 0.40f,
            0.37f, 0.55f, 0.72f, 0.80f, 0.72f, 0.55f, 0.37f,
            0.28f, 0.43f, 0.55f, 0.60f, 0.55f, 0.43f, 0.28f,
            0.15f, 0.28f, 0.37f, 0.40f, 0.37f, 0.28f, 0.15f,
        )
    }
}
