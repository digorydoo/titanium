package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f

/**
 * Since there are no union types in Kotlin (yet), this enum must hold the possible values for all kinds of RigidBody.
 */
enum class HitArea {
    UNSPECIFIED, // spheres
    TOP_FACE, // cylinders, cuboids
    BOTTOM_FACE, // cylinders, cuboids
    TOP_TIP, // capsules
    BOTTOM_TIP, // capsules
    TOP_SPHERE, // capsules
    BOTTOM_SPHERE, // capsules
    SIDE, // cylinders
    NORTH_FACE, // cuboids
    EAST_FACE, // cuboids
    SOUTH_FACE, // cuboids
    WEST_FACE, // cuboids
}

class UnexpectedHitAreaError(area: HitArea): Exception("Unexpected hit area: $area")

internal interface HitResult {
    val hitPt: Point3f
    val hitNormal12: Point3f // normal at hitPt pointing from body1 to body2
    val area1: HitArea
    val area2: HitArea
}

internal class MutableHitResult: HitResult {
    override val hitPt = MutablePoint3f()
    override val hitNormal12 = MutablePoint3f()
    override var area1 = HitArea.UNSPECIFIED
    override var area2 = HitArea.UNSPECIFIED

    fun set(other: HitResult) {
        hitPt.set(other.hitPt)
        hitNormal12.set(other.hitNormal12)
        area1 = other.area1
        area2 = other.area2
    }

    fun setSwapped(other: HitResult) {
        hitPt.set(other.hitPt)
        val n = other.hitNormal12
        hitNormal12.set(-n.x, -n.y, -n.z)
        area1 = other.area2
        area2 = other.area1
    }

    fun swapPointOfView() {
        hitNormal12.x *= -1.0f
        hitNormal12.y *= -1.0f
        hitNormal12.z *= -1.0f
        val tmp = area1
        area1 = area2
        area2 = tmp
    }

    override fun toString() =
        "MutableHitResult(hitPt=$hitPt, hitNormal12=$hitNormal12, area1=$area1, area2=$area2)"
}
