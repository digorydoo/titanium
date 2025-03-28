package ch.digorydoo.titanium.engine.physics.rigid_body

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.utils.EPSILON
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
) {
    var sizeX = sizeX; private set
    var sizeY = sizeY; private set
    var sizeZ = sizeZ; private set

    var halfSizeX = sizeX / 2.0f; private set
    var halfSizeY = sizeY / 2.0f; private set
    var halfSizeZ = sizeZ / 2.0f; private set

    override var enclosingRadius = sqrt(sizeX * sizeX + sizeY * sizeY + sizeZ * sizeZ) * 0.5f + EPSILON; private set

    fun setSize(sx: Float, sy: Float, sz: Float) {
        sizeX = sx
        sizeY = sy
        sizeZ = sz
        halfSizeX = sx / 2.0f
        halfSizeY = sy / 2.0f
        halfSizeZ = sz / 2.0f
        enclosingRadius = sqrt(sizeX * sizeX + sizeY * sizeY + sizeZ * sizeZ) * 0.5f + EPSILON
    }

    override fun toString() = "FixedCuboidBody($name, m=$mass, size=($sizeX, $sizeY, $sizeZ))"
}
