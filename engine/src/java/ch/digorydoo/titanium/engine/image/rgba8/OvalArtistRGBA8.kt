package ch.digorydoo.titanium.engine.image.rgba8

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.titanium.engine.image.OvalArtist
import java.nio.ByteBuffer
import kotlin.math.sqrt

internal class OvalArtistRGBA8(
    private val imgRGBA8: ByteBuffer,
    private val imgWidth: Int,
    private val imgHeight: Int,
): OvalArtist {
    override fun fill(rect: Recti, c: Colour) {
        val cx: Int = (rect.right + rect.left) / 2
        val cy: Int = (rect.bottom + rect.top) / 2
        fillTLQuarter(rect.left, rect.top, cx, cy, c)
        fillTRQuarter(cx, rect.top, rect.right, cy, c)
        fillBRQuarter(cx, cy, rect.right, rect.bottom, c)
        fillBLQuarter(rect.left, cy, cx, rect.bottom, c)
    }

    override fun draw(rect: Recti, c: Colour) {
        val cx: Int = (rect.right + rect.left) / 2
        val cy: Int = (rect.bottom + rect.top) / 2
        drawTLQuarter(rect.left, rect.top, cx, cy, c)
        drawTRQuarter(cx, rect.top, rect.right, cy, c)
        drawBRQuarter(cx, cy, rect.right, rect.bottom, c)
        drawBLQuarter(rect.left, cy, cx, rect.bottom, c)
    }

    override fun fillTLQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour) {
        val r = c.redByte
        val g = c.greenByte
        val b = c.blueByte
        val a = c.alphaByte
        val imgWidth = imgWidth
        val imgHeight = imgHeight
        val buf = imgRGBA8

        val maxx = right - left
        val maxy = bottom - top

        for (scany in 0 ..< maxy) {
            val y = top + scany

            if (y >= 0 && y < imgHeight) {
                val yw4 = y * imgWidth * 4
                val rely = 1.0f - (scany.toFloat() + 0.5f) / (maxy - 1)
                val startx = maxx - (0.5f + sqrt(1.0f - rely * rely) * maxx).toInt()

                for (scanx in startx ..< maxx) {
                    val x = scanx + left

                    if (x >= 0 && x < imgWidth) {
                        var scan = yw4 + (x * 4)
                        buf.put(scan++, r)
                        buf.put(scan++, g)
                        buf.put(scan++, b)
                        buf.put(scan, a)
                    }
                }
            }
        }
    }

    override fun drawTLQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour) {
        val r = c.redByte
        val g = c.greenByte
        val b = c.blueByte
        val a = c.alphaByte
        val imgWidth = imgWidth
        val imgHeight = imgHeight
        val buf = imgRGBA8

        val maxx = right - left
        val maxy = bottom - top
        var prevStartX = maxx - 1

        for (scany in 0 ..< maxy) {
            val y = top + scany

            if (y >= 0 && y < imgHeight) {
                val yw4 = y * imgWidth * 4
                val rely = 1.0f - (scany.toFloat() + 0.5f) / (maxy - 1)
                val startx = maxx - (0.5f + sqrt(1.0f - rely * rely) * maxx).toInt()

                for (scanx in startx .. prevStartX) {
                    val x = scanx + left

                    if (x >= 0 && x < imgWidth) {
                        var scan = yw4 + (x * 4)
                        buf.put(scan++, r)
                        buf.put(scan++, g)
                        buf.put(scan++, b)
                        buf.put(scan, a)
                    }
                }

                prevStartX = startx
            }
        }
    }

    override fun fillTRQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour) {
        val r = c.redByte
        val g = c.greenByte
        val b = c.blueByte
        val a = c.alphaByte
        val imgWidth = imgWidth
        val imgHeight = imgHeight
        val buf = imgRGBA8

        val maxx = right - left
        val maxy = bottom - top

        for (scany in 0 ..< maxy) {
            val y = top + scany

            if (y >= 0 && y < imgHeight) {
                val yw4 = y * imgWidth * 4
                val rely = 1.0f - (scany.toFloat() + 0.5f) / (maxy - 1)
                val startx = maxx - (0.5f + sqrt(1.0f - rely * rely) * maxx).toInt()

                for (scanx in startx ..< maxx) {
                    val x = right - 1 - scanx

                    if (x >= 0 && x < imgWidth) {
                        var scan = yw4 + (x * 4)
                        buf.put(scan++, r)
                        buf.put(scan++, g)
                        buf.put(scan++, b)
                        buf.put(scan, a)
                    }
                }
            }
        }
    }

    override fun drawTRQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour) {
        val r = c.redByte
        val g = c.greenByte
        val b = c.blueByte
        val a = c.alphaByte
        val imgWidth = imgWidth
        val imgHeight = imgHeight
        val buf = imgRGBA8

        val maxx = right - left
        val maxy = bottom - top
        var prevStartX = maxx - 1

        for (scany in 0 ..< maxy) {
            val y = top + scany

            if (y >= 0 && y < imgHeight) {
                val yw4 = y * imgWidth * 4
                val rely = 1.0f - (scany.toFloat() + 0.5f) / (maxy - 1)
                val startx = maxx - (0.5f + sqrt(1.0f - rely * rely) * maxx).toInt()

                for (scanx in startx .. prevStartX) {
                    val x = right - 1 - scanx

                    if (x >= 0 && x < imgWidth) {
                        var scan = yw4 + (x * 4)
                        buf.put(scan++, r)
                        buf.put(scan++, g)
                        buf.put(scan++, b)
                        buf.put(scan, a)
                    }
                }

                prevStartX = startx
            }
        }
    }

    override fun fillBRQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour) {
        val r = c.redByte
        val g = c.greenByte
        val b = c.blueByte
        val a = c.alphaByte
        val imgWidth = imgWidth
        val imgHeight = imgHeight
        val buf = imgRGBA8

        val maxx = right - left
        val maxy = bottom - top

        for (scany in 0 ..< maxy) {
            val y = bottom - 1 - scany

            if (y >= 0 && y < imgHeight) {
                val yw4 = y * imgWidth * 4
                val rely = 1.0f - (scany.toFloat() + 0.5f) / (maxy - 1)
                val startx = maxx - (0.5f + sqrt(1.0f - rely * rely) * maxx).toInt()

                for (scanx in startx ..< maxx) {
                    val x = right - 1 - scanx

                    if (x >= 0 && x < imgWidth) {
                        var scan = yw4 + (x * 4)
                        buf.put(scan++, r)
                        buf.put(scan++, g)
                        buf.put(scan++, b)
                        buf.put(scan, a)
                    }
                }
            }
        }
    }

    override fun drawBRQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour) {
        val r = c.redByte
        val g = c.greenByte
        val b = c.blueByte
        val a = c.alphaByte
        val imgWidth = imgWidth
        val imgHeight = imgHeight
        val buf = imgRGBA8

        val maxx = right - left
        val maxy = bottom - top
        var prevStartX = maxx - 1

        for (scany in 0 ..< maxy) {
            val y = bottom - 1 - scany

            if (y >= 0 && y < imgHeight) {
                val yw4 = y * imgWidth * 4
                val rely = 1.0f - (scany.toFloat() + 0.5f) / (maxy - 1)
                val startx = maxx - (0.5f + sqrt(1.0f - rely * rely) * maxx).toInt()

                for (scanx in startx .. prevStartX) {
                    val x = right - 1 - scanx

                    if (x >= 0 && x < imgWidth) {
                        var scan = yw4 + (x * 4)
                        buf.put(scan++, r)
                        buf.put(scan++, g)
                        buf.put(scan++, b)
                        buf.put(scan, a)
                    }
                }

                prevStartX = startx
            }
        }
    }

    override fun fillBLQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour) {
        val r = c.redByte
        val g = c.greenByte
        val b = c.blueByte
        val a = c.alphaByte
        val imgWidth = imgWidth
        val imgHeight = imgHeight
        val buf = imgRGBA8

        val maxx = right - left
        val maxy = bottom - top

        for (scany in 0 ..< maxy) {
            val y = bottom - 1 - scany

            if (y >= 0 && y < imgHeight) {
                val yw4 = y * imgWidth * 4
                val rely = 1.0f - (scany.toFloat() + 0.5f) / (maxy - 1)
                val startx = maxx - (0.5f + sqrt(1.0f - rely * rely) * maxx).toInt()

                for (scanx in startx ..< maxx) {
                    val x = left + scanx

                    if (x >= 0 && x < imgWidth) {
                        var scan = yw4 + (x * 4)
                        buf.put(scan++, r)
                        buf.put(scan++, g)
                        buf.put(scan++, b)
                        buf.put(scan, a)
                    }
                }
            }
        }
    }

    override fun drawBLQuarter(left: Int, top: Int, right: Int, bottom: Int, c: Colour) {
        val r = c.redByte
        val g = c.greenByte
        val b = c.blueByte
        val a = c.alphaByte
        val imgWidth = imgWidth
        val imgHeight = imgHeight
        val buf = imgRGBA8

        val maxx = right - left
        val maxy = bottom - top
        var prevStartX = maxx - 1

        for (scany in 0 ..< maxy) {
            val y = bottom - 1 - scany

            if (y >= 0 && y < imgHeight) {
                val yw4 = y * imgWidth * 4
                val rely = 1.0f - (scany.toFloat() + 0.5f) / (maxy - 1)
                val startx = maxx - (0.5f + sqrt(1.0f - rely * rely) * maxx).toInt()

                for (scanx in startx .. prevStartX) {
                    val x = left + scanx

                    if (x >= 0 && x < imgWidth) {
                        var scan = yw4 + (x * 4)
                        buf.put(scan++, r)
                        buf.put(scan++, g)
                        buf.put(scan++, b)
                        buf.put(scan, a)
                    }
                }

                prevStartX = startx
            }
        }
    }
}
