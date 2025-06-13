package ch.digorydoo.titanium.engine.physics.strategy

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_capsule.BounceCylinderVsCapsule
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_capsule.CheckCylinderVsCapsule
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_capsule.SeparateCylinderVsCapsule
import ch.digorydoo.titanium.engine.utils.assertWithin
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CollideCylinderVsCapsuleTest {
    @Test
    fun `should collide when the two bodies get too close and the speed is parallel to the x-axis`() {
        val b1 = FixedCylinderBody(
            "b1",
            initialPos = MutablePoint3f(10.0f, 7.2f, 10.96f),
            mass = 13.0f,
            gravity = false,
            radius = 0.3f,
            height = 1.0f,
            elasticity = 0.5f,
            friction = 0.5f,
        )
        val b2 = FixedCapsuleBody(
            "b2",
            initialPos = MutablePoint3f(10.5f, 7.1f, 10.45f),
            mass = 11.0f,
            gravity = false,
            radius = 0.2f,
            height = 0.5f,
            elasticity = 0.5f,
            friction = 0.72f,
        )

        val chk = CheckCylinderVsCapsule()
        val sep = SeparateCylinderVsCapsule()
        val bnc = BounceCylinderVsCapsule()
        val hit = MutableHitResult()

        // The two bodies should not collide at their original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        // Apply a force to each towards the other
        b1.apply {
            addForce(220.0f, 0.0f, 0.0f)
            applyForces()
        }
        b2.apply {
            addForce(-220.0f, 0.0f, 0.0f)
            applyForces()
        }

        // The two bodies should now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2")

        assertEquals(10.298547f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.14f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.58f, hit.hitPt.z, TOLERANCE, "hitPt.z")

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
        assertEquals(10.96f, b1.nextPos.z, "b1.nextPos.z") // the same as before

        // b2.nextPos has moved a little in the direction of its speed
        assertEquals(10.494445f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(7.1f, b2.nextPos.y, "b2.nextPos.y") // the same as before
        assertEquals(10.45f, b2.nextPos.z, "b2.nextPos.z") // the same as before

        // The two should no longer collide after separation
        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit))

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, "b1.pos.x")
        assertEquals(7.2f, b1.pos.y, "b1.pos.y")
        assertEquals(10.96f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.5f, b2.pos.x, "b2.pos.x")
        assertEquals(7.1f, b2.pos.y, "b2.pos.y")
        assertEquals(10.45f, b2.pos.z, "b2.pos.z")

        // b1.nextPos has been moved a little
        assertEquals(10.004602f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(7.20002f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // has slightly moved due to ydiff of centres
        assertEquals(10.96f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has been moved a little
        assertEquals(10.494543f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(7.09998f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y") // has slightly moved due to ydiff of centres
        assertEquals(10.45f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // Since it was SIDE, the bodies should now be separated in XY
        val centre1 = Point2f(b1.nextPos.x, b1.nextPos.y)
        val centre2 = Point2f(b2.nextPos.x, b2.nextPos.y)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.00006f, newDistance - (b1.radius + b2.radius), "gap") // less than 6 mm/100

        // nextSpeed should still be the previous value
        assertEquals(0.2820513f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(-0.33333334f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        bnc.bounce(b1, b2, hit)

        // b1 had no speed before we added a force
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2 had no speed before we added a force
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextSpeed has been modified
        assertEquals(-0.06769069f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.013821525f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified
        assertEquals(0.07999805f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(-0.01633464f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should collide when the two bodies get too close and the speed is parallel to the y-axis`() {
        val b1 = FixedCylinderBody(
            "b1",
            initialPos = MutablePoint3f(8.2f, 7.5f, 10.94f),
            mass = 7.0f,
            gravity = false,
            radius = 0.3f,
            height = 1.0f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val b2 = FixedCapsuleBody(
            "b2",
            initialPos = MutablePoint3f(8.1f, 7.0f, 10.48f),
            mass = 9.0f,
            gravity = false,
            radius = 0.2f,
            height = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val chk = CheckCylinderVsCapsule()
        val sep = SeparateCylinderVsCapsule()
        val bnc = BounceCylinderVsCapsule()
        val hit = MutableHitResult()

        // The two bodies should not collide at their original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        // Apply a force to each towards the other
        b1.apply {
            addForce(0.0f, -144.0f, 0.0f)
            applyForces()
        }
        b2.apply {
            addForce(0.0f, 144.0f, 0.0f)
            applyForces()
        }

        // The two bodies now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2")

        // The hit point was set to a point within the colliding area
        assertEquals(8.14f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.200381f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.584999f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2, and z should be 0 since it's SIDE
        assertEquals(-0.20002124f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-0.9797916f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // b1.nextSpeed points in the negative direction of y
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.34285715f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed points in the positive direction of y
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.26666668f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1.nextPos has moved a little in the direction of its speed
        assertEquals(8.2f, b1.nextPos.x, "b1.nextPos.x") // the same as before
        assertEquals(7.4942856f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.94f, b1.nextPos.z, "b1.nextPos.z") // the same as before

        // b2.nextPos has moved a little in the direction of its speed
        assertEquals(8.1f, b2.nextPos.x, "b2.nextPos.x") // the same as before
        assertEquals(7.0044446f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.48f, b2.nextPos.z, "b2.nextPos.z") // the same as before

        // The two should no longer collide after separation
        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")

        // b1.pos is unchanged
        assertEquals(8.2f, b1.pos.x, "b1.pos.x")
        assertEquals(7.5f, b1.pos.y, "b1.pos.y")
        assertEquals(10.94f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(8.1f, b2.pos.x, "b2.pos.x")
        assertEquals(7.0f, b2.pos.y, "b2.pos.y")
        assertEquals(10.48f, b2.pos.z, "b2.pos.z")

        // b1.nextPos has been moved a little
        assertEquals(8.20001f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x") // was pushed slightly towards +x
        assertEquals(7.4943376f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // slightly back towards its orig pos
        assertEquals(10.94f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // unchanged

        // b2.nextPos has been moved a little
        assertEquals(8.09999f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x") // was pushed slightly towards -x
        assertEquals(7.0043926f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y") // slightly back towards its orig pos
        assertEquals(10.48f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // unchanged

        // Since it was SIDE, the bodies should now be separated in XY
        val centre1 = Point2f(b1.nextPos.x, b1.nextPos.y)
        val centre2 = Point2f(b2.nextPos.x, b2.nextPos.y)
        val newDistance = centre1.distanceTo(centre2).toFloat()
        assertWithin(0.0f ..< 0.00006f, newDistance - (b1.radius + b2.radius), "gap") // less than 6 mm/100

        // b1.nextSpeed should still be the same
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.34285715f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed should still be the same
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.26666668f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        bnc.bounce(b1, b2, hit)

        // b1.speed should still be the same
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2.speed should still be the same
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextSpeed has been modified
        assertEquals(0.07082545f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.07125551f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified
        assertEquals(-0.055086464f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(-0.055420958f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should collide when the two bodies get too close and the speed is parallel to the z-axis`() {
        val b1 = FixedCylinderBody(
            "b1",
            initialPos = MutablePoint3f(8.35f, 7.1f, 11.18f),
            mass = 7.0f,
            gravity = false,
            radius = 0.3f,
            height = 1.0f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val b2 = FixedCapsuleBody(
            "b2",
            initialPos = MutablePoint3f(8.1f, 7.2f, 10.42f),
            mass = 9.0f,
            gravity = false,
            radius = 0.2f,
            height = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val chk = CheckCylinderVsCapsule()
        val sep = SeparateCylinderVsCapsule()
        val bnc = BounceCylinderVsCapsule()
        val hit = MutableHitResult()

        // The two bodies should not collide at their original position
        b1.applyForces()
        b2.applyForces()
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        // Apply a force to each towards the other
        b1.apply {
            addForce(0.0f, 0.0f, -145.0f)
            applyForces()
        }
        b2.apply {
            addForce(0.0f, 0.0f, 145.0f)
            applyForces()
        }

        // The two bodies now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        // b1's bottom collided with b2's top
        assertEquals(HitArea.BOTTOM_FACE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_TIP, hit.area2, "hit.area2")

        // The hit point was set to a point within the colliding area
        assertEquals(8.1f, hit.hitPt.x, TOLERANCE, "hitPt.x") // it's the capsule's top tip
        assertEquals(7.2f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.674476f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, "hitNormal12.x") // exact, because it was the capsule's tip
        assertEquals(0.0f, hit.hitNormal12.y, "hitNormal12.y")
        assertEquals(-1.0f, hit.hitNormal12.z, "hitNormal12.z")

        // b1.nextSpeed points in the negative direction of z
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.3452381f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed points in the positive direction of z
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.26851854f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1.nextPos has moved a little in the direction of its speed
        assertEquals(8.35f, b1.nextPos.x, "b1.nextPos.x") // the same as before
        assertEquals(7.1f, b1.nextPos.y, "b1.nextPos.y") // the same as before
        assertEquals(11.174247f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos has moved a little in the direction of its speed
        assertEquals(8.1f, b2.nextPos.x, "b2.nextPos.x") // the same as before
        assertEquals(7.2f, b2.nextPos.y, "b2.nextPos.y") // the same as before
        assertEquals(10.424476f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // The two should no longer collide after separation
        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")

        // b1.pos is unchanged
        assertEquals(8.35f, b1.pos.x, "b1.pos.x")
        assertEquals(7.1f, b1.pos.y, "b1.pos.y")
        assertEquals(11.18f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(8.1f, b2.pos.x, "b2.pos.x")
        assertEquals(7.2f, b2.pos.y, "b2.pos.y")
        assertEquals(10.42f, b2.pos.z, "b2.pos.z")

        // b1.nextPos has been moved a little
        assertEquals(8.35f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x") // unchanged
        assertEquals(7.1f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // unchanged
        assertEquals(11.174386f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // slightly back towards its orig pos

        // b2.nextPos has been moved a little
        assertEquals(8.1f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x") // unchanged
        assertEquals(7.2f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y") // unchanged
        assertEquals(10.424336f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // slightly back towards its orig pos

        // Since it was the capsule's TOP_TIP, the bodies should now be separated in z
        val b1Bottom = b1.nextPos.z - b1.height / 2.0f
        val b2Top = b2.nextPos.z + b2.height / 2.0f
        val newDistance = b1Bottom - b2Top
        assertWithin(0.0f ..< 0.00006f, newDistance, "gap") // less than 6 mm/100

        // b1.nextSpeed should still be the same
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.3452381f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed should still be the same
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.26851854f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        bnc.bounce(b1, b2, hit)

        // b1 had no speed before we added a force
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, "b1.speed.z")

        // b2 had no speed before we added a force
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // The cylinder is standing on the capsule, which is an unstable configuration, so no remedy against
        // "hopping" was applied, and the z component of nextSpeed is allowed to differ.

        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.086309545f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z") // away from b2

        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(-0.06712962f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z") // not the same as b1.nextSpeed.z
    }

    @Test
    fun `should behave graciously if a cylinder and a capsule are spawned in the exact same spot`() {
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
        val b2 = FixedCapsuleBody(
            "b2",
            initialPos = pos,
            mass = 42.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.6f,
            elasticity = 0.5f,
            friction = 0.5f,
        )

        val chk = CheckCylinderVsCapsule()
        val sep = SeparateCylinderVsCapsule()
        val bnc = BounceCylinderVsCapsule()
        val hit = MutableHitResult()

        // Apply forces to end anim phase 1 and update nextPos and nextSpeed
        b1.applyForces()
        b2.applyForces()

        // The two bodies should obviously collide
        Log.enabled = false // suppress expected log message
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "initial pos should collide")
        Log.enabled = true

        // The hit area happens to be declared SIDE even though this is arbitrary
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2")

        assertEquals(42.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(33.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(24.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 is expected to be set to a random normalised vector with z=0
        assertEquals(0.0f, hit.hitNormal12.z, "hitNormal12.z")

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

        // Bounce will force the two bodies apart such that they no longer collide

        try {
            Log.enabled = false // suppress expected log message
            sep.separate(b1, b2, hit)
            bnc.bounce(b1, b2, hit)
            assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
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

        // Since it was SIDE, separation is expected to happen in the XY plane
        assertTrue(b1.nextPos.z == b1.pos.z, "should not have changed b1.nextPos.z")
        assertTrue(b2.nextPos.z == b2.pos.z, "should not have changed b2.nextPos.z")

        // The bodies should now be separated in XY
        val dx = b1.nextPos.x - b2.nextPos.x
        val dy = b1.nextPos.y - b2.nextPos.y
        val distance = sqrt(dx * dx + dy * dy)
        val perfectDistance = b1.radius + b2.radius
        val gap = distance - perfectDistance
        assertWithin(0.0f ..< 0.00006f, gap, "gap") // less than 6 mm/100

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
    fun `should properly handle collisions in situation 1`() {
        val b1 = FixedCylinderBody(
            "b1",
            initialPos = MutablePoint3f(10.0f, 10.0f, 3.0f),
            mass = 10.0f,
            gravity = false,
            radius = 4.0f,
            height = 6.0f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val b2 = FixedCapsuleBody(
            "b2",
            initialPos = MutablePoint3f(10.0f, 5.0f, 8.7f),
            mass = 10.0f,
            gravity = false,
            radius = 2.0f,
            height = 5.0f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val chk = CheckCylinderVsCapsule()
        val sep = SeparateCylinderVsCapsule()
        val bnc = BounceCylinderVsCapsule()
        val hit = MutableHitResult()

        // The bodies should not collide initially
        assertFalse(chk.check(b1, b1.pos, b2, b2.pos, hit), "should not collide")

        // We give b1 an initial speed
        b1.speed.set(0.0f, -19.5f, 19.5f)
        b1.applyForces()
        b2.applyForces()

        // The bodies should now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.TOP_FACE, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM_SPHERE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(5.675f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(6.325f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-0.33871955f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.9408874f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // The cylinder moved along its speed
        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(9.675f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(3.325f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // The capsule has not moved
        assertEquals(10.0f, b2.nextPos.x, "b2.nextPos.x")
        assertEquals(5.0f, b2.nextPos.y, "b2.nextPos.y")
        assertEquals(8.7f, b2.nextPos.z, "b2.nextPos.z")

        // The bodies should no longer collide after separation
        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit))

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, "b1.pos.y")
        assertEquals(3.0f, b1.pos.z, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.0f, b2.pos.x, "b2.pos.x")
        assertEquals(5.0f, b2.pos.y, "b2.pos.y")
        assertEquals(8.7f, b2.pos.z, "b2.pos.z")

        // b1.nextPos has moved a little
        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x") // unchanged
        assertEquals(9.676229f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // slightly back towards its orig pos
        assertEquals(3.3215892f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")  // slightly back towards its orig pos

        // b2.nextPos has moved a little
        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x") // unchanged
        assertEquals(4.998772f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y") // slightly away from cylinder
        assertEquals(8.703411f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // slightly away from cylinder

        // b1.nextSpeed is unchanged
        assertEquals(0.0f, b1.nextSpeed.x, "b1.nextSpeed.x")
        assertEquals(-19.5f, b1.nextSpeed.y, "b1.nextSpeed.y")
        assertEquals(19.5f, b1.nextSpeed.z, "b1.nextSpeed.z")

        // b2.nextSpeed is unchanged
        assertEquals(0.0f, b2.nextSpeed.x, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, "b2.nextSpeed.z")

        // Since it wasn't the capsule's tip, the bounding boxes should still have a slight overlap
        val b1Top = b1.nextPos.z + b1.height / 2.0f
        val b2Bottom = b2.nextPos.z - b2.height / 2.0f
        val overlap = b1Top - b2Bottom
        assertWithin(0.05f ..< 0.15f, overlap, "overlap")

        // Compute nextSpeed
        bnc.bounce(b1, b2, hit)

        // b1.speed is unchanged
        assertEquals(0.0f, b1.speed.x, "b1.speed.x")
        assertEquals(-19.5f, b1.speed.y, "b1.speed.y")
        assertEquals(19.5f, b1.speed.z, "b1.speed.z")

        // b2.speed is unchanged
        assertEquals(0.0f, b2.speed.x, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, "b2.speed.z")

        // b1.nextSpeed slowed down a bit
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-19.156996f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(8.714978f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed got some of b1's kinetic energy
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(-0.3430048f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(10.785023f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should properly handle collisions in situation 2`() {
        val b1 = FixedCylinderBody(
            "b1",
            initialPos = Point3f.zero,
            mass = 20.0f,
            gravity = false,
            radius = 0.45f,
            height = 1.0f,
            elasticity = 0.5f,
            friction = 0.5f,
        )
        val b2 = FixedCapsuleBody(
            "b2",
            initialPos = Point3f.zero,
            mass = 64.0f,
            gravity = false,
            radius = 0.3f,
            height = 1.0f,
            elasticity = 0.5f,
            friction = 0.5f,
        )

        b1.apply {
            pos.set(149.41211f, 294.9641f, 2.2805471f)
            speed.set(0.084f, 0.0209f, -0.871855f)
            applyForces()
        }

        b2.apply {
            pos.set(148.69055f, 294.80444f, 1.5000299f)
            speed.set(-0.108f, -0.024f, 0.0f)
            applyForces()
        }

        val chk = CheckCylinderVsCapsule()
        val sep = SeparateCylinderVsCapsule()
        val hit = MutableHitResult()

        assertFalse(chk.check(b1, b1.pos, b2, b2.pos, hit), "should not collide at orig pos")
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide at nextPos")

        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
    }

    companion object {
        private const val TOLERANCE = 0.000001f
    }
}
