package ch.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.toFloatBuffer
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.core.App.Companion.FIXED_ASPECT_RATIO
import ch.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer
import ch.digorydoo.titanium.main.opengl.checkGLError
import ch.digorydoo.titanium.main.shader.ShaderAttributes.Attribute
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL14.GL_FUNC_ADD
import org.lwjgl.opengl.GL14.glBlendEquation

class UISpriteRendererImpl(private val delegate: Delegate, private val antiAliasing: Boolean): UISpriteRenderer() {
    private val shader = Shader(ProgramType.UI_SPRITES)
    private val positionVBO = ShaderVBO()
    private val texCoordVBO = ShaderVBO()

    init {
        shader.create()
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
        val scaledSize = delegate.frameSize.newScaled(delegate.scaleFactor)

        val left: Float
        val top: Float

        if (delegate.renderPosIsNormalised) {
            left = delegate.renderPos.x
            top = delegate.renderPos.y
        } else {
            left = -1.0f + App.dpToGlX(delegate.renderPos.x)
            top = 1.0f - App.dpToGlY(delegate.renderPos.y)
        }

        val right = left + App.dpToGlX(scaledSize.x)
        val bottom = top - App.dpToGlY(scaledSize.y)

        positions.put(0, left)
        positions.put(1, bottom)
        positions.put(2, 0.0f)

        positions.put(3, left)
        positions.put(4, top)
        positions.put(5, 0.0f)

        positions.put(6, right)
        positions.put(7, bottom)
        positions.put(8, 0.0f)

        positions.put(9, right)
        positions.put(10, top)
        positions.put(11, 0.0f)

        positionVBO.bind()
        positionVBO.setData(positions)
        shader.bindVAO()
        shader.connectToVBO(Attribute.ModelPos)

        shader.uniforms.setRotOrigin((left + right) / 2.0f, (top + bottom) / 2.0f, 0.0f)
    }

    // FIXME inefficient, use uniforms
    private fun updateTexCoords() {
        val tex = delegate.tex ?: return
        val scale = delegate.texScaleFactor

        val left = delegate.texOffset.x
        val top = delegate.texOffset.y
        val right = left + delegate.frameSize.x
        val bottom = top + delegate.frameSize.y

        texCoords.put(0, scale.x * left / tex.width)
        texCoords.put(1, scale.y * bottom / tex.height)

        texCoords.put(2, scale.x * left / tex.width)
        texCoords.put(3, scale.y * top / tex.height)

        texCoords.put(4, scale.x * right / tex.width)
        texCoords.put(5, scale.y * bottom / tex.height)

        texCoords.put(6, scale.x * right / tex.width)
        texCoords.put(7, scale.y * top / tex.height)

        texCoordVBO.bind()
        texCoordVBO.setData(texCoords)
        shader.bindVAO()
        shader.connectToVBO(Attribute.TexCoord)

        shader.uniforms.setTextureSamplerUnit() // our tex goes into uniform Texture
        tex.apply()
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

        val filter = if (antiAliasing) GL_LINEAR else GL_NEAREST
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        shader.uniforms.apply {
            setBrightness(delegate.brightness)
            setRotationPhi(delegate.rotation)
            setAspectRatio(FIXED_ASPECT_RATIO)
            setOpacity(delegate.opacity)
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
        private val TAG = Log.Tag("UISpriteRendererImpl")
    }
}
