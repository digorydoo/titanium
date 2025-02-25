package ch.digorydoo.titanium.main.shader.bricks

import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.scene.Lighting
import ch.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags
import ch.digorydoo.titanium.engine.shader.ShaderProgram
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.main.shader.Shader
import ch.digorydoo.titanium.main.shader.ShaderAttributes.Attribute
import ch.digorydoo.titanium.main.shader.bricks.BrickShaderHoldersMap.ShaderHolder

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
