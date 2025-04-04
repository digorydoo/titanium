package ch.digorydoo.titanium.engine.physics.collision_strategy

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.utils.assertGreaterThan
import ch.digorydoo.titanium.engine.utils.assertLessThan
import ch.digorydoo.titanium.engine.utils.assertWithin
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CollideCylinderVsCylinderTest {
    @Test
    fun `should collide when the two cylinders get too close and the speed is parallel to the x-axis`() {
        val b1 = FixedCylinderBody(
            "b1",
            initialPos = MutablePoint3f(10.0f, 7.2f, 11.1f),
            mass = 13.0f,
            gravity = false,
            radius = 0.3f,
            height = 1.0f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val b2 = FixedCylinderBody(
            "b2",
            initialPos = MutablePoint3f(10.5f, 7.1f, 10.45f),
            mass = 11.0f,
            gravity = false,
            radius = 0.2f,
            height = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val ck = CollideCylinderVsCylinder()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit)

        // The two cylinders should not collide at their original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(check())

        // Apply a force to each towards the other
        b1.apply {
            addForce(220.0f, 0.0f, 0.0f)
            applyForces()
        }
        b2.apply {
            addForce(-220.0f, 0.0f, 0.0f)
            applyForces()
        }

        // The two cylinders should now collide
        assertTrue(check())

        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2")

        assertEquals(10.298547f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.14f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.65f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2, and z should be 0 since it's SIDE
        assertEquals(0.97978365f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-0.2000601f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // b1.nextSpeed points in the positive direction of x
        assertEquals(0.2820513f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed points in the negative direction of x
        assertEquals(-0.33333334f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1.nextPos has moved a little in the direction of its speed
        assertEquals(10.004701f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(7.2f, b1.nextPos.y, "b1.nextPos.y") // the same as before
        assertEquals(11.1f, b1.nextPos.z, "b1.nextPos.z") // the same as before

        // b2.nextPos has moved a little in the direction of its speed
        assertEquals(10.494445f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(7.1f, b2.nextPos.y, "b2.nextPos.y") // the same as before
        assertEquals(10.45f, b2.nextPos.z, "b2.nextPos.z") // the same as before

        // The two should no longer collide after bounce
        bounce()
        assertFalse(check())

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, "b1.pos.x")
        assertEquals(7.2f, b1.pos.y, "b1.pos.y")
        assertEquals(11.1f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.5f, b2.pos.x, "b2.pos.x")
        assertEquals(7.1f, b2.pos.y, "b2.pos.y")
        assertEquals(10.45f, b2.pos.z, "b2.pos.z")

        // b1 had no speed before we added a force
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2 had no speed before we added a force
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextPos has been moved a little
        assertEquals(10.00463f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(7.200014f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // has slightly moved due to y diff of centres
        assertEquals(11.1f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has been moved a little
        assertEquals(10.494528f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(7.0999827f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y") // has slightly moved due to y diff of centres
        assertEquals(10.45f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified
        assertEquals(-0.06769069f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.013821525f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified
        assertEquals(0.07999805f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(-0.01633464f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // Since it was SIDE, the cylinders should now be separated in XY
        val centre1 = Point2f(b1.nextPos.x, b1.nextPos.y)
        val centre2 = Point2f(b2.nextPos.x, b2.nextPos.y)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.000007f, newDistance - (b1.radius + b2.radius), "gap") // less than 7 mm/1000
    }

    @Test
    fun `should collide when the two cylinders get too close and the speed is parallel to the y-axis`() {
        val b1 = FixedCylinderBody(
            "b1",
            initialPos = MutablePoint3f(8.2f, 7.5f, 11.1f),
            mass = 7.0f,
            gravity = false,
            radius = 0.3f,
            height = 1.0f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val b2 = FixedCylinderBody(
            "b2",
            initialPos = MutablePoint3f(8.1f, 7.0f, 10.45f),
            mass = 9.0f,
            gravity = false,
            radius = 0.2f,
            height = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val ck = CollideCylinderVsCylinder()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit)

        // The two cylinders should not collide at their original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(check())

        // Apply a force to each towards the other
        b1.apply {
            addForce(0.0f, -145.0f, 0.0f)
            applyForces()
        }
        b2.apply {
            addForce(0.0f, 145.0f, 0.0f)
            applyForces()
        }

        // The two cylinders now collide
        assertTrue(check())

        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2")

        // The hit point was set to a point within the colliding area
        assertEquals(8.14f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.2003837f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.65f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2, and z should be 0 since it's SIDE
        assertEquals(-0.20004871f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-0.9797859f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // b1.nextSpeed points in the negative direction of y
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.3452381f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed points in the positive direction of y
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.26851854f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1.nextPos has moved a little in the direction of its speed
        assertEquals(8.2f, b1.nextPos.x, "b1.nextPos.x") // the same as before
        assertEquals(7.494246f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(11.1f, b1.nextPos.z, "b1.nextPos.z") // the same as before

        // b2.nextPos has moved a little in the direction of its speed
        assertEquals(8.1f, b2.nextPos.x, "b2.nextPos.x") // the same as before
        assertEquals(7.004475f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.45f, b2.nextPos.z, "b2.nextPos.z") // the same as before

        // The two should no longer collide after bounce
        bounce()
        assertFalse(check())

        // b1.pos is unchanged
        assertEquals(8.2f, b1.pos.x, "b1.pos.x")
        assertEquals(7.5f, b1.pos.y, "b1.pos.y")
        assertEquals(11.1f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(8.1f, b2.pos.x, "b2.pos.x")
        assertEquals(7.0f, b2.pos.y, "b2.pos.y")
        assertEquals(10.45f, b2.pos.z, "b2.pos.z")

        // b1 had no speed before we added a force
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2 had no speed before we added a force
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextPos has been moved a little
        assertEquals(8.200014f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x") // has slightly moved due to x diff of centres
        assertEquals(7.494318f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(11.1f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has been moved a little
        assertEquals(8.099989f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x") // has slightly moved due to x diff of centres
        assertEquals(7.0044193f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.45f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified
        assertEquals(0.016917167f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.082855366f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified
        assertEquals(-0.013157794f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(-0.06444303f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // Since it was SIDE, the cylinders should now be separated in XY
        val centre1 = Point2f(b1.nextPos.x, b1.nextPos.y)
        val centre2 = Point2f(b2.nextPos.x, b2.nextPos.y)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.000007f, newDistance - (b1.radius + b2.radius), "gap") // less than 7 mm/1000
    }

    @Test
    fun `should collide when the two cylinders get too close and the speed is parallel to the z-axis`() {
        val b1 = FixedCylinderBody(
            "b1",
            initialPos = MutablePoint3f(8.3f, 7.1f, 11.21f),
            mass = 7.0f,
            gravity = false,
            radius = 0.3f,
            height = 1.0f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val b2 = FixedCylinderBody(
            "b2",
            initialPos = MutablePoint3f(8.1f, 7.2f, 10.45f),
            mass = 9.0f,
            gravity = false,
            radius = 0.2f,
            height = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val ck = CollideCylinderVsCylinder()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit)

        // The two cylinders should not collide at their original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(check())

        // Apply a force to each towards the other
        b1.apply {
            addForce(0.0f, 0.0f, -145.0f)
            applyForces()
        }
        b2.apply {
            addForce(0.0f, 0.0f, 145.0f)
            applyForces()
        }

        // The two cylinders now collide
        assertTrue(check())

        // b1's bottom collided with b2's top
        assertEquals(HitArea.BOTTOM, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP, hit.area2, "hit.area2")

        // The hit point was set to a point within the colliding area
        assertEquals(8.18f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.16f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.704361f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2, and XY should be 0 since it's BOTTOM/TOP
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-1.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // b1.nextSpeed points in the negative direction of z
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.3452381f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed points in the positive direction of z
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.26851854f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1.nextPos has moved a little in the direction of its speed
        assertEquals(8.3f, b1.nextPos.x, "b1.nextPos.x") // the same as before
        assertEquals(7.1f, b1.nextPos.y, "b1.nextPos.y") // the same as before
        assertEquals(11.2042465f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has moved a little in the direction of its speed
        assertEquals(8.1f, b2.nextPos.x, "b2.nextPos.x") // the same as before
        assertEquals(7.2f, b2.nextPos.y, "b2.nextPos.y") // the same as before
        assertEquals(10.454475f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // The two should no longer collide after bounce
        bounce()
        assertFalse(check())

        // b1.pos is unchanged
        assertEquals(8.3f, b1.pos.x, "b1.pos.x")
        assertEquals(7.1f, b1.pos.y, "b1.pos.y")
        assertEquals(11.21f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(8.1f, b2.pos.x, "b2.pos.x")
        assertEquals(7.2f, b2.pos.y, "b2.pos.y")
        assertEquals(10.45f, b2.pos.z, "b2.pos.z")

        // b1 had no speed before we added a force
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2 had no speed before we added a force
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextPos has been moved a little
        assertEquals(8.3f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(7.1f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(11.204376f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has been moved a little
        assertEquals(8.1f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(7.2f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.454375f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")

        // b2.nextSpeed has been modified
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")

        // b1 is standing on top of b2, and the difference in z speed is small.
        // The strategy is expected to set b1's z speed to b2's before bouncing.
        assertEquals(0.26851854f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")
        assertEquals(0.26851854f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should correctly bounce one cylinder off the other when moving in an arbitrary direction`() {
        val b1 = FixedCylinderBody(
            "b1",
            initialPos = MutablePoint3f(10.0f, 10.0f, 10.5f),
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 1.0f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val b2 = FixedCylinderBody(
            "b2",
            initialPos = MutablePoint3f(10.0f, 10.51f, 10.76f),
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val ck = CollideCylinderVsCylinder()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit)

        // We give b1 an initial speed
        b1.speed.set(0.0f, 0.6f, 0.6f)

        // Update nextPos and nextSpeed
        b1.applyForces()
        b2.applyForces()

        // The cylinders should now collide
        assertTrue(check())

        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.26f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.76f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // hitNormal12 should point from b1 to b2, and z should be 0 since it's SIDE
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(1.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.01f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.51f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.51f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.76f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // The cylinders should no longer collide after bounce
        bounce()
        assertFalse(check())

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, TOLERANCE, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, TOLERANCE, "b1.pos.y")
        assertEquals(10.5f, b1.pos.z, TOLERANCE, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.0f, b2.pos.x, TOLERANCE, "b2.pos.x")
        assertEquals(10.51f, b2.pos.y, TOLERANCE, "b2.pos.y")
        assertEquals(10.76f, b2.pos.z, TOLERANCE, "b2.pos.z")

        // b1.speed is unchanged
        assertEquals(0.0f, b1.speed.x, TOLERANCE, "b1.speed.x")
        assertEquals(0.6f, b1.speed.y, TOLERANCE, "b1.speed.y")
        assertEquals(0.6f, b1.speed.z, TOLERANCE, "b1.speed.z")

        // b2.speed is unchanged
        assertEquals(0.0f, b2.speed.x, TOLERANCE, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, TOLERANCE, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, TOLERANCE, "b2.speed.z")

        // b1.nextPos has moved a little
        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.009997f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.51f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has moved a little
        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.510003f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.76f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified: still moves in the same direction, but slower
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.225f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.49200004f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified: moves away from b1, faster than b1
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.375f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.10799999f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // Since it was SIDE, the cylinders should now be separated in XY
        val centre1 = Point2f(b1.nextPos.x, b1.nextPos.y)
        val centre2 = Point2f(b2.nextPos.x, b2.nextPos.y)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.000007f, newDistance - (b1.radius + b2.radius), "gap") // less than 7 mm/1000
    }

    @Test
    fun `should behave graciously if two identical cylinders are spawned in the exact same spot`() {
        val pos = MutablePoint3f(42.0f, 33.0f, 24.0f)
        val b1 = FixedCylinderBody(
            "b1",
            initialPos = pos,
            mass = 42.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            elasticity = 0.5f,
            friction = 0.5f,
        )
        val b2 = FixedCylinderBody(
            "b2",
            initialPos = pos,
            mass = 42.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            elasticity = 0.5f,
            friction = 0.5f,
        )

        val ck = CollideCylinderVsCylinder()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit)

        // Apply forces to end anim phase 1 and update nextPos and nextSpeed
        b1.applyForces()
        b2.applyForces()

        // The two cylinders should obviously collide
        Log.enabled = false // suppress expected log message
        assertTrue(check(), "initial pos should collide")
        Log.enabled = true

        // The hit area happens to be declared SIDE even though this is arbitrary
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2")

        // The hit point is set to the centre of the two cylinders
        assertEquals(42.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(33.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(24.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 is random and cannot be tested, but it should be properly normalised
        val n = hit.hitNormal12
        val len = sqrt(n.x * n.x + n.y * n.y + n.z * n.z)
        assertLessThan(len, 1.01f, "hitNormal12")
        assertGreaterThan(len, 0.99f, "hitNormal12")

        // b1.nextSpeed is zero, since no forces were acting on the body
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed is zero, since no forces were acting on the body
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1.nextPos is still the same position
        assertEquals(42.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(33.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(24.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos is still the same position
        assertEquals(42.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(33.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(24.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // Bounce will force the two cylinders apart such that they no longer collide
        try {
            Log.enabled = false // suppress expected log message
            bounce()

            assertFalse(
                check(),
                "should no longer collide, but they do: " +
                    "b1.nextPos=${b1.nextPos}, " +
                    "b2.nextPos=${b2.nextPos}, " +
                    "hitNormal12=${hit.hitNormal12}, " +
                    "expected distance in XY=${b1.radius + b2.radius}, " +
                    "actual distance in XY=" +
                    Point2f(b1.nextPos.x, b1.nextPos.y).distanceTo(Point2f(b2.nextPos.x, b2.nextPos.y))
            )
        } finally {
            Log.enabled = true
        }

        // A separation force was not added
        assertEquals(0.0f, b1.resultingForce.x, "b1.resultingForce.x")
        assertEquals(0.0f, b1.resultingForce.y, "b1.resultingForce.y")
        assertEquals(0.0f, b1.resultingForce.z, "b1.resultingForce.z")
        assertEquals(0.0f, b2.resultingForce.x, "b2.resultingForce.x")
        assertEquals(0.0f, b2.resultingForce.y, "b2.resultingForce.y")
        assertEquals(0.0f, b2.resultingForce.z, "b2.resultingForce.z")

        // b1.pos is unchanged
        assertEquals(42.0f, b1.pos.x, "b1.pos.x")
        assertEquals(33.0f, b1.pos.y, "b1.pos.y")
        assertEquals(24.0f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(42.0f, b2.pos.x, "b2.pos.x")
        assertEquals(33.0f, b2.pos.y, "b2.pos.y")
        assertEquals(24.0f, b2.pos.z, "b2.pos.z")

        // b1 had no speed
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2 had no speed
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextPos was moved. Since the direction was random, we only test if it has changed.
        assertTrue(b1.nextPos.x != b1.pos.x || b1.nextPos.y != b1.pos.y, "should have moved b1.nextPos")

        // Forced separation along a random direction should never involve z.
        assertEquals(0.0f, b1.nextPos.z - b1.pos.z, "should not involve z with forced separation")

        // b2.nextPos was moved, too.
        assertTrue(b2.nextPos.x != b2.pos.x || b2.nextPos.y != b2.pos.y, "should have moved b2.nextPos")
        assertEquals(0.0f, b2.nextPos.z - b2.pos.z, "should not involve z with forced separation")

        // The new distance should be in the range we'd expect. Since z was not involved, we can just check XY length.
        // Since it was SIDE, the cylinders should now be separated in XY
        val centre1 = Point2f(b1.nextPos.x, b1.nextPos.y)
        val centre2 = Point2f(b2.nextPos.x, b2.nextPos.y)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.000011f, newDistance - (b1.radius + b2.radius), "gap") // less than 11 mm/1000

        // b1.nextSpeed is still zero
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed is still zero
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    companion object {
        private const val TOLERANCE = 0.0000001f
    }
}
