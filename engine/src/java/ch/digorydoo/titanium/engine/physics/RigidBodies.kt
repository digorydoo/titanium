package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f

/**
 * A sphere has the centre point at (pos.x, pos.y, pos.z + zOffset). This body is "fixed", i.e. has no angular
 * momentum.
 */
class FixedSphereBody(
    name: String,
    pos: MutablePoint3f,
    elasticity: Float,
    friction: Float,
    mass: Float,
    gravity: Boolean,
    val radius: Float,
    val zOffset: Float,
): RigidBody(
    name = name,
    pos = pos,
    elasticity = elasticity,
    friction = friction,
    mass = mass,
    gravity = gravity,
) {
    override fun toString() =
        "FixedSphereBody($name, m=$mass, r=$radius)"
}

/**
 * A cylinder has the centre point at (pos.x, pos.y, pos.z + zOffset). The bottom is at pos.z + zOffset - height / 2,
 * while the top is at pos.z + zOffset + height / 2. This body is "fixed", i.e. the cylinder is always upright, and
 * there is no angular momentum.
 */
class FixedCylinderBody(
    name: String,
    pos: MutablePoint3f,
    elasticity: Float,
    friction: Float,
    mass: Float,
    gravity: Boolean,
    val radius: Float,
    val height: Float,
    val zOffset: Float,
): RigidBody(
    name = name,
    pos = pos,
    elasticity = elasticity,
    friction = friction,
    mass = mass,
    gravity = gravity,
) {
    override fun toString() =
        "FixedCylinderBody($name, m=$mass, r=$radius, h=$height)"
}

/**
 * A plane is given by the RigidBody's pos and the normal. The normal must have length 1. This body is "fixed", i.e. has
 * no angular momentum.
 */
class FixedPlaneBody(
    name: String,
    pos: MutablePoint3f,
    elasticity: Float,
    friction: Float,
    mass: Float,
    gravity: Boolean,
    val normal: Point3f,
): RigidBody(
    name = name,
    pos = pos,
    elasticity = elasticity,
    friction = friction,
    mass = mass,
    gravity = gravity,
) {
    override fun toString() =
        "FixedPlaneBody($name, m=$mass, n=$normal)"
}
