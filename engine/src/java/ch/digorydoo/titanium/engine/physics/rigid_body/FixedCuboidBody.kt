package ch.digorydoo.titanium.engine.physics.rigid_body

import ch.digorydoo.kutils.point.Point3f
import kotlin.math.sqrt

/**
 * A cuboid has the centre point at pos. This body is "fixed", i.e. cannot be rotated, and there is no angular momentum.
 */
class FixedCuboidBody(
    name: String,
    initialPos: Point3f,
    mass: Float,
    elasticity: Float,
    friction: Float,
    gravity: Boolean,
    sizeX: Float,
    sizeY: Float,
    sizeZ: Float,
): RigidBody(
    name = name,
    initialPos = initialPos,
    mass = mass,
    elasticity = elasticity,
    friction = friction,
    gravity = gravity,
    collisionRadius = sqrt(sizeX * sizeX + sizeY * sizeY + sizeZ * sizeZ) * 0.5f + COLLISION_VICINITY
) {
    val size = Point3f(sizeX, sizeY, sizeZ)
    val halfSize = Point3f(sizeX / 2.0f, sizeY / 2.0f, sizeZ / 2.0f)
    override fun toString() = "FixedCuboidBody($name, m=$mass, size=$size)"
}
