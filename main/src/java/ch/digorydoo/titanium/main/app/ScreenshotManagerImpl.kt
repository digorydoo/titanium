package ch.digorydoo.titanium.main.app

import ch.digorydoo.kutils.string.toPrecision
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.core.App.Companion.FIXED_ASPECT_RATIO
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
            if (App.singleton?.isAboutToTakeScreenshot != true) {
                // Skip this frame, take the screenshot on the next frame. We do this, because some UI gels may not have
                // had the time for properly hide themselves.
                App.singleton?.isAboutToTakeScreenshot = true
            } else {
                App.singleton?.isAboutToTakeScreenshot = false
                val screenshot = getScreenshot(window)
                listeners.forEach { it.onScreenshot(screenshot) }
                listeners.clear()
            }
        }
    }

    private fun getScreenshot(window: Long): ImageData {
        val raw = getRawScreenshot(window) // upside-down and may contain the black areas at the sides
        val cropped = flipYAndCrop(raw) // may still have the wrong aspect-ratio if stretchViewport is enabled
        return scaleAndStretch(cropped) // has our FIXED_ASPECT_RATIO
    }

    private fun getRawScreenshot(window: Long): ImageData {
        val widthArr = intArrayOf(0)
        val heightArr = intArrayOf(0)
        glfwGetFramebufferSize(window, widthArr, heightArr)
        val rawWidth = widthArr[0]
        val rawHeight = heightArr[0]

        Log.info("Taking screenshot of size ${rawWidth}x${rawHeight}")

        val rawBuf = ByteBuffer.allocateDirect(rawWidth * rawHeight * 3).apply {
            order(ByteOrder.nativeOrder())
            position(0)
        }

        glPixelStorei(GL_PACK_ALIGNMENT, 1) // important, otherwise crash when width is not aligned
        glReadPixels(0, 0, rawWidth, rawHeight, GL_RGB, GL_UNSIGNED_BYTE, rawBuf)
        checkGLError()

        return ImageData(rawBuf, ImageData.Type.RGB8, rawWidth, rawHeight)
    }

    private fun flipYAndCrop(raw: ImageData): ImageData {
        val app = App.singleton as AppImpl

        val croppedWidth: Int
        val croppedHeight: Int

        if (app.prefs.stretchViewport) {
            // Nothing to crop.
            croppedWidth = raw.width
            croppedHeight = raw.height
        } else {
            val resolutionMgr = app.resolutionMgr
            val physicalAspectRatio = resolutionMgr.physicalAspectRatio
            val fbAspectRatio = raw.width.toFloat() / raw.height

            val relAspectRatio =
                if (fbAspectRatio < physicalAspectRatio) {
                    physicalAspectRatio / fbAspectRatio
                } else {
                    fbAspectRatio / physicalAspectRatio
                }

            Log.info("   framebuf aspect ratio 16:${(16 / fbAspectRatio).toPrecision(1)}")
            Log.info("   physical aspect ratio 16:${(16 / physicalAspectRatio).toPrecision(1)}")
            Log.info("   rel=$relAspectRatio")

            val fitAspectRatio = fbAspectRatio * relAspectRatio

            // The screenshot we took has the size of the framebuffer, which includes the black stripes that may appear
            // when the aspect ratio is not our FIXED_ASPECT_RATIO. Clip away those stripes.

            if (fitAspectRatio > FIXED_ASPECT_RATIO) {
                // The window is too wide, i.e. there are black stripes to the left and right.
                croppedWidth = (raw.height.toFloat() * FIXED_ASPECT_RATIO / relAspectRatio).toInt()
                croppedHeight = raw.height
            } else {
                // The window is too high, i.e. there are black stripes at the top and bottom.
                croppedWidth = raw.width
                croppedHeight = (raw.width.toFloat() / FIXED_ASPECT_RATIO / relAspectRatio).toInt()
            }
        }

        Log.info("   cropping it to ${croppedWidth}x${croppedHeight}")
        Log.info("   croppedPixels is 16:${(16 / (croppedWidth.toFloat() / croppedHeight)).toPrecision(1)}")

        val croppedBuf = ByteBuffer.allocateDirect(croppedWidth * croppedHeight * 3).apply {
            order(ByteOrder.nativeOrder())
        }

        var dx = (raw.width - croppedWidth) / 2
        var dy = (raw.height - croppedHeight) / 2

        if (dx < 0) {
            Log.warn("dx is negative: $dx")
            dx = 0
        }

        if (dy < 0) {
            Log.warn("dy is negative: $dy")
            dy = 0
        }

        // This also flips the image, because GL returns it upside-down.

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

    fun scaleAndStretch(src: ImageData): ImageData {
        // The screenshot is currently used in two locations:
        //    * by GameMenu as a background, and
        //    * by ButtonBuilder as a thumbnail for SaveGameMenu.
        // Both do not need the screenshot in full quality, so we can make the width quite small. The width affects
        // performance of the blurring done by GameMenu.

        val smallWidth = 640
        val smallHeight = (smallWidth.toFloat() / FIXED_ASPECT_RATIO).toInt()

        val smallerBuf = ByteBuffer.allocateDirect(smallWidth * smallHeight * 3).apply {
            order(ByteOrder.nativeOrder())
        }

        return ImageData(smallerBuf, ImageData.Type.RGB8, smallWidth, smallHeight).apply {
            drawImageScaled(src, 0, 0, smallWidth, smallHeight, antiAliasing = false)
        }
    }
}
