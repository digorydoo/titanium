package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.RigidBody.Companion.STATIC_MASS
import kotlin.math.sign
import kotlin.math.sqrt

internal class CollideSphereVsCylinder: CollisionStrategy<FixedSphereBody, FixedCylinderBody>() {
    private val tmp1 = MutablePoint3f()
    private val tmp2 = MutablePoint3f()
    private val tmp3 = MutablePoint2f()
    private val tmp4 = MutablePoint2f()
    private val tmp5 = MutablePoint2f()

    override fun checkNextPos(body1: FixedSphereBody, body2: FixedCylinderBody, outHitPt: MutablePoint3f) =
        check(
            cx1 = body1.nextPos.x,
            cy1 = body1.nextPos.y,
            cz1 = body1.nextPos.z + body1.zOffset,
            r1 = body1.radius,
            cx2 = body2.nextPos.x,
            cy2 = body2.nextPos.y,
            cz2 = body2.nextPos.z + body2.zOffset,
            r2 = body2.radius,
            h2 = body2.height,
            outHitPt,
        )

    private fun check(
        // Sphere
        cx1: Float,
        cy1: Float,
        cz1: Float,
        r1: Float,
        // Cylinder
        cx2: Float,
        cy2: Float,
        cz2: Float,
        r2: Float,
        h2: Float,
        // out
        outHitPt: MutablePoint3f?,
    ): Boolean {
        if (cz1 + r1 < cz2 - h2 * 0.5f) return false
        if (cz1 - r1 > cz2 + h2 * 0.5f) return false

        val dx = cx2 - cx1
        val dy = cy2 - cy1
        val dsqr = (dx * dx) + (dy * dy) // squared distance in the XY plane
        val rsum = r1 + r2
        if (dsqr > rsum * rsum) return false

        if (outHitPt != null) {
            val vertical = dsqr <= r2 * r2 // vertical hit if sphere's centre is within cylinder's circle in XY plane
            val d = sqrt(dsqr) // always >= 0

            if (d <= EPSILON || vertical) {
                if (cz1 < cz2) {
                    // The sphere's top centre point collides with the cylinder's bottom plane.
                    outHitPt.x = cx1
                    outHitPt.y = cy1
                    outHitPt.z = cz1 + r1
                } else {
                    // The sphere's bottom centre point collides with the cylinder's top plane.
                    outHitPt.x = cx1
                    outHitPt.y = cy1
                    outHitPt.z = cz1 - r1
                }
            } else {
                // The two circles collide within the XY plane
                outHitPt.x = cx1 + r1 * (dx / d)
                outHitPt.y = cy1 + r1 * (dy / d)
                outHitPt.z = cz1
            }
        }

        return true
    }

    override fun bounce(body1: FixedSphereBody, body2: FixedCylinderBody) {
        val vertical: Boolean

        if (body1.mass < STATIC_MASS && body1.mass <= body2.mass) {
            vertical = separate(body1, body2) // updates body1.nextPos
        } else if (body2.mass < STATIC_MASS) {
            vertical = separate(body2, body1) // updates body2.nextPos
        } else {
            Log.warn("Cannot separate $body1 from $body2")
            return
        }

        val m1 = if (body1.mass <= EPSILON) EPSILON else body1.mass
        val m2 = if (body2.mass <= EPSILON) EPSILON else body2.mass
        val e = body1.elasticity * body2.elasticity

        val p1 = tmp1.set(body1.nextPos.x, body1.nextPos.y, body1.nextPos.z + body1.zOffset)
        val p2 = tmp2.set(body2.nextPos.x, body2.nextPos.y, body2.nextPos.z + body2.zOffset)

        if (vertical) {
            // We treat this like bouncing the sphere off the XY plane.

            val v1z = body1.nextSpeed.z
            val v2z = body2.nextSpeed.z
            val vdiffz = v1z - v2z
            val expectedSign = if (p1.z < p2.z) 1.0f else -1.0f

            if (sign(vdiffz) != expectedSign) {
                // The two objects are separating
                return
            }

            val totalMass = m1 + m2
            val p = v1z * m1 + v2z * m2

            body1.nextSpeed.z = (p - vdiffz * e * m2) / totalMass
            body2.nextSpeed.z = (p + vdiffz * e * m1) / totalMass
        } else {
            // We treat this like bouncing a circle off another circle in the XY plane.

            val v1 = tmp3.set(body1.nextSpeed.x, body1.nextSpeed.y)
            val v2 = tmp4.set(body2.nextSpeed.x, body2.nextSpeed.y)

            val pdiff = tmp5.set(p2.x - p1.x, p2.y - p1.y)
            val n = pdiff / pdiff.length()

            val v1parallel = n * v1.dotProduct(n)
            val v2parallel = n * v2.dotProduct(n)

            val v1perpendicular = v1 - v1parallel
            val v2perpendicular = v2 - v2parallel

            val totalMass = m1 + m2
            val vdiff = v1parallel - v2parallel
            val p = v1parallel * m1 + v2parallel * m2

            v1.set(v1perpendicular + (p - vdiff * e * m2) / totalMass)
            v2.set(v2perpendicular + (p + vdiff * e * m1) / totalMass)

            body1.nextSpeed.x = v1.x
            body1.nextSpeed.y = v1.y

            body2.nextSpeed.x = v2.x
            body2.nextSpeed.y = v2.y
        }
    }

