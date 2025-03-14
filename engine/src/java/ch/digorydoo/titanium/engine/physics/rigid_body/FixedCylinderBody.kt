package ch.digorydoo.titanium.engine.physics.rigid_body

import ch.digorydoo.kutils.point.MutablePoint3f
import kotlin.math.sqrt

/**
 * A cylinder has the centre point at (pos.x, pos.y, pos.z + zOffset). The bottom is at pos.z + zOffset - height / 2,
 * while the top is at pos.z + zOffset + height / 2. This body is "fixed", i.e. the cylinder is always upright, and
 * there is no angular momentum.
 */
class FixedCylinderBody(
    name: String,
    pos: MutablePoint3f,
    mass: Float,
    elasticity: Float,
    friction: Float,
    gravity: Boolean,
    val radius: Float,
    val height: Float,
    val zOffset: Float,
): RigidBody(
    name = name,
    pos = pos,
    mass = mass,
    elasticity = elasticity,
    friction = friction,
    gravity = gravity,
    collisionRadius = sqrt((height * height / 4.0f) + (radius * radius)) + COLLISION_VICINITY
) {
    override fun toString() =
        "FixedCylinderBody($name, m=$mass, r=$radius, h=$height)"
}
