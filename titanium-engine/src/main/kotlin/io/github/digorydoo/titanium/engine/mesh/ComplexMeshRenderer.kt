package io.github.digorydoo.titanium.engine.mesh

import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import io.github.digorydoo.titanium.engine.shader.Renderer

abstract class ComplexMeshRenderer: Renderer {
    abstract class Delegate {
        abstract val mesh: ComplexMesh?
        abstract val renderPos: Vector3f // translation
        open val rotationPhi = 0.0f // rotation around Z-axis, in Radians
        open val scaleFactor = Vector3f(WORLD_BRICK_SIZE, WORLD_BRICK_SIZE, WORLD_BRICK_SIZE)
        open val emittingLight = 0.0f // will be added to material
    }
}
