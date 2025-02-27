package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.bricks.CollideCylinderVsBrick
import ch.digorydoo.titanium.engine.physics.bricks.CollideSphereVsBrick
import ch.digorydoo.titanium.engine.physics.regular.CollideCylinderVsCylinder
import ch.digorydoo.titanium.engine.physics.regular.CollideSphereVsCylinder
import ch.digorydoo.titanium.engine.physics.regular.CollideSphereVsPlane
import ch.digorydoo.titanium.engine.physics.regular.CollideSphereVsSphere

class CollisionManager {
    private val hitPt = MutablePoint3f()

    private val sphereVsSphere = CollideSphereVsSphere()
    private val sphereVsPlane = CollideSphereVsPlane()
    private val sphereVsCylinder = CollideSphereVsCylinder()
    private val cylinderVsCylinder = CollideCylinderVsCylinder()

    private val sphereVsBrick = CollideSphereVsBrick()
    private val cylinderVsBrick = CollideCylinderVsBrick()

    /**
     * Called by GelLayer for all pairs of GraphicElement whose canCollide returned true. The same pair with the gels
     * reversed will NOT be called in the same frame. The call to this function happens after all gels have completed
     * their animatePhase1, and before they start their animatePhase2. When gels collide, their didCollide callbacks are
     * called that need to decide what to do. If either callback returns true, the bodies will bounce off each-other.
     */
    fun handleCollisions(gel1: GraphicElement, gel2: GraphicElement) {
        val b1 = gel1.body ?: return
        val b2 = gel2.body ?: return

        if (checkNextPos(b1, b2)) {
            val gel1WantsBounce = gel1.didCollide(gel2, hitPt)
            val gel2WantsBounce = gel2.didCollide(gel1, hitPt)

            if (gel1WantsBounce || gel2WantsBounce) {
                bounce(b1, b2)
            }
        }
    }

    /**
     * @return true if the two objects collide;
     *    false if the two objects do not collide;
     *    null if we can't tell, because this combination is not implemented
     */
    private fun checkNextPos(body1: RigidBody, body2: RigidBody): Boolean {
        return when (body1) {
            is FixedSphereBody -> when (body2) {
                is FixedSphereBody -> sphereVsSphere.checkNextPos(body1, body2, hitPt)
                is FixedCylinderBody -> sphereVsCylinder.checkNextPos(body1, body2, hitPt)
                is FixedPlaneBody -> sphereVsPlane.checkNextPos(body1, body2, hitPt)
            }
            is FixedCylinderBody -> when (body2) {
                is FixedSphereBody -> sphereVsCylinder.checkNextPos(body2, body1, hitPt)
                is FixedCylinderBody -> cylinderVsCylinder.checkNextPos(body1, body2, hitPt)
                is FixedPlaneBody -> throw NotImplementedError()
            }
            is FixedPlaneBody -> when (body2) {
                is FixedSphereBody -> sphereVsPlane.checkNextPos(body2, body1, hitPt)
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

    /**
     * Called by GelLayer for each GraphicElement whose canCollide returned true and whose mass is less than
     * STATIC_MASS.
     */
    fun handleCollisions(gel: GraphicElement, brickVolume: BrickVolume) {
        val body = gel.body ?: return

        when (body) {
            is FixedSphereBody -> {
                sphereVsBrick.checkNextPos(body, brickVolume) { brick, hitPt, hitNormal, bounce ->
                    gel.didCollide(brick, hitPt, hitNormal)
                    bounce()
                }
            }
            is FixedCylinderBody -> {
                cylinderVsBrick.checkNextPos(body, brickVolume) { brick, hitPt, hitNormal, bounce ->
                    gel.didCollide(brick, hitPt, hitNormal)
                    bounce()
                }
            }
            is FixedPlaneBody -> throw NotImplementedError()
        }
    }
}
