package ch.digorydoo.titanium.main.texture

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.texture.ImageData
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.main.opengl.checkGLError
import ch.digorydoo.titanium.main.texture.TextureManagerImpl.SamplerUnit
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TextureImpl(
    private val texId: Int,
    private val imgData: ImageData,
    override val fileName: String,
    override val shared: Boolean,
): Texture {
    init {
        require(imgData.type == ImageData.Type.RGBA8) { "Unsupported type: ${imgData.type}" }
    }

    override val width get() = imgData.width
    override val height get() = imgData.height

    private var pushNeeded = true
    private var valid = true

    override fun freeRequireUnshared() {
        require(!shared) { "Cannot free shared texture: $this" }
        if (valid) freeNow()
    }

    override fun dangerouslyFree() {
        if (valid) freeNow()
    }

    private fun freeNow() {
        require(valid)
        valid = false
        // Log.info("TextureImpl: Freeing $this")
        glDeleteTextures(texId)
        checkGLError()
    }

    @Suppress("removal")
    protected fun finalize() {
        // Check that free has been called. We can't throw from finalize, so log only.
        if (valid) Log.error("Texture still valid at finalize: $this")
    }

    override fun apply() {
        require(valid)

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
        require(valid)

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
        require(valid)
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
            "fileName=\"$fileName\"",
            "size=(${width}x$height)",
            "shared=$shared",
        ).joinToString(", ").let { "Texture($it)" }
}
