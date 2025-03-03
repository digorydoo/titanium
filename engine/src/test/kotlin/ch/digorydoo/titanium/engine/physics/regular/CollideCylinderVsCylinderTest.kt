package ch.digorydoo.titanium.engine.physics.regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.physics.FixedCylinderBody
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CollideCylinderVsCylinderTest {
    @Test
    fun `should not collide when the two cylinders are far enough along the x-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedCylinderBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 1.0f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.51f, 10.0f, 10.0f)
        val b2 = FixedCylinderBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.nextPos.set(p2)

        val ck = CollideCylinderVsCylinder()
        val hitPt = MutablePoint3f()
        assertFalse(ck.checkNextPos(b1, b2, hitPt))
    }

    @Test
    fun `should not collide when the two cylinders are far enough along the y-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedCylinderBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 1.0f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.51f, 10.0f)
        val b2 = FixedCylinderBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.nextPos.set(p2)

        val ck = CollideCylinderVsCylinder()
        val hitPt = MutablePoint3f()
        assertFalse(ck.checkNextPos(b1, b2, hitPt))
    }

    @Test
    fun `should not collide when the two cylinders are far enough along the z-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedCylinderBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 1.0f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.0f, 11.01f)
        val b2 = FixedCylinderBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.nextPos.set(p2)

        val ck = CollideCylinderVsCylinder()
        val hitPt = MutablePoint3f()
        assertFalse(ck.checkNextPos(b1, b2, hitPt))
    }

    @Test
    fun `should collide when the two cylinders are too close, almost separated along the x-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedCylinderBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 1.0f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.49f, 10.0f, 10.0f)
        val b2 = FixedCylinderBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.nextPos.set(p2)

        val ck = CollideCylinderVsCylinder()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))
        assertEquals(10.25f, hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.5f, hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should collide when the two cylinders are too close, almost separated along the y-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedCylinderBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 1.0f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.49f, 10.0f)
        val b2 = FixedCylinderBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.nextPos.set(p2)

        val ck = CollideCylinderVsCylinder()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))
        assertEquals(10.0f, hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.25f, hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.5f, hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should collide when the two cylinders are too close, almost separated along the z-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedCylinderBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 1.0f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.nextPos.set(p1)

        val p2 = MutablePoint3f(10.0f, 10.0f, 10.99f)
        val b2 = FixedCylinderBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.nextPos.set(p2)

        val ck = CollideCylinderVsCylinder()
        val hitPt = MutablePoint3f()

        assertTrue(ck.checkNextPos(b1, b2, hitPt))
        assertEquals(10.0f, hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(11.0f, hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should correctly bounce one cylinder off the other when moving along the x-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedCylinderBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 1.0f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.speed.set(1.0f, 0.0f, 0.0f)
        b1.applyForces() // updates nextPos and nextSpeed

        val p2 = MutablePoint3f(10.51f, 10.0f, 10.0f)
        val b2 = FixedCylinderBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.applyForces() // updates nextPos and nextSpeed

        val ck = CollideCylinderVsCylinder()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))

        ck.bounce(b1, b2)

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, TOLERANCE, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, TOLERANCE, "b1.pos.y")
        assertEquals(10.0f, b1.pos.z, TOLERANCE, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.51f, b2.pos.x, TOLERANCE, "b2.pos.x")
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
        assertEquals(10.009998f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos is unchanged
        assertEquals(10.51f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified: still moves in the same direction, but slower
        assertEquals(0.375f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified: moves away from b1, faster than b1
        assertEquals(0.625f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should correctly bounce one cylinder off the other when moving along the y-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedCylinderBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 1.0f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.speed.set(0.0f, 1.0f, 0.0f)
        b1.applyForces() // updates nextPos and nextSpeed

        val p2 = MutablePoint3f(10.0f, 10.51f, 10.0f)
        val b2 = FixedCylinderBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.applyForces() // updates nextPos and nextSpeed

        val ck = CollideCylinderVsCylinder()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))

        ck.bounce(b1, b2)

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, TOLERANCE, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, TOLERANCE, "b1.pos.y")
        assertEquals(10.0f, b1.pos.z, TOLERANCE, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.0f, b2.pos.x, TOLERANCE, "b2.pos.x")
        assertEquals(10.51f, b2.pos.y, TOLERANCE, "b2.pos.y")
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
        assertEquals(10.009998f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos is unchanged
        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.51f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified: still moves in the same direction, but slower
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.375f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified: moves away from b1, faster than b1
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.625f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should correctly bounce one cylinder off the other when moving along the z-axis`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedCylinderBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 1.0f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.speed.set(0.0f, 0.0f, 1.0f)
        b1.applyForces() // updates nextPos and nextSpeed

        val p2 = MutablePoint3f(10.0f, 10.0f, 11.01f)
        val b2 = FixedCylinderBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.applyForces() // updates nextPos and nextSpeed

        val ck = CollideCylinderVsCylinder()
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
        assertEquals(11.01f, b2.pos.z, TOLERANCE, "b2.pos.z")

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
        assertEquals(11.01f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified: still moves in the same direction, but slower
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.375f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified: moves away from b1, faster than b1
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.625f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should correctly bounce one cylinder off the other when moving in an arbitrary direction`() {
        val p1 = MutablePoint3f(10.0f, 10.0f, 10.0f)
        val b1 = FixedCylinderBody(
            "b1",
            p1,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 1.0f,
            zOffset = 0.5f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b1.speed.set(0.0f, 0.6f, 0.6f)
        b1.applyForces() // updates nextPos and nextSpeed

        val p2 = MutablePoint3f(10.0f, 10.51f, 10.51f)
        val b2 = FixedCylinderBody(
            "b2",
            p2,
            mass = 10.0f,
            gravity = false,
            radius = 0.25f,
            height = 0.5f,
            zOffset = 0.25f,
            elasticity = 0.5f,
            friction = 0.2f,
        )
        b2.applyForces() // updates nextPos and nextSpeed

        val ck = CollideCylinderVsCylinder()
        val hitPt = MutablePoint3f()
        assertTrue(ck.checkNextPos(b1, b2, hitPt))

        ck.bounce(b1, b2)

        // b1.pos is unchanged
        assertEquals(10.0f, b1.pos.x, TOLERANCE, "b1.pos.x")
        assertEquals(10.0f, b1.pos.y, TOLERANCE, "b1.pos.y")
        assertEquals(10.0f, b1.pos.z, TOLERANCE, "b1.pos.z")

        // b2.pos is unchanged
        assertEquals(10.0f, b2.pos.x, TOLERANCE, "b2.pos.x")
        assertEquals(10.51f, b2.pos.y, TOLERANCE, "b2.pos.y")
        assertEquals(10.51f, b2.pos.z, TOLERANCE, "b2.pos.z")

        // b1.speed is unchanged
        assertEquals(0.0f, b1.speed.x, TOLERANCE, "b1.speed.x")
        assertEquals(0.6f, b1.speed.y, TOLERANCE, "b1.speed.y")
        assertEquals(0.6f, b1.speed.z, TOLERANCE, "b1.speed.z")

        // b2.speed is unchanged
        assertEquals(0.0f, b2.speed.x, TOLERANCE, "b2.speed.x")
        assertEquals(0.0f, b2.speed.y, TOLERANCE, "b2.speed.y")
        assertEquals(0.0f, b2.speed.z, TOLERANCE, "b2.speed.z")

        // b1.nextPos has been moved
        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.009998f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.01f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2.nextPos is unchanged
        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.51f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.51f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // b1.nextSpeed has been modified: still moves in the same direction, but slower
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.225f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.49200004f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2.nextSpeed has been modified: moves away from b1, faster than b1
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.375f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.10799999f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    companion object {
        private const val TOLERANCE = 0.000001f
    }
}
