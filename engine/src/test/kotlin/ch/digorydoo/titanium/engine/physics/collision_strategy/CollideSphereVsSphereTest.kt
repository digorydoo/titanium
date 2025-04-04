package ch.digorydoo.titanium.engine.physics.collision_strategy

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.utils.assertGreaterThan
import ch.digorydoo.titanium.engine.utils.assertLessThan
import ch.digorydoo.titanium.engine.utils.assertWithin
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CollideSphereVsSphereTest {
    @Test
    fun `should collide when the two spheres get too close and the speed is parallel to the x-axis`() {
        val b1 = FixedSphereBody(
            "b1",
            initialPos = MutablePoint3f(42.1f, 64.1f, 96.5f),
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = MutablePoint3f(42.6f, 64.2f, 96.57f),
            mass = 9.0f,
            gravity = false,
            radius = 0.2f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val ck = CollideSphereVsSphere()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit)

        // The two spheres should not collide at the original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(check())

        // Apply a force to each towards the other
        b1.apply {
            addForce(270.0f, 0.0f, 0.0f)
            applyForces()
        }
        b2.apply {
            addForce(-270.0f, 0.0f, 0.0f)
            applyForces()
        }

        // The two spheres should now collide
        assertTrue(check())

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific hit areas
        assertEquals(HitArea.UNSPECIFIED, hit.area2, "hit.area2")

        assertEquals(42.398396f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(64.16008f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(96.54206f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.9696586f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.20027128f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.14019142f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // The hitNormal12 should be properly normalised
        val n = hit.hitNormal12
        val len = sqrt(n.x * n.x + n.y * n.y + n.z * n.z)
        assertLessThan(len, 1.01f, "hitNormal12")
        assertGreaterThan(len, 0.99f, "hitNormal12")

        // b1.nextSpeed points in the positive direction of x
        assertEquals(0.45000002f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed points in the negative direction of x
        assertEquals(-0.5f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1.nextPos has moved a little in the direction of its speed
        assertEquals(42.1075f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(64.1f, b1.nextPos.y, "b1.nextPos.y") // the same as before
        assertEquals(96.5f, b1.nextPos.z, "b1.nextPos.z") // the same as before

        // b2.nextPos has moved a little in the direction of its speed
        assertEquals(42.591663f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(64.2f, b2.nextPos.y, "b2.nextPos.y") // the same as before
        assertEquals(96.57f, b2.nextPos.z, "b2.nextPos.z") // the same as before

        // The two should no longer collide after bounce
        bounce()
        assertFalse(check())

        // b1.pos is unchanged
        assertEquals(42.1f, b1.pos.x, "b1.pos.x")
        assertEquals(64.1f, b1.pos.y, "b1.pos.y")
        assertEquals(96.5f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(42.6f, b2.pos.x, "b2.pos.x")
        assertEquals(64.2f, b2.pos.y, "b2.pos.y")
        assertEquals(96.57f, b2.pos.z, "b2.pos.z")

        // b1 had no speed before we added a force
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2 had no speed before we added a force
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextPos has been moved a little
        assertEquals(42.10718f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(64.09993f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(96.499954f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has been moved a little
        assertEquals(42.592014f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(64.20007f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(96.57005f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified
        assertEquals(-0.10577601f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.021848304f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.015293476f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified
        assertEquals(0.117528886f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.024275893f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.016992752f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // Check the gap between the nextPos
        val centre1 = Point3f(b1.nextPos.x, b1.nextPos.y, b1.nextPos.z)
        val centre2 = Point3f(b2.nextPos.x, b2.nextPos.y, b2.nextPos.z)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.000006f, newDistance - (b1.radius + b2.radius), "gap") // less than 6 mm/1000
    }

    @Test
    fun `should collide when the two spheres get too close and the speed is parallel to the y-axis`() {
        val b1 = FixedSphereBody(
            "b1",
            initialPos = MutablePoint3f(64.2f, 42.1f, 96.5f),
            mass = 8.0f,
            gravity = false,
            radius = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = MutablePoint3f(64.1f, 42.6f, 96.57f),
            mass = 9.0f,
            gravity = false,
            radius = 0.2f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val ck = CollideSphereVsSphere()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit)

        // The two spheres should not collide at the original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(check())

        // Apply a force to each towards the other
        b1.apply {
            addForce(0.0f, 232.0f, 0.0f)
            applyForces()
        }
        b2.apply {
            addForce(0.0f, -232.0f, 0.0f)
            applyForces()
        }

        // The two spheres should now collide
        assertTrue(check())

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific hit areas
        assertEquals(HitArea.UNSPECIFIED, hit.area2, "hit.area2")

        assertEquals(64.139984f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(42.398975f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(96.54201f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(-0.2000312f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.9697324f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.14002337f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // b1.nextSpeed points in the positive direction of y
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.48333335f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed points in the negative direction of y
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(-0.42962965f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1.nextPos has moved a little in the direction of its speed
        assertEquals(64.2f, b1.nextPos.x, "b1.nextPos.x") // the same as before
        assertEquals(42.108055f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(96.5f, b1.nextPos.z, "b1.nextPos.z") // the same as before

        // b2.nextPos has moved a little in the direction of its speed
        assertEquals(64.1f, b2.nextPos.x, "b2.nextPos.x") // the same as before
        assertEquals(42.59284f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(96.57f, b2.nextPos.z, "b2.nextPos.z") // the same as before

        // The two should no longer collide after bounce
        bounce()
        assertFalse(check())

        // b1.pos is unchanged
        assertEquals(64.2f, b1.pos.x, "b1.pos.x")
        assertEquals(42.1f, b1.pos.y, "b1.pos.y")
        assertEquals(96.5f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(64.1f, b2.pos.x, "b2.pos.x")
        assertEquals(42.6f, b2.pos.y, "b2.pos.y")
        assertEquals(96.57f, b2.pos.z, "b2.pos.z")

        // b1 had no speed before we added a force
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2 had no speed before we added a force
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextPos has been moved a little
        assertEquals(64.200005f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(42.10801f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(96.49999f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has been moved a little
        assertEquals(64.09999f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(42.59288f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(96.57001f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified
        assertEquals(0.023438208f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.11362947f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.016408f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified
        assertEquals(-0.020833962f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.10100394f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.01458489f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // Check the gap between the nextPos
        val centre1 = Point3f(b1.nextPos.x, b1.nextPos.y, b1.nextPos.z)
        val centre2 = Point3f(b2.nextPos.x, b2.nextPos.y, b2.nextPos.z)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.000006f, newDistance - (b1.radius + b2.radius), "gap") // less than 6 mm/1000
    }

    @Test
    fun `should collide when the two spheres get too close and the speed is parallel to the z-axis`() {
        val b1 = FixedSphereBody(
            "b1",
            initialPos = MutablePoint3f(64.2f, 42.1f, 96.88f),
            mass = 8.0f,
            gravity = false,
            radius = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = MutablePoint3f(64.1f, 42.2f, 96.4f),
            mass = 9.0f,
            gravity = false,
            radius = 0.2f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val ck = CollideSphereVsSphere()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit)

        // The two spheres should not collide at the original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(check(), "initial pos should not collide")

        // Apply a force to each towards the other
        b1.apply {
            addForce(0.0f, 0.0f, -300.0f)
            applyForces()
        }
        b2.apply {
            addForce(0.0f, 0.0f, 300.0f)
            applyForces()
        }

        // The two spheres should now collide
        assertTrue(check(), "should collide after adding forces")

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific hit areas
        assertEquals(HitArea.UNSPECIFIED, hit.area2, "hit.area2")

        assertEquals(64.1377f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(42.1623f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(96.58281f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(-0.20765787f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.2076658f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-0.9559043f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // b1.nextSpeed points in the positive direction of y
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.62500006f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed points in the negative direction of y
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.5555556f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1.nextPos has moved a little in the direction of its speed
        assertEquals(64.2f, b1.nextPos.x, "b1.nextPos.x") // the same as before
        assertEquals(42.1f, b1.nextPos.y, "b1.nextPos.y") // the same as before
        assertEquals(96.86958f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has moved a little in the direction of its speed
        assertEquals(64.1f, b2.nextPos.x, "b2.nextPos.x") // the same as before
        assertEquals(42.2f, b2.nextPos.y, "b2.nextPos.y") // the same as before
        assertEquals(96.40926f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // The two should no longer collide after bounce
        bounce()
        assertFalse(check())

        // b1.pos is unchanged
        assertEquals(64.2f, b1.pos.x, "b1.pos.x")
        assertEquals(42.1f, b1.pos.y, "b1.pos.y")
        assertEquals(96.88f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(64.1f, b2.pos.x, "b2.pos.x")
        assertEquals(42.2f, b2.pos.y, "b2.pos.y")
        assertEquals(96.4f, b2.pos.z, "b2.pos.z")

        // b1 had no speed before we added a force
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2 had no speed before we added a force
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextPos has been moved a little
        assertEquals(64.20203f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(42.09797f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(96.87892f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has been moved a little
        assertEquals(64.0982f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(42.201805f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(96.40096f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified
        assertEquals(0.031015258f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.031017529f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.14277408f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified
        assertEquals(-0.027569119f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.027571138f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(-0.12691034f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // Check the distance between the nextPos
        val centre1 = Point3f(b1.nextPos.x, b1.nextPos.y, b1.nextPos.z)
        val centre2 = Point3f(b2.nextPos.x, b2.nextPos.y, b2.nextPos.z)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.00001f, newDistance - (b1.radius + b2.radius), "gap") // less than 1 mm/100
    }

    @Test
    fun `should collide when the two spheres get to close and the speed is arbitrary`() {
        val b1 = FixedSphereBody(
            "b1",
            initialPos = MutablePoint3f(5.0f, 7.2f, 3.0f),
            mass = 42.0f,
            gravity = false,
            radius = 3.0f,
            elasticity = 0.9f,
            friction = 0.1f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = MutablePoint3f(6.0f, 5.1f, 7.48f),
            mass = 3.0f, // lighter in weight
            gravity = false,
            radius = 2.0f, // smaller
            elasticity = 0.9f,
            friction = 0.1f,
        )

        val ck = CollideSphereVsSphere()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit)

        // The two spheres do not collide at their original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(check(), "initial pos should not collide")

        // Apply a force to each towards the other
        b1.apply {
            addForce(373.0f, -333.0f, 331.2f)
            applyForces()
        }
        b2.apply {
            addForce(-369.0f, 361.0f, -291.0f)
            applyForces()
        }

        // The two spheres should now collide
        assertTrue(check(), "should collide after adding forces")

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific hit areas
        assertEquals(HitArea.UNSPECIFIED, hit.area2, "hit.area2")

        assertEquals(5.580488f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(5.959172f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(5.6727147f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.1926736f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-0.41287515f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.89017475f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // b1.nextSpeed points in the direction of the force
        assertEquals(0.14801589f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.13214286f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.13142858f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed points in the direction of the force; speed is greater than b1's, because b2 has less mass
        assertEquals(-2.0500002f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(2.0055556f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(-1.6166668f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1.nextPos has moved a little in the direction of its speed
        assertEquals(5.002467f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(7.1977973f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(3.0021904765f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has moved a little in the direction of its speed
        assertEquals(5.965833f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(5.1334257f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(7.4530554f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // The two should no longer collide after bounce
        bounce()
        assertFalse(check())

        // b1.pos is unchanged
        assertEquals(5.0f, b1.pos.x, "b1.pos.x")
        assertEquals(7.2f, b1.pos.y, "b1.pos.y")
        assertEquals(3.0f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(6.0f, b2.pos.x, "b2.pos.x")
        assertEquals(5.1f, b2.pos.y, "b2.pos.y")
        assertEquals(7.48f, b2.pos.z, "b2.pos.z")

        // b1 had no speed before we added a force
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2 had no speed before we added a force
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextPos has been moved a little
        assertEquals(5.002467f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(7.197798f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(3.0021896f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has been moved a little
        assertEquals(5.965836f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(5.1334195f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(7.4530683f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified
        assertEquals(0.05256485f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.027236171f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.16197348f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified
        assertEquals(-0.71368563f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(-0.22575086f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(2.4909616f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // Check the gap between the nextPos
        val centre1 = Point3f(b1.nextPos.x, b1.nextPos.y, b1.nextPos.z)
        val centre2 = Point3f(b2.nextPos.x, b2.nextPos.y, b2.nextPos.z)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.000006f, newDistance - (b1.radius + b2.radius), "gap") // less than 6 mm/1000
    }

    @Test
    fun `should behave graciously if two identical spheres are spawned in the exact same spot`() {
        val pos = MutablePoint3f(42.0f, 33.0f, 24.0f)
        val b1 = FixedSphereBody(
            "b1",
            initialPos = pos,
            mass = 42.0f,
            gravity = false,
            radius = 0.25f,
            elasticity = 0.5f,
            friction = 0.5f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = pos,
            mass = 42.0f,
            gravity = false,
            radius = 0.25f,
            elasticity = 0.5f,
            friction = 0.5f,
        )

        val ck = CollideSphereVsSphere()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit)

        // Apply forces to end anim phase 1 and update nextPos and nextSpeed
        b1.applyForces()
        b2.applyForces()

        // The two spheres should obviously collide
        Log.enabled = false // suppress expected log message
        assertTrue(check(), "initial pos should collide")
        Log.enabled = true

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific hit areas
        assertEquals(HitArea.UNSPECIFIED, hit.area2, "hit.area2")

        // The hit point is set to the centre of the two spheres
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

        // Bounce will force the two spheres apart such that they no longer collide
        Log.enabled = false // suppress expected log message
        bounce()
        Log.enabled = true

        assertFalse(
            check(),
            "should no longer collide, but they do: b1.nextPos=${b1.nextPos}, b2.nextPos=${b2.nextPos}"
        )

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
        val dx = b1.nextPos.x - b2.nextPos.x
        val dy = b1.nextPos.y - b2.nextPos.y
        val dist = sqrt(dx * dx + dy * dy)
        val rsum = b1.radius + b2.radius
        assertWithin(0.0f .. 0.00002f, dist - rsum, "new distance") // less than 2 mm/100

        // b1.nextSpeed is still zero
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed is still zero
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should force apart two spheres colliding at their initial position even though positions differ`() {
        val b1 = FixedSphereBody(
            "b1",
            initialPos = MutablePoint3f(10.0f, 10.0f, 10.0f),
            mass = 9.0f,
            gravity = false,
            radius = 0.5f,
            elasticity = 0.8f,
            friction = 0.2f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = MutablePoint3f(10.2f, 9.8f, 10.1f),
            mass = 7.0f,
            gravity = false,
            radius = 0.5f,
            elasticity = 0.8f,
            friction = 0.2f,
        )

        b1.applyForces()
        b2.applyForces()

        val ck = CollideSphereVsSphere()
        val hit = MutableHitResult()

        // They should collide at their original position
        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit), "should collide")

        // Since b1.pos and b2.pos are farther away than just EPSILON, forcing the bodies apart should not have taken
        // a random direction, which means that the result should be stable.

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, "b1.pos.y")
        assertEquals(10.0f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.2f, b2.pos.x, "b2.pos.x")
        assertEquals(9.8f, b2.pos.y, "b2.pos.y")
        assertEquals(10.1f, b2.pos.z, "b2.pos.z")

        // b1.speed is still zero
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2.speed is still zero
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextSpeed is still zero
        assertEquals(0.0f, b1.nextSpeed.x, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, "b1.nextSpeed.z")

        // b2.nextSpeed is still zero
        assertEquals(0.0f, b2.nextSpeed.x, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, "b2.nextSpeed.z")

        // b1.nextPos has moved a little
        assertEquals(9.795832f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.204168f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.897915f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has moved a little
        assertEquals(10.462502f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(9.537498f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.231252f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // The new distance should be in the range we'd expect
        val dx = b1.nextPos.x - b2.nextPos.x
        val dy = b1.nextPos.y - b2.nextPos.y
        val dz = b1.nextPos.z - b2.nextPos.z
        val dist = sqrt(dx * dx + dy * dy + dz * dz)
        val rsum = b1.radius + b2.radius
        assertWithin(0.0f .. 0.000006f, dist - rsum, "new distance") // less than 6 mm/1000

        // They should no longer collide now
        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should no longer collide")
    }

    @Test
    fun `should force apart two spheres colliding at their initial pos when the first one is a LARGE_MASS`() {
        val b1 = FixedSphereBody(
            "b1",
            initialPos = MutablePoint3f(10.0f, 10.0f, 10.0f),
            mass = LARGE_MASS,
            gravity = false,
            radius = 0.5f,
            elasticity = 0.8f,
            friction = 0.2f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = MutablePoint3f(10.2f, 9.8f, 10.1f),
            mass = 7.0f,
            gravity = false,
            radius = 0.5f,
            elasticity = 0.8f,
            friction = 0.2f,
        )

        b1.applyForces()
        b2.applyForces()

        val ck = CollideSphereVsSphere()
        val hit = MutableHitResult()

        // They should collide at their original position
        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit), "should collide")

        // Since b1.pos and b2.pos are farther away than just EPSILON, forcing the bodies apart should not have taken
        // a random direction, which means that the result should be stable.

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, "b1.pos.y")
        assertEquals(10.0f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.2f, b2.pos.x, "b2.pos.x")
        assertEquals(9.8f, b2.pos.y, "b2.pos.y")
        assertEquals(10.1f, b2.pos.z, "b2.pos.z")

        // b1.speed is still zero
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2.speed is still zero
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextSpeed is still zero
        assertEquals(0.0f, b1.nextSpeed.x, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, "b1.nextSpeed.z")

        // b2.nextSpeed is still zero
        assertEquals(0.0f, b2.nextSpeed.x, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, "b2.nextSpeed.z")

        // b1.nextPos is unchanged, because b1 is a LARGE_MASS
        assertEquals(10.0f, b1.nextPos.x, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, "b1.nextPos.z")

        // b2.pos has been moved
        assertEquals(10.66667f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(9.33333f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.333337f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // The new distance should be in the range we'd expect
        val dx = b1.nextPos.x - b2.nextPos.x
        val dy = b1.nextPos.y - b2.nextPos.y
        val dz = b1.nextPos.z - b2.nextPos.z
        val dist = sqrt(dx * dx + dy * dy + dz * dz)
        val rsum = b1.radius + b2.radius
        assertWithin(0.0f .. 0.000006f, dist - rsum, "new distance") // less than 6 mm/1000

        // They should no longer collide now
        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should no longer collide")
    }

    @Test
    fun `should force apart two spheres colliding at their initial pos when the second one is a LARGE_MASS`() {
        val b1 = FixedSphereBody(
            "b1",
            initialPos = MutablePoint3f(10.0f, 10.0f, 10.0f),
            mass = 9.0f,
            gravity = false,
            radius = 0.5f,
            elasticity = 0.8f,
            friction = 0.2f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = MutablePoint3f(10.2f, 9.8f, 10.1f),
            mass = LARGE_MASS,
            gravity = false,
            radius = 0.5f,
            elasticity = 0.8f,
            friction = 0.2f,
        )

        b1.applyForces()
        b2.applyForces()

        val ck = CollideSphereVsSphere()
        val hit = MutableHitResult()

        // They should collide at their original position
        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit), "should collide")

        // Since b1.pos and b2.pos are farther away than just EPSILON, forcing the bodies apart should not have taken
        // a random direction, which means that the result should be stable.

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, "b1.pos.y")
        assertEquals(10.0f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.2f, b2.pos.x, "b2.pos.x")
        assertEquals(9.8f, b2.pos.y, "b2.pos.y")
        assertEquals(10.1f, b2.pos.z, "b2.pos.z")

        // b1.speed is still zero
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2.speed is still zero
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextSpeed is still zero
        assertEquals(0.0f, b1.nextSpeed.x, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, "b1.nextSpeed.z")

        // b2.nextSpeed is still zero
        assertEquals(0.0f, b2.nextSpeed.x, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, "b2.nextSpeed.z")

        // b1.nextPos has been moved
        assertEquals(9.53333f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.46667f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.766664f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.pos is unchanged, because b2 is a LARGE_MASS
        assertEquals(10.2f, b2.nextPos.x, "b2.nextPos.x")
        assertEquals(9.8f, b2.nextPos.y, "b2.nextPos.y")
        assertEquals(10.1f, b2.nextPos.z, "b2.nextPos.z")

        // The new distance should be in the range we'd expect
        val dx = b1.nextPos.x - b2.nextPos.x
        val dy = b1.nextPos.y - b2.nextPos.y
        val dz = b1.nextPos.z - b2.nextPos.z
        val dist = sqrt(dx * dx + dy * dy + dz * dz)
        val rsum = b1.radius + b2.radius
        assertWithin(0.0f .. 0.000006f, dist - rsum, "new distance") // less than 6 mm/1000

        // They should no longer collide now
        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should no longer collide")
    }

    @Test
    fun `should correctly handle a secondary collision appearing with three spheres in a row`() {
        val b1 = FixedSphereBody(
            "b1",
            initialPos = MutablePoint3f(1.0f, 0.0f, 0.25f),
            mass = LARGE_MASS,
            gravity = false,
            radius = 0.25f,
            elasticity = 1.0f,
            friction = 0.0f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = MutablePoint3f(1.5000001f, 0.0f, 0.25f),
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            elasticity = 1.0f,
            friction = 0.0f,
        )
        val b3 = FixedSphereBody(
            "b3",
            initialPos = MutablePoint3f(2.0000002f, 0.0f, 0.25f),
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            elasticity = 1.0f,
            friction = 0.0f,
        )

        val ck = CollideSphereVsSphere()
        val hit = MutableHitResult()

        fun check12() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit)
        fun check21() = ck.checkAndBounceIfNeeded(b2, b1, canBounce = false, hit)
        fun check13() = ck.checkAndBounceIfNeeded(b1, b3, canBounce = false, hit)
        fun check23() = ck.checkAndBounceIfNeeded(b2, b3, canBounce = false, hit)

        fun bounce12() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = true, hit)
        fun bounce23() = ck.checkAndBounceIfNeeded(b2, b3, canBounce = true, hit)

        b1.applyForces()
        b2.applyForces()
        b3.applyForces()

        // None of them should collide at their original position
        assertFalse(check12(), "initially, b1 and b2 should not collide")
        assertFalse(check23(), "initially, b2 and b3 should not collide")
        assertFalse(check13(), "initially, b1 and b3 should not collide")

        // Give b2 and b3 a push towards b1
        b2.addForce(-50.0f, 0.0f, 0.0f)
        b3.addForce(-50.0f, 0.0f, 0.0f)

        // At the end of anim phase 1, forces are applied and nextSpeed and nextPos are updated
        b1.applyForces()
        b2.applyForces()
        b3.applyForces()

        // b1 should still not move
        assertEquals(0.0f, b1.nextSpeed.x, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, "b1.nextSpeed.z")

        // b2 should have a slight speed in the negative direction of x
        assertEquals(-0.083333336f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, "b2.nextSpeed.z")

        // b3 should have got the exact same speed since the masses of b2 and b3 are the same
        assertEquals(b2.nextSpeed.x, b3.nextSpeed.x, "b3.nextSpeed.x")
        assertEquals(b2.nextSpeed.y, b3.nextSpeed.y, "b3.nextSpeed.y")
        assertEquals(b2.nextSpeed.z, b3.nextSpeed.z, "b3.nextSpeed.z")

        // b2 and b3 should not collide at this point, because their speeds are the same.
        assertFalse(check23(), "b2 and b3 should not collide even after adding forces")

        // Assuming the order within the GelLayer is b1, b2, b3, then b1 will check and bounce against b2 first.
        // b2 runs into b1, and since b1 is a LARGE_MASS, b2 will move all the way required for separating them.
        assertTrue(check12(), "b1 and b2 should collide after adding forces")
        bounce12()

        // b1 should still not move
        assertEquals(0.0f, b1.nextSpeed.x, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, "b1.nextSpeed.z")

        // b2's nextSpeed was completely reversed, since bodies are 100% elastic with no friction involved
        assertEquals(0.083333336f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, "b2.nextSpeed.z")

        // Next, b1 is checked against b3. They are still quite far from each-other.
        assertFalse(check13(), "b1 and b3 should not collide even after adding forces")

        // b1's check for primary collisions is finished. Since there was a collision, CollisionManager checks it
        // again against all gels in the vicinity, which we simulate here. Only b2 is in the vicinity of b1. The two
        // should not have any secondary collision, since we've just bounced them.
        assertFalse(check12(), "b1 and b2 should not have a secondary collision")

        // Next, it's b2's turn to check. It checks only forward, so it only checks against b3. We've already checked
        // them, and they did not collide; but in the meantime, b2 has been pushed back, which causes a new collision.
        // Note that this is not called a secondary collision, because b3 is at a larger index than b2.
        assertTrue(check23(), "b2 and b3 should collide after b2 was pushed back")
        bounce23()

        // Since this is a perfectly elastic collision, b2 and b3 should swap velocities. b2 looks towards b1 again:
        assertEquals(-0.083333336f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, "b2.nextSpeed.z")

        // b3's speed now points away from the group
        assertEquals(0.083333336f, b3.nextSpeed.x, TOLERANCE, "b3.nextSpeed.x")
        assertEquals(0.0f, b3.nextSpeed.y, "b3.nextSpeed.y")
        assertEquals(0.0f, b3.nextSpeed.z, "b3.nextSpeed.z")

        // b2's check for primary collisions is finished. There was a collision, so we need to check for secondary
        // collisions. Both b1 and b3 are in the vicinity of b2. Since bouncing b2 and b3 should have moved both bodies,
        // the separation delta for b2 is half the separation distance, which has a good likelihood not to lead to a
        // secondary collision with b1 (and in fact will not in our test case). On the other hand, b2 and b3 have
        // just been bounced and should certainly not collide.
        // FIXME assertFalse(check21(), "b2 should not have been pushed back into b1")
        assertFalse(check23(), "b2 should not collide with b3 after bounce")

        // At this point, none of the bodies collide. b1 should still not move, since it's a LARGE_MASS.
        assertEquals(0.0f, b1.nextSpeed.x, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, "b1.nextSpeed.z")

        // Since no further bounce was involved, b2 and b3 still have the speed as we last saw them.
        // This is the end of the frame, so gels are moved.
        b1.move()
        b2.move()
        b3.move()

        // There is no force acting on the bodies, so anim phase 1 ends just like this.
        b1.applyForces()
        b2.applyForces()
        b3.applyForces()

        // In collision phase, b1 first checks against b2. b2's speed pointed towards b1 and causes a collision.
        assertTrue(check12(), "b1 and b2 should collide at start of new frame")
        bounce12()

        // b1 should still not move
        assertEquals(0.0f, b1.nextSpeed.x, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, "b1.nextSpeed.z")

        // b2's nextSpeed is completely reversed again
        assertEquals(0.083333336f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, "b2.nextSpeed.z")

        // Next, b1 is checked against b3. They are still quite far from each-other.
        assertFalse(check13(), "b1 and b3 should not collide at start of new frame")

        // b1's check for primary collisions is finished, and check for secondary collisions happen. Only b2 is in the
        // vicinity of b1, and they can't have a secondary collision, since we've just bounced them.
        assertFalse(check12(), "b1 and b2 should not have a secondary collision in new frame")

        // Next, it's b2's turn to check, which checks against b3. Since b3 is moving away from the group, the small
        // separation push from b1 did no longer cause a new collision.
        assertFalse(check23(), "b2 and b3 should collide after b2 was pushed back")

        // b2's check for primary collisions is finished, and there was no collision. This finishes the new frame.
        // b1 is still not in motion.
        assertEquals(0.0f, b1.nextSpeed.x, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, "b1.nextSpeed.z")

        // b2 now looks away from b1
        assertEquals(0.083333336f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, "b2.nextSpeed.z")

        // b3 is just ahead of b2 with the same speed
        assertEquals(0.083333336f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, "b2.nextSpeed.z")
    }

    companion object {
        private const val TOLERANCE = 0.0000001f
    }
}
