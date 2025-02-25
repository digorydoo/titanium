package ch.digorydoo.titanium.main.shader.bricks

import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.scene.Lighting
import ch.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags
import ch.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.main.opengl.checkGLError
import ch.digorydoo.titanium.main.shader.Shader
import ch.digorydoo.titanium.main.shader.ShaderAttributes.Attribute
import ch.digorydoo.titanium.main.shader.bricks.BrickShaderHoldersMap.ShaderHolder
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE

class ConcreteShaderHolder(): ShaderHolder {
    override val intendedForSolid = true
    private val shaderRendersShadows = (App.sceneLoader.sceneBeingLoaded ?: App.scene).hasShadows

    private val shaderForNear = Shader(
        ProgramType.BRICK_VOLUME,
        setOfNotNull(
            ShaderFlags.CONCRETE,
            if (shaderRendersShadows) ShaderFlags.SHADOWS else null,
            ShaderFlags.LAMP0,
            ShaderFlags.LAMP1,
            ShaderFlags.LAMP2,
            ShaderFlags.LAMP3,
            ShaderFlags.LAMP4,
        )
    )

    private val shaderForFar = Shader(ProgramType.BRICK_VOLUME, setOf(ShaderFlags.CONCRETE))

    override fun create() {
        shaderForNear.create()
        shaderForFar.create()
    }

    override fun free() {
        shaderForNear.free()
        shaderForFar.free()
    }

    override fun connectToVBO(attr: Attribute) {
        shaderForNear.connectToVBO(attr)
        shaderForFar.connectToVBO(attr)
    }

    override fun prepareTransparent(distance: Float, lgt: Lighting, tex: Texture, material: BrickMaterial) =
        false

    override fun prepareSolid(distance: Float, lgt: Lighting, tex: Texture, material: BrickMaterial): Boolean {
        val near = distance < 100.0f
        val shader = if (near) shaderForNear else shaderForFar
        shader.program.use()
        shader.bindVAO()
        val uniforms = shader.uniforms

        if (near) {
            uniforms.apply {
                setLamp0Props()
                setLamp1Props()
                setLamp2Props()
                setLamp3Props()
                setLamp4Props()
            }
        }

        uniforms.apply {
            setSunDir(lgt.sunDir.vector)
            setHazyColour(lgt.hazyColour)
            setHaziness(lgt.haziness)
            setBrightness(lgt.brightness)
            setAmbientLightColour(lgt.ambientLight)
            setDiffuseLightColour(lgt.diffuseLight)
            setTexIntensity(lgt.texIntensity)
            setProjection()
        }

        uniforms.setTextureSamplerUnit() // assign GL_TEXTUREx to shader location
        tex.apply()
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR) // some anti-aliasing
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        checkGLError()

        if (near && shaderRendersShadows) {
            // Make the ShadowMap available to the shader
            uniforms.setShadowProjection()
            uniforms.setShadowMapSamplerUnit()

            // Should be bound already
            // (App.singleton as AppImpl).shadowBuffer.bindTexture()
        }

        return true
    }
}
