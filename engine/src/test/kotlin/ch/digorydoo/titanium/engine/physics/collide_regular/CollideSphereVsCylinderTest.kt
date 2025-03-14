package ch.digorydoo.titanium.engine.physics.collide_regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.utils.assertGreaterThan
import ch.digorydoo.titanium.engine.utils.assertLessThan
import org.junit.jupiter.api.BeforeAll
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CollideSphereVsCylinderTest {
    @Test
    fun `should collide when the two bodies get too close and the speed is parallel to the x-axis`() {
        val sphere = FixedSphereBody(
            "sphere",
            pos = MutablePoint3f(10.0f, 7.2f, 9.95f),
            mass = 13.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val cylinder = FixedCylinderBody(
            "cylinder",
            pos = MutablePoint3f(10.5f, 7.1f, 10.2f),
            mass = 11.0f,
            gravity = false,
            radius = 0.2f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val ck = CollideSphereVsCylinder()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(sphere, cylinder, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(sphere, cylinder, canBounce = true, hit)

        // The two bodies should not collide at their original position
        sphere.applyForces()
        cylinder.applyForces()
        assertFalse(check())

        // Apply a force to each towards the other
        sphere.apply {
            addForce(220.0f, 0.0f, 0.0f)
            applyForces()
        }
        cylinder.apply {
            addForce(-220.0f, 0.0f, 0.0f)
            applyForces()
        }

        // The two bodies should now collide
        assertTrue(check())

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific areas
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2")

        assertEquals(10.298547f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.14f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.45f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // sphere.nextSpeed points in the positive direction of x
        assertEquals(0.2820513f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // cylinder.nextSpeed points in the negative direction of x
        assertEquals(-0.33333334f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(0.0f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(0.0f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")

        // sphere.nextPos has moved a little in the direction of its speed
        assertEquals(10.004701f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(7.2f, sphere.nextPos.y, "sphere.nextPos.y") // the same as before
        assertEquals(9.95f, sphere.nextPos.z, "sphere.nextPos.z") // the same as before

        // cylinder.nextPos has moved a little in the direction of its speed
        assertEquals(10.494445f, cylinder.nextPos.x, TOLERANCE, "cylinder.nextPos.x")
        assertEquals(7.1f, cylinder.nextPos.y, "cylinder.nextPos.y") // the same as before
        assertEquals(10.2f, cylinder.nextPos.z, "cylinder.nextPos.z") // the same as before

        // The two should no longer collide after bounce
        bounce()
        assertFalse(check())

        // sphere.pos is unchanged
        assertEquals(10.0f, sphere.pos.x, "sphere.pos.x")
        assertEquals(7.2f, sphere.pos.y, "sphere.pos.y")
        assertEquals(9.95f, sphere.pos.z, "sphere.pos.z")

        // cylinder.pos is unchanged
        assertEquals(10.5f, cylinder.pos.x, "cylinder.pos.x")
        assertEquals(7.1f, cylinder.pos.y, "cylinder.pos.y")
        assertEquals(10.2f, cylinder.pos.z, "cylinder.pos.z")

        // sphere had no speed before we added a force
        assertEquals(0.0f, sphere.speed.x, "sphere.speed.x")
        assertEquals(0.0f, sphere.speed.y, "sphere.speed.y")
        assertEquals(0.0f, sphere.speed.z, "sphere.speed.z")

        // cylinder had no speed before we added a force
        assertEquals(0.0f, cylinder.speed.x, "cylinder.speed.x")
        assertEquals(0.0f, cylinder.speed.y, "cylinder.speed.y")
        assertEquals(0.0f, cylinder.speed.z, "cylinder.speed.z")

        // sphere.nextPos has been moved a little towards its original pos
        assertEquals(10.004406f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(7.2f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(9.95f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        // cylinder.nextPos has been moved a little towards its original pos
        assertEquals(10.494793f, cylinder.nextPos.x, TOLERANCE, "cylinder.nextPos.x")
        assertEquals(7.1f, cylinder.nextPos.y, TOLERANCE, "cylinder.nextPos.y")
        assertEquals(10.2f, cylinder.nextPos.z, TOLERANCE, "cylinder.nextPos.z")

        // sphere.nextSpeed has been modified
        assertEquals(-0.067697614f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.013804928f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // cylinder.nextSpeed has been modified
        assertEquals(0.0800063f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(-0.016314922f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(0.0f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")
    }

    @Test
    fun `should collide when the two bodies get too close and the speed is parallel to the y-axis`() {
        val sphere = FixedSphereBody(
            "sphere",
            pos = MutablePoint3f(8.2f, 7.5f, 9.9f),
            mass = 7.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val cylinder = FixedCylinderBody(
            "cylinder",
            pos = MutablePoint3f(8.1f, 7.0f, 10.2f),
            mass = 9.0f,
            gravity = false,
            radius = 0.2f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val ck = CollideSphereVsCylinder()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(sphere, cylinder, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(sphere, cylinder, canBounce = true, hit)

        // The two bodies should not collide at their original position
        sphere.applyForces()
        cylinder.applyForces()
        assertFalse(check())

        // Apply a force to each towards the other
        sphere.apply {
            addForce(0.0f, -145.0f, 0.0f)
            applyForces()
        }
        cylinder.apply {
            addForce(0.0f, 145.0f, 0.0f)
            applyForces()
        }

        // The two bodies now collide
        assertTrue(check())

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific areas
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2")

        // The hit point was set to a point within the colliding area
        assertEquals(8.14f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.2003837f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.45f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // sphere.nextSpeed points in the negative direction of y
        assertEquals(0.0f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.3452381f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // cylinder.nextSpeed points in the positive direction of y
        assertEquals(0.0f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(0.26851854f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(0.0f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")

        // sphere.nextPos has moved a little in the direction of its speed
        assertEquals(8.2f, sphere.nextPos.x, "sphere.nextPos.x") // the same as before
        assertEquals(7.494246f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(9.9f, sphere.nextPos.z, "sphere.nextPos.z") // the same as before

        // cylinder.nextPos has moved a little in the direction of its speed
        assertEquals(8.1f, cylinder.nextPos.x, "cylinder.nextPos.x") // the same as before
        assertEquals(7.004475f, cylinder.nextPos.y, TOLERANCE, "cylinder.nextPos.y")
        assertEquals(10.2f, cylinder.nextPos.z, "cylinder.nextPos.z") // the same as before

        // The two should no longer collide after bounce
        bounce()
        assertFalse(check())

        // sphere.pos is unchanged
        assertEquals(8.2f, sphere.pos.x, "sphere.pos.x")
        assertEquals(7.5f, sphere.pos.y, "sphere.pos.y")
        assertEquals(9.9f, sphere.pos.z, "sphere.pos.z")

        // cylinder.pos is unchanged
        assertEquals(8.1f, cylinder.pos.x, "cylinder.pos.x")
        assertEquals(7.0f, cylinder.pos.y, "cylinder.pos.y")
        assertEquals(10.2f, cylinder.pos.z, "cylinder.pos.z")

        // sphere had no speed before we added a force
        assertEquals(0.0f, sphere.speed.x, "sphere.speed.x")
        assertEquals(0.0f, sphere.speed.y, "sphere.speed.y")
        assertEquals(0.0f, sphere.speed.z, "sphere.speed.z")

        // cylinder had no speed before we added a force
        assertEquals(0.0f, cylinder.speed.x, "cylinder.speed.x")
        assertEquals(0.0f, cylinder.speed.y, "cylinder.speed.y")
        assertEquals(0.0f, cylinder.speed.z, "cylinder.speed.z")

        // sphere.nextPos has been moved a little towards its original pos
        assertEquals(8.2f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(7.494606f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(9.9f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        // cylinder.nextPos has been moved a little towards its original pos
        assertEquals(8.1f, cylinder.nextPos.x, TOLERANCE, "cylinder.nextPos.x")
        assertEquals(7.004195f, cylinder.nextPos.y, TOLERANCE, "cylinder.nextPos.y")
        assertEquals(10.2f, cylinder.nextPos.z, TOLERANCE, "cylinder.nextPos.z")

        // sphere.nextSpeed has been modified
        assertEquals(0.016896777f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.08286408f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // cylinder.nextSpeed has been modified
        assertEquals(-0.013141938f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(-0.06444984f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(0.0f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")
    }

    @Test
    fun `should collide when the two bodies get too close and the speed is parallel to the z-axis`() {
        val sphere = FixedSphereBody(
            "sphere",
            pos = MutablePoint3f(8.3f, 7.1f, 10.46f),
            mass = 7.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val cylinder = FixedCylinderBody(
            "cylinder",
            pos = MutablePoint3f(8.1f, 7.2f, 10.15f),
            mass = 9.0f,
            gravity = false,
            radius = 0.2f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val ck = CollideSphereVsCylinder()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(sphere, cylinder, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(sphere, cylinder, canBounce = true, hit)

        // The two bodies should not collide at their original position
        sphere.applyForces()
        cylinder.applyForces()
        assertFalse(check(), "should not collide at initial pos")

        // Apply a force to each towards the other
        sphere.apply {
            addForce(0.0f, 0.0f, -145.0f)
            applyForces()
        }
        cylinder.apply {
            addForce(0.0f, 0.0f, 145.0f)
            applyForces()
        }

        // The two bodies now collide
        assertTrue(check(), "should collide after adding forces")

        // sphere's bottom collided with cylinder's top
        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific areas
        assertEquals(HitArea.TOP, hit.area2, "hit.area2")

        // The hit point was set to a point within the colliding area
        assertEquals(8.18f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.16f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.654361f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // sphere.nextSpeed points in the negative direction of z
        assertEquals(0.0f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(-0.3452381f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // cylinder.nextSpeed points in the positive direction of z
        assertEquals(0.0f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(0.0f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(0.26851854f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")

        // sphere.nextPos has moved a little in the direction of its speed
        assertEquals(8.3f, sphere.nextPos.x, "sphere.nextPos.x") // the same as before
        assertEquals(7.1f, sphere.nextPos.y, "sphere.nextPos.y") // the same as before
        assertEquals(10.4542465f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        // cylinder.nextPos has moved a little in the direction of its speed
        assertEquals(8.1f, cylinder.nextPos.x, "cylinder.nextPos.x") // the same as before
        assertEquals(7.2f, cylinder.nextPos.y, "cylinder.nextPos.y") // the same as before
        assertEquals(10.154475f, cylinder.nextPos.z, TOLERANCE, "cylinder.nextPos.z")

        // The two should no longer collide after bounce
        bounce()
        assertFalse(check())

        // sphere.pos is unchanged
        assertEquals(8.3f, sphere.pos.x, "sphere.pos.x")
        assertEquals(7.1f, sphere.pos.y, "sphere.pos.y")
        assertEquals(10.46f, sphere.pos.z, "sphere.pos.z")

        // cylinder.pos is unchanged
        assertEquals(8.1f, cylinder.pos.x, "cylinder.pos.x")
        assertEquals(7.2f, cylinder.pos.y, "cylinder.pos.y")
        assertEquals(10.15f, cylinder.pos.z, "cylinder.pos.z")

        // sphere had no speed before we added a force
        assertEquals(0.0f, sphere.speed.x, "sphere.speed.x")
        assertEquals(0.0f, sphere.speed.y, "sphere.speed.y")
        assertEquals(0.0f, sphere.speed.z, "sphere.speed.z")

        // cylinder had no speed before we added a force
        assertEquals(0.0f, cylinder.speed.x, "cylinder.speed.x")
        assertEquals(0.0f, cylinder.speed.y, "cylinder.speed.y")
        assertEquals(0.0f, cylinder.speed.z, "cylinder.speed.z")

        // sphere.nextPos has been moved a little towards its original pos
        assertEquals(8.3f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(7.1f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.454606f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        // cylinder.nextPos has been moved a little towards its original pos
        assertEquals(8.1f, cylinder.nextPos.x, TOLERANCE, "cylinder.nextPos.x")
        assertEquals(7.2f, cylinder.nextPos.y, TOLERANCE, "cylinder.nextPos.y")
        assertEquals(10.154196f, cylinder.nextPos.z, TOLERANCE, "cylinder.nextPos.z")

        // sphere.nextSpeed has been modified
        assertEquals(0.0f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.086309545f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // cylinder.nextSpeed has been modified
        assertEquals(0.0f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(0.0f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(-0.06712962f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")
    }

    @Test
    fun `should correctly bounce one cylinder off the other when moving in an arbitrary direction`() {
        val sphere = FixedSphereBody(
            "sphere",
            pos = MutablePoint3f(10.0f, 10.0f, 10.0f),
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val cylinder = FixedCylinderBody(
            "cylinder",
            pos = MutablePoint3f(10.0f, 10.51f, 10.51f),
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val ck = CollideSphereVsCylinder()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(sphere, cylinder, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(sphere, cylinder, canBounce = true, hit)

        // We give sphere an initial speed
        sphere.speed.set(0.0f, 0.6f, 0.6f)

        // Update nextPos and nextSpeed
        sphere.applyForces()
        cylinder.applyForces()

        // The bodies should now collide
        assertTrue(check())

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific areas
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.26f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.635f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The bodies should no longer collide after bounce
        bounce()
        assertFalse(check())

        // sphere.pos is unchanged
        assertEquals(10.0f, sphere.pos.x, TOLERANCE, "sphere.pos.x")
        assertEquals(10.0f, sphere.pos.y, TOLERANCE, "sphere.pos.y")
        assertEquals(10.0f, sphere.pos.z, TOLERANCE, "sphere.pos.z")

        // cylinder.pos is unchanged
        assertEquals(10.0f, cylinder.pos.x, TOLERANCE, "cylinder.pos.x")
        assertEquals(10.51f, cylinder.pos.y, TOLERANCE, "cylinder.pos.y")
        assertEquals(10.51f, cylinder.pos.z, TOLERANCE, "cylinder.pos.z")

        // sphere.speed is unchanged
        assertEquals(0.0f, sphere.speed.x, TOLERANCE, "sphere.speed.x")
        assertEquals(0.6f, sphere.speed.y, TOLERANCE, "sphere.speed.y")
        assertEquals(0.6f, sphere.speed.z, TOLERANCE, "sphere.speed.z")

        // cylinder.speed is unchanged
        assertEquals(0.0f, cylinder.speed.x, TOLERANCE, "cylinder.speed.x")
        assertEquals(0.0f, cylinder.speed.y, TOLERANCE, "cylinder.speed.y")
        assertEquals(0.0f, cylinder.speed.z, TOLERANCE, "cylinder.speed.z")

        // sphere.nextPos has been moved
        assertEquals(10.0f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.009375f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.009375f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        // cylinder.nextPos is unchanged
        assertEquals(10.0f, cylinder.nextPos.x, TOLERANCE, "cylinder.nextPos.x")
        assertEquals(10.51f, cylinder.nextPos.y, TOLERANCE, "cylinder.nextPos.y")
        assertEquals(10.51f, cylinder.nextPos.z, TOLERANCE, "cylinder.nextPos.z")

        // sphere.nextSpeed has been modified
        assertEquals(0.0f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.225f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.49200004f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // cylinder.nextSpeed has been modified
        assertEquals(0.0f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(0.375f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(0.10799999f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")
    }

    @Test
    fun `should behave graciously if a sphere and a cylinder are spawned in the exact same spot`() {
        val pos = MutablePoint3f(42.0f, 33.0f, 24.0f)
        val sphere = FixedSphereBody(
            "sphere",
            pos = pos,
            mass = 42.0f,
            gravity = false,
            radius = 0.25f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.5f,
        )
        val cylinder = FixedCylinderBody(
            "cylinder",
            pos = pos,
            mass = 42.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.5f,
        )

        val ck = CollideSphereVsCylinder()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(sphere, cylinder, canBounce = false, hit)
        fun bounce() = ck.checkAndBounceIfNeeded(sphere, cylinder, canBounce = true, hit)

        // Apply forces to end anim phase 1 and update nextPos and nextSpeed
        sphere.applyForces()
        cylinder.applyForces()

        // The two bodies should obviously collide
        assertTrue(check(), "initial pos should collide")

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific areas
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2") // kind of arbitrary

        // The hit point is set to the centre of the two bodies
        assertEquals(42.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(33.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(24.25f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // sphere.nextSpeed is zero, since no forces were acting on the body
        assertEquals(0.0f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // cylinder.nextSpeed is zero, since no forces were acting on the body
        assertEquals(0.0f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(0.0f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(0.0f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")

        // sphere.nextPos is still the same position
        assertEquals(42.0f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(33.0f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(24.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        // cylinder.nextPos is still the same position
        assertEquals(42.0f, cylinder.nextPos.x, TOLERANCE, "cylinder.nextPos.x")
        assertEquals(33.0f, cylinder.nextPos.y, TOLERANCE, "cylinder.nextPos.y")
        assertEquals(24.0f, cylinder.nextPos.z, TOLERANCE, "cylinder.nextPos.z")

        // Bounce will force the two bodies apart such that they no longer collide
        bounce()
        assertFalse(
            check(),
            "should no longer collide, but they do: " +
                "sphere.nextPos=${sphere.nextPos}, cylinder.nextPos=${cylinder.nextPos}"
        )

        // A separation force was not added
        assertEquals(0.0f, sphere.resultingForce.x, "sphere.resultingForce.x")
        assertEquals(0.0f, sphere.resultingForce.y, "sphere.resultingForce.y")
        assertEquals(0.0f, sphere.resultingForce.z, "sphere.resultingForce.z")
        assertEquals(0.0f, cylinder.resultingForce.x, "cylinder.resultingForce.x")
        assertEquals(0.0f, cylinder.resultingForce.y, "cylinder.resultingForce.y")
        assertEquals(0.0f, cylinder.resultingForce.z, "cylinder.resultingForce.z")

        // sphere.pos is unchanged
        assertEquals(42.0f, sphere.pos.x, "sphere.pos.x")
        assertEquals(33.0f, sphere.pos.y, "sphere.pos.y")
        assertEquals(24.0f, sphere.pos.z, "sphere.pos.z")

        // cylinder.pos is unchanged
        assertEquals(42.0f, cylinder.pos.x, "cylinder.pos.x")
        assertEquals(33.0f, cylinder.pos.y, "cylinder.pos.y")
        assertEquals(24.0f, cylinder.pos.z, "cylinder.pos.z")

        // sphere had no speed
        assertEquals(0.0f, sphere.speed.x, "sphere.speed.x")
        assertEquals(0.0f, sphere.speed.y, "sphere.speed.y")
        assertEquals(0.0f, sphere.speed.z, "sphere.speed.z")

        // cylinder had no speed
        assertEquals(0.0f, cylinder.speed.x, "cylinder.speed.x")
        assertEquals(0.0f, cylinder.speed.y, "cylinder.speed.y")
        assertEquals(0.0f, cylinder.speed.z, "cylinder.speed.z")

        // sphere.nextPos was moved. Since the direction was random, we only test if it has changed.
        assertTrue(
            sphere.nextPos.x != sphere.pos.x || sphere.nextPos.y != sphere.pos.y,
            "should have moved sphere.nextPos"
        )

        // Forced separation along a random direction should never involve z.
        assertEquals(0.0f, sphere.nextPos.z - sphere.pos.z, "should not involve z with forced separation")

        // cylinder.nextPos was moved, too.
        assertTrue(
            cylinder.nextPos.x != cylinder.pos.x || cylinder.nextPos.y != cylinder.pos.y,
            "should have moved cylinder.nextPos"
        )
        assertEquals(0.0f, cylinder.nextPos.z - cylinder.pos.z, "should not involve z with forced separation")

        // The new distance should be in the range we'd expect. Since z was not involved, we can just check XY length.
        val dx = sphere.nextPos.x - cylinder.nextPos.x
        val dy = sphere.nextPos.y - cylinder.nextPos.y
        val dist = sqrt(dx * dx + dy * dy)
        val rsum = sphere.radius + cylinder.radius
        assertLessThan(dist, rsum + 0.01f, "new distance")
        assertGreaterThan(dist, rsum - 0.01f, "new distance")

        // sphere.nextSpeed is still zero
        assertEquals(0.0f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // cylinder.nextSpeed is still zero
        assertEquals(0.0f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(0.0f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(0.0f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")
    }

    companion object {
        private const val TOLERANCE = 0.000001f

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            Log.ttyOutput = Log.TtyOutput.OFF
        }
    }
}
