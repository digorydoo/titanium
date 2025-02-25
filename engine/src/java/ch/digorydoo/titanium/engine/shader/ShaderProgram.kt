package ch.digorydoo.titanium.engine.shader

import ch.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags

interface ShaderProgram {
    enum class ProgramType {
        BRICK_GLASS,
        BRICK_VOLUME,
        MESH,
        PAPER,
        SHADOWS,
        SKYDOME,
        UI_SPRITES,
        WATER,
    }

    val type: ProgramType
    val flags: Set<ShaderFlags>
}
