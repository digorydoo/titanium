package ch.digorydoo.titanium.main.shader.bricks

import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.scene.Lighting
import ch.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags
import ch.digorydoo.titanium.engine.shader.ShaderProgram.ProgramType
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.main.opengl.checkGLError
import ch.digorydoo.titanium.main.shader.Shader
import ch.digorydoo.titanium.main.shader.ShaderAttributes.Attribute
import ch.digorydoo.titanium.main.shader.bricks.BrickShaderHoldersMap.ShaderHolder
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12

class MetalShaderHolder(): ShaderHolder {
    override val intendedForSolid = true

    private val shaderForNear = Shader(
        ProgramType.BRICK_VOLUME,
        setOf(
            ShaderFlags.METAL,
            ShaderFlags.SHININESS,
            ShaderFlags.LAMP0,
            ShaderFlags.LAMP1,
            ShaderFlags.LAMP2,
            ShaderFlags.LAMP3,
            ShaderFlags.LAMP4
        )
    )

    private val shaderForFar = Shader(ProgramType.BRICK_VOLUME, setOf(ShaderFlags.METAL))

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

        uniforms.setTextureSamplerUnit() // assign GL_TEXTUREx to shader location

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
            setProjection()
        }

        if (near) {
            uniforms.setCameraDir()
        }

        tex.apply()

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR) // some anti-aliasing
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
        checkGLError()

        uniforms.apply {
            setAmbientLightColour(lgt.ambientLight)
            setDiffuseLightColour(lgt.diffuseLight)
            setTexIntensity(lgt.texIntensity)
        }

        return true
    }
}
