package ch.digorydoo.titanium.game.player

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.EPSILON
import ch.digorydoo.titanium.game.core.GameSampleId
import kotlin.math.PI
import kotlin.math.sqrt

class PlayerBehaviour(
    private val gel: GraphicElement,
    private val frameMgr: PlayerFrameManager,
): GraphicElement.Behaviour {
    var didCollideWithFloor = true
    var touchDownSpeed = 0.0f
    var timeOfGroundContact = 0.0f
    private var hasGroundContact = true

    private val leftJoyWithCameraCorrection = MutablePoint2f()

    override fun animate() {
        val pos = gel.pos

        if (didCollideWithFloor) {
            if (!hasGroundContact && touchDownSpeed < TOUCHDOWN_MIN_SPEED) {
                val volume = clamp((touchDownSpeed - TOUCHDOWN_MIN_SPEED) / (TOUCHDOWN_MAX_SPEED - TOUCHDOWN_MIN_SPEED))
                App.sound.play(GameSampleId.TOUCHDOWN, pos, volume)
            }
            hasGroundContact = true
            didCollideWithFloor = false
        } else {
            hasGroundContact = false
        }

        if (App.dlg.hasActiveDlg || App.editor.isShown) {
            return
        }

        leftJoyWithCameraCorrection
            .set(App.input.values.leftJoy)
            .rotate(App.camera.currentPhi + 0.5 * PI)

        if (shouldJump()) {
            jump()
        } else if (hasGroundContact) {
            handleWalkKeys()
        }
    }

    private fun shouldJump(): Boolean {
        var shouldJump = false

        if (!frameMgr.isJumping) {
            val input = App.input.values

            if (input.actionY.pressedOnce) {
                if (hasGroundContact) {
                    shouldJump = true
                } else {
                    if (App.time.sessionTime - timeOfGroundContact < 0.2f) {
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
        body.addForce(0.0f, 0.0f, JUMP_Z_FORCE)

        if (!leftJoyWithCameraCorrection.isZero()) {
            body.speed.x *= 0.3f
            body.speed.y *= 0.3f
            body.addForce(
                leftJoyWithCameraCorrection.y * JUMP_XY_FORCE,
                leftJoyWithCameraCorrection.x * JUMP_XY_FORCE,
                0.0f
            )
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

            var dx = leftJoyWithCameraCorrection.y // sic!
            var dy = leftJoyWithCameraCorrection.x
            val joyLen = sqrt(dx * dx + dy * dy)
            val joyNormX = dx / joyLen
            val joyNormY = dy / joyLen

            val speedFactor = if (App.input.values.shift.pressed) 2.0f else clamp(joyLen, 0.0f, 1.0f)
            val targetSpeed = WALK_SPEED * speedFactor

            dx = body.speed.x
            dy = body.speed.y
            val speedLen = sqrt(dx * dx + dy * dy)

            // speedInDesiredDir = speedLen * (`normalised speed` dot joyNorm)
            val speedInDesiredDir = when {
                speedLen <= EPSILON -> 0.0f
                else -> speedLen * (dx / speedLen * joyNormX + dy / speedLen * joyNormY)
            }

            if (speedInDesiredDir < targetSpeed) {
                body.addForce(joyNormX * WALK_FORCE, joyNormY * WALK_FORCE, 0.0f)
            }

            frameMgr.walk(dir, clamp(speedFactor, 0.42f, 1.8f))
        }
    }

    companion object {
        private const val WALK_SPEED = 5.0f
        private const val JUMP_Z_FORCE = 42.0f
        private const val JUMP_XY_FORCE = 42.0f
        private const val WALK_FORCE = 1300.0f
        private const val TOUCHDOWN_MIN_SPEED = -2.77f // speed where touchdown sound has volume 0
        private const val TOUCHDOWN_MAX_SPEED = -6.66f // speed where touchdown sound has full volume
    }
}
