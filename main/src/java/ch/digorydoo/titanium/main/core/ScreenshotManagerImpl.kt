package ch.digorydoo.titanium.main.core

import ch.digorydoo.kutils.point.Point2i
import ch.digorydoo.kutils.string.toPrecision
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.core.FIXED_ASPECT_RATIO
import ch.digorydoo.titanium.engine.core.ScreenshotManager
import ch.digorydoo.titanium.engine.texture.ImageData
import ch.digorydoo.titanium.main.opengl.checkGLError
import org.lwjgl.glfw.GLFW.glfwGetFramebufferSize
import org.lwjgl.opengl.GL11.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ScreenshotManagerImpl: ScreenshotManager() {
    fun takeIfNecessary(window: Long) {
        if (listeners.isNotEmpty()) {
            if (!isAboutToTakeScreenshot) {
                // Skip this frame, take the screenshot on the next frame. We do this, because some UI gels may not
                // have had the time for properly hide themselves.
                isAboutToTakeScreenshot = true
            } else {
                isAboutToTakeScreenshot = false
                val screenshot = getScreenshot(window)
                listeners.forEach { it.onScreenshot(screenshot) }
                listeners.clear()
            }
        }
    }

    private fun getScreenshot(window: Long): ImageData {
        val raw = getRawScreenshot(window) // upside-down and may contain the black areas at the sides
        var ar = raw.width.toFloat() / raw.height
        Log.info(TAG, "Took screenshot of size ${raw.width}x${raw.height} (16:${(16 / ar).toPrecision(1)})")

        val result = if (App.prefs.stretchViewport) {
            // There are no black stripes, but we still have to flip the image and ensure the correct aspect ratio.
            val flipped = flipYAndCrop(raw, raw.width, raw.height)
            stretchToAspectRatio(flipped)
        } else {
            val sz = getCropSize(raw.width, raw.height)
            flipYAndCrop(raw, sz.x, sz.y)
        }

        ar = result.width.toFloat() / result.height
        Log.info(TAG, "Returning ${result.width}x${result.height} (16:${(16 / ar).toPrecision(1)})")
        return result
    }

    private fun getRawScreenshot(window: Long): ImageData {
        val widthArr = intArrayOf(0)
        val heightArr = intArrayOf(0)
        glfwGetFramebufferSize(window, widthArr, heightArr)
        val rawWidth = widthArr[0]
        val rawHeight = heightArr[0]

        val rawBuf = ByteBuffer.allocateDirect(rawWidth * rawHeight * 3).apply {
            order(ByteOrder.nativeOrder())
            position(0)
        }

        glPixelStorei(GL_PACK_ALIGNMENT, 1) // important, otherwise crash when width is not aligned
        glReadPixels(0, 0, rawWidth, rawHeight, GL_RGB, GL_UNSIGNED_BYTE, rawBuf)
        checkGLError()

        return ImageData(rawBuf, ImageData.Type.RGB8, rawWidth, rawHeight)
    }

    private fun getCropSize(imgWidth: Int, imgHeight: Int): Point2i {
        val resolutionMgr = Main.resolutionMgr
        val physicalAspectRatio = resolutionMgr.physicalAspectRatio
        val imgAspectRatio = imgWidth.toFloat() / imgHeight

        val relAspectRatio =
            if (imgAspectRatio < physicalAspectRatio) {
                physicalAspectRatio / imgAspectRatio
            } else {
                imgAspectRatio / physicalAspectRatio
            }

        val fitAspectRatio = imgAspectRatio * relAspectRatio

        return if (fitAspectRatio > FIXED_ASPECT_RATIO) {
            // The window is too wide, i.e. there are black stripes to the left and right.
            Point2i(
                (imgHeight.toFloat() * FIXED_ASPECT_RATIO / relAspectRatio).toInt(),
                imgHeight
            )
        } else {
            // The window is too high, i.e. there are black stripes at the top and bottom.
            Point2i(
                imgWidth,
                (imgWidth.toFloat() / FIXED_ASPECT_RATIO / relAspectRatio).toInt()
            )
        }
    }

    private fun flipYAndCrop(raw: ImageData, croppedWidth: Int, croppedHeight: Int): ImageData {
        val croppedBuf = ByteBuffer.allocateDirect(croppedWidth * croppedHeight * 3).apply {
            order(ByteOrder.nativeOrder())
        }

        var dx = (raw.width - croppedWidth) / 2
        var dy = (raw.height - croppedHeight) / 2

        if (dx < 0) {
            Log.warn(TAG, "dx is negative: $dx")
            dx = 0
        }

        if (dy < 0) {
            Log.warn(TAG, "dy is negative: $dy")
            dy = 0
        }

        val srcBuf = raw.buf
        var srcIdx = ((raw.height - 1 - dy) * raw.width + dx) * 3
        val srcRowBytes = raw.width * 3
        croppedBuf.position(0)

        (0 ..< croppedHeight).forEach {
            srcBuf.position(srcIdx)

            (0 ..< croppedWidth).forEach {
                croppedBuf.put(srcBuf.get()) // R
                croppedBuf.put(srcBuf.get()) // G
                croppedBuf.put(srcBuf.get()) // B
            }

            srcIdx -= srcRowBytes
        }

        return ImageData(croppedBuf, ImageData.Type.RGB8, croppedWidth, croppedHeight)
    }

    fun stretchToAspectRatio(src: ImageData): ImageData {
        val newHeight = (src.width.toFloat() / FIXED_ASPECT_RATIO).toInt()

        if (newHeight == src.height) {
            return src
        }

        val newBuf = ByteBuffer.allocateDirect(src.width * newHeight * 3).apply {
            order(ByteOrder.nativeOrder())
        }

        return ImageData(newBuf, ImageData.Type.RGB8, src.width, newHeight).apply {
            drawImageScaled(src, 0, 0, src.width, newHeight, antiAliasing = false)
        }
    }

    companion object {
        private val TAG = Log.Tag("ScreenshotManagerImpl")
    }
}