    /**
     * Updates body1.nextPos such that it no longer collides with body2.
     * @return whether or not the hit was vertical
     */
    private fun separate(sphere: FixedSphereBody, cylinder: FixedCylinderBody): Boolean {
        val p1 = tmp1.set(sphere.nextPos.x, sphere.nextPos.y, sphere.nextPos.z + sphere.zOffset)
        val p2 = tmp2.set(cylinder.nextPos.x, cylinder.nextPos.y, cylinder.nextPos.z + cylinder.zOffset)

        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val dsqr = (dx * dx) + (dy * dy) // squared distance in the XY plane
        val vertical = dsqr <= cylinder.radius * cylinder.radius

        if (vertical) {
            // Separate the bodies along the z-axis
            val moveBy = sphere.radius + cylinder.height * 0.5f + 2 * EPSILON

            if (p1.z < p2.z) {
                sphere.nextPos.z = cylinder.nextPos.z + cylinder.zOffset - moveBy - sphere.zOffset
            } else {
                sphere.nextPos.z = cylinder.nextPos.z + cylinder.zOffset + moveBy - sphere.zOffset
            }
        } else {
            // Separate the bodies in the XY plane
            val dlen = sqrt(dsqr)
            val n = tmp5.set(dx / dlen, dy / dlen)
            val moveBy = sphere.radius + cylinder.radius + 2 * EPSILON
            sphere.nextPos.x = cylinder.nextPos.x - n.x * moveBy
            sphere.nextPos.y = cylinder.nextPos.y - n.y * moveBy
        }

        return vertical
    }

    /**
     * Updates body1.nextPos such that it no longer collides with body2.
     * @return whether or not the hit was vertical
     */
    private fun separate(cylinder: FixedCylinderBody, sphere: FixedSphereBody): Boolean {
        val p1 = tmp1.set(cylinder.nextPos.x, cylinder.nextPos.y, cylinder.nextPos.z + cylinder.zOffset)
        val p2 = tmp2.set(sphere.nextPos.x, sphere.nextPos.y, sphere.nextPos.z + sphere.zOffset)

        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val dsqr = (dx * dx) + (dy * dy) // squared distance in the XY plane
        val vertical = dsqr <= cylinder.radius * cylinder.radius

        if (vertical) {
            // Separate the bodies along the z-axis
            val moveBy = sphere.radius + cylinder.height * 0.5f + 2 * EPSILON

            if (p1.z < p2.z) {
                cylinder.nextPos.z = sphere.nextPos.z + sphere.zOffset - moveBy - cylinder.zOffset
            } else {
                cylinder.nextPos.z = sphere.nextPos.z + sphere.zOffset + moveBy - cylinder.zOffset
            }
        } else {
            // Separate the bodies in the XY plane
            val dlen = sqrt(dsqr)
            val n = tmp5.set(dx / dlen, dy / dlen)
            val moveBy = sphere.radius + cylinder.radius + 2 * EPSILON
            cylinder.nextPos.x = sphere.nextPos.x - n.x * moveBy
            cylinder.nextPos.y = sphere.nextPos.y - n.y * moveBy
        }

        return vertical
    }
}
