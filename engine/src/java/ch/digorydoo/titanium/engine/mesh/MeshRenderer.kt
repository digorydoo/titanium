package ch.digorydoo.titanium.engine.mesh

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.Brick.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.shader.Renderer

abstract class MeshRenderer: Renderer {
    abstract class Delegate {
        abstract val mesh: Mesh
        abstract val renderPos: Point3f // translation
        open val rotationPhi = 0.0f // rotation around Z-axis, in Radians
        open val scaleFactor = Point3f(WORLD_BRICK_SIZE, WORLD_BRICK_SIZE, WORLD_BRICK_SIZE)
        open val emittingLight = 0.0f // will be added to material
    }
}
