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

class UnexpectedHitAreaError(area: HitArea): Exception("Unexpected hit area: $area")

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

internal enum class CuboidHit {
    UNKNOWN, // results not known yet, or results depend on other faces
    HIT_WITH_CLOSEST_PT_INSIDE_FACE, // a clear hit within the cuboid face
    HIT_WITH_CLOSEST_PT_OUTSIDE_FACE, // a hit near the edge or corner of the face
    HIT_PARTIALLY_COVERED_FACE, // a less likely hit with a face that's partially covered
    HIT_FULLY_COVERED_FACE, // a rather unlikely hit with a face that seems fully covered
    DECISIVE_MISS, // the face was not hit, and a hit with another face of the cuboid is not possible
}

internal class CuboidCheckResults {
    var hit = CuboidHit.UNKNOWN
    var hitPtValid = false
    var hitPtX = 0.0f
    var hitPtY = 0.0f
    var hitPtZ = 0.0f
    var distanceToClosestPtOnPlane = 0.0f
}
