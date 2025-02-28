package ch.digorydoo.titanium.engine.camera

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.string.initCap
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.EPSILON
import ch.digorydoo.titanium.engine.utils.SmoothFloat
import ch.digorydoo.titanium.engine.utils.SmoothPoint3f
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max

/**
 * This class holds the various properties of the camera
 */
class CameraProps {
    enum class Mode(val value: Int) {
        FIXED_SOURCE(1), FIXED_DISTANCE(2), SMART(3);

        val displayText = initCap(toString().replace("_", " ").lowercase()) // for Editor

        companion object {
            fun fromIntOrNull(value: Int) =
                entries.find { it.value == value }
        }
    }

    enum class Kind {
        PERSPECTIVE, ORTHOGONAL
    }

    var mode = Mode.SMART
    var kind = Kind.PERSPECTIVE
    var targetGel: GraphicElement? = null

    val sourcePos = SmoothPoint3f(accel = 1.5f, normalBrake = 0.25f, strongBrake = 0.30f)
    val targetPos = SmoothPoint3f(accel = 0.8f, normalBrake = 0.20f, strongBrake = 0.25f)
    val phi = SmoothFloat(DEFAULT_PHI, accel = 2.9f, normalBrake = 0.25f, strongBrake = 0.30f)
    val rho = SmoothFloat(DEFAULT_RHO, accel = 2.9f, normalBrake = 0.25f, strongBrake = 0.30f)
    val distance = SmoothFloat(DEFAULT_DISTANCE, accel = 0.5f, normalBrake = 0.25f, strongBrake = 0.30f)
    val dir = Direction.northVector.toMutable()

    private val tempPt = MutablePoint3f()

    fun setTarget(x: Float, y: Float, z: Float, jump: Boolean) {
        targetPos.desired.set(x, y, z)
        if (jump) targetPos.jump()
        targetGel = null
    }

    fun setTarget(gel: GraphicElement?, jump: Boolean) {
        if (gel == null) {
            targetPos.desired.set(targetPos.current)
            targetGel = null
        } else {
            targetPos.desired.set(gel.pos)
            targetGel = gel
            if (jump) targetPos.jump()
        }
    }

    fun setSource(x: Float, y: Float, z: Float, jump: Boolean) {
        sourcePos.desired.set(x, y, z)
        if (jump) sourcePos.jump()
    }

    fun setSourceRelativeToTarget(newPhi: Float, newRho: Float, newDistance: Float, jump: Boolean) {
        phi.desired = newPhi
        rho.desired = newRho
        distance.desired = newDistance
        setSourceFromAnglesAndDistance(newPhi, newRho, newDistance)

        if (jump) {
            phi.jump()
            rho.jump()
            distance.jump()
            sourcePos.jump()
        }
    }

    fun setSourceFromAnglesAndDistance() =
        setSourceFromAnglesAndDistance(phi.current, rho.current, distance.current)

    fun setSourceFromAnglesAndDistance(forPhi: Float, forRho: Float, forDistance: Float) {
        sourcePos.desired
            .set(0.0f, 0.0f, forDistance) // source when forPhi == 0 && forRho == 0
            .rotateYZ(-forRho)
            .rotateXY(-forPhi)
            .add(targetPos.current)
    }

    fun setAnglesAndDistanceFromSource() {
        tempPt.set(sourcePos.current).subtract(targetPos.current)
        distance.desired = max(0.1f, tempPt.length())
        phi.desired = -(PI / 2 + atan2(tempPt.y.toDouble(), tempPt.x.toDouble())).toFloat()
        tempPt.rotateXY(phi.desired)
        rho.desired = (PI / 2 - atan2(tempPt.z.toDouble(), tempPt.y.toDouble())).toFloat()
    }

    fun updateDir() {
        dir.set(targetPos.current).subtract(sourcePos.current)
        val len = dir.length()

        if (len < EPSILON) {
            dir.set(Direction.northVector) // target is too close to source
        } else {
            dir.set(dir.x / len, dir.y / len, dir.z / len) // normalize
        }
    }

    companion object {
        const val DEFAULT_PHI = -2.1f // rotation around z
        const val DEFAULT_RHO = -0.82f // inclination
        const val DEFAULT_DISTANCE = 5.0f
    }
}
