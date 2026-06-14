package io.github.digorydoo.titanium.engine.camera

import ch.digorydoo.kutils.vector.MutableVector3f
import io.github.digorydoo.titanium.engine.camera.Camera.Companion.TARGET_Z_OFFSET
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.utils.Direction
import io.github.digorydoo.titanium.engine.utils.EPSILON
import io.github.digorydoo.titanium.engine.utils.SmoothAngle
import io.github.digorydoo.titanium.engine.utils.SmoothFloat
import io.github.digorydoo.titanium.engine.utils.SmoothVector3f
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max

/**
 * This class holds the various properties of the camera
 */
class CameraProps {
    enum class ProjectionMode { PERSPECTIVE, ORTHOGONAL }

    var projectionMode = ProjectionMode.PERSPECTIVE
    var targetGel: GraphicElement? = null
    val sourcePos = SmoothVector3f()
    val targetPos = SmoothVector3f()
    val phi = SmoothAngle(DEFAULT_PHI)
    val rho = SmoothAngle(DEFAULT_RHO)
    val distance = SmoothFloat(DEFAULT_DISTANCE)
    val dir = Direction.northVector.toMutable()
    private val tempPt = MutableVector3f()

    var directingMode = CameraDirectingMode.SMART; private set

    fun setDirectingMode(newMode: CameraDirectingMode) {
        directingMode = newMode

        if (newMode == CameraDirectingMode.MAP) {
            setSourceRelativeToTarget(
                newPhi = -(PI / 2.0).toFloat(),
                newRho = 0.0f,
                newDistance = CAMERA_TOP_DOWN_DISTANCE,
                jump = false
            )
        }
    }

    var inertia = CameraInertia.NORMAL; private set

    init {
        setInertia(inertia)
    }

    fun setInertia(newInertia: CameraInertia) {
        inertia = newInertia
        sourcePos.apply {
            accel = newInertia.sourceAccel
            normalBrake = newInertia.sourceNormalBrake
            strongBrake = newInertia.sourceStrongBrake
        }
        targetPos.apply {
            accel = newInertia.targetAccel
            normalBrake = newInertia.targetNormalBrake
            strongBrake = newInertia.targetStrongBrake
        }
        phi.apply {
            accel = newInertia.angleAccel
            normalBrake = newInertia.angleNormalBrake
            strongBrake = newInertia.angleStrongBrake
        }
        rho.apply {
            accel = newInertia.angleAccel
            normalBrake = newInertia.angleNormalBrake
            strongBrake = newInertia.angleStrongBrake
        }
        distance.apply {
            accel = newInertia.distanceAccel
            normalBrake = newInertia.distanceNormalBrake
            strongBrake = newInertia.distanceStrongBrake
        }
    }

    var inputMode = CameraInputMode.OFF; private set

    fun setInputMode(newMode: CameraInputMode) {
        inputMode = newMode

        // stopTreshold is used to prevent from overshooting; but in CONSTRAINED mode, the values should always return
        // to their initial position when the user lets go of the joystick.
        val stopThreshold = if (newMode == CameraInputMode.CONSTRAINED) 0f else 0.0008f
        phi.stopThreshold = stopThreshold
        rho.stopThreshold = stopThreshold
    }

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
            val pos = gel.pos
            targetPos.desired.set(pos.x, pos.y, pos.z + TARGET_Z_OFFSET)
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
        private const val CAMERA_TOP_DOWN_DISTANCE = 420.0f

        const val DEFAULT_PHI = -2.1f // rotation around z
        const val DEFAULT_RHO = -0.82f // inclination
        const val DEFAULT_DISTANCE = 5.0f
    }
}
