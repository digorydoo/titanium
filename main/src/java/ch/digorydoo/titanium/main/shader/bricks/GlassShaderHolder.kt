package ch.digorydoo.titanium.main.shader.bricks

import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.scene.Lighting
import ch.digorydoo.titanium.engine.shader.ShaderProgram
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.main.shader.Shader
import ch.digorydoo.titanium.main.shader.ShaderAttributes
import ch.digorydoo.titanium.main.shader.bricks.BrickShaderHoldersMap.ShaderHolder

class GlassShaderHolder: ShaderHolder {
    override val intendedForSolid = false
    private val shader = Shader(ShaderProgram.ProgramType.BRICK_GLASS)

    override fun create() {
        shader.create()
    }

    override fun free() {
        shader.free()
    }

    override fun connectToVBO(attr: ShaderAttributes.Attribute) {
        shader.connectToVBO(attr)
    }

    override fun prepareSolid(distance: Float, lgt: Lighting, tex: Texture, material: BrickMaterial) =
        false

    override fun prepareTransparent(distance: Float, lgt: Lighting, tex: Texture, material: BrickMaterial): Boolean {
        if (distance > 42.0f) return false
        shader.program.use()
        shader.bindVAO()

        shader.uniforms.apply {
            setSunDir(lgt.sunDir.vector)
            setHazyColour(lgt.hazyColour)
            setHaziness(lgt.haziness)
            setBrightness(lgt.brightness)
            setDiffuseLightColour(lgt.diffuseLight)
            setCameraSourcePos()
            setProjection()
        }

        return true
    }
}
