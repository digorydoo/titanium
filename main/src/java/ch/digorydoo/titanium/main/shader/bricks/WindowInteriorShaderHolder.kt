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

class WindowInteriorShaderHolder(): ShaderHolder {
    override val intendedForSolid = true

    private val shader = Shader(ProgramType.BRICK_VOLUME, setOf(ShaderFlags.WINDOW_INTERIOR))

    override fun create() {
        shader.create()
    }

    override fun free() {
        shader.free()
    }

    override fun connectToVBO(attr: Attribute) {
        if (attr == Attribute.Normal) return // currently not used
        shader.connectToVBO(attr)
    }

    override fun prepareTransparent(distance: Float, lgt: Lighting, tex: Texture, material: BrickMaterial) =
        false

    override fun prepareSolid(distance: Float, lgt: Lighting, tex: Texture, material: BrickMaterial): Boolean {
        shader.program.use()
        shader.bindVAO()
        val uniforms = shader.uniforms

        uniforms.setTextureSamplerUnit() // assign GL_TEXTUREx to shader location

        uniforms.apply {
            setHazyColour(lgt.hazyColour)
            setHaziness(lgt.haziness)
            setBrightness(lgt.brightness)
            setAmbientLightColour(lgt.ambientLight)
            setTexIntensity(lgt.texIntensity)
            setProjection()
        }

        tex.apply()
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR) // some anti-aliasing
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
        checkGLError()

        return true
    }
}
