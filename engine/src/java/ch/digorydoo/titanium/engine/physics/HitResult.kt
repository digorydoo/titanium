package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f

/**
 * Since there are no union types in Kotlin (yet), this enum must hold the possible values for all kinds of RigidBody.
 */
enum class HitArea {
    UNSPECIFIED, // spheres
    TOP, // cylinders, cuboids
    BOTTOM, // cylinders, cuboids
    SIDE, // cylinders
    NORTH_FACE, // cuboids
    EAST_FACE, // cuboids
    SOUTH_FACE, // cuboids
    WEST_FACE, // cuboids
}

internal interface HitResult {
    val hitPt: Point3f
    val area1: HitArea
    val area2: HitArea
}

internal class MutableHitResult: HitResult {
    override val hitPt = MutablePoint3f()
    override var area1 = HitArea.UNSPECIFIED
    override var area2 = HitArea.UNSPECIFIED
}
