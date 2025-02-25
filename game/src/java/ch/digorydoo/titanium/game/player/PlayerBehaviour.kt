package ch.digorydoo.titanium.game.player

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.game.core.GameSampleId
import kotlin.math.PI

class PlayerBehaviour(
    private val gel: GraphicElement,
    private val frameMgr: PlayerFrameManager,
): GraphicElement.Behaviour {
    private val targetSpeed = MutablePoint2f()
    private var didHaveGroundContact = true

    private val leftJoyWithCameraCorrection = MutablePoint2f()

    override fun animate() {
        val body = gel.body ?: return
        val pos = gel.pos

        if (body.hasGroundContact && !didHaveGroundContact && body.touchDownSpeed < TOUCHDOWN_MIN_SPEED) {
            val volume =
                clamp((body.touchDownSpeed - TOUCHDOWN_MIN_SPEED) / (TOUCHDOWN_MAX_SPEED - TOUCHDOWN_MIN_SPEED))
            App.sound.play(GameSampleId.TOUCHDOWN, pos, volume)
        }

        didHaveGroundContact = body.hasGroundContact

        if (App.dlg.hasActiveDlg || App.editor.isShown) {
            return
        }

        leftJoyWithCameraCorrection
            .set(App.input.values.leftJoy)
            .rotate(App.camera.currentPhi + 0.5 * PI)

        if (shouldJump()) {
            jump()
        } else if (body.hasGroundContact) {
            handleWalkKeys()
        }
    }

    private fun shouldJump(): Boolean {
        val body = gel.body ?: return false
        var shouldJump = false

        if (!frameMgr.isJumping) {
            val input = App.input.values

            if (input.actionY.pressedOnce) {
                if (body.hasGroundContact) {
                    shouldJump = true
                } else {
                    if (App.time.sessionTime - body.timeOfGroundContact < 0.2f) {
                        // The gel has lost ground contact very recently ago.
                        // We still allow to start a jump to make controls a little easier.
                        shouldJump = true
                    }
                }
            }
        }

        return shouldJump
    }

    private fun jump() {
        val body = gel.body ?: return
        val pos = gel.pos

        frameMgr.jump()
        App.sound.play(GameSampleId.JUMP, pos)
        body.force.z += JUMP_Z_ACCEL * body.mass

        if (!leftJoyWithCameraCorrection.isZero()) {
            body.speed.x *= 0.3f
            body.speed.y *= 0.3f
            body.force.x += leftJoyWithCameraCorrection.y * JUMP_XY_ACCEL * body.mass
            body.force.y += leftJoyWithCameraCorrection.x * JUMP_XY_ACCEL * body.mass
        }
    }

    private fun handleWalkKeys() {
        val body = gel.body ?: return
        val dir = Direction.fromVector(App.input.values.leftJoy, PI.toFloat() / 4.0f)

        if (dir == null) {
            // leftJoy is at rest. Go idle!
            if (!frameMgr.isIdle) {
                frameMgr.goIdle()
            }
        } else {
            // Walk or run!

            val speedFactor =
                if (App.input.values.shift.pressed) 2.0f
                else clamp(leftJoyWithCameraCorrection.length(), -1.0f, 1.0f)

            targetSpeed.set(leftJoyWithCameraCorrection.y, leftJoyWithCameraCorrection.x).normalize()
            targetSpeed *= WALK_SPEED * speedFactor
            body.addSpeedRelativeForce(targetSpeed.x, targetSpeed.y, 0.0f, WALK_ACCEL)
            frameMgr.walk(dir, clamp(speedFactor, 0.42f, 1.8f))
        }
    }

    companion object {
        private const val WALK_SPEED = 4.2f
        private const val JUMP_Z_ACCEL = 244.0f
        private const val JUMP_XY_ACCEL = 144.0f
        private const val WALK_ACCEL = 30.0f
        private const val TOUCHDOWN_MIN_SPEED = -2.77f // speed where touchdown sound has volume 0
        private const val TOUCHDOWN_MAX_SPEED = -6.66f // speed where touchdown sound has full volume
    }
}
