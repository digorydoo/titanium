package io.github.digorydoo.titanium.main.texture

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.texture.ImageData
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.main.core.LeakDetector
import io.github.digorydoo.titanium.main.opengl.checkGLError
import io.github.digorydoo.titanium.main.texture.TextureManagerImpl.SamplerUnit
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TextureImpl(
    private val texId: Int,
    private val imgData: ImageData,
    override val shared: Boolean,
): Texture {
    init {
        require(imgData.type == ImageData.Type.RGBA8) { "Unsupported type: ${imgData.type}" }
    }

    override val width get() = imgData.width
    override val height get() = imgData.height

    private var pushNeeded = true
    private val leakDetector = LeakDetector(TAG.name, initiallyValid = true)

    override fun freeRequireUnshared() {
        require(!shared) { "Cannot free shared texture: $this" }
        if (leakDetector.resourceValid) freeNow()
    }

    override fun dangerouslyFree() {
        if (leakDetector.resourceValid) freeNow()
    }

    private fun freeNow() {
        require(leakDetector.resourceValid)
        // Log.info(TAG, "Freeing $this")
        glDeleteTextures(texId)
        checkGLError()
        leakDetector.resourceValid = false
    }

    override fun apply() {
        require(leakDetector.resourceValid)

        if (pushNeeded) {
            pushData()
        }

        checkGLError()
        glActiveTexture(GL_TEXTURE0 + SamplerUnit.TEXTURE.index)
        checkGLError()
        glBindTexture(GL_TEXTURE_2D, texId)
        checkGLError()
    }

    private fun pushData() {
        require(leakDetector.resourceValid)

        pushNeeded = false

        checkGLError()
        glActiveTexture(GL_TEXTURE0 + SamplerUnit.TEXTURE.index)
        glBindTexture(GL_TEXTURE_2D, texId)

        imgData.buf.position(0)
        glPixelStorei(GL_PACK_ALIGNMENT, 1)
        glTexImage2D(
            GL_TEXTURE_2D,
            0,                            // mip map level, 0=base image
            GL_RGBA,                      // internal format
            imgData.width,
            imgData.height,
            0,                            // border
            GL_RGBA,                      // format
            GL_UNSIGNED_INT_8_8_8_8_REV,  // type
            imgData.buf
        )
        checkGLError()
    }

    override fun drawInto(lambda: ImageData.() -> Unit) {
        require(leakDetector.resourceValid)
        imgData.lambda()
        pushNeeded = true
    }

    override fun copyAsRGB8(): ImageData {
        val buf = ByteBuffer.allocateDirect(width * height * 3)
        buf.order(ByteOrder.nativeOrder())
        buf.position(0)
        imgData.buf.position(0)

        var togo = width * height

        while (togo > 0) {
            buf.put(imgData.buf.get()) // R
            buf.put(imgData.buf.get()) // G
            buf.put(imgData.buf.get()) // B
            imgData.buf.get() // skip A
            togo--
        }

        return ImageData(buf, ImageData.Type.RGB8, width, height)
    }

    override fun toString() =
        arrayOf(
            "texId=$texId",
            "size=(${width}x$height)",
            "shared=$shared",
        ).joinToString(", ").let { "Texture($it)" }

    companion object {
        private val TAG = Log.Tag("TextureImpl")
    }
}
