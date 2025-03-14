package ch.digorydoo.titanium.engine.physics.rigid_body

import ch.digorydoo.kutils.point.MutablePoint3f

/**
 * A sphere has the centre point at (pos.x, pos.y, pos.z + zOffset). This body is "fixed", i.e. has no angular
 * momentum.
 */
class FixedSphereBody(
    name: String,
    pos: MutablePoint3f,
    mass: Float,
    elasticity: Float,
    friction: Float,
    gravity: Boolean,
    val radius: Float,
    val zOffset: Float,
): RigidBody(
    name = name,
    pos = pos,
    mass = mass,
    elasticity = elasticity,
    friction = friction,
    gravity = gravity,
    collisionRadius = radius + COLLISION_VICINITY
) {
    override fun toString() =
        "FixedSphereBody($name, m=$mass, r=$radius)"
}
