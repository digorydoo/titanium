package ch.digorydoo.titanium.engine.physics.strategy

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCapsuleBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_cuboid.BounceCapsuleVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_cuboid.CheckCapsuleVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.capsule_vs_cuboid.SeparateCapsuleVsCuboid
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CollideCapsuleVsCuboidTest {
    private fun getCapsule(mass: Float, radius: Float, height: Float, friction: Float = 0.5f) =
        FixedCapsuleBody(
            "capsule",
            initialPos = Point3f.zero,
            mass = mass,
            radius = radius,
            height = height,
            gravity = false,
            elasticity = 0.8f,
            friction = friction,
        )

    private fun getCuboid(mass: Float, sizeX: Float, sizeY: Float, sizeZ: Float, friction: Float = 0.72f) =
        FixedCuboidBody(
            "cuboid",
            initialPos = Point3f.zero,
            mass = mass,
            sizeX = sizeX,
            sizeY = sizeY,
            sizeZ = sizeZ,
            gravity = false,
            elasticity = 0.8f,
            friction = friction,
        )

    @Test
    fun `should check collision between z-centre of capsule side vs cuboid north face`() {
        val b1 = getCapsule(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(9.29999f, 10.0f, 10.0f)
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(10.0f, 0.0f, 0.0f)
            applyForces()
        }

        assertEquals(9.300203f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertEquals(0.012820514f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")

        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

        assertEquals(9.6f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(1.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(9.3000765f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertEquals(10.000127f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // nextSpeed should still be the previous value
        assertEquals(0.012820514f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        bnc.bounce(b1, b2, hit)

        // Now nextSpeed should be the speed after bounce
        assertEquals(0.0031837607f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(0.01138889f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

    }

    @Test
    fun `should check collision between capsule top sphere and cuboid bottom north edge`() {
        val b1 = getCapsule(mass = 4.2f, radius = 1.5f, height = 5.0f)
        val b2 = getCuboid(mass = 6.0f, sizeX = 4.0f, sizeY = 4.0f, sizeZ = 4.0f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(4.8819f, 8.0f, 4.0f)
            applyForces()
        }
        b2.apply {
            pos.set(8.0f, 8.0f, 8.0f)
            applyForces()
        }

        // Bodies should not collide initially
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(3.0f, 0.0f, 0.0f)
            applyForces()
        }

        // b1 got a speed from its force
        assertEquals(0.011904763f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2 did not get any speed
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1 was moved along its speed
        assertEquals(4.882098f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(8.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(4.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2 is still where it was
        assertEquals(8.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(8.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(8.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // Bodies should now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.TOP_SPHERE, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM_FACE, hit.area2, "hit.area2") // not NORTH_FACE since it was the edge

        assertEquals(6.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(8.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(6.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.74531686f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.6667105f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")

        // b1 should have been moved away from b2
        assertEquals(4.882043f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x") // slightly towards orig pos
        assertEquals(8.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(3.9999504f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // slightly away from b2

        // b2 should have been moved away from b1
        assertEquals(8.000055f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x") // slightly away from b1
        assertEquals(8.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(8.00005f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // slightly away from b1

        bnc.bounce(b1, b2, hit)

        assertEquals(0.0042723864f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x") // slowed down a bit
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.0043064035f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z") // away from b2

        assertEquals(0.0053426633f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x") // got about half b1's original vx
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.003014482f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z") // away from b1
    }

    @Test
    fun `should check collision between capsule bottom sphere and cuboid bottom north edge`() {
        val b1 = getCapsule(mass = 4.2f, radius = 1.5f, height = 5.0f)
        val b2 = getCuboid(mass = 6.0f, sizeX = 4.0f, sizeY = 4.0f, sizeZ = 4.0f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(4.8819f, 8.0f, 6.0f)
            applyForces()
        }
        b2.apply {
            pos.set(8.0f, 8.0f, 2.0f)
            applyForces()
        }

        // Bodies should not collide initially
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(3.0f, 0.0f, 0.0f)
            applyForces()
        }

        // b1 got a speed from its force
        assertEquals(0.011904763f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2 did not get any speed
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1 was moved along its speed
        assertEquals(4.882098f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(8.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(6.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2 is still where it was
        assertEquals(8.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(8.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(2.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // Bodies should now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.BOTTOM_SPHERE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2") // not NORTH_FACE since it was the edge

        assertEquals(6.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(8.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(4.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.74531686f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-0.6667105f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")

        // b1 should have been moved away from b2
        assertEquals(4.882043f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x") // slightly towards orig pos
        assertEquals(8.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(6.0000496f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // slightly away from b2

        // b2 should have been moved away from b1
        assertEquals(8.000055f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x") // slightly away from b1
        assertEquals(8.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(1.9999505f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // slightly away from b1

        bnc.bounce(b1, b2, hit)

        assertEquals(0.0042723864f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x") // slowed down a bit
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0043064035f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z") // away from b2

        assertEquals(0.0053426633f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x") // got about half b1's original vx
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(-0.003014482f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z") // away from b1
    }

    @Test
    fun `should check collision between z-centre of capsule side vs cuboid east face`() {
        val b1 = getCapsule(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 10.60001f, 10.0f)
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(0.0f, -10.0f, 0.0f)
            applyForces()
        }

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.599796f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.3f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-1.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.599923f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(9.999873f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.0031837607f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(-0.01138889f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should check collision between capsule top sphere and cuboid bottom east edge`() {
        val b1 = getCapsule(mass = 4.2f, radius = 1.5f, height = 5.0f)
        val b2 = getCuboid(mass = 6.0f, sizeX = 4.0f, sizeY = 4.0f, sizeZ = 4.0f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(5.0f, 8.1181f, 4.0f)
            applyForces()
        }
        b2.apply {
            pos.set(5.0f, 5.0f, 8.0f)
            applyForces()
        }

        // Bodies should not collide initially
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(0.0f, -3.0f, 0.0f)
            applyForces()
        }

        // b1 got a speed from its force
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.011904763f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2 did not get any speed
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1 was moved along its speed
        assertEquals(5.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(8.117902f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(4.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2 is still where it was
        assertEquals(5.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(5.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(8.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // Bodies should now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.TOP_SPHERE, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM_FACE, hit.area2, "hit.area2") // not EAST_FACE since it was the edge

        assertEquals(5.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(6.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-0.74531686f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.6667105f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")

        // b1 should have been moved away from b2
        assertEquals(5.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(8.117957f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // slightly towards orig pos
        assertEquals(3.9999504f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // slightly away from b2

        // b2 should have been moved away from b1
        assertEquals(5.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(4.9999447f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y") // slightly away from b1
        assertEquals(8.00005f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // slightly away from b1

        bnc.bounce(b1, b2, hit)

        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.0042723864f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y") // slowed down a bit
        assertEquals(-0.0043064035f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z") // away from b2

        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(-0.0053426633f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y") // got about half b1's original vy
        assertEquals(0.003014482f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z") // away from b1
    }

    @Test
    fun `should check collision between capsule bottom sphere and cuboid bottom east edge`() {
        val b1 = getCapsule(mass = 4.2f, radius = 1.5f, height = 5.0f)
        val b2 = getCuboid(mass = 6.0f, sizeX = 4.0f, sizeY = 4.0f, sizeZ = 4.0f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(5.0f, 8.1181f, 6.0f)
            applyForces()
        }
        b2.apply {
            pos.set(5.0f, 5.0f, 2.0f)
            applyForces()
        }

        // Bodies should not collide initially
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(0.0f, -3.0f, 0.0f)
            applyForces()
        }

        // b1 got a speed from its force
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.011904763f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2 did not get any speed
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1 was moved along its speed
        assertEquals(5.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(8.117902f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(6.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2 is still where it was
        assertEquals(5.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(5.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(2.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // Bodies should now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.BOTTOM_SPHERE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2") // not EAST_FACE since it was the edge

        assertEquals(5.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(4.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-0.74531686f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-0.6667105f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")

        // b1 should have been moved away from b2
        assertEquals(5.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(8.117957f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // slightly towards orig pos
        assertEquals(6.0000496f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // slightly away from b2

        // b2 should have been moved away from b1
        assertEquals(5.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(4.9999447f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y") // slightly away from b1
        assertEquals(1.9999505f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // slightly away from b1

        bnc.bounce(b1, b2, hit)

        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.0042723864f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y") // slowed down a bit
        assertEquals(0.0043064035f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z") // away from b2

        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(-0.0053426633f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y") // got about half b1's original vy
        assertEquals(-0.003014482f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z") // away from b1
    }

    @Test
    fun `should check collision between z-centre of capsule side vs cuboid south face`() {
        val b1 = getCapsule(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.70001f, 10.0f, 10.0f)
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(-10.0f, 0.0f, 0.0f)
            applyForces()
        }

        assertEquals(10.699797f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SOUTH_FACE, hit.area2, "hit.area2")

        assertEquals(10.4f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(-1.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(10.6999235f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertEquals(9.999873f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        assertEquals(-0.0031837607f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(-0.01138889f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should check collision between capsule top sphere and cuboid bottom south edge`() {
        val b1 = getCapsule(mass = 4.2f, radius = 1.5f, height = 5.0f)
        val b2 = getCuboid(mass = 6.0f, sizeX = 4.0f, sizeY = 4.0f, sizeZ = 4.0f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(5.1181f, 8.0f, 4.0f)
            applyForces()
        }
        b2.apply {
            pos.set(2.0f, 8.0f, 8.0f)
            applyForces()
        }

        // Bodies should not collide initially
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(-3.0f, 0.0f, 0.0f)
            applyForces()
        }

        // b1 got a speed from its force
        assertEquals(-0.011904763f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2 did not get any speed
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1 was moved along its speed
        assertEquals(5.117902f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(8.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(4.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2 is still where it was
        assertEquals(2.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(8.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(8.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // Bodies should now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.TOP_SPHERE, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM_FACE, hit.area2, "hit.area2") // not SOUTH_FACE since it was the edge

        assertEquals(4.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(8.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(6.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(-0.74531686f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.6667105f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")

        // b1 should have been moved away from b2
        assertEquals(5.117957f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x") // slightly towards orig pos
        assertEquals(8.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(3.9999504f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // slightly away from b2

        // b2 should have been moved away from b1
        assertEquals(1.9999447f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x") // slightly away from b1
        assertEquals(8.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(8.00005f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // slightly away from b1

        bnc.bounce(b1, b2, hit)

        assertEquals(-0.0042723864f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x") // slowed down a bit
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.0043064035f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z") // away from b2

        assertEquals(-0.0053426633f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x") // got about half b1's original vx
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.003014482f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z") // away from b1
    }

    @Test
    fun `should check collision between capsule bottom sphere and cuboid top south edge`() {
        val b1 = getCapsule(mass = 4.2f, radius = 1.5f, height = 5.0f)
        val b2 = getCuboid(mass = 6.0f, sizeX = 4.0f, sizeY = 4.0f, sizeZ = 4.0f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(5.1181f, 8.0f, 6.0f)
            applyForces()
        }
        b2.apply {
            pos.set(2.0f, 8.0f, 2.0f)
            applyForces()
        }

        // Bodies should not collide initially
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(-3.0f, 0.0f, 0.0f)
            applyForces()
        }

        // b1 got a speed from its force
        assertEquals(-0.011904763f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2 did not get any speed
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1 was moved along its speed
        assertEquals(5.117902f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(8.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(6.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2 is still where it was
        assertEquals(2.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(8.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(2.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // Bodies should now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.BOTTOM_SPHERE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2") // not SOUTH_FACE since it was the edge

        assertEquals(4.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(8.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(4.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(-0.74531686f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-0.6667105f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")

        // b1 should have been moved away from b2
        assertEquals(5.117957f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x") // slightly towards orig pos
        assertEquals(8.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(6.0000496f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // slightly away from b2

        // b2 should have been moved away from b1
        assertEquals(1.9999447f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x") // slightly away from b1
        assertEquals(8.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(1.9999505f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // slightly away from b1

        bnc.bounce(b1, b2, hit)

        assertEquals(-0.0042723864f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x") // slowed down a bit
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0043064035f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z") // away from b2

        assertEquals(-0.0053426633f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x") // got about half b1's original vx
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(-0.003014482f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z") // away from b1
    }

    @Test
    fun `should check collision between z-centre of capsule side vs cuboid west face`() {
        val b1 = getCapsule(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 9.39999f, 10.0f)
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 10.0f, 0.0f)
            applyForces()
        }

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(9.400204f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.WEST_FACE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(9.7f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(1.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(9.400077f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.000127f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0031837607f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.01138889f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should check collision between capsule top sphere and cuboid bottom west edge`() {
        val b1 = getCapsule(mass = 4.2f, radius = 1.5f, height = 5.0f)
        val b2 = getCuboid(mass = 6.0f, sizeX = 4.0f, sizeY = 4.0f, sizeZ = 4.0f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(5.0f, 7.8819f, 4.0f)
            applyForces()
        }
        b2.apply {
            pos.set(5.0f, 11.0f, 8.0f)
            applyForces()
        }

        // Bodies should not collide initially
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 3.0f, 0.0f)
            applyForces()
        }

        // b1 got a speed from its force
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.011904763f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2 did not get any speed
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1 was moved along its speed
        assertEquals(5.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(7.882098f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(4.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2 is still where it was
        assertEquals(5.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(11.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(8.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // Bodies should now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.TOP_SPHERE, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM_FACE, hit.area2, "hit.area2") // not WEST_FACE since it was the edge

        assertEquals(5.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(9.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(6.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.74531686f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.6667105f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")

        // b1 should have been moved away from b2
        assertEquals(5.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(7.882043f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // slightly towards orig pos
        assertEquals(3.9999504f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // slightly away from b2

        // b2 should have been moved away from b1
        assertEquals(5.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(11.000055f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y") // slightly away from b1
        assertEquals(8.00005f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // slightly away from b1

        bnc.bounce(b1, b2, hit)

        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0042723864f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y") // slowed down a bit
        assertEquals(-0.0043064035f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z") // away from b2

        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0053426633f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y") // got about half b1's original vy
        assertEquals(0.003014482f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z") // away from b1
    }

    @Test
    fun `should check collision between capsule bottom sphere and cuboid top west edge`() {
        val b1 = getCapsule(mass = 4.2f, radius = 1.5f, height = 5.0f)
        val b2 = getCuboid(mass = 6.0f, sizeX = 4.0f, sizeY = 4.0f, sizeZ = 4.0f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(5.0f, 7.8819f, 6.0f)
            applyForces()
        }
        b2.apply {
            pos.set(5.0f, 11.0f, 2.0f)
            applyForces()
        }

        // Bodies should not collide initially
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 3.0f, 0.0f)
            applyForces()
        }

        // b1 got a speed from its force
        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.011904763f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2 did not get any speed
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1 was moved along its speed
        assertEquals(5.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(7.882098f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(6.0f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2 is still where it was
        assertEquals(5.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(11.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(2.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // Bodies should now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.BOTTOM_SPHERE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2") // not WEST_FACE since it was the edge

        assertEquals(5.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(9.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(4.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.74531686f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-0.6667105f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")

        // b1 should have been moved away from b2
        assertEquals(5.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(7.882043f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // slightly towards orig pos
        assertEquals(6.0000496f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // slightly away from b2

        // b2 should have been moved away from b1
        assertEquals(5.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(11.000055f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y") // slightly away from b1
        assertEquals(1.9999505f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // slightly away from b1

        bnc.bounce(b1, b2, hit)

        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0042723864f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y") // slowed down a bit
        assertEquals(0.0043064035f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z") // away from b2

        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0053426633f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y") // got about half b1's original vy
        assertEquals(-0.003014482f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z") // away from b1
    }

    @Test
    fun `should check collision between capsule top vs cuboid bottom, XY centre`() {
        val b1 = getCapsule(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 42.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 10.0f, 9.29999f)
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(1.0f, -1.0f, 10.0f)
            applyForces()
        }

        assertEquals(10.000021f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(9.999979f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.300203f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertEquals(0.0012820514f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.0012820514f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.012820514f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.TOP_SPHERE, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM_FACE, hit.area2, "hit.area2")

        assertEquals(10.000021f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(9.999979f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.8f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(1.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(b1, b2, hit)

        assertEquals(10.000021f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x") // unchanged, because z speed was cleared
        assertEquals(9.999979f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // (see below, b1.nextSpeed.z)
        assertEquals(9.3000765f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // between pos and previous nextPos

        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.000127f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
        bnc.bounce(b1, b2, hit)

        // A cuboid on top of a capsule is an instable configuration, and the strategy is not expected to
        // apply a remedy against "hopping".

        // The speed in XY is so small that the force of friction causes the two speeds to collapse to one.
        assertEquals(0.00030303f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.00030303f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.0032354323f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(0.00030303f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(-0.00030303f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0049696974f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should check collision between capsule bottom vs cuboid top, XY centre`() {
        val b1 = getCapsule(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 10.0f, 10.70001f)
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(5.0f, 5.0f, -10.0f)
            applyForces()
        }

        assertEquals(10.000107f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.000107f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.699797f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertEquals(0.006410257f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.006410257f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.012820514f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.BOTTOM_SPHERE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2")

        assertEquals(10.000107f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.000107f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.2f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-1.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(b1, b2, hit)

        assertEquals(10.000107f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x") // unchanged
        assertEquals(10.000107f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // unchanged
        assertEquals(10.6999235f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // between pos and previous nextPos

        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x") // unchanged
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y") // unchanged
        assertEquals(9.999873f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // moved slightly in the other direction

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
        bnc.bounce(b1, b2, hit)

        // A capsule on top of a cuboid is an instable configuration, and the strategy is not expected to
        // apply a remedy against "hopping".

        // Unlike in the previous test, we added a slightly stronger XY force, which no longer causes the speeds in
        // XY to collapse due to friction.
        assertEquals(0.004914454f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.004914454f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.0031837607f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(0.001767767f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.001767767f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(-0.01138889f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should check collision between capsule top sphere vs cuboid bottom NW corner`() {
        val b1 = getCapsule(mass = 4.2f, radius = 1.5f, height = 5.0f)
        val b2 = getCuboid(mass = 6.0f, sizeX = 4.0f, sizeY = 4.0f, sizeZ = 4.0f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(5.2093f, 8.2093f, 4.0f)
            applyForces()
        }
        b2.apply {
            pos.set(8.0f, 11.0f, 8.0f)
            applyForces()
        }

        // Bodies should not collide initially
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(48.0f, 48.0f, 10.0f)
            applyForces()
        }

        // b1 got a speed from its force
        assertEquals(0.19047621f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.19047621f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.039682545f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2 did not get any speed
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1 was moved along its speed
        assertEquals(5.212475f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(8.212475f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(4.0006614f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2 is still where it was
        assertEquals(8.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(11.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(8.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // Bodies should now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.TOP_SPHERE, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM_FACE, hit.area2, "hit.area2") // always horizontal face, not one of the sides

        // The hit point is the cuboid's exact corner
        assertEquals(6.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(9.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(6.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.52629673f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.52629673f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.66784996f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")

        // b1 should have been moved away from b2
        assertEquals(5.2115016f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x") // slightly towards orig pos
        assertEquals(8.211502f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // slightly towards orig pos
        assertEquals(3.9994266f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // slightly away from b2

        // b2 should have been moved away from b1
        assertEquals(8.000973f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x") // slightly away from b1
        assertEquals(11.000973f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y") // slightly away from b1
        assertEquals(8.001235f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // slightly away from b1

        bnc.bounce(b1, b2, hit)

        assertEquals(0.05252493f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x") // slowed down a bit
        assertEquals(0.05252493f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y") // slowed down a bit
        assertEquals(-0.07078804f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z") // away from b2

        assertEquals(0.09656589f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x") // got about half b1's original vx
        assertEquals(0.09656589f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y") // got about half b1's original vy
        assertEquals(0.07732941f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z") // away from b1
    }

    @Test
    fun `should check collision between capsule bottom sphere vs cuboid top SE corner`() {
        val b1 = getCapsule(mass = 4.2f, radius = 1.5f, height = 5.0f)
        val b2 = getCuboid(mass = 6.0f, sizeX = 4.0f, sizeY = 4.0f, sizeZ = 4.0f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(4.7907f, 7.7907f, 6.0f)
            applyForces()
        }
        b2.apply {
            pos.set(2.0f, 5.0f, 2.0f)
            applyForces()
        }

        // Bodies should not collide initially
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(-48.0f, -48.0f, -10.0f)
            applyForces()
        }

        // b1 got a speed from its force
        assertEquals(-0.19047621f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.19047621f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(-0.039682545f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        // b2 did not get any speed
        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        // b1 was moved along its speed
        assertEquals(4.787525f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(7.787525f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(5.9993386f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        // b2 is still where it was
        assertEquals(2.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(5.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(2.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // Bodies should now collide
        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")

        assertEquals(HitArea.BOTTOM_SPHERE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2") // always horizontal face, not one of the sides

        // The hit point is the cuboid's exact corner
        assertEquals(4.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(4.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(-0.52629673f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-0.52629673f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-0.66784996f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(b1, b2, hit)
        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")

        // b1 should have been moved away from b2
        assertEquals(4.7884984f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x") // slightly towards orig pos
        assertEquals(7.7884984f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // slightly towards orig pos
        assertEquals(6.0005736f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // slightly away from b2

        // b2 should have been moved away from b1
        assertEquals(1.9990269f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x") // slightly away from b1
        assertEquals(4.999027f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y") // slightly away from b1
        assertEquals(1.9987652f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z") // slightly away from b1

        bnc.bounce(b1, b2, hit)

        assertEquals(-0.05252493f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x") // slowed down a bit
        assertEquals(-0.05252493f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y") // slowed down a bit
        assertEquals(0.07078804f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z") // away from b2

        assertEquals(-0.09656589f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x") // got about half b1's original vx
        assertEquals(-0.09656589f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y") // got about half b1's original vy
        assertEquals(-0.07732941f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z") // away from b1
    }

    @Test
    fun `should force capsule and cuboid apart when they collide at their orig pos with a clear dir of separation`() {
        val capsule = getCapsule(mass = 7.0f, radius = 1.0f, height = 2.1f)
        val cuboid = getCuboid(mass = 9.0f, sizeX = 2.0f, sizeY = 2.0f, sizeZ = 2.0f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        capsule.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }
        cuboid.apply {
            pos.set(9.0f, 11.0f, 9.0f)
            applyForces()
        }

        assertTrue(chk.check(capsule, capsule.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(capsule, cuboid, hit)
        bnc.bounce(capsule, cuboid, hit)

        // Since the positions of the bodies are not identical, there is a clear direction of separation, and the
        // result should not be random even though it is also a bit arbitrary since there is a large overlap in all
        // directions.
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SOUTH_FACE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.475f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(-1.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // Position was moved just along the x-axis (north-south axis)
        assertEquals(10.500025f, capsule.nextPos.x, TOLERANCE, "capsule.nextPos.x")
        assertEquals(10.0f, capsule.nextPos.y, TOLERANCE, "capsule.nextPos.y")
        assertEquals(10.0f, capsule.nextPos.z, TOLERANCE, "capsule.nextPos.z")

        assertEquals(8.499975f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(11.0f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(9.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        // Speed wasn't changed at all since no force was involved
        assertEquals(0.0f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(0.0f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y")
        assertEquals(0.0f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")

        assertEquals(0.0f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.0f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        assertFalse(chk.check(capsule, capsule.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should correctly handle a situation when a long capsule completely overlaps the cuboid`() {
        val capsule = getCapsule(mass = 7.0f, radius = 0.2f, height = 10.0f)
        val cuboid = getCuboid(mass = 9.0f, sizeX = 4.0f, sizeY = 4.0f, sizeZ = 4.0f)
        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val hit = MutableHitResult()

        capsule.apply {
            pos.set(5.0f, 5.0f, 5.0f)
            applyForces()
        }
        cuboid.apply {
            pos.set(5.0f, 5.0f, 5.0f)
            applyForces()
        }

        assertTrue(chk.check(capsule, capsule.nextPos, cuboid, cuboid.nextPos, hit), "should collide")

        // There is an initial overlap of two faces, so the HitArea is arbitrary
        assertEquals(HitArea.BOTTOM_SPHERE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2")

        assertEquals(5.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(5.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(7.0f, hit.hitPt.z, TOLERANCE, "hitPt.z") // cuboid top face

        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-1.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(capsule, cuboid, hit)

        assertEquals(5.0f, capsule.nextPos.x, TOLERANCE, "capsule.nextPos.x")
        assertEquals(5.0f, capsule.nextPos.y, TOLERANCE, "capsule.nextPos.y")
        assertEquals(8.500025f, capsule.nextPos.z, TOLERANCE, "capsule.nextPos.z")

        assertEquals(5.0f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(5.0f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(1.499975f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertFalse(chk.check(capsule, capsule.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should properly bounce bodies, situation 1`() {
        val capsule = getCapsule(mass = 64.0f, radius = 0.25f, height = 1.6f).apply {
            pos.set(178.25668f, 203.53041f, 7.1275916f)
            speed.set(-4.993949f, -3.892018f, 0.17770779f)
            applyForces()
        }
        val cuboid = getCuboid(mass = LARGE_MASS, sizeX = 1.0f, sizeY = 1.0f, sizeZ = 1.0f).apply {
            pos.set(177.5f, 203.5f, 6.5f)
            applyForces()
        }

        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        assertFalse(chk.check(capsule, capsule.pos, cuboid, cuboid.pos, hit), "should not collide initially")
        assertTrue(chk.check(capsule, capsule.nextPos, cuboid, cuboid.nextPos, hit), "should collide")

        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SOUTH_FACE, hit.area2, "hit.area2")

        assertEquals(178.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(203.46555f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(6.6652765f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(-1.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(178.17345f, capsule.nextPos.x, TOLERANCE, "capsule.nextPos.x")
        assertEquals(203.46555f, capsule.nextPos.y, TOLERANCE, "capsule.nextPos.y")
        assertEquals(7.1305532f, capsule.nextPos.z, TOLERANCE, "capsule.nextPos.z")

        sep.separate(capsule, cuboid, hit)
        assertFalse(chk.check(capsule, capsule.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")

        bnc.bounce(capsule, cuboid, hit)
    }

    @Test
    fun `should properly handle collisions in situation 2`() {
        val capsule = getCapsule(mass = 20.0f, radius = 2.25f, height = 5.0f)
        val cuboid = getCuboid(mass = LARGE_MASS, sizeX = 5.0f, sizeY = 5.0f, sizeZ = 5.0f)

        val chk = CheckCapsuleVsCuboid()
        val sep = SeparateCapsuleVsCuboid()
        val bnc = BounceCapsuleVsCuboid()
        val hit = MutableHitResult()

        capsule.apply {
            pos.set(7.00002f, 3.74996f, 7.0f)
            speed.set(0.0f, 5.056857f, 0.0f)
            applyForces()
        }
        cuboid.apply {
            pos.set(4.5f, 8.5f, 7.0f)
            applyForces()
        }

        assertEquals(7.00002f, capsule.nextPos.x, TOLERANCE, "capsule.nextPos.x")
        assertEquals(3.834241f, capsule.nextPos.y, TOLERANCE, "capsule.nextPos.y")
        assertEquals(7.0f, capsule.nextPos.z, TOLERANCE, "capsule.nextPos.z")

        assertFalse(chk.check(capsule, capsule.pos, cuboid, cuboid.pos, hit), "should not collide at orig pos")
        assertTrue(chk.check(capsule, capsule.nextPos, cuboid, cuboid.nextPos, hit), "should collide at nextPos")

        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.WEST_FACE, hit.area2, "hit.area2")

        assertEquals(7.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(6.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(7.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(-0.00000924f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(1.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(capsule, cuboid, hit)

        assertEquals(7.000021f, capsule.nextPos.x, TOLERANCE, "capsule.nextPos.x")
        assertEquals(3.74995f, capsule.nextPos.y, TOLERANCE, "capsule.nextPos.y") // almost back to orig pos
        assertEquals(7.0f, capsule.nextPos.z, TOLERANCE, "capsule.nextPos.z")

        assertEquals(4.5f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(8.5f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(7.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertFalse(chk.check(capsule, capsule.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
        bnc.bounce(capsule, cuboid, hit)

        assertEquals(0.0000299274f, capsule.nextSpeed.x, TOLERANCE, "capsule.nextSpeed.x")
        assertEquals(-3.2363887f, capsule.nextSpeed.y, TOLERANCE, "capsule.nextSpeed.y") // almost fully reflected
        assertEquals(0.0f, capsule.nextSpeed.z, TOLERANCE, "capsule.nextSpeed.z")

        assertEquals(0.0f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.0f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")
    }

    companion object {
        private const val TOLERANCE = 0.000001f
    }
}
