package io.github.digorydoo.titanium.main.brick

import io.github.digorydoo.titanium.engine.brick.BrickMaterial
import io.github.digorydoo.titanium.engine.scene.Lighting
import io.github.digorydoo.titanium.engine.shader.ShaderProgram
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.main.brick.BrickShaderHoldersMap.ShaderHolder
import io.github.digorydoo.titanium.main.shader.Shader
import io.github.digorydoo.titanium.main.shader.ShaderAttributes

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
