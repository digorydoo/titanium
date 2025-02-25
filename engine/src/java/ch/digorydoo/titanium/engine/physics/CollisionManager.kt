package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.Brick.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement

class CollisionManager {
    private val hitPt = MutablePoint3f()
    private val sphereVsSphere = CollideSphereVsSphere()
    private val cylinderVsCylinder = CollideCylinderVsCylinder()
    private val sphereVsCylinder = CollideSphereVsCylinder()
    private val sphereVsPlane = CollideSphereVsPlane()

    /**
     * Called by GelLayer for all pairs of GraphicElement whose canCollide returned true. The same pair with the gels
     * reversed will NOT be called in the same frame. The call to this function happens after all gels have completed
     * their animatePhase1, and before they start their animatePhase2. When gels collide, their didCollide callbacks are
     * called that need to decide what to do. If either callback returns true, the bodies will bounce off each-other.
     */
    fun handleCollisions(gel1: GraphicElement, gel2: GraphicElement) {
        val b1 = gel1.body ?: return
        val b2 = gel2.body ?: return

        if (check(b1, b2)) {
            // Log.info("Gels collide: $gel1 vs. $gel2")
            val gel1WantsBounce = gel1.didCollide(gel2, hitPt)
            val gel2WantsBounce = gel2.didCollide(gel1, hitPt)

            if (gel1WantsBounce || gel2WantsBounce) {
                bounce(b1, b2)
            }
        }
    }

    /**
     * Called by GelLayer for each GraphicElement whose canCollide returned true and whose mass is less than
     * STATIC_MASS.
     */
    fun handleCollisions(gel: GraphicElement, brickVolume: BrickVolume) {
        val body = gel.body ?: return

        // getFloor ignores floors above the given z, so we have to add WORLD_BRICK_SIZE to be sure to get the current
        // floor. Also, nextPos will drop slightly due to gravity, so we have to add an extra brick for compensation!

        val floor = body.floor
        val curPos = body.pos
        val nextPos = body.nextPos
        brickVolume.getFloor(nextPos.x, nextPos.y, nextPos.z + 3 * WORLD_BRICK_SIZE, floor)

        if (nextPos.z <= floor.worldCoords.z) {
            nextPos.z = floor.worldCoords.z

            if (!body.hasGroundContact) {
                body.hasGroundContact = true
                body.timeOfGroundContact = App.time.sessionTime
                body.touchDownSpeed = body.nextSpeed.z
            }

            val factor = 1.0f - 0.1f * (1.0f - body.elasticity)
            body.nextSpeed.x *= factor
            body.nextSpeed.y *= factor
            body.nextSpeed.z = 0.0f
        } else if (nextPos.z > floor.worldCoords.z) {
            body.hasGroundContact = false
        }

        if (curPos.z + MAX_STEP < nextPos.z) {
            val hitPt = Point3f(nextPos)
            nextPos.set(curPos)
            gel.didCollide(floor, hitPt)
        }
    }

    /**
     * @return true if the two objects collide;
     *    false if the two objects do not collide;
     *    null if we can't tell, because this combination is not implemented
     */
    private fun check(body1: RigidBody, body2: RigidBody): Boolean {
        return when (body1) {
            is FixedSphereBody -> when (body2) {
                is FixedSphereBody -> sphereVsSphere.check(body1, body2, hitPt)
                is FixedCylinderBody -> sphereVsCylinder.check(body1, body2, hitPt)
                is FixedPlaneBody -> sphereVsPlane.check(body1, body2, hitPt)
            }
            is FixedCylinderBody -> when (body2) {
                is FixedSphereBody -> sphereVsCylinder.check(body2, body1, hitPt)
                is FixedCylinderBody -> cylinderVsCylinder.check(body1, body2, hitPt)
                is FixedPlaneBody -> throw NotImplementedError()
            }
            is FixedPlaneBody -> when (body2) {
                is FixedSphereBody -> sphereVsPlane.check(body2, body1, hitPt)
                is FixedCylinderBody -> throw NotImplementedError()
                is FixedPlaneBody -> throw NotImplementedError()
            }
        }
    }

    /**
     * Moves the two bodies apart and applies the correct forces. Note that it is required that check() be called
     * beforehand, because collision strategies may need some values from bounce() that they computed in check().
     */
    private fun bounce(body1: RigidBody, body2: RigidBody) {
        when (body1) {
            is FixedSphereBody -> when (body2) {
                is FixedSphereBody -> sphereVsSphere.bounce(body1, body2)
                is FixedCylinderBody -> sphereVsCylinder.bounce(body1, body2)
                is FixedPlaneBody -> sphereVsPlane.bounce(body1, body2)
            }
            is FixedCylinderBody -> when (body2) {
                is FixedSphereBody -> sphereVsCylinder.bounce(body2, body1)
                is FixedCylinderBody -> cylinderVsCylinder.bounce(body1, body2)
                is FixedPlaneBody -> throw NotImplementedError()
            }
            is FixedPlaneBody -> when (body2) {
                is FixedSphereBody -> sphereVsPlane.bounce(body2, body1)
                is FixedCylinderBody -> throw NotImplementedError()
                is FixedPlaneBody -> throw NotImplementedError()
            }
        }
    }

    companion object {
        private const val MAX_STEP = WORLD_BRICK_SIZE / 4.0f
    }
}
