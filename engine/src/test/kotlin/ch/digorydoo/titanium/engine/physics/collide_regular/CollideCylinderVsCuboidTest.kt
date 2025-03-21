package ch.digorydoo.titanium.engine.physics.collide_regular

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CollideCylinderVsCuboidTest {
    private fun getCylinder(mass: Float, radius: Float, height: Float) = FixedCylinderBody(
        "cylinder",
        initialPos = Point3f.zero,
        mass = mass,
        radius = radius,
        height = height,
        gravity = false,
        elasticity = 0.8f,
        friction = 0.2f,
    )

    private fun getCuboid(mass: Float, sizeX: Float, sizeY: Float, sizeZ: Float) = FixedCuboidBody(
        "cuboid",
        initialPos = Point3f.zero,
        mass = mass,
        sizeX = sizeX,
        sizeY = sizeY,
        sizeZ = sizeZ,
        gravity = false,
        elasticity = 0.8f,
        friction = 0.2f,
    )

    @Test
    fun `should check collision between centre of cylinder side vs cuboid north face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(9.29999f, 10.0f, 10.0f)
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(10.0f, 0.0f, 0.0f)
            applyForces()
        }

        assertEquals(9.300203f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

        assertEquals(9.6f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between upper area of cylinder side vs cuboid north face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(9.29999f, 10.0f, 9.31f) // z should still collide, but x does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(10.0f, 0.0f, 0.0f)
            applyForces()
        }

        assertEquals(9.300203f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.31f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

        assertEquals(9.6f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.805f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between lower area of cylinder side vs cuboid north face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(9.29999f, 10.0f, 10.69f) // z should still collide, but x does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(10.0f, 0.0f, 0.0f)
            applyForces()
        }

        assertEquals(9.300203f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.69f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

        assertEquals(9.6f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.195f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between centre of cylinder side vs cuboid east face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 10.60001f, 10.0f)
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, -10.0f, 0.0f)
            applyForces()
        }

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.599796f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.3f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between upper area of cylinder side vs cuboid east face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 10.60001f, 9.31f) // z should still collide, but y does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, -10.0f, 0.0f)
            applyForces()
        }

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.599796f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.31f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.3f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.805f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between lower area of cylinder side vs cuboid east face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 10.60001f, 10.69f) // z should still collide, but y does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, -10.0f, 0.0f)
            applyForces()
        }

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.599796f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.69f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.3f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.195f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between centre of cylinder side vs cuboid south face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.70001f, 10.0f, 10.0f)
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(-10.0f, 0.0f, 0.0f)
            applyForces()
        }

        assertEquals(10.699797f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SOUTH_FACE, hit.area2, "hit.area2")

        assertEquals(10.4f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between upper area of cylinder side vs cuboid south face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.70001f, 10.0f, 9.31f) // z should still collide, but x does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(-10.0f, 0.0f, 0.0f)
            applyForces()
        }

        assertEquals(10.699797f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.31f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SOUTH_FACE, hit.area2, "hit.area2")

        assertEquals(10.4f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.805f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between lower area of cylinder side vs cuboid south face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.70001f, 10.0f, 10.69f) // z should still collide, but x does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(-10.0f, 0.0f, 0.0f)
            applyForces()
        }

        assertEquals(10.699797f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.69f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SOUTH_FACE, hit.area2, "hit.area2")

        assertEquals(10.4f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.195f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between centre of cylinder side vs cuboid west face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 9.39999f, 10.0f)
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 10.0f, 0.0f)
            applyForces()
        }

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(9.400203f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.WEST_FACE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(9.7f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between upper area of cylinder side vs cuboid west face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 9.39999f, 9.31f) // z should still collide, but y does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 10.0f, 0.0f)
            applyForces()
        }

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(9.400203f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.31f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.WEST_FACE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(9.7f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.805f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between lower area of cylinder side vs cuboid west face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 9.39999f, 10.69f) // z should still collide, but y does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 10.0f, 0.0f)
            applyForces()
        }

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(9.400203f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.69f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.WEST_FACE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(9.7f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.195f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between cylinder top vs cuboid bottom, XY centre`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 10.0f, 9.29999f)
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 0.0f, 10.0f)
            applyForces()
        }

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.300203f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.TOP, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.8f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between cylinder top vs cuboid bottom, XY north`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(9.31f, 10.0f, 9.29999f) // x should still collide, but z does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 0.0f, 10.0f)
            applyForces()
        }

        assertEquals(9.31f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.300203f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.TOP, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM, hit.area2, "hit.area2")

        assertEquals(9.6f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.8f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between cylinder top vs cuboid bottom, XY east`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 10.59f, 9.29999f) // y should still collide, but z does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 0.0f, 10.0f)
            applyForces()
        }

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.59f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.300203f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.TOP, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.3f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.8f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between cylinder top vs cuboid bottom, XY south`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.69f, 10.0f, 9.29999f) // x should still collide, but z does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 0.0f, 10.0f)
            applyForces()
        }

        assertEquals(10.69f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.300203f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.TOP, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM, hit.area2, "hit.area2")

        assertEquals(10.4f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.8f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between cylinder top vs cuboid bottom, XY west`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 9.41f, 9.29999f) // y should still collide, but z does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 0.0f, 10.0f)
            applyForces()
        }

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(9.41f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.300203f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.TOP, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(9.7f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.8f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between cylinder bottom vs cuboid top, XY centre`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 10.0f, 10.70001f)
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 0.0f, -10.0f)
            applyForces()
        }

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.699797f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.BOTTOM, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.2f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between cylinder bottom vs cuboid top, XY north`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(9.31f, 10.0f, 10.70001f) // x should still collide, but z does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 0.0f, -10.0f)
            applyForces()
        }

        assertEquals(9.31f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.699797f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.BOTTOM, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP, hit.area2, "hit.area2")

        assertEquals(9.6f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.2f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check collision between cylinder bottom vs cuboid top, XY south`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.69f, 10.0f, 10.70001f) // x should still collide, but z does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 0.0f, -10.0f)
            applyForces()
        }

        assertEquals(10.69f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.699797f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit), "should collide")
        assertEquals(HitArea.BOTTOM, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP, hit.area2, "hit.area2")

        assertEquals(10.4f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.2f, hit.hitPt.z, TOLERANCE, "hitPt.z")
    }

    @Test
    fun `should check and bounce cylinder vs cuboid when cylinder is near top NW corner`() {
        val cylinder = getCylinder(mass = 7.0f, radius = 2.0f, height = 3.0f)
        val cuboid = getCuboid(mass = 9.0f, sizeX = 2.0f, sizeY = 3.0f, sizeZ = 4.0f)
        val ck = CollideCylinderVsCuboid()
        val hit = MutableHitResult()

        cylinder.apply {
            pos.set(2.584f, 2.584f, 7.5001f)
            applyForces()
        }
        cuboid.apply {
            pos.set(5.0f, 5.5f, 4.0f)
            applyForces()
        }

        assertFalse(ck.checkAndBounceIfNeeded(cylinder, cuboid, canBounce = false, hit), "should not collide")

        // Add a force to the cylinder, and make sure then length in z is greater than the length in XY
        cylinder.apply {
            addForce(48.0f, 48.0f, -70.0f) // abs(fz) > sqrt(fx*fx + fy*fy) ≈ 67.89
            applyForces()
        }

        // The bodies should now collide, and since fz was greater, it should take this as a hit to the bottom/top.
        assertTrue(ck.checkAndBounceIfNeeded(cylinder, cuboid, canBounce = false, hit), "should collide")
        assertEquals(HitArea.BOTTOM, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP, hit.area2, "hit.area2")

        // Reset, and add a force to the cylinder, this time making sure than fz is smaller
        cylinder.apply {
            stopAllMotion()
            addForce(48.0f, 48.0f, -20.0f)
            applyForces()
        }
        cuboid.stopAllMotion()

        // The bodies should collide, and since fz was smaller, it should take this as a hit to the cuboid side.
        assertTrue(ck.checkAndBounceIfNeeded(cylinder, cuboid, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2") // could also have been WEST_FACE

        // Reset, and try the first check again at an initial position that already has a z overlap.
        cylinder.apply {
            stopAllMotion()
            pos.set(2.584f, 2.584f, 7.0f)
            applyForces()
        }
        cuboid.stopAllMotion()
        assertFalse(ck.checkAndBounceIfNeeded(cylinder, cuboid, canBounce = false, hit), "should not collide")
        cylinder.apply {
            addForce(48.0f, 48.0f, -70.0f) // abs(fz) > sqrt(fx*fx + fy*fy) ≈ 67.89
            applyForces()
        }

        // The bodies should collide, but now that we have an initial z overlap, it can no longer be bottom/top
        assertTrue(ck.checkAndBounceIfNeeded(cylinder, cuboid, canBounce = false, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

        assertEquals(4.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(4.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(5.7486115f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(2.5859048f, cylinder.nextPos.x, TOLERANCE, "cylinder.nextPos.x")
        assertEquals(2.5859048f, cylinder.nextPos.y, TOLERANCE, "cylinder.nextPos.y")
        assertEquals(6.9972224f, cylinder.nextPos.z, TOLERANCE, "cylinder.nextPos.z")

        assertEquals(5.0f, cuboid.nextPos.x, "cuboid.nextPos.x") // unchanged
        assertEquals(5.5f, cuboid.nextPos.y, "cuboid.nextPos.y") // unchanged
        assertEquals(4.0f, cuboid.nextPos.z, "cuboid.nextPos.z") // unchanged

        assertEquals(0.11428572f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(0.11428572f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(-0.16666667f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")

        assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x") // unchanged
        assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y") // unchanged
        assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z") // unchanged

        // Bounce the bodies
        // FIXME bounce not implemented
        // assertTrue(ck.checkAndBounceIfNeeded(cylinder, cuboid, canBounce = true, hit), "should collide")

        // The bodies should no longer collide after bounce
        // assertFalse(ck.checkAndBounceIfNeeded(cylinder, cuboid, canBounce = true, hit), "should no longer collide")
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
