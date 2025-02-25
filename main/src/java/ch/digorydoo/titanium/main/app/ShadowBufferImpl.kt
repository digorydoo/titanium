package ch.digorydoo.titanium.main.app

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.core.ShadowBuffer
import ch.digorydoo.titanium.main.opengl.checkGLError
import ch.digorydoo.titanium.main.texture.TextureManagerImpl.SamplerUnit
import org.lwjgl.opengl.GL30.*
import java.nio.ByteBuffer

class ShadowBufferImpl: ShadowBuffer() {
    private var frameBuf = -1
    private var texId = -1

    fun initialize() {
        frameBuf = glGenFramebuffers()
        require(frameBuf > 0) { "ShadowBuffer failed to create frameBuf" }
        checkGLError()

        glBindFramebuffer(GL_FRAMEBUFFER, frameBuf)
        checkGLError()

        texId = glGenTextures()
        require(texId > 0) { "Failed to generate texture" }
        checkGLError()

        glBindTexture(GL_TEXTURE_2D, texId)
        checkGLError()

        // val numBytes = SHADOW_BUFFER_WIDTH * SHADOW_BUFFER_HEIGHT * 4
        // val bytes = ByteBuffer.allocateDirect(numBytes).apply { order(ByteOrder.nativeOrder()) }

        // bytes.position(0)
        //
        // for (y in 0 ..< SHADOW_BUFFER_HEIGHT) {
        //     for (x in 0 ..< SHADOW_BUFFER_WIDTH) {
        //         bytes.put(
        //             // 0xFF: everything's in light
        //             // 0x00: everything's in the dark
        //             when {
        //                 x == 0 || x == SHADOW_BUFFER_WIDTH - 1 -> 0xFF
        //                 y == 0 || y == SHADOW_BUFFER_HEIGHT - 1 -> 0xFF
        //                 x == y || x == SHADOW_BUFFER_HEIGHT - 1 - y -> 0xFF
        //                 x == SHADOW_BUFFER_WIDTH / 2 - 1 || y == SHADOW_BUFFER_HEIGHT / 2 - 1 -> 0x00
        //                 x % 3 == 0 || y % 3 == 0 -> 0x7F
        //                 y < SHADOW_BUFFER_HEIGHT / 2 && x % 2 == 0 -> 0xFF
        //                 y >= SHADOW_BUFFER_HEIGHT / 2 && y % 2 == 0 -> 0xFF
        //                 else -> 0x00
        //             }.toByte()
        //         )
        //     }
        // }
        //
        // while (bytes.position() < numBytes) {
        //     bytes.put((Random.nextFloat() * 256.0f).toInt().toByte())
        // }
        //
        // bytes.position(0)

        glPixelStorei(GL_PACK_ALIGNMENT, 1)
        glTexImage2D(
            GL_TEXTURE_2D,
            0, // mipmap level
            GL_DEPTH_COMPONENT, // GL_RGBA for colour
            SHADOW_BUFFER_WIDTH,
            SHADOW_BUFFER_HEIGHT,
            0, // border
            GL_DEPTH_COMPONENT, // GL_RGBA for colour
            GL_UNSIGNED_BYTE, // or GL_UNSIGNED_INT, GL_FLOAT, GL_UNSIGNED_INT_8_8_8_8_REV,
            null as ByteBuffer?,
        )
        checkGLError()

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE)

        // Needs to be LEQUAL, not GEQUAL, because if there's no brick in the way, fragment should be in light,
        // because shadow buffer is cleared to 0.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL)

        val borderColour = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColour)

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texId, 0)
        checkGLError()

        glDrawBuffer(GL_NONE)
        glReadBuffer(GL_NONE)

        require(glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE) { "Framebuffer not complete" }

        glBindFramebuffer(GL_FRAMEBUFFER, 0) // unbind
        checkGLError()
    }

    fun tearDown() {
        glDeleteFramebuffers(frameBuf)
        frameBuf = -1

        glDeleteTextures(texId)
        texId = -1
    }

    fun startDrawingInto() {
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuf)
        checkGLError()

        glViewport(0, 0, SHADOW_BUFFER_WIDTH, SHADOW_BUFFER_HEIGHT)
        checkGLError()

        glActiveTexture(GL_TEXTURE0 + SamplerUnit.SHADOW_MAP.index)
        checkGLError()

        glBindTexture(GL_TEXTURE_2D, texId)
        checkGLError()

        glColorMask(false, false, false, false) // we only care about depth
        checkGLError()

        glDepthMask(true) // enable for writing
        checkGLError()

        // glClearDepth(0.0) // NO! Needs to stay 1.0! 1 is default; 1=everything in shadow; 0 = everything in light
        glClear(GL_DEPTH_BUFFER_BIT)
        checkGLError()
        // glClearDepth(1.0) // restore
    }

    fun stopDrawingInto() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        checkGLError()

        glColorMask(true, true, true, true) // re-enable
        // glDepthMask(true) // stays enabled
        checkGLError()

        val app = App.singleton as AppImpl
        val viewPort = app.resolutionMgr.viewPort
        glViewport(viewPort.left, viewPort.top, viewPort.width, viewPort.height)
    }

    companion object {
        private const val SHADOW_BUFFER_WIDTH = 1024
        private const val SHADOW_BUFFER_HEIGHT = 1024
    }
}
