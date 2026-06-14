package io.github.digorydoo.titanium.engine.shader

import io.github.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags

interface ShaderProgram {
    enum class ProgramType {
        BRICK_GLASS,
        BRICK_VOLUME,
        MESH,
        PAPER,
        SHADOWS,
        SKYDOME,
        UI_CIRCULAR_PROGRESS,
        UI_SOLID,
        UI_SPRITES,
        WATER,
    }

    val type: ProgramType
    val flags: Set<ShaderFlags>
}
