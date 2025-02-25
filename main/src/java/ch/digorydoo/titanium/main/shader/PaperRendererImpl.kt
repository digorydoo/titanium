package ch.digorydoo.titanium.main.shader

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.kutils.utils.toFloatBuffer
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.shader.Renderer.BlendMode
import ch.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags.ALWAYS_FAR_Z
import ch.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import ch.digorydoo.titanium.main.opengl.checkGLError
import ch.digorydoo.titanium.main.shader.ShaderAttributes.Attribute
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL14.GL_FUNC_ADD
import org.lwjgl.opengl.GL14.glBlendEquation

class PaperRendererImpl(
    private val delegate: Delegate,
    private val antiAliasing: Boolean,
    private val blendMode: BlendMode,
    private val depthTest: Boolean,
    private val stellarObject: Boolean,
): PaperRenderer() {
    private val shader = Shader(
        ProgramType.PAPER,
        if (stellarObject) setOf(ALWAYS_FAR_Z) else null
    )
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

    @Suppress("removal")
    protected fun finalize() {
        // Check that free has been called. We can't throw from finalize, so log only.
        if (valid) Log.error("PaperRendererImpl still valid at finalize")
    }

    private val positions = floatArrayOf(
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f,
    ).toFloatBuffer()

    private val texCoords = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 0.0f,
        0.0f, 0.0f,
        0.0f, 0.0f,
    ).toFloatBuffer()

    // FIXME inefficient, use uniforms
    private fun updatePositions() {
        val scaledOrigin = delegate.origin.newScaled(delegate.scaleFactor)
        val scaledSize = delegate.frameSize.newScaled(delegate.scaleFactor)

        val x = delegate.renderPos.x
        val y1 = delegate.renderPos.y - scaledOrigin.x
        val z1 = delegate.renderPos.z + scaledOrigin.y
        val z0 = z1 - scaledSize.y
        val y3 = y1 + scaledSize.x

        positions.put(0, x)
        positions.put(1, y1)
        positions.put(2, z0)

        positions.put(3, x)
        positions.put(4, y1)
        positions.put(5, z1)

        positions.put(6, x)
        positions.put(7, y3)
        positions.put(8, z0)

        positions.put(9, x)
        positions.put(10, y3)
        positions.put(11, z1)

        positionVBO.bind()
        positionVBO.setData(positions)
        shader.bindVAO()
        shader.connectToVBO(Attribute.ModelPos)

        shader.uniforms.setRotOrigin(delegate.renderPos.x, delegate.renderPos.y, delegate.renderPos.z)
    }

    // FIXME inefficient, use uniforms
    private fun updateTexCoords() {
        val tex = delegate.tex ?: return

        val left = delegate.texOffset.x
        val top = delegate.texOffset.y
        val right = left + delegate.frameSize.x
        val bottom = top + delegate.frameSize.y

        texCoords.put(0, right / tex.width)
        texCoords.put(1, bottom / tex.height)

        texCoords.put(2, right / tex.width)
        texCoords.put(3, top / tex.height)

        texCoords.put(4, left / tex.width)
        texCoords.put(5, bottom / tex.height)

        texCoords.put(6, left / tex.width)
        texCoords.put(7, top / tex.height)

        texCoordVBO.bind()
        texCoordVBO.setData(texCoords)
        shader.bindVAO()
        shader.connectToVBO(Attribute.TexCoord)

        shader.uniforms.setTextureSamplerUnit() // our tex goes into uniform Texture
        tex.apply()
    }

    override fun renderShadows() {}

    override fun renderSolid() {
        require(valid)
        if (blendMode != BlendMode.ADD) render()
    }

    override fun renderTransparent() {
        require(valid)
        if (blendMode == BlendMode.ADD && delegate.opacity > 0.0f) render()
    }

    private fun render() {
        checkGLError()

        shader.program.use()
        checkGLError()

        updatePositions()
        updateTexCoords()
        checkGLError()

        val filter = if (antiAliasing) GL_LINEAR else GL_NEAREST
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        shader.uniforms.apply {
            setProjection()
            setRotationPhi(delegate.rotationPhi)
            setRotationRho(delegate.rotationRho)
            setMultColour(delegate.multColour)
        }

        if (depthTest) {
            glEnable(GL_DEPTH_TEST)
            glDepthFunc(if (stellarObject) GL_LEQUAL else GL_LESS)
        } else {
            glDisable(GL_DEPTH_TEST)
        }

        val lgt = App.scene.lighting

        when (blendMode) {
            BlendMode.NONE -> {
                shader.uniforms.apply {
                    setBrightness(lgt.brightness)
                    setContrast(lgt.contrast)
                    setTexIntensity(lgt.texIntensity)
                }
                glDisable(GL_BLEND)
            }
            BlendMode.ADD -> {
                shader.uniforms.apply {
                    setBrightness(lgt.brightness * delegate.opacity)
                    setContrast(1.0f)
                    setTexIntensity(1.0f)
                }
                glEnable(GL_BLEND)
                glBlendEquation(GL_FUNC_ADD)
                glBlendFunc(GL_ONE, GL_ONE)
            }
        }

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        checkGLError()

        glDisable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)
    }
}
