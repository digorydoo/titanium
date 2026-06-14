package io.github.digorydoo.titanium.engine.camera

import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import io.github.digorydoo.titanium.engine.gel.GraphicElement

/**
 * This class implements the facade that's used to access various aspects of the game camera.
 */
class Camera {
    private val props = CameraProps()
    private val director = CameraDirector(props)
    private val inputHandler = CameraInputHandler(props)
    private val projection = Projection()

    var directingMode: CameraDirectingMode
        get() = props.directingMode
        set(m) {
            props.setDirectingMode(m)
        }

    var inputMode: CameraInputMode
        get() = props.inputMode
        set(m) {
            props.setInputMode(m)
        }

    var inertia: CameraInertia
        get() = props.inertia
        set(newInertia) {
            props.setInertia(newInertia)
        }

    val sourcePos get() = props.sourcePos.current
    val targetPos get() = props.targetPos.current
    val targetGel get() = props.targetGel
    val currentPhi get() = props.phi.current
    val currentRho get() = props.rho.current
    val currentDir get() = props.dir as Vector3f
    val currentDistance get() = props.distance.current
    val projMatrix get() = projection.matrix

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
            props.targetPos.desired.z += TARGET_Z_OFFSET
        }
        props.targetPos.animate()
        inputHandler.handle()
        director.moveCamera()
        props.updateDir()
        projection.recompute(props)
    }

    companion object {
        const val TARGET_Z_OFFSET = 1.5f * WORLD_BRICK_SIZE // because targetGel.pos.z is too close to ground
    }
}
