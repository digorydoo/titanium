package ch.digorydoo.titanium.engine.physics.rigid_body

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.utils.EPSILON
import kotlin.math.sqrt

/**
 * This body is "fixed", i.e. the cylinder is always upright, and there is no angular momentum.
 */
class FixedCylinderBody(
    name: String,
    initialPos: Point3f,
    mass: Float,
    elasticity: Float,
    friction: Float,
    gravity: Boolean,
    val radius: Float,
    val height: Float,
): RigidBody(
    name = name,
    initialPos = initialPos,
    mass = mass,
    elasticity = elasticity,
    friction = friction,
    gravity = gravity,
) {
    override val enclosingRadius = sqrt((height * height / 4.0f) + (radius * radius)) + EPSILON
    override fun toString() =
        "FixedCylinderBody($name, m=$mass, r=$radius, h=$height)"
}
