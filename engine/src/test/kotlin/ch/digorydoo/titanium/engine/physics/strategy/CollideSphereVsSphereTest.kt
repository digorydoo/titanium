package ch.digorydoo.titanium.engine.physics.strategy

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_sphere.BounceSphereVsSphere
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_sphere.CheckSphereVsSphere
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_sphere.SeparateSphereVsSphere
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
            friction = 0.5f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = MutablePoint3f(42.6f, 64.2f, 96.57f),
            mass = 9.0f,
            gravity = false,
            radius = 0.2f,
            elasticity = 0.5f,
            friction = 0.72f,
        )

        val chk = CheckSphereVsSphere()
        val sep = SeparateSphereVsSphere()
        val bnc = BounceSphereVsSphere()
        val hit = MutableHitResult()

        // The two spheres should not collide at the original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit))

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
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit))

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

        // The two should no longer collide after separation
        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit))

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
        assertEquals(42.107143f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(64.09992f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(96.49995f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has been moved a little
        assertEquals(42.59202f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(64.20007f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(96.57005f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // Check the gap between the nextPos
        val centre1 = Point3f(b1.nextPos.x, b1.nextPos.y, b1.nextPos.z)
        val centre2 = Point3f(b2.nextPos.x, b2.nextPos.y, b2.nextPos.z)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.00006f, newDistance - (b1.radius + b2.radius), "gap") // less than 6 mm/100

        // nextSpeed should still have the previous values
        assertEquals(0.45000002f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(-0.5f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        bnc.bounce(b1, b2, hit)

        // Now nextSpeed should be the speed after bounce
        assertEquals(-0.10577606f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.021848084f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.015294424f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(0.117529f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.024275647f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.016993802f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
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

        val chk = CheckSphereVsSphere()
        val sep = SeparateSphereVsSphere()
        val bnc = BounceSphereVsSphere()
        val hit = MutableHitResult()

        // The two spheres should not collide at the original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit))

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
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit))

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
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit))

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
        assertEquals(64.20001f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(42.10799f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(96.49999f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has been moved a little
        assertEquals(64.09998f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(42.592903f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(96.57001f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified
        assertEquals(0.10230403f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.08939078f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.071607366f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified
        assertEquals(-0.09093691f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.07945845f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.06365099f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // Check the gap between the nextPos
        val centre1 = Point3f(b1.nextPos.x, b1.nextPos.y, b1.nextPos.z)
        val centre2 = Point3f(b2.nextPos.x, b2.nextPos.y, b2.nextPos.z)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.00006f, newDistance - (b1.radius + b2.radius), "gap") // less than 6 mm/100
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
            friction = 0.5f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = MutablePoint3f(64.1f, 42.2f, 96.4f),
            mass = 9.0f,
            gravity = false,
            radius = 0.2f,
            elasticity = 0.5f,
            friction = 0.72f,
        )

        val chk = CheckSphereVsSphere()
        val sep = SeparateSphereVsSphere()
        val bnc = BounceSphereVsSphere()
        val hit = MutableHitResult()

        // The two spheres should not collide at the original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "initial pos should not collide")

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
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide after adding forces")

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
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit))

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
        assertEquals(64.20192f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(42.09808f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(96.878426f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has been moved a little
        assertEquals(64.098076f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(42.20192f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(96.40042f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified
        assertEquals(0.031016849f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.031015709f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.14277448f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified
        assertEquals(-0.027570538f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.027569525f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(-0.1269107f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // Check the distance between the nextPos
        val centre1 = Point3f(b1.nextPos.x, b1.nextPos.y, b1.nextPos.z)
        val centre2 = Point3f(b2.nextPos.x, b2.nextPos.y, b2.nextPos.z)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.00006f, newDistance - (b1.radius + b2.radius), "gap") // less than 6 mm/100
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

        val chk = CheckSphereVsSphere()
        val sep = SeparateSphereVsSphere()
        val bnc = BounceSphereVsSphere()
        val hit = MutableHitResult()

        // The two spheres do not collide at their original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "initial pos should not collide")

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
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide after adding forces")

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
        assertEquals(3.0021906f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has moved a little in the direction of its speed
        assertEquals(5.965833f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(5.1334257f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(7.4530554f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // The two should no longer collide after bounce
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit))

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
        assertEquals(5.0024614f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(7.1978097f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(3.0021641f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has been moved a little
        assertEquals(5.965839f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(5.1334133f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(7.453082f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified
        assertEquals(0.07995011f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.011336647f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.1752753f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified
        assertEquals(-1.0970794f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(-0.0031573176f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(2.6771874f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // Check the gap between the nextPos
        val centre1 = Point3f(b1.nextPos.x, b1.nextPos.y, b1.nextPos.z)
        val centre2 = Point3f(b2.nextPos.x, b2.nextPos.y, b2.nextPos.z)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.00006f, newDistance - (b1.radius + b2.radius), "gap") // less than 6 mm/100
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

        val chk = CheckSphereVsSphere()
        val sep = SeparateSphereVsSphere()
        val bnc = BounceSphereVsSphere()
        val hit = MutableHitResult()

        // Apply forces to end anim phase 1 and update nextPos and nextSpeed
        b1.applyForces()
        b2.applyForces()

        // The two spheres should obviously collide
        Log.enabled = false // suppress expected log message
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "initial pos should collide")
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
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        Log.enabled = true

        assertFalse(
            chk.check(b1, b1.nextPos, b2, b2.nextPos, hit),
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
        assertWithin(0.0f .. 0.00006f, dist - rsum, "new distance") // less than 6 mm/100

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
            friction = 0.5f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = MutablePoint3f(10.2f, 9.8f, 10.1f),
            mass = 7.0f,
            gravity = false,
            radius = 0.5f,
            elasticity = 0.8f,
            friction = 0.72f,
        )

        b1.applyForces()
        b2.applyForces()

        val chk = CheckSphereVsSphere()
        val sep = SeparateSphereVsSphere()
        val bnc = BounceSphereVsSphere()
        val hit = MutableHitResult()

        // They should collide at their original position
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)

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
        assertEquals(9.76665f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.23335f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.883325f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has moved a little
        assertEquals(10.43335f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(9.56665f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.216676f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // The new distance should be in the range we'd expect
        val dx = b1.nextPos.x - b2.nextPos.x
        val dy = b1.nextPos.y - b2.nextPos.y
        val dz = b1.nextPos.z - b2.nextPos.z
        val dist = sqrt(dx * dx + dy * dy + dz * dz)
        val rsum = b1.radius + b2.radius
        assertWithin(0.0f .. 0.00006f, dist - rsum, "new distance") // less than 6 mm/100

        // They should no longer collide now
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
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
            friction = 0.5f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = MutablePoint3f(10.2f, 9.8f, 10.1f),
            mass = 7.0f,
            gravity = false,
            radius = 0.5f,
            elasticity = 0.8f,
            friction = 0.72f,
        )

        b1.applyForces()
        b2.applyForces()

        val chk = CheckSphereVsSphere()
        val sep = SeparateSphereVsSphere()
        val bnc = BounceSphereVsSphere()
        val hit = MutableHitResult()

        // They should collide at their original position
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)

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
        assertEquals(10.666699f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(9.333301f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.333351f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // The new distance should be in the range we'd expect
        val dx = b1.nextPos.x - b2.nextPos.x
        val dy = b1.nextPos.y - b2.nextPos.y
        val dz = b1.nextPos.z - b2.nextPos.z
        val dist = sqrt(dx * dx + dy * dy + dz * dz)
        val rsum = b1.radius + b2.radius
        assertWithin(0.0f .. 0.00006f, dist - rsum, "new distance") // less than 6 mm/100

        // They should no longer collide now
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
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
            friction = 0.5f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = MutablePoint3f(10.2f, 9.8f, 10.1f),
            mass = LARGE_MASS,
            gravity = false,
            radius = 0.5f,
            elasticity = 0.8f,
            friction = 0.72f,
        )

        b1.applyForces()
        b2.applyForces()

        val chk = CheckSphereVsSphere()
        val sep = SeparateSphereVsSphere()
        val bnc = BounceSphereVsSphere()
        val hit = MutableHitResult()

        // They should collide at their original position
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)

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
        assertEquals(9.5333f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.4667f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.766649f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

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
        assertWithin(0.0f .. 0.00006f, dist - rsum, "new distance") // less than 6 mm/100

        // They should no longer collide now
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should properly handle collisions in situation 1`() {
        val b1 = FixedSphereBody(
            "b1",
            initialPos = Point3f.zero,
            mass = 0.52f,
            gravity = false,
            radius = 0.25f,
            elasticity = 0.96f,
            friction = 0.001f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = Point3f.zero,
            mass = 0.52f,
            gravity = false,
            radius = 0.25f,
            elasticity = 0.96f,
            friction = 0.001f,
        )

        val chk = CheckSphereVsSphere()
        val sep = SeparateSphereVsSphere()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(160.0f, 297.0f, 1.387f)
            speed.set(0.0f, 0.0f, -1.2486165f)
            applyForces()
        }

        b2.apply {
            pos.set(159.8931f, 297.49f, 1.385f)
            speed.set(-0.89576447f, -4.2357774f, -1.1003304f)
            applyForces()
        }

        assertFalse(chk.check(b1, b1.pos, b2, b2.pos, hit), "should not collide initially")
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should properly handle collisions in situation 2`() {
        val b1 = FixedSphereBody(
            "b1",
            initialPos = Point3f.zero,
            mass = 0.52f,
            gravity = false,
            radius = 0.25f,
            elasticity = 0.96f,
            friction = 0.001f,
        )
        val b2 = FixedSphereBody(
            "b2",
            initialPos = Point3f.zero,
            mass = 0.42f,
            gravity = false,
            radius = 0.33f,
            elasticity = 0.96f,
            friction = 0.001f,
        )

        val chk = CheckSphereVsSphere()
        val sep = SeparateSphereVsSphere()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(158.93892f, 289.37955f, 1.25003f)
            speed.set(0.3419082f, -3.8005176f, -0.16350001f)
            applyForces()
        }

        b2.apply {
            pos.set(158.60207f, 288.8678f, 1.3300301f)
            speed.set(0.0f, 0.0f, -0.16350001f)
            applyForces()
        }

        assertFalse(chk.check(b1, b1.pos, b2, b2.pos, hit), "should not collide initially")
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
    }

    companion object {
        private const val TOLERANCE = 0.000001f
    }
}
