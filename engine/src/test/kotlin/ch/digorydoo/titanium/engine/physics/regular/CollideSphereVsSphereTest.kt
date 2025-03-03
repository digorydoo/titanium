package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.physics.FixedSphereBody
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CollideSphereVsSphereTest {
    @Test
    fun `should not collide when the two spheres are far enough along the x-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.51f, 10.0f, 10.0f)
        val b2 = FixedSphereBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.2f,
            zOffset = 0.2f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.nextPos.set(p2)

        val ck = CollideSphereVsSphere()
        val hitPt = MutablePoint3f()
        assertFalse(ck.checkNextPos(b1, b2, hitPt))
    }

    @Test
    fun `should not collide when the two spheres are far enough along the y-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.51f, 10.0f)
        val b2 = FixedSphereBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.2f,
            zOffset = 0.2f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.nextPos.set(p2)

        val ck = CollideSphereVsSphere()
        val hitPt = MutablePoint3f()
        assertFalse(ck.checkNextPos(b1, b2, hitPt))
    }

    @Test
    fun `should not collide when the two spheres are far enough along the z-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.0f, 10.61f)
        val b2 = FixedSphereBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.2f,
            zOffset = 0.2f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.nextPos.set(p2)

        val ck = CollideSphereVsSphere()
        val hitPt = MutablePoint3f()
        assertFalse(ck.checkNextPos(b1, b2, hitPt))
    }

    @Test
    fun `should collide when the two spheres are too close, almost separated along the x-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.48f, 10.0f, 10.0f)
        val b2 = FixedSphereBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.2f,
            zOffset = 0.2f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.nextPos.set(p2)

        val ck = CollideSphereVsSphere()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))
        assertEquals(10.2936945f, hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.238813f, hitPt.z, TOLERANCE, "hitPt.z") // between sphere 1's and sphere 2's centre point
    }

    @Test
    fun `should collide when the two spheres are too close, almost separated along the y-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.48f, 10.0f)
        val b2 = FixedSphereBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.2f,
            zOffset = 0.2f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.nextPos.set(p2)

        val ck = CollideSphereVsSphere()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))
        assertEquals(10.0f, hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.2936945f, hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.238813f, hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should collide when the two spheres are too close, almost separated along the z-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.0f, 10.59f)
        val b2 = FixedSphereBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.2f,
            zOffset = 0.2f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.nextPos.set(p2)

        val ck = CollideSphereVsSphere()
        val hitPt = MutablePoint3f()

        assertTrue(ck.checkNextPos(b1, b2, hitPt))
        assertEquals(10.0f, hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.6f, hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should correctly bounce the sphere off the other when moving along the x-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.speed.set(1.0f, 0.0f, 0.0f)
        b1.applyForces() // updates nextPos and nextSpeed

        val p2 = MutablePoint3f(10.50f, 10.0f, 10.0f)
        val b2 = FixedSphereBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.2f,
            zOffset = 0.2f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.applyForces() // updates nextPos and nextSpeed

        val ck = CollideSphereVsSphere()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))

        ck.bounce(b1, b2)

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, TOLERANCE, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, TOLERANCE, "b1.pos.y")
        assertEquals(10.0f, b1.pos.z, TOLERANCE, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.5f, b2.pos.x, TOLERANCE, "b2.pos.x")
        assertEquals(10.0f, b2.pos.y, TOLERANCE, "b2.pos.y")
        assertEquals(10.0f, b2.pos.z, TOLERANCE, "b2.pos.z")

        // b1.speed is unchanged
        assertEquals(1.0f, b1.speed.x, TOLERANCE, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, TOLERANCE, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, TOLERANCE, "b1.speed.z")

        // b2.speed is unchanged
        assertEquals(0.0f, b2.speed.x, TOLERANCE, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, TOLERANCE, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, TOLERANCE, "b2.speed.z")

        // b1.nextPos has been moved
        assertEquals(10.010368f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.001303f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // actually slightly inaccurate

        // b2.nextPos is unchanged
        assertEquals(10.5f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified: still moves in the same direction, but slower, with a slight jump
        assertEquals(0.3801311f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.024800457f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z") // because other sphere is smaller

        // b2.nextSpeed has been modified: moves away from b1, faster than b1
        assertEquals(0.6198689f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(-0.024800472f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should correctly bounce the sphere off the other when moving along the y-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.speed.set(0.0f, 1.0f, 0.0f)
        b1.applyForces() // updates nextPos and nextSpeed

        val p2 = MutablePoint3f(10.0f, 10.50f, 10.0f)
        val b2 = FixedSphereBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.2f,
            zOffset = 0.2f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.applyForces() // updates nextPos and nextSpeed

        val ck = CollideSphereVsSphere()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))

        ck.bounce(b1, b2)

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, TOLERANCE, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, TOLERANCE, "b1.pos.y")
        assertEquals(10.0f, b1.pos.z, TOLERANCE, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.0f, b2.pos.x, TOLERANCE, "b2.pos.x")
        assertEquals(10.5f, b2.pos.y, TOLERANCE, "b2.pos.y")
        assertEquals(10.0f, b2.pos.z, TOLERANCE, "b2.pos.z")

        // b1.speed is unchanged
        assertEquals(0.0f, b1.speed.x, TOLERANCE, "b1.speed.x")
        assertEquals(1.0f, b1.speed.y, TOLERANCE, "b1.speed.y")
        assertEquals(0.0f, b1.speed.z, TOLERANCE, "b1.speed.z")

        // b2.speed is unchanged
        assertEquals(0.0f, b2.speed.x, TOLERANCE, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, TOLERANCE, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, TOLERANCE, "b2.speed.z")

        // b1.nextPos has been moved
        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.010368f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.001303f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // actually slightly inaccurate

        // b2.nextPos is unchanged
        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.5f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified: still moves in the same direction, but slower, with a slight jump
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.3801311f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.024800457f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z") // because other sphere is smaller

        // b2.nextSpeed has been modified: moves away from b1, faster than b1
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.6198689f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(-0.024800472f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should correctly bounce the sphere off the other when moving along the z-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.speed.set(0.0f, 0.0f, 1.0f)
        b1.applyForces() // updates nextPos and nextSpeed

        val p2 = MutablePoint3f(10.0f, 10.0f, 10.61f)
        val b2 = FixedSphereBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.2f,
            zOffset = 0.2f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.applyForces() // updates nextPos and nextSpeed

        val ck = CollideSphereVsSphere()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))

        ck.bounce(b1, b2)

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, TOLERANCE, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, TOLERANCE, "b1.pos.y")
        assertEquals(10.0f, b1.pos.z, TOLERANCE, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.0f, b2.pos.x, TOLERANCE, "b2.pos.x")
        assertEquals(10.0f, b2.pos.y, TOLERANCE, "b2.pos.y")
        assertEquals(10.61f, b2.pos.z, TOLERANCE, "b2.pos.z")

        // b1.speed is unchanged
        assertEquals(0.0f, b1.speed.x, TOLERANCE, "b1.speed.x")
        assertEquals(0.0f, b1.speed.y, TOLERANCE, "b1.speed.y")
        assertEquals(1.0f, b1.speed.z, TOLERANCE, "b1.speed.z")

        // b2.speed is unchanged
        assertEquals(0.0f, b2.speed.x, TOLERANCE, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, TOLERANCE, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, TOLERANCE, "b2.speed.z")

        // b1.nextPos has been moved
        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.009998f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos is unchanged
        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.61f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified: still moves in the same direction, but slower
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.375f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified: moves away from b1, faster than b1
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.625f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    companion object {
        private const val TOLERANCE = 0.000001f
    }
}
