package io.github.digorydoo.titanium.engine.mesh

import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.shader.Renderer

abstract class ComplexMeshRenderer: Renderer {
    abstract class Delegate {
        abstract val mesh: ComplexMesh?
        abstract val renderPos: Vector3f // translation
        open val rotationPhi = 0.0f // rotation around Z-axis, in Radians
        open val scaleFactor = 1f
        open val emittingLight = 0.0f // will be added to material
    }
}
