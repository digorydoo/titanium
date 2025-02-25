package ch.digorydoo.titanium.engine.camera

import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.camera.CameraProps.Mode
import ch.digorydoo.titanium.engine.core.App
import kotlin.math.max

class CameraDirector(private val props: CameraProps) {
    fun moveCamera() {
        when (props.mode) {
            Mode.FIXED_SOURCE -> {
                props.setAnglesAndDistanceFromSource()
                props.sourcePos.animate()
                props.phi.jump()
                props.rho.jump()
                props.distance.jump()
            }
            Mode.FIXED_DISTANCE -> {
                props.phi.animate()
                props.rho.animate()
                props.distance.animate()
                props.setSourceFromAnglesAndDistance()
                props.sourcePos.jump()
            }
            Mode.SMART -> {
                props.phi.animate()
                props.rho.animate()
                props.distance.animate()
                adjustDistanceIfNecessary()
                props.setSourceFromAnglesAndDistance()
                props.sourcePos.jump()
            }
        }
    }

    /**
     * Shortens or grows the distance between the camera and its target, depending whether there are any walls blocking
     * the view, and whether there is a ceiling.
     */
    private fun adjustDistanceIfNecessary() {
        val sourcePos = props.sourcePos
        val targetPos = props.targetPos
        val phi = props.phi
        val rho = props.rho
        val distance = props.distance

        // Make the distance shorter if there is a ceiling directly above the target.
        distance.desired = if (App.editor.isShown) EDITOR_DESIRED_DISTANCE else CameraProps.DEFAULT_DISTANCE
        sourcePos.desired.set(targetPos.current)
        sourcePos.desired.z += MAX_CEILING_DISTANCE

        App.bricks.forEachBrickOnWorldLine(targetPos.current, sourcePos.desired) { brick, _, _ ->
            if (isSolid(brick.shape)) {
                distance.desired = DESIRED_DISTANCE_IN_CONFINED_SPACE
                false // abort
            } else {
                true
            }
        }

        // Find any walls that we find between the target and the position at the desired distance.
        props.setSourceFromAnglesAndDistance(phi.current, rho.current, distance.desired)

        App.bricks.forEachBrickOnWorldLine(targetPos.current, sourcePos.desired) { brick, worldPt, _ ->
            if (!isSolid(brick.shape)) {
                true // continue
            } else if (App.editor.isShown && worldPt.distanceTo(targetPos.current) <= Brick.WORLD_BRICK_SIZE) {
                // This wall is too close. Maybe the target is stuck in a brick. Ignore this wall.
                true // continue
            } else {
                // Move the camera a bit away from the nearest wall we found.

                val distanceToWall = (worldPt - targetPos.current).length()

                if (App.editor.isShown) {
                    // In the editor, it's sometimes ok if we can see through a wall, but we should never be very close
                    // to the target.
                    distance.desired = max(EDITOR_MIN_DISTANCE, distanceToWall - 0.1f * Brick.WORLD_BRICK_SIZE)
                } else {
                    // In the game, we can get very close to the target, but we must stay in front of blocking walls.
                    distance.desired = max(MIN_DISTANCE, distanceToWall - 0.2f * Brick.WORLD_BRICK_SIZE)

                    if (distance.desired < distance.current) {
                        distance.jump()
                    }
                }

                false // abort
            }
        }
    }

    /**
     * Shapes that return false here will be ignored by camera positioning.
     */
    private fun isSolid(shape: BrickShape) =
        shape.relVolume >= 0.15f

    companion object {
        private const val DESIRED_DISTANCE_IN_CONFINED_SPACE = 4.0f
        private const val MIN_DISTANCE = 0.5f
        private const val EDITOR_MIN_DISTANCE = 3.0f
        private const val EDITOR_DESIRED_DISTANCE = 10.0f
        private const val MAX_CEILING_DISTANCE = 4.0f
    }
}
