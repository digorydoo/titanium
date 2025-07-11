package ch.digorydoo.titanium.engine.utils

import ch.digorydoo.kutils.math.normAngle
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.point.Point3f
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max

enum class Side { NORTH, EAST, SOUTH, WEST }

enum class Direction {
    NORTH, EAST, SOUTH, WEST, NW, NE, SW, SE;

    companion object {
        val northVector = Point3f(-1.0f, 0.0f, 0.0f)
        val eastVector = Point3f(0.0f, 1.0f, 0.0f)
        val southVector = Point3f(1.0f, 0.0f, 0.0f)
        val westVector = Point3f(0.0f, -1.0f, 0.0f)
        val northEastVector = Point3f(-1.0f, 1.0f, 0.0f).newNormalized()
        val southEastVector = Point3f(1.0f, 1.0f, 0.0f).newNormalized()
        val southWestVector = Point3f(1.0f, -1.0f, 0.0f).newNormalized()
        val northWestVector = Point3f(-1.0f, -1.0f, 0.0f).newNormalized()
        val upVector = Point3f(0.0f, 0.0f, 1.0f)
        val downVector = Point3f(0.0f, 0.0f, -1.0f)

        fun fromVector(pt: Point2f, offset: Float) =
            fromVector(pt.x, pt.y, offset)

        private fun fromVector(x: Float, y: Float, offset: Float): Direction? {
            val maxAbs = max(abs(x), abs(y))

            if (maxAbs < 0.0001f) {
                return null
            }

            var rho = atan2(-y, x) // rho is -PI..+PI

            if (offset != 0.0f) {
                rho = normAngle(rho + offset)
            }

            return fromAngle(rho)
        }

        fun fromAngle(rho: Float): Direction {
            require(rho in -PI .. PI) // callers are required to call normDir() if necessary
            return when {
                rho >= PI * (1 - 1 / 8.0) -> WEST
                rho >= PI * (1 / 2.0 + 1 / 8.0) -> NW
                rho >= PI * (1 / 4.0 + 1 / 8.0) -> NORTH
                rho >= PI * (1 / 8.0) -> NE
                rho >= -PI * (1 / 8.0) -> EAST
                rho >= -PI * (1 / 4.0 + 1 / 8.0) -> SE
                rho >= -PI * (1 / 2.0 + 1 / 8.0) -> SOUTH
                rho >= -PI * (1 - 1 / 8.0) -> SW
                else -> WEST
            }
        }
    }
}
