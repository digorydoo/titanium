package ch.digorydoo.titanium.engine.physics.rigid_body

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.utils.EPSILON

/**
 * This body is "fixed", i.e. has no angular momentum.
 */
class FixedSphereBody(
    name: String,
    initialPos: Point3f,
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
