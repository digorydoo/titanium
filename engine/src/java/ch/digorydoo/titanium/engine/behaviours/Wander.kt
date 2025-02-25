package ch.digorydoo.titanium.engine.behaviours

import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class Wander(private val gel: GraphicElement): GraphicElement.Behaviour {
    private var phi = (2.0 * Math.PI * Math.random()).toFloat()
    private var changeDirTime = 0.0f

    override fun animate() {
        val body = gel.body ?: return

        // if (body.isAffectedByGravity && !body.hasGroundContact) {
        //     return // gel is falling
        // }

        if (App.time.sessionTime > changeDirTime) {
            val dur = MIN_SAME_DIR_DURATION + (MAX_SAME_DIR_DURATION - MIN_SAME_DIR_DURATION) * Math.random()
            changeDirTime = App.time.sessionTime + dur.toFloat()
            phi += ((-1.0 + 2.0 * Math.random()) * MAX_DIR_CHANGE_AMOUNT).toFloat()
        }

        body.addSpeedRelativeForce(WANDER_SPEED * cos(phi), WANDER_SPEED * sin(phi), 0.0f, WANDER_ACCEL)
    }

    // Call steerAway from a Collidable.didCollide
    fun steerAway(cx: Float, cy: Float) {
        val pos = gel.pos
        phi = atan2(pos.y - cy, pos.x - cx)
    }

    fun steerAway(collisionPt: Point2f) =
        steerAway(collisionPt.x, collisionPt.y)

    companion object {
        private const val WANDER_SPEED = 16.0f / 36
        private const val WANDER_ACCEL = 42.0f
        private const val MIN_SAME_DIR_DURATION = 1.0f
        private const val MAX_SAME_DIR_DURATION = 10.0f
        private const val MAX_DIR_CHANGE_AMOUNT = 0.5f * Math.PI
    }
}
