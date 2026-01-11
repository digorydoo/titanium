package ch.digorydoo.titanium.game.player

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.normAngle
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.Behaviour
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.EPSILON
import ch.digorydoo.titanium.game.core.GameSampleId
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

class PlayerBehaviour(
    private val gel: PlayerGel,
    private val frameCycles: PlayerFrameCycles,
    initialOrientation: Float,
): Behaviour {
    var didCollideWithFloor = true
    var touchDownSpeed = 0.0f
    var timeOfGroundContact = 0.0f
    var orientation = initialOrientation
    private var hasGroundContact = true

    private val leftJoyWithCameraCorrection = MutablePoint2f()

    fun animate() {
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

        if (App.dlg.isInDlgMode || App.editor.isShown) {
            return
        }

        val leftJoyAtRest = App.input.leftJoy.maxAbsComponent() <= EPSILON

        if (leftJoyAtRest) {
            leftJoyWithCameraCorrection.set(0.0f, 0.0f)
        } else {
            leftJoyWithCameraCorrection
                .set(App.input.leftJoy)
                .rotate(App.camera.currentPhi + 0.5 * PI)
        }

        if (shouldJump()) {
            jump()
        } else if (!hasGroundContact) {
            if (!leftJoyAtRest) {
                handleDirInputWhenInMidAir()
            }
            val dir = getDirFromOrientation()
            if (dir != frameCycles.dir) frameCycles.turn(dir)
        } else {
            if (!leftJoyAtRest) {
                handleDirInputWhenOnGround()
            } else if (!frameCycles.isIdle) {
                frameCycles.goIdle()
            } else {
                val dir = getDirFromOrientation()
                if (dir != frameCycles.dir) frameCycles.turn(dir)
            }
        }
    }

    private fun shouldJump(): Boolean {
        var shouldJump = false

        if (!frameCycles.isJumping) {
            val input = App.input

            if (input.jumpBtn.pressedOnce) {
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
        val body = gel.body
        val pos = gel.pos

        App.sound.play(GameSampleId.JUMP, pos)
        body.addForce(0.0f, 0.0f, JUMP_Z_FORCE)

        val dir = getDirFromOrientation()
        frameCycles.jump(dir)

        if (!leftJoyWithCameraCorrection.isZero()) {
            body.speed.x *= 0.6f
            body.speed.y *= 0.6f
            body.addForce(
                leftJoyWithCameraCorrection.y * JUMP_XY_FORCE,
                leftJoyWithCameraCorrection.x * JUMP_XY_FORCE,
                0.0f
            )
        }
    }

    private fun handleDirInputWhenInMidAir() {
        val body = gel.body
        var dx = leftJoyWithCameraCorrection.y // sic!
        var dy = leftJoyWithCameraCorrection.x
        val joyLen = sqrt(dx * dx + dy * dy)
        val joyNormX = dx / joyLen
        val joyNormY = dy / joyLen

        dx = body.speed.x
        dy = body.speed.y
        val speedLen = sqrt(dx * dx + dy * dy)

        // speedInDesiredDir = speedLen * (`normalised speed` dot joyNorm)
        val speedInDesiredDir = when {
            speedLen <= EPSILON -> 0.0f
            else -> speedLen * (dx / speedLen * joyNormX + dy / speedLen * joyNormY)
        }

        if (speedInDesiredDir < MIDAIR_SPEED) {
            body.addForce(joyNormX * MIDAIR_FORCE, joyNormY * MIDAIR_FORCE, 0.0f)
        }

        orientation = atan2(dy, dx)
    }

    private fun handleDirInputWhenOnGround() {
        val body = gel.body
        var dx = leftJoyWithCameraCorrection.y // sic!
        var dy = leftJoyWithCameraCorrection.x
        val joyLen = sqrt(dx * dx + dy * dy)
        val joyNormX = dx / joyLen
        val joyNormY = dy / joyLen

        val speedFactor = if (App.input.dashBtn.pressed) 2.0f else clamp(joyLen, 0.0f, 1.0f)
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

        val dir = getDirFromOrientation()
        frameCycles.walk(dir, clamp(speedFactor, 0.42f, 1.8f))
        orientation = atan2(dy, dx)
    }

    private fun getDirFromOrientation() =
        Direction.fromAngle(normAngle(orientation + App.camera.currentPhi + ORIENTATION_DIR_OFFSET))

    companion object {
        private const val WALK_SPEED = 5.0f
        private const val WALK_FORCE = 1300.0f
        private const val MIDAIR_SPEED = 1.0f
        private const val MIDAIR_FORCE = 1100.0f
        private const val JUMP_Z_FORCE = 18100.0f
        private const val JUMP_XY_FORCE = 4096.0f
        private const val TOUCHDOWN_MIN_SPEED = -2.77f // speed where touchdown sound has volume 0
        private const val TOUCHDOWN_MAX_SPEED = -6.66f // speed where touchdown sound has full volume
        private const val ORIENTATION_DIR_OFFSET = (PI / 4.0f).toFloat()
    }
}
