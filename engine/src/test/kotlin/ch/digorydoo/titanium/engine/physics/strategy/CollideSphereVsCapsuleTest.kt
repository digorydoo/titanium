package ch.digorydoo.titanium.engine.physics.strategy

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_capsule.BounceSphereVsCapsule
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_capsule.CheckSphereVsCapsule
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_capsule.SeparateSphereVsCapsule
import ch.digorydoo.titanium.engine.utils.assertGreaterThan
import ch.digorydoo.titanium.engine.utils.assertLessThan
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CollideSphereVsCapsuleTest {
    @Test
    fun `should collide when the two bodies get too close and the speed is parallel to the x-axis`() {
        val sphere = FixedSphereBody(
            "sphere",
            initialPos = MutablePoint3f(10.0f, 7.2f, 10.46f),
            mass = 13.0f,
            gravity = false,
            radius = 0.3f,
            elasticity = 0.5f,
            friction = 0.5f,
        )
        val capsule = FixedCapsuleBody(
            "capsule",
            initialPos = MutablePoint3f(10.5f, 7.1f, 10.44f),
            mass = 11.0f,
            gravity = false,
            radius = 0.2f,
            height = 0.5f,
            elasticity = 0.5f,
            friction = 0.72f,
        )

        val chk = CheckSphereVsCapsule()
        val sep = SeparateSphereVsCapsule()
        val bnc = BounceSphereVsCapsule()
        val hit = MutableHitResult()

        // The two bodies should not collide at their original position
        sphere.applyForces()
        capsule.applyForces()
        assertFalse(chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit))

        // Apply a force to each towards the other
        sphere.apply {
            addForce(220.0f, 0.0f, 0.0f)
            applyForces()
        }
        capsule.apply {
            addForce(-220.0f, 0.0f, 0.0f)
            applyForces()
        }

        // The two bodies should now collide
        assertTrue(chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit))

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific areas
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2")

        assertEquals(10.298547f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.14f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.46f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from sphere to capsule, and z should be 0 since it's SIDE
        assertEquals(0.97978365f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-0.2000601f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // It should be properly normalised
        val n = hit.hitNormal12
        val len = sqrt(n.x * n.x + n.y * n.y + n.z * n.z)
        assertLessThan(len, 1.01f, "hitNormal12")
        assertGreaterThan(len, 0.99f, "hitNormal12")

        // sphere.nextSpeed points in the positive direction of x
        assertEquals(0.2820513f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // capsule.nextSpeed points in the negative direction of x
        assertEquals(-0.33333334f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(0.0f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y")
        assertEquals(0.0f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")

        // sphere.nextPos has moved a little in the direction of its speed
        assertEquals(10.004701f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(7.2f, sphere.nextPos.y, "sphere.nextPos.y") // the same as before
        assertEquals(10.46f, sphere.nextPos.z, "sphere.nextPos.z") // the same as before

        // capsule.nextPos has moved a little in the direction of its speed
        assertEquals(10.494445f, capsule.nextPos.x, TOLERANCE, "capsule.nextPos.x")
        assertEquals(7.1f, capsule.nextPos.y, "capsule.nextPos.y") // the same as before
        assertEquals(10.44f, capsule.nextPos.z, "capsule.nextPos.z") // the same as before

        // The two should no longer collide after separation
        sep.separate(sphere, capsule, hit)
        assertFalse(chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit))

        // sphere.pos is unchanged
        assertEquals(10.0f, sphere.pos.x, "sphere.pos.x")
        assertEquals(7.2f, sphere.pos.y, "sphere.pos.y")
        assertEquals(10.46f, sphere.pos.z, "sphere.pos.z")

        // capsule.pos is unchanged
        assertEquals(10.5f, capsule.pos.x, "capsule.pos.x")
        assertEquals(7.1f, capsule.pos.y, "capsule.pos.y")
        assertEquals(10.44f, capsule.pos.z, "capsule.pos.z")

        // sphere had no speed before we added a force
        assertEquals(0.0f, sphere.speed.x, "sphere.speed.x")
        assertEquals(0.0f, sphere.speed.y, "sphere.speed.y")
        assertEquals(0.0f, sphere.speed.z, "sphere.speed.z")

        // capsule had no speed before we added a force
        assertEquals(0.0f, capsule.speed.x, "capsule.speed.x")
        assertEquals(0.0f, capsule.speed.y, "capsule.speed.y")
        assertEquals(0.0f, capsule.speed.z, "capsule.speed.z")

        // sphere.nextPos has been moved a little
        assertEquals(10.004602f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(7.20002f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.46f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        // capsule.nextPos has been moved a little
        assertEquals(10.494543f, capsule.nextPos.x, TOLERANCE, "capsule.nextPos.x")
        assertEquals(7.09998f, capsule.nextPos.y, TOLERANCE, "capsule.nextPos.y")
        assertEquals(10.44f, capsule.nextPos.z, TOLERANCE, "capsule.nextPos.z")

        // nextSpeed should still have the previous values
        assertEquals(0.2820513f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(-0.33333334f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(0.0f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y")
        assertEquals(0.0f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")

        bnc.bounce(sphere, capsule, hit)

        // Now nextSpeed should be the speed after bounce
        assertEquals(-0.06769057f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.013821598f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(0.079997964f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(-0.016334612f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y")
        assertEquals(0.0f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")
    }

    @Test
    fun `should collide when the two bodies get too close and the speed is parallel to the y-axis`() {
        val sphere = FixedSphereBody(
            "sphere",
            initialPos = MutablePoint3f(8.2f, 7.5f, 10.4f),
            mass = 7.0f,
            gravity = false,
            radius = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val capsule = FixedCapsuleBody(
            "capsule",
            initialPos = MutablePoint3f(8.1f, 7.0f, 10.45f),
            mass = 9.0f,
            gravity = false,
            radius = 0.2f,
            height = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val chk = CheckSphereVsCapsule()
        val sep = SeparateSphereVsCapsule()
        val bnc = BounceSphereVsCapsule()
        val hit = MutableHitResult()

        // The two bodies should not collide at their original position
        sphere.applyForces()
        capsule.applyForces()
        assertFalse(chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit))

        // Apply a force to each towards the other
        sphere.apply {
            addForce(0.0f, -145.0f, 0.0f)
            applyForces()
        }
        capsule.apply {
            addForce(0.0f, 145.0f, 0.0f)
            applyForces()
        }

        // The two bodies now collide
        assertTrue(chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit))

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific areas
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2")

        // The hit point was set to a point within the colliding area
        assertEquals(8.14f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.2003837f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.4f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from sphere to capsule, and z should be 0 since it's SIDE
        assertEquals(-0.20004871f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-0.9797859f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // sphere.nextSpeed points in the negative direction of y
        assertEquals(0.0f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.3452381f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // capsule.nextSpeed points in the positive direction of y
        assertEquals(0.0f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(0.26851854f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y")
        assertEquals(0.0f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")

        // sphere.nextPos has moved a little in the direction of its speed
        assertEquals(8.2f, sphere.nextPos.x, "sphere.nextPos.x") // the same as before
        assertEquals(7.494246f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.4f, sphere.nextPos.z, "sphere.nextPos.z") // the same as before

        // capsule.nextPos has moved a little in the direction of its speed
        assertEquals(8.1f, capsule.nextPos.x, "capsule.nextPos.x") // the same as before
        assertEquals(7.004475f, capsule.nextPos.y, TOLERANCE, "capsule.nextPos.y")
        assertEquals(10.45f, capsule.nextPos.z, "capsule.nextPos.z") // the same as before

        // The two should no longer collide after bounce
        sep.separate(sphere, capsule, hit)
        bnc.bounce(sphere, capsule, hit)
        assertFalse(chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit))

        // sphere.pos is unchanged
        assertEquals(8.2f, sphere.pos.x, "sphere.pos.x")
        assertEquals(7.5f, sphere.pos.y, "sphere.pos.y")
        assertEquals(10.4f, sphere.pos.z, "sphere.pos.z")

        // capsule.pos is unchanged
        assertEquals(8.1f, capsule.pos.x, "capsule.pos.x")
        assertEquals(7.0f, capsule.pos.y, "capsule.pos.y")
        assertEquals(10.45f, capsule.pos.z, "capsule.pos.z")

        // sphere had no speed before we added a force
        assertEquals(0.0f, sphere.speed.x, "sphere.speed.x")
        assertEquals(0.0f, sphere.speed.y, "sphere.speed.y")
        assertEquals(0.0f, sphere.speed.z, "sphere.speed.z")

        // capsule had no speed before we added a force
        assertEquals(0.0f, capsule.speed.x, "capsule.speed.x")
        assertEquals(0.0f, capsule.speed.y, "capsule.speed.y")
        assertEquals(0.0f, capsule.speed.z, "capsule.speed.z")

        // sphere.nextPos has been moved a little
        assertEquals(8.200017f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(7.4943314f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.4f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        // capsule.nextPos has been moved a little
        assertEquals(8.099983f, capsule.nextPos.x, TOLERANCE, "capsule.nextPos.x")
        assertEquals(7.00439f, capsule.nextPos.y, TOLERANCE, "capsule.nextPos.y")
        assertEquals(10.45f, capsule.nextPos.z, TOLERANCE, "capsule.nextPos.z")

        // sphere.nextSpeed has been modified
        assertEquals(0.07132859f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.071745925f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // capsule.nextSpeed has been modified
        assertEquals(-0.055477783f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(-0.055802368f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y")
        assertEquals(0.0f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")
    }

    @Test
    fun `should collide with the capsule top when the sphere is near its top`() {
        val sphere = FixedSphereBody(
            "sphere",
            initialPos = MutablePoint3f(8.3f, 7.1f, 10.96f),
            mass = 7.0f,
            gravity = false,
            radius = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        val capsule = FixedCapsuleBody(
            "capsule",
            initialPos = MutablePoint3f(8.1f, 7.2f, 10.4f),
            mass = 9.0f,
            gravity = false,
            radius = 0.2f,
            height = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        val chk = CheckSphereVsCapsule()
        val sep = SeparateSphereVsCapsule()
        val bnc = BounceSphereVsCapsule()
        val hit = MutableHitResult()

        // The two bodies should not collide at their original position
        sphere.applyForces()
        capsule.applyForces()
        assertFalse(
            chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit),
            "should not collide at initial pos"
        )

        // Apply a force to each towards the other
        sphere.apply {
            addForce(4.2f, 0.0f, -892.0f)
            applyForces()
        }
        capsule.apply {
            addForce(0.0f, 4.2f, 892.0f)
            applyForces()
        }

        // The two bodies now collide
        assertTrue(
            chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit),
            "should collide after adding forces"
        )

        // sphere's bottom collided with capsule's top
        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific areas
        assertEquals(HitArea.TOP_SPHERE, hit.area2, "hit.area2")

        // The hit point was set to a point within the colliding area
        assertEquals(8.180059f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.160082f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.656342f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from sphere to capsule
        assertEquals(-0.40035993f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.20027247f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-0.89420515f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // sphere.nextSpeed points in the negative direction of z
        assertEquals(0.01f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(-2.1238096f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // capsule.nextSpeed points in the positive direction of z
        assertEquals(0.0f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(0.00777778f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y")
        assertEquals(1.651852f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")

        // sphere.nextPos has moved in the direction of its speed
        assertEquals(8.300167f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x") // the same as before
        assertEquals(7.1f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y") // the same as before
        assertEquals(10.924603f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        // capsule.nextPos has moved in the direction of its speed
        assertEquals(8.1f, capsule.nextPos.x, TOLERANCE, "capsule.nextPos.x") // the same as before
        assertEquals(7.2001295f, capsule.nextPos.y, TOLERANCE, "capsule.nextPos.y") // the same as before
        assertEquals(10.42753f, capsule.nextPos.z, TOLERANCE, "capsule.nextPos.z")

        // The two should no longer collide after separation
        sep.separate(sphere, capsule, hit)
        assertFalse(chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit), "should no longer collide")

        // sphere.pos is unchanged
        assertEquals(8.3f, sphere.pos.x, "sphere.pos.x")
        assertEquals(7.1f, sphere.pos.y, "sphere.pos.y")
        assertEquals(10.96f, sphere.pos.z, "sphere.pos.z")

        // capsule.pos is unchanged
        assertEquals(8.1f, capsule.pos.x, "capsule.pos.x")
        assertEquals(7.2f, capsule.pos.y, "capsule.pos.y")
        assertEquals(10.4f, capsule.pos.z, "capsule.pos.z")

        // sphere.nextPos has been moved a little
        assertEquals(8.300183f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(7.099992f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.924641f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        // capsule.nextPos has been moved a little
        assertEquals(8.099984f, capsule.nextPos.x, TOLERANCE, "capsule.nextPos.x")
        assertEquals(7.2001376f, capsule.nextPos.y, TOLERANCE, "capsule.nextPos.y")
        assertEquals(10.427493f, capsule.nextPos.z, TOLERANCE, "capsule.nextPos.z")

        // sphere.nextSpeed should still be the same
        assertEquals(0.01f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(-2.1238096f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // capsule.nextSpeed should still be the same
        assertEquals(0.0f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(0.00777778f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y")
        assertEquals(1.651852f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")

        bnc.bounce(sphere, capsule, hit)

        // sphere.speed should be unchanged
        assertEquals(0.0f, sphere.speed.x, "sphere.speed.x")
        assertEquals(0.0f, sphere.speed.y, "sphere.speed.y")
        assertEquals(0.0f, sphere.speed.z, "sphere.speed.z")

        // capsule.speed should be unchanged
        assertEquals(0.0f, capsule.speed.x, "capsule.speed.x")
        assertEquals(0.0f, capsule.speed.y, "capsule.speed.y")
        assertEquals(0.0f, capsule.speed.z, "capsule.speed.z")

        // sphere.nextSpeed has been modified
        assertEquals(0.89824545f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.44420424f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.029402971f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // capsule.nextSpeed has been modified
        assertEquals(-0.6908576f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(0.35326993f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y")
        assertEquals(-0.022868931f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")
    }

    @Test
    fun `should collide with the capsule bottom when the sphere is near its bottom`() {
        val sphere = FixedSphereBody(
            "sphere",
            initialPos = MutablePoint3f(10.0f, 10.0f, 10.5f),
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            elasticity = 0.5f,
            friction = 0.5f,
        )
        val capsule = FixedCapsuleBody(
            "capsule",
            initialPos = MutablePoint3f(10.0f, 10.51f, 10.76f),
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.6f,
            elasticity = 0.5f,
            friction = 0.72f,
        )

        val chk = CheckSphereVsCapsule()
        val sep = SeparateSphereVsCapsule()
        val bnc = BounceSphereVsCapsule()
        val hit = MutableHitResult()

        // The bodies should not collide initially
        sphere.applyForces()
        capsule.applyForces()
        assertFalse(chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit), "should not collide")

        // Give the sphere a speed
        sphere.speed.set(0.0f, 3.17f, 0.49f)
        sphere.applyForces()
        capsule.applyForces()

        // The bodies should now collide
        assertTrue(chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit), "should collide")

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific areas
        assertEquals(HitArea.BOTTOM_SPHERE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.281537f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.609136f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from sphere to capsule
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.91481245f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.40387896f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(10.0f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.052834f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.508166f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.0f, capsule.nextPos.x, TOLERANCE, "capsule.nextPos.x")
        assertEquals(10.51f, capsule.nextPos.y, TOLERANCE, "capsule.nextPos.y")
        assertEquals(10.76f, capsule.nextPos.z, TOLERANCE, "capsule.nextPos.z")

        // The bodies should no longer collide after separation
        sep.separate(sphere, capsule, hit)
        assertFalse(chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit))

        // sphere.pos is unchanged
        assertEquals(10.0f, sphere.pos.x, TOLERANCE, "sphere.pos.x")
        assertEquals(10.0f, sphere.pos.y, TOLERANCE, "sphere.pos.y")
        assertEquals(10.5f, sphere.pos.z, TOLERANCE, "sphere.pos.z")

        // capsule.pos is unchanged
        assertEquals(10.0f, capsule.pos.x, TOLERANCE, "capsule.pos.x")
        assertEquals(10.51f, capsule.pos.y, TOLERANCE, "capsule.pos.y")
        assertEquals(10.76f, capsule.pos.z, TOLERANCE, "capsule.pos.z")

        // sphere.nextPos has been moved
        assertEquals(10.0f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.0526905f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.508103f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        // capsule.nextPos has been moved
        assertEquals(10.0f, capsule.nextPos.x, TOLERANCE, "capsule.nextPos.x")
        assertEquals(10.510143f, capsule.nextPos.y, TOLERANCE, "capsule.nextPos.y")
        assertEquals(10.760063f, capsule.nextPos.z, TOLERANCE, "capsule.nextPos.z")

        // sphere.nextSpeed should still be the same
        assertEquals(0.0f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(3.17f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.49f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // capsule.nextSpeed should still be the same
        assertEquals(0.0f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(0.0f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y")
        assertEquals(0.0f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")

        // Compute the new nextSpeed
        bnc.bounce(sphere, capsule, hit)

        // sphere.speed is unchanged
        assertEquals(0.0f, sphere.speed.x, TOLERANCE, "sphere.speed.x")
        assertEquals(3.17f, sphere.speed.y, TOLERANCE, "sphere.speed.y")
        assertEquals(0.49f, sphere.speed.z, TOLERANCE, "sphere.speed.z")

        // capsule.speed is unchanged
        assertEquals(0.0f, capsule.speed.x, TOLERANCE, "capsule.speed.x")
        assertEquals(0.0f, capsule.speed.y, TOLERANCE, "capsule.speed.y")
        assertEquals(0.0f, capsule.speed.z, TOLERANCE, "capsule.speed.z")

        // sphere.nextSpeed has been modified
        assertEquals(0.0f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(1.2307551f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.08860445f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // capsule.nextSpeed has been modified
        assertEquals(0.0f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(1.9392445f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y")
        assertEquals(0.4013955f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")
    }

    @Test
    fun `should behave graciously if a sphere and a capsule are spawned in the exact same spot`() {
        val pos = MutablePoint3f(42.0f, 33.0f, 24.0f)
        val sphere = FixedSphereBody(
            "sphere",
            initialPos = pos,
            mass = 42.0f,
            gravity = false,
            radius = 0.25f,
            elasticity = 0.5f,
            friction = 0.5f,
        )
        val capsule = FixedCapsuleBody(
            "capsule",
            initialPos = pos,
            mass = 42.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.6f,
            elasticity = 0.5f,
            friction = 0.5f,
        )

        val chk = CheckSphereVsCapsule()
        val sep = SeparateSphereVsCapsule()
        val bnc = BounceSphereVsCapsule()
        val hit = MutableHitResult()

        // Apply forces to end anim phase 1 and update nextPos and nextSpeed
        sphere.applyForces()
        capsule.applyForces()

        // The two bodies should obviously collide
        Log.enabled = false // suppress expected log message
        assertTrue(chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit), "initial pos should collide")
        Log.enabled = true

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1") // spheres do not have specific areas
        assertEquals(HitArea.SIDE, hit.area2, "hit.area2") // kind of arbitrary

        // The hit point is set to the centre of the two bodies
        assertEquals(42.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(33.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(24.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 is random and cannot be tested, but it should be properly normalised
        val n = hit.hitNormal12
        val len = sqrt(n.x * n.x + n.y * n.y + n.z * n.z)
        assertLessThan(len, 1.01f, "hitNormal12")
        assertGreaterThan(len, 0.99f, "hitNormal12")

        // sphere.nextSpeed is zero, since no forces were acting on the body
        assertEquals(0.0f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // capsule.nextSpeed is zero, since no forces were acting on the body
        assertEquals(0.0f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(0.0f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y")
        assertEquals(0.0f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")

        // sphere.nextPos is still the same position
        assertEquals(42.0f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(33.0f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(24.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        // capsule.nextPos is still the same position
        assertEquals(42.0f, capsule.nextPos.x, TOLERANCE, "capsule.nextPos.x")
        assertEquals(33.0f, capsule.nextPos.y, TOLERANCE, "capsule.nextPos.y")
        assertEquals(24.0f, capsule.nextPos.z, TOLERANCE, "capsule.nextPos.z")

        // Bounce will force the two bodies apart such that they no longer collide
        Log.enabled = false // suppress expected log message
        sep.separate(sphere, capsule, hit)
        bnc.bounce(sphere, capsule, hit)
        Log.enabled = true

        assertFalse(chk.check(sphere, sphere.nextPos, capsule, capsule.nextPos, hit), "should no longer collide")

        // A separation force was not added
        assertEquals(0.0f, sphere.resultingForce.x, "sphere.resultingForce.x")
        assertEquals(0.0f, sphere.resultingForce.y, "sphere.resultingForce.y")
        assertEquals(0.0f, sphere.resultingForce.z, "sphere.resultingForce.z")
        assertEquals(0.0f, capsule.resultingForce.x, "capsule.resultingForce.x")
        assertEquals(0.0f, capsule.resultingForce.y, "capsule.resultingForce.y")
        assertEquals(0.0f, capsule.resultingForce.z, "capsule.resultingForce.z")

        // sphere.pos is unchanged
        assertEquals(42.0f, sphere.pos.x, "sphere.pos.x")
        assertEquals(33.0f, sphere.pos.y, "sphere.pos.y")
        assertEquals(24.0f, sphere.pos.z, "sphere.pos.z")

        // capsule.pos is unchanged
        assertEquals(42.0f, capsule.pos.x, "capsule.pos.x")
        assertEquals(33.0f, capsule.pos.y, "capsule.pos.y")
        assertEquals(24.0f, capsule.pos.z, "capsule.pos.z")

        // sphere had no speed
        assertEquals(0.0f, sphere.speed.x, "sphere.speed.x")
        assertEquals(0.0f, sphere.speed.y, "sphere.speed.y")
        assertEquals(0.0f, sphere.speed.z, "sphere.speed.z")

        // capsule had no speed
        assertEquals(0.0f, capsule.speed.x, "capsule.speed.x")
        assertEquals(0.0f, capsule.speed.y, "capsule.speed.y")
        assertEquals(0.0f, capsule.speed.z, "capsule.speed.z")

        // sphere.nextPos was moved. Since the direction was random, we only test if it has changed.
        assertTrue(
            sphere.nextPos.x != sphere.pos.x || sphere.nextPos.y != sphere.pos.y,
            "should have moved sphere.nextPos"
        )

        // Forced separation along a random direction should never involve z.
        assertEquals(0.0f, sphere.nextPos.z - sphere.pos.z, "should not involve z with forced separation")

        // capsule.nextPos was moved, too.
        assertTrue(
            capsule.nextPos.x != capsule.pos.x || capsule.nextPos.y != capsule.pos.y,
            "should have moved capsule.nextPos"
        )
        assertEquals(0.0f, capsule.nextPos.z - capsule.pos.z, "should not involve z with forced separation")

        // The new distance should be in the range we'd expect. Since z was not involved, we can just check XY length.
        val dx = sphere.nextPos.x - capsule.nextPos.x
        val dy = sphere.nextPos.y - capsule.nextPos.y
        val dist = sqrt(dx * dx + dy * dy)
        val rsum = sphere.radius + capsule.radius
        assertLessThan(dist, rsum + 0.03f, "new distance") // 3 cm, not very good
        assertGreaterThan(dist, rsum - 0.03f, "new distance")

        // sphere.nextSpeed is still zero
        assertEquals(0.0f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        // capsule.nextSpeed is still zero
        assertEquals(0.0f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(0.0f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y")
        assertEquals(0.0f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")
    }

    @Test
    fun `should properly handle collisions in situation 2`() {
        val b1 = FixedSphereBody(
            "b1",
            initialPos = Point3f.zero,
            mass = 0.52f,
            gravity = false,
            radius = 0.25f,
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
            pos.set(149.8945f, 292.7302f, 1.25003f)
            speed.set(0.8f, -4.7f, 0.0005f)
            applyForces()
        }

        b2.apply {
            pos.set(150.13234f, 292.2022f, 1.5000299f)
            speed.set(1.111f, -2.233f, 0.16345f)
            applyForces()
        }

        val chk = CheckSphereVsCapsule()
        val sep = SeparateSphereVsCapsule()
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
