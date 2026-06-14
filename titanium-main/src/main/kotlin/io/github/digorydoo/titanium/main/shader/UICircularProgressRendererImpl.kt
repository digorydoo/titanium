package io.github.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.utils.toFloatBuffer
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import io.github.digorydoo.titanium.engine.sprite.UICircularProgressRenderer
import io.github.digorydoo.titanium.main.opengl.checkGLError
import io.github.digorydoo.titanium.main.shader.ShaderAttributes.Attribute
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL14.GL_FUNC_ADD
import org.lwjgl.opengl.GL14.glBlendEquation

class UICircularProgressRendererImpl(private val delegate: Delegate): UICircularProgressRenderer() {
    private val shader = Shader(ProgramType.UI_CIRCULAR_PROGRESS)
    private val positionVBO = ShaderVBO()
    private val texCoordVBO = ShaderVBO()

    init {
        shader.create()
        println(shader.uniforms)
        positionVBO.create(ShaderVBO.Type.DYNAMIC_DRAW)
        texCoordVBO.create(ShaderVBO.Type.DYNAMIC_DRAW)
    }

    private var valid = true

    override fun free() {
        if (valid) {
            shader.free()
            positionVBO.free()
            texCoordVBO.free()
            valid = false
        }
    }

    protected fun finalize() {
        // Check that free has been called. We can't throw from finalize, so log only.
        if (valid) Log.error(TAG, "still valid at finalize")
    }

    private val positions = floatArrayOf(
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f,
    ).toFloatBuffer()

    private val texCoords = floatArrayOf(
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    ).toFloatBuffer()

    // FIXME inefficient, use uniforms
    private fun updatePositions() {
        val size = delegate.frameSize
        val resolutionMgr = App.resolutionMgr

        val left = -1.0f + resolutionMgr.dpToGlX(delegate.renderPos.x)
        val top = 1.0f - resolutionMgr.dpToGlY(delegate.renderPos.y)
        val right = left + resolutionMgr.dpToGlX(size.x)
        val bottom = top - resolutionMgr.dpToGlY(size.y)

        positions.apply {
            put(0, left)
            put(1, bottom)
            put(2, 0.0f)

            put(3, left)
            put(4, top)
            put(5, 0.0f)

            put(6, right)
            put(7, bottom)
            put(8, 0.0f)

            put(9, right)
            put(10, top)
            put(11, 0.0f)
        }

        positionVBO.apply {
            bind()
            setData(positions)
        }

        shader.apply {
            bindVAO()
            connectToVBO(Attribute.ModelPos)
        }
    }

    private fun updateTexCoords() {
        texCoordVBO.bind()
        texCoordVBO.setData(texCoords)
        shader.bindVAO()
        shader.connectToVBO(Attribute.TexCoord)
    }

    override fun renderShadows() {}
    override fun renderSolid() {}

    override fun renderTransparent() {
        checkGLError()

        shader.program.use()

        updatePositions()
        checkGLError()

        updateTexCoords()
        checkGLError()

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR) // GL_LINEAR = anti-aliasing
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        shader.uniforms.apply {
            setTintColour(delegate.colour)
            setProgress(delegate.progress)
            setPenSize(delegate.penSize / delegate.frameSize.x)
        }

        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendEquation(GL_FUNC_ADD)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        glDisable(GL_BLEND)
        checkGLError()
    }

    companion object {
        private val TAG = Log.Tag("UICircularProgressRendererImpl")
    }
}
