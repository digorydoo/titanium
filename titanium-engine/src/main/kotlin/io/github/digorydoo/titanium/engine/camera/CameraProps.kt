package io.github.digorydoo.titanium.engine.camera

import ch.digorydoo.kutils.string.initCap
import ch.digorydoo.kutils.vector.MutableVector3f
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
    enum class Mode(val value: Int) {
        FIXED_SOURCE(1), FIXED_DISTANCE(2), SMART(3);

        val displayText = initCap(toString().replace("_", " ").lowercase()) // for Editor

        companion object {
            fun fromIntOrNull(value: Int) =
                entries.find { it.value == value }
        }
    }

    enum class Kind { PERSPECTIVE, ORTHOGONAL }

    enum class Inertia(
        val sourceAccel: Float,
        val sourceNormalBrake: Float,
        val sourceStrongBrake: Float,
        val targetAccel: Float,
        val targetNormalBrake: Float,
        val targetStrongBrake: Float,
        val angleAccel: Float,
        val angleNormalBrake: Float,
        val angleStrongBrake: Float,
        val distanceAccel: Float,
        val distanceNormalBrake: Float,
        val distanceStrongBrake: Float,
    ) {
        NORMAL(
            sourceAccel = 1.5f,
            sourceNormalBrake = 0.25f,
            sourceStrongBrake = 0.30f,
            targetAccel = 0.8f,
            targetNormalBrake = 0.20f,
            targetStrongBrake = 0.25f,
            angleAccel = 2.9f,
            angleNormalBrake = 0.25f,
            angleStrongBrake = 0.30f,
            distanceAccel = 0.5f,
            distanceNormalBrake = 0.25f,
            distanceStrongBrake = 0.30f,
        ),
        HIGH(
            sourceAccel = 0.17f,
            sourceNormalBrake = 0.10f,
            sourceStrongBrake = 0.15f,
            targetAccel = 0.12f,
            targetNormalBrake = 0.10f,
            targetStrongBrake = 0.15f,
            angleAccel = 0.42f,
            angleNormalBrake = 0.15f,
            angleStrongBrake = 0.20f,
            distanceAccel = 0.12f,
            distanceNormalBrake = 0.10f,
            distanceStrongBrake = 0.15f,
        )
    }

    var mode = Mode.SMART
    var kind = Kind.PERSPECTIVE
    var targetGel: GraphicElement? = null
    val sourcePos = SmoothVector3f()
    val targetPos = SmoothVector3f()
    val phi = SmoothAngle(DEFAULT_PHI)
    val rho = SmoothAngle(DEFAULT_RHO)
    val distance = SmoothFloat(DEFAULT_DISTANCE)
    val dir = Direction.northVector.toMutable()

    private val tempPt = MutableVector3f()

    var inertia = Inertia.NORMAL; private set

    init {
        setInertia(inertia)
    }

    fun setInertia(newInertia: Inertia) {
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
