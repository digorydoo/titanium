package ch.digorydoo.titanium.engine.sky

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.point.MutablePoint3f
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MutableSunDirection private constructor(
    override val vector: MutablePoint3f,
    override var phi: Float,
    override var rho: Float,
): SunDirection(vector, phi, rho) {
    constructor(): this(MutablePoint3f(), 0.0f, 0.0f)
    constructor(other: SunDirection): this(MutablePoint3f(other.vector), other.phi, other.rho)

    fun set(other: SunDirection) {
        vector.set(other.vector)
        phi = other.phi
        rho = other.rho
    }

    fun setFromRelTime(relTime: Float) {
        // Log.info("MutableSunDirection: setFromRelTime")
        print(".")

        // phi rotates around z
        // rho rotates around x
        // theta rorates around z
        // Note that theta has no effect when phi is PI/2 and rho is 0.
        // (If we applied transformations reversely, theta would have no effect when phi is 0, which is worse).

        val thePhi = 0.0f // 0 is east, PI/2 is north
        val theRho = (relTime * PI * 2.0 + 3.0 * PI / 2.0).toFloat() // 0 is horizon, PI/2 is top
        // val theta = 1.4f // keeps sun very close to horizon
        val theta = 0.2f // rotates around Y when phi is 0; adds slight skew towards south

        val cphi = cos(thePhi)
        val sphi = sin(thePhi)
        val crho = cos(theRho)
        val srho = sin(theRho)
        val ctheta = cos(theta)
        val stheta = sin(theta)

        val tx = 0.0f
        val ty = 1.0f
        val tz = 0.0f

        // Computes R_theta * R_rho * R_phi * (tx, ty, tz)
        // We could simplify this computation, because phi, tx, ty, tz are constants.
        // However, I leave it like this for the moment for clarity.
        vector.x = tx * (cphi * ctheta + sphi * stheta * srho) +
            ty * (cphi * stheta * srho - sphi * ctheta) +
            tz * stheta * crho
        vector.y = tx * sphi * crho + ty * cphi * crho - tz * srho
        vector.z = tx * (sphi * ctheta * srho - cphi * stheta) +
            ty * (sphi * stheta + cphi * ctheta * srho) +
            tz * ctheta * crho

        // angles.x becomes the phi rotation of the orthogonal projection
        // angles.y becomes the rho rotation of the orthogonal projection
        //
        // x=-3*PI/2, y=-theta -> 24:00 relTime 0.00f
        // x=-PI,     y=-PI/2  -> 06:00 relTime 0.25f
        // x=-PI/2,   y=-theta -> 12:00 relTime 0.50f
        // x=0.0f,    y=-PI/2  -> 18:00 relTime 0.75f
        // x=PI/2,    y=-theta -> 24:00 relTime 1.00f
        //
        // This isn't entirely correct, because the cos() below causes the setting sun to slow down and rise again
        // after 6PM, shining from the north. But it's probably better than the correct path, because the correct
        // path would cause the sun to shine from below.
        // FIXME Actually, we should probably compute the vector from these shadowAngles and make the sun follow that
        // path. Then we could use the second half for the path of the moon...

        phi = relTime * (2.0 * PI).toFloat() - (3 * PI / 2).toFloat()
        rho = -lerp((PI / 2).toFloat(), theta, 0.5f + 0.5f * cos(relTime * 2.0f * PI * 2.0f).toFloat())

        // println("relTime=$relTime, x=${shadowAngles.x} y=${shadowAngles.y}")
    }
}
