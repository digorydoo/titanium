package ch.digorydoo.titanium.engine.sky

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.core.GameTime.Companion.RELTIME1200

open class SunDirection protected constructor(open val vector: Point3f, open val phi: Float, open val rho: Float) {
    companion object {
        val sunAtNoon = fromRelTime(RELTIME1200)

        // relTime is day-relative, i.e. 0.0f is 0:00, 0.99f is close to 23:59.
        fun fromRelTime(relTime: Float): SunDirection {
            val mutable = MutableSunDirection()
            mutable.setFromRelTime(relTime)
            return SunDirection(mutable.vector, mutable.phi, mutable.rho)
        }
    }
}
