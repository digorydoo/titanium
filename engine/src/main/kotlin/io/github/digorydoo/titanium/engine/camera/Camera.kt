package io.github.digorydoo.titanium.engine.camera

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import io.github.digorydoo.titanium.engine.camera.CameraProps.Mode
import io.github.digorydoo.titanium.engine.camera.CameraProps.Mode.FIXED_SOURCE
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.core.GameTime.Companion.DELTA_TIME
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max

/**
 * This class implements the facade that's used to access various aspects of the game camera.
 */
class Camera {
    private val props = CameraProps()
    private val director = CameraDirector(props)
    private val projection = Projection()

    var mode: Mode
        get() = props.mode
        set(m) {
            props.mode = m
        }

    fun setTopDownMode() {
        mode = Mode.FIXED_DISTANCE
        setSourceRelativeToTarget(phi = -(PI / 2.0).toFloat(), rho = 0.0f, distance = CAMERA_TOP_DOWN_DISTANCE)
    }

    val isInTopDownMode get() = mode == Mode.FIXED_DISTANCE && props.distance.desired >= CAMERA_TOP_DOWN_DISTANCE

    val sourcePos get() = props.sourcePos.current
    val targetPos get() = props.targetPos.current
    val targetGel get() = props.targetGel
    val currentPhi get() = props.phi.current
    val currentRho get() = props.rho.current
    val currentDir get() = props.dir as Vector3f
    val projMatrix get() = projection.matrix

    val currentSpeedApprox: Float
        get() {
            // Exposing the speed is mainly useful for cutscenes when they need to wait until the camera slows down.
            // Therefore, a rough approximation is enough. If I use this more often, I should move it to animate()
            // and store the result in a variable.

            val src = props.sourcePos
            val srcSpeed = src.speed
            val vx = abs(srcSpeed.x)
            val vy = abs(srcSpeed.y)
            val vz = abs(srcSpeed.z)
            val approxPosSpeed = max(vx, max(vy, vz))

            // In certain camera modes, the speed of the sourcePos is not updated, because its values come from the
            // angles and the distance. Therefore, we also need to take these values into account.

            val d = props.distance
            val distanceSpeed = abs(d.speed)
            val r = d.current
            val vphi = r * abs(props.phi.speed)
            val vrho = r * abs(props.rho.speed)
            val approxAngleSpeed = max(vphi, vrho)

            return max(approxPosSpeed, max(approxAngleSpeed, distanceSpeed))
        }

    fun setTarget(worldCoords: Vector3f, jump: Boolean = false) =
        props.setTarget(worldCoords.x, worldCoords.y, worldCoords.z, jump)

    fun setTarget(x: Float, y: Float, z: Float, jump: Boolean = false) =
        props.setTarget(x, y, z, jump)

    fun setTarget(gel: GraphicElement?, jump: Boolean = false) =
        props.setTarget(gel, jump)

    fun setSource(x: Float, y: Float, z: Float, jump: Boolean = false) =
        props.setSource(x, y, z, jump)

    fun setSourceRelativeToTarget(
        phi: Float = CameraProps.DEFAULT_PHI, // 3*PI/2 = towards the north; positive values move clockwise
        rho: Float = CameraProps.DEFAULT_RHO, // 0 = from exactly above; PI/2 = horizontally
        distance: Float = CameraProps.DEFAULT_DISTANCE,
        jump: Boolean = false,
    ) {
        props.setSourceRelativeToTarget(phi, rho, distance, jump)
    }

    fun animate() {
        props.targetGel?.let {
            props.targetPos.desired.set(it.pos)
            props.targetPos.desired.z += 1.5f * WORLD_BRICK_SIZE // because pos is the gel's feet
        }
        props.targetPos.animate()
        handleInput()
        director.moveCamera()
        props.updateDir()
        projection.recompute(props)
    }

    // FIXME move this to PlayerControl
    private fun handleInput() {
        if (mode == FIXED_SOURCE || App.gameMenu.isShown) return

        val sx = if (App.prefs.swapCameraX) -1.0f else 1.0f
        val sy = if (App.prefs.swapCameraY) -1.0f else 1.0f

        val factor = App.prefs.speedOfCameraControls.speed * DELTA_TIME
        val rightJoy = App.input.rightJoy
        val dphi = rightJoy.x * CAMERA_PHI_SPEED * factor * sx
        val drho = rightJoy.y * CAMERA_RHO_SPEED * factor * sy

        props.phi.desired += dphi
        props.rho.desired = clamp(props.rho.desired + drho, -3.0f, -0.09f)
    }

    companion object {
        private const val CAMERA_PHI_SPEED = 2.5f
        private const val CAMERA_RHO_SPEED = 2.0f
        private const val CAMERA_TOP_DOWN_DISTANCE = 420.0f
    }
}
