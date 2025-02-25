package ch.digorydoo.titanium.engine.camera

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.Brick.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.camera.CameraProps.Mode
import ch.digorydoo.titanium.engine.camera.CameraProps.Mode.FIXED_SOURCE
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.core.GameTime.Companion.DELTA_TIME
import ch.digorydoo.titanium.engine.gel.GraphicElement
import kotlin.math.PI

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
        setSourceRelativeToTarget(phi = -(PI / 2.0).toFloat(), rho = 0.0f, dist = CAMERA_TOP_DOWN_DISTANCE)
    }

    val isInTopDownMode get() = mode == Mode.FIXED_DISTANCE && props.distance.desired >= CAMERA_TOP_DOWN_DISTANCE

    val sourcePos get() = props.sourcePos.current
    val targetPos get() = props.targetPos.current
    val currentPhi get() = props.phi.current
    val currentRho get() = props.rho.current
    val currentDir get() = props.dir as Point3f
    val projMatrix get() = projection.matrix

    fun setTarget(pt: Point3f, jump: Boolean = false) =
        props.setTarget(pt.x, pt.y, pt.z, jump)

    fun setTarget(x: Float, y: Float, z: Float, jump: Boolean = false) =
        props.setTarget(x, y, z, jump)

    fun setTarget(gel: GraphicElement?, jump: Boolean = false) =
        props.setTarget(gel, jump)

    fun setSource(x: Float, y: Float, z: Float, jump: Boolean = false) =
        props.setSource(x, y, z, jump)

    fun setSourceRelativeToTarget(
        phi: Float = CameraProps.DEFAULT_PHI,
        rho: Float = CameraProps.DEFAULT_RHO,
        dist: Float = CameraProps.DEFAULT_DISTANCE,
        jump: Boolean = false,
    ) {
        props.setSourceRelativeToTarget(phi, rho, dist, jump)
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

    private fun handleInput() {
        if (mode == FIXED_SOURCE || App.gameMenu.isShown) return

        val sx = if (App.prefs.swapCameraX) -1.0f else 1.0f
        val sy = if (App.prefs.swapCameraY) -1.0f else 1.0f

        val factor = App.prefs.cameraSpeed.speed * DELTA_TIME
        val rightJoy = App.input.values.rightJoy
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
