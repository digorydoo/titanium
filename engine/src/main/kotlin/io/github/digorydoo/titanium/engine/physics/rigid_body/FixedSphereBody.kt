package io.github.digorydoo.titanium.engine.physics.rigid_body

import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.utils.EPSILON

/**
 * This body is "fixed", i.e. has no angular momentum.
 */
class FixedSphereBody(
    name: String,
    initialPos: Vector3f,
    mass: Float,
    elasticity: Float,
    friction: Float,
    gravity: Boolean,
    val radius: Float,
): RigidBody(
    name = name,
    initialPos = initialPos,
    mass = mass,
    elasticity = elasticity,
    friction = friction,
    gravity = gravity,
) {
    override val enclosingRadius = radius + EPSILON
    override fun toString() = "FixedSphereBody($name, m=$mass, r=$radius)"
}
