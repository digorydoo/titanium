package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.physics.FixedPlaneBody
import ch.digorydoo.titanium.engine.physics.FixedSphereBody
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CollideSphereVsPlaneTest {
    @Test
    fun `should not collide when the plane is perpendicular to the x-axis, and the sphere is far enough`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.34f, 10.0f, 10.0f)
        val n = MutablePoint3f(1.0f, 0.1f, 0.1f).apply { normalize() }
        val b2 = FixedPlaneBody("b2", p2, mass = 10.0f, gravity = false, normal = n, elasticity = 0.5f)
        b2.nextPos.set(p2)

        val ck = CollideSphereVsPlane()
        val hitPt = MutablePoint3f()
        assertFalse(ck.checkNextPos(b1, b2, hitPt))
    }

    @Test
    fun `should not collide when the plane is perpendicular to the y-axis, and the sphere is far enough`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.34f, 10.0f)
        val n = MutablePoint3f(0.1f, 1.0f, 0.1f).apply { normalize() }
        val b2 = FixedPlaneBody("b2", p2, mass = 10.0f, gravity = false, normal = n, elasticity = 0.5f)
        b2.nextPos.set(p2)

        val ck = CollideSphereVsPlane()
        val hitPt = MutablePoint3f()
        assertFalse(ck.checkNextPos(b1, b2, hitPt))
    }

    @Test
    fun `should not collide when the plane is perpendicular to the z-axis, and the sphere is far enough`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.0f, 10.61f)
        val n = MutablePoint3f(0.1f, 0.1f, 1.0f).apply { normalize() }
        val b2 = FixedPlaneBody("b2", p2, mass = 10.0f, gravity = false, normal = n, elasticity = 0.5f)
        b2.nextPos.set(p2)

        val ck = CollideSphereVsPlane()
        val hitPt = MutablePoint3f()
        assertFalse(ck.checkNextPos(b1, b2, hitPt))
    }

    @Test
    fun `should collide when the plane is perpendicular to the x-axis, and the sphere is too close`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.33f, 10.0f, 10.0f)
        val n = MutablePoint3f(1.0f, 0.1f, 0.1f).apply { normalize() }
        val b2 = FixedPlaneBody("b2", p2, mass = 10.0f, gravity = false, normal = n, elasticity = 0.5f)
        b2.nextPos.set(p2)

        val ck = CollideSphereVsPlane()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))
        assertEquals(10.294118f, hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.029411f, hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.3294115f, hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should give the same result when the plane is perpendicular to the x-axis, and the normal is flipped`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.33f, 10.0f, 10.0f)
        val n = MutablePoint3f(-1.0f, -0.1f, -0.1f).apply { normalize() }
        val b2 = FixedPlaneBody("b2", p2, mass = 10.0f, gravity = false, normal = n, elasticity = 0.5f)
        b2.nextPos.set(p2)

        val ck = CollideSphereVsPlane()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))
        assertEquals(10.294118f, hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.029411f, hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.3294115f, hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should collide when the plane is perpendicular to the y-axis, and the sphere is too close`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.33f, 10.0f)
        val n = MutablePoint3f(0.1f, 1.0f, 0.1f).apply { normalize() }
        val b2 = FixedPlaneBody("b2", p2, mass = 10.0f, gravity = false, normal = n, elasticity = 0.5f)
        b2.nextPos.set(p2)

        val ck = CollideSphereVsPlane()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))
        assertEquals(10.029411f, hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.294118f, hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.3294115f, hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should give the same result when the plane is perpendicular to the y-axis, and normal is flipped`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.33f, 10.0f)
        val n = MutablePoint3f(-0.1f, -1.0f, -0.1f).apply { normalize() }
        val b2 = FixedPlaneBody("b2", p2, mass = 10.0f, gravity = false, normal = n, elasticity = 0.5f)
        b2.nextPos.set(p2)

        val ck = CollideSphereVsPlane()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))
        assertEquals(10.029411f, hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.294118f, hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.3294115f, hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should collide when the plane is perpendicular to the z-axis, and the sphere is too close`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.0f, 10.6f)
        val n = MutablePoint3f(0.1f, 0.1f, 1.0f).apply { normalize() }
        val b2 = FixedPlaneBody("b2", p2, mass = 10.0f, gravity = false, normal = n, elasticity = 0.5f)
        b2.nextPos.set(p2)

        val ck = CollideSphereVsPlane()
        val hitPt = MutablePoint3f()

        assertTrue(ck.checkNextPos(b1, b2, hitPt))
        assertEquals(10.029411f, hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.029411f, hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.594118f, hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should give the same result when the plane is perpendicular to the z-axis, and the normal is flipped`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.0f, 10.6f)
        val n = MutablePoint3f(-0.1f, -0.1f, -1.0f).apply { normalize() }
        val b2 = FixedPlaneBody("b2", p2, mass = 10.0f, gravity = false, normal = n, elasticity = 0.5f)
        b2.nextPos.set(p2)

        val ck = CollideSphereVsPlane()
        val hitPt = MutablePoint3f()

        assertTrue(ck.checkNextPos(b1, b2, hitPt))
        assertEquals(10.029411f, hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.029411f, hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.594118f, hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should correctly bounce the sphere off the plane when the plane is perpendicular to the x-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f
        )
        b1.speed.set(1.0f, 0.0f, 0.0f)
        b1.applyForces() // updates nextPos and nextSpeed

        val p2 = MutablePoint3f(10.34f, 10.0f, 10.0f)
        val n = MutablePoint3f(1.0f, 0.1f, 0.1f).apply { normalize() }
        val b2 = FixedPlaneBody("b2", p2, mass = 10.0f, gravity = false, normal = n, elasticity = 0.5f)
        b2.applyForces() // updates nextPos and nextSpeed

        val ck = CollideSphereVsPlane()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))

        ck.bounce(b1, b2)

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, TOLERANCE, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, TOLERANCE, "b1.pos.y")
        assertEquals(10.0f, b1.pos.z, TOLERANCE, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.34f, b2.pos.x, TOLERANCE, "b2.pos.x")
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
        assertEquals(10.007204f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(9.999054f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.999054f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos is unchanged
        assertEquals(10.34f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified: still moves in the same direction, but slower
        assertEquals(0.38725492f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.061274506f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.061274506f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified: moves away from b1, faster than b1
        assertEquals(0.6127451f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.061274506f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.061274506f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should correctly bounce when the plane is perpendicular to the x-axis, and the normal is flipped`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f
        )
        b1.speed.set(1.0f, 0.0f, 0.0f)
        b1.applyForces() // updates nextPos and nextSpeed

        val p2 = MutablePoint3f(10.34f, 10.0f, 10.0f)
        val n = MutablePoint3f(-1.0f, -0.1f, -0.1f).apply { normalize() }
        val b2 = FixedPlaneBody("b2", p2, mass = 10.0f, gravity = false, normal = n, elasticity = 0.5f)
        b2.applyForces() // updates nextPos and nextSpeed

        val ck = CollideSphereVsPlane()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))

        ck.bounce(b1, b2)

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, TOLERANCE, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, TOLERANCE, "b1.pos.y")
        assertEquals(10.0f, b1.pos.z, TOLERANCE, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.34f, b2.pos.x, TOLERANCE, "b2.pos.x")
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
        assertEquals(10.007204f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(9.999054f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.999054f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos is unchanged
        assertEquals(10.34f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified: still moves in the same direction, but slower
        assertEquals(0.38725492f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.061274506f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.061274506f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified: moves away from b1, faster than b1
        assertEquals(0.6127451f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.061274506f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.061274506f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should correctly bounce the sphere off the plane when the plane is perpendicular to the y-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f
        )
        b1.speed.set(0.0f, 1.0f, 0.0f)
        b1.applyForces() // updates nextPos and nextSpeed

        val p2 = MutablePoint3f(10.0f, 10.34f, 10.0f)
        val n = MutablePoint3f(0.1f, 1.0f, 0.1f).apply { normalize() }
        val b2 = FixedPlaneBody("b2", p2, mass = 10.0f, gravity = false, normal = n, elasticity = 0.5f)
        b2.applyForces() // updates nextPos and nextSpeed

        val ck = CollideSphereVsPlane()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))

        ck.bounce(b1, b2)

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, TOLERANCE, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, TOLERANCE, "b1.pos.y")
        assertEquals(10.0f, b1.pos.z, TOLERANCE, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.0f, b2.pos.x, TOLERANCE, "b2.pos.x")
        assertEquals(10.34f, b2.pos.y, TOLERANCE, "b2.pos.y")
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
        assertEquals(9.999054f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.007204f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.999054f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos is unchanged
        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.34f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified: still moves in the same direction, but slower
        assertEquals(-0.061274506f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.38725492f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.061274506f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified: moves away from b1, faster than b1
        assertEquals(0.061274506f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.6127451f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.061274506f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should correctly bounce the sphere off the plane when the plane is perpendicular to the z-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedSphereBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.3f,
            zOffset = 0.3f,
            elasticity = 0.5f
        )
        b1.speed.set(0.0f, 0.0f, 1.0f)
        b1.applyForces() // updates nextPos and nextSpeed

        val p2 = MutablePoint3f(10.0f, 10.0f, 10.61f)
        val n = MutablePoint3f(0.1f, 0.1f, 1.0f).apply { normalize() }
        val b2 = FixedPlaneBody("b2", p2, mass = 10.0f, gravity = false, normal = n, elasticity = 0.5f)
        b2.applyForces() // updates nextPos and nextSpeed

        val ck = CollideSphereVsPlane()
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
        assertEquals(9.999054f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(9.999054f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.007203f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos is unchanged
        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.61f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified: still moves in the same direction, but slower
        assertEquals(-0.061274506f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.061274506f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.38725492f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified: moves away from b1, faster than b1
        assertEquals(0.061274506f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.061274506f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.6127451f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    companion object {
        private const val TOLERANCE = 0.000001f
    }
}
