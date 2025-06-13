package ch.digorydoo.titanium.engine.physics.rigid_body

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.utils.EPSILON

/**
 * This body is "fixed", i.e. the capsule is always upright, and there is no angular momentum.
 */
class FixedCapsuleBody(
    name: String,
    initialPos: Point3f,
    mass: Float,
    elasticity: Float,
    friction: Float,
    gravity: Boolean,
    val radius: Float, // radius of the top and bottom sphere, thus also the radius of the body in XY
    val height: Float, // height including the top and bottom
): RigidBody(
    name = name,
    initialPos = initialPos,
    mass = mass,
    elasticity = elasticity,
    friction = friction,
    gravity = gravity,
) {
    init {
        require(height > 2 * radius) { "A capsule must be larger in height than its contained spheres" }
    }

    override val enclosingRadius = height / 2.0f + EPSILON

    override fun toString() = "FixedCapsuleBody($name, m=$mass, r=$radius, h=$height)"
}
