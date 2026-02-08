package io.github.digorydoo.titanium.main.brick

import io.github.digorydoo.titanium.engine.brick.BrickMaterial
import io.github.digorydoo.titanium.engine.scene.Lighting
import io.github.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags
import io.github.digorydoo.titanium.engine.shader.ShaderProgram
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.titanium.main.brick.BrickShaderHoldersMap.ShaderHolder
import io.github.digorydoo.titanium.main.shader.Shader
import io.github.digorydoo.titanium.main.shader.ShaderAttributes.Attribute

class WaterShaderHolder: ShaderHolder {
    override val intendedForSolid = false

    private val shader = Shader(
        ShaderProgram.ProgramType.WATER,
        setOf(ShaderFlags.NO_FLOW, ShaderFlags.SPECULAR_LIGHT, ShaderFlags.PSEUDO_ENV_MAP)
    )

    private val needsCameraDir = shader.program.flags.contains(ShaderFlags.PSEUDO_ENV_MAP)

    override fun create() {
        shader.create()
    }

    override fun free() {
        shader.free()
    }

    override fun connectToVBO(attr: Attribute) {
        // It's suboptimal that BrickVolumeRenderImpl always sends down the normals even for materials that don't use
        // it, but it would make things much more complicated to avoid this, so let's simply not connect the attribute.
        if (attr == Attribute.Normal) return
        shader.connectToVBO(attr)
    }

    override fun prepareSolid(distance: Float, lgt: Lighting, tex: Texture, material: BrickMaterial) =
        false

    override fun prepareTransparent(distance: Float, lgt: Lighting, tex: Texture, material: BrickMaterial): Boolean {
        if (distance > 80.0f) return false
        shader.program.use()
        shader.bindVAO()
        val uniforms = shader.uniforms

        uniforms.apply {
            setSunDir(lgt.sunDir.vector)
            setHazyColour(lgt.hazyColour)
            setHaziness(lgt.haziness)
            setBrightness(lgt.brightness)
            setProjection()
            setCameraSourcePos()
            setSessionTime()
        }

        if (needsCameraDir) {
            uniforms.setCameraDir()
        }

        return true
    }
}
