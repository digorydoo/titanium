package ch.digorydoo.titanium.engine.physics.strategy

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCylinderBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_cuboid.BounceCylinderVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_cuboid.CheckCylinderVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.cylinder_vs_cuboid.SeparateCylinderVsCuboid
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CollideCylinderVsCuboidTest {
    private fun getCylinder(mass: Float, radius: Float, height: Float, friction: Float = 0.5f) =
        FixedCylinderBody(
            "cylinder",
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
    fun `should check collision between z-centre of cylinder side vs cuboid north face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
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
    fun `should check collision between upper area of cylinder side vs cuboid north face`() {
        val b1 = getCylinder(mass = 3.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 42.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(9.29999f, 10.0f, 9.31f) // z should still collide, but x does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(3.0f, 0.0f, 0.0f) // only a small force is necessary, because b1.mass is small
            applyForces()
        }

        assertEquals(9.300267f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.31f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

        assertEquals(9.6f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.805f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(1.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(9.300109f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.31f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertEquals(10.000158f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        assertEquals(-0.00884445f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(0.0018222225f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should check collision between lower area of cylinder side vs cuboid north face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(9.29999f, 10.0f, 10.69f) // z should still collide, but x does not
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
        assertEquals(10.69f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

        assertEquals(9.6f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.195f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(1.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(9.3000765f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.69f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertEquals(10.000127f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.0f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        assertEquals(0.0031837607f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(0.01138889f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should check collision between z-centre of cylinder side vs cuboid east face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
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
    fun `should check collision between upper area of cylinder side vs cuboid east face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 10.60001f, 9.31f) // z should still collide, but y does not
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
        assertEquals(9.31f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.3f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.805f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-1.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.599923f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.31f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

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
    fun `should check collision between lower area of cylinder side vs cuboid east face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 10.60001f, 10.69f) // z should still collide, but y does not
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
        assertEquals(10.69f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.3f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.195f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-1.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.599923f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.69f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

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
    fun `should check collision between z-centre of cylinder side vs cuboid south face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
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
    fun `should check collision between upper area of cylinder side vs cuboid south face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.70001f, 10.0f, 9.31f) // z should still collide, but x does not
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
        assertEquals(9.31f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SOUTH_FACE, hit.area2, "hit.area2")

        assertEquals(10.4f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.805f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(-1.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(10.6999235f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.31f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

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
    fun `should check collision between lower area of cylinder side vs cuboid south face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.70001f, 10.0f, 10.69f) // z should still collide, but x does not
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
        assertEquals(10.69f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SOUTH_FACE, hit.area2, "hit.area2")

        assertEquals(10.4f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.195f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(-1.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(10.6999235f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.69f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

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
    fun `should check collision between z-centre of cylinder side vs cuboid west face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
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
    fun `should check collision between upper area of cylinder side vs cuboid west face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 9.39999f, 9.31f) // z should still collide, but y does not
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
        assertEquals(9.31f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.WEST_FACE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(9.7f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.805f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(1.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(9.400077f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(9.31f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

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
    fun `should check collision between lower area of cylinder side vs cuboid west face`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.0f, 9.39999f, 10.69f) // z should still collide, but y does not
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
        assertEquals(10.69f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.WEST_FACE, hit.area2, "hit.area2")

        assertEquals(10.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(9.7f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.195f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(1.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(10.0f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(9.400077f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.69f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

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
    fun `should check collision between cylinder top vs cuboid bottom, XY centre`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 42.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
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
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.TOP_FACE, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM_FACE, hit.area2, "hit.area2")

        assertEquals(10.000021f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(9.999979f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.8f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(1.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(10.000021f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x") // unchanged, because z speed was cleared
        assertEquals(9.999979f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y") // (see below, b1.nextSpeed.z)
        assertEquals(9.3000765f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // between pos and previous nextPos

        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(10.000127f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        // The speed in XY is so small that the force of friction causes the two speeds to collapse to one.
        assertEquals(0.00030303f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(-0.00030303f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.00030303f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(-0.00030303f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")

        // The cuboid is standing on top of the cylinder, and the difference in z speed is small.
        // The strategy is expected to set the cuboid's z speed to the cylinder's before bouncing.
        assertEquals(0.012820514f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")
        assertEquals(0.012820514f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should check collision between cylinder bottom vs cuboid top, XY centre`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
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

        assertEquals(HitArea.BOTTOM_FACE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2")

        assertEquals(10.000107f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.000107f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.2f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-1.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(b1, b2, hit)

        assertEquals(10.000107f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.000107f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.6999235f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // between pos and previous nextPos

        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(9.999873f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
        bnc.bounce(b1, b2, hit)

        // The cylinder is standing on top of the cuboid, and the difference in z speed is small.
        // The strategy is expected to set the cylinder's z speed to the cuboid's before bouncing.

        // Unlike in the previous test, we added a slightly stronger XY force, which no longer causes the speeds in
        // XY to collapse due to friction.

        assertEquals(0.004914454f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.004914454f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")

        assertEquals(0.001767767f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.001767767f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")
    }

    @Test
    fun `should check collision between cylinder bottom vs cuboid top, XY north`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(9.31f, 10.0f, 10.70001f) // x should still collide, but z does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 0.0f, -10.0f)
            applyForces()
        }

        assertEquals(9.31f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.699797f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.BOTTOM_FACE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2")

        assertEquals(9.6f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.2f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-1.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(9.31f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.6999235f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // between pos and previous nextPos

        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(9.999873f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")

        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")

        // The cylinder is standing on top of the cuboid, and the difference in z speed is small.
        // The strategy is expected to set the cylinder's z speed to the cuboid's before bouncing.
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should check collision between cylinder bottom vs cuboid top, XY south`() {
        val b1 = getCylinder(mass = 13.0f, radius = 0.3f, height = 1.0f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.8f, sizeY = 0.6f, sizeZ = 0.4f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        b1.apply {
            pos.set(10.69f, 10.0f, 10.70001f) // x should still collide, but z does not
            applyForces()
        }
        b2.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should not collide")

        b1.apply {
            addForce(0.0f, 0.0f, -10.0f)
            applyForces()
        }

        assertEquals(10.69f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.699797f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z")

        assertTrue(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should collide")
        sep.separate(b1, b2, hit)
        bnc.bounce(b1, b2, hit)
        assertEquals(HitArea.BOTTOM_FACE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2")

        assertEquals(10.4f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.2f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-1.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(10.69f, b1.nextPos.x, TOLERANCE, "b1.nextPos.x")
        assertEquals(10.0f, b1.nextPos.y, TOLERANCE, "b1.nextPos.y")
        assertEquals(10.6999235f, b1.nextPos.z, TOLERANCE, "b1.nextPos.z") // between pos and previous nextPos

        assertEquals(10.0f, b2.nextPos.x, TOLERANCE, "b2.nextPos.x")
        assertEquals(10.0f, b2.nextPos.y, TOLERANCE, "b2.nextPos.y")
        assertEquals(9.999873f, b2.nextPos.z, TOLERANCE, "b2.nextPos.z")

        assertEquals(0.0f, b1.nextSpeed.x, TOLERANCE, "b1.nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, TOLERANCE, "b1.nextSpeed.y")

        assertEquals(0.0f, b2.nextSpeed.x, TOLERANCE, "b2.nextSpeed.x")
        assertEquals(0.0f, b2.nextSpeed.y, TOLERANCE, "b2.nextSpeed.y")

        // The cylinder is standing on top of the cuboid, and the difference in z speed is small.
        // The strategy is expected to set the cylinder's z speed to the cuboid's before bouncing.
        assertEquals(0.0f, b1.nextSpeed.z, TOLERANCE, "b1.nextSpeed.z")
        assertEquals(0.0f, b2.nextSpeed.z, TOLERANCE, "b2.nextSpeed.z")

        assertFalse(chk.check(b1, b1.nextPos, b2, b2.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should check and bounce cylinder vs cuboid when cylinder is near top NW corner`() {
        val cylinder = getCylinder(mass = 7.0f, radius = 2.0f, height = 3.0f)
        val cuboid = getCuboid(mass = 9.0f, sizeX = 2.0f, sizeY = 3.0f, sizeZ = 4.0f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        cylinder.apply {
            pos.set(2.584f, 2.584f, 7.5001f)
            applyForces()
        }
        cuboid.apply {
            pos.set(5.0f, 5.5f, 4.0f)
            applyForces()
        }

        assertFalse(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should not collide")

        // Add a force to the cylinder, and make sure then length in z is greater than the length in XY
        cylinder.apply {
            addForce(48.0f, 48.0f, -70.0f) // abs(fz) > sqrt(fx*fx + fy*fy) ≈ 67.89
            applyForces()
        }

        // The bodies should now collide with a hit to the bottom/top.
        assertTrue(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        assertEquals(HitArea.BOTTOM_FACE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2")

        // Reset, and add a force to the cylinder, this time making sure than fz is smaller
        cylinder.apply {
            stopAllMotion()
            addForce(48.0f, 48.0f, -20.0f)
            applyForces()
        }
        cuboid.stopAllMotion()

        // The bodies should collide, and it should still be bottom/top (important for bricks).
        assertTrue(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        assertEquals(HitArea.BOTTOM_FACE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2")

        // Reset, and try the first check again at an initial position that already has a z overlap.
        cylinder.apply {
            stopAllMotion()
            pos.set(2.584f, 2.584f, 7.0f)
            applyForces()
        }
        cuboid.stopAllMotion()
        assertFalse(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should not collide")
        cylinder.apply {
            addForce(48.0f, 48.0f, -70.0f) // abs(fz) > sqrt(fx*fx + fy*fy) ≈ 67.89
            applyForces()
        }

        // The bodies should collide, but now that we have an initial z overlap, it can no longer be bottom/top
        assertTrue(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

        assertEquals(4.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(4.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(5.7486115f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(1.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

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
        assertTrue(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(cylinder, cuboid, hit)
        bnc.bounce(cylinder, cuboid, hit)
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

        assertEquals(4.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(4.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(5.7486115f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(1.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // cylinder.nextPos has moved a little
        assertEquals(2.5857615f, cylinder.nextPos.x, TOLERANCE, "cylinder.nextPos.x")
        assertEquals(2.5859048f, cylinder.nextPos.y, TOLERANCE, "cylinder.nextPos.y")
        assertEquals(6.9972224f, cylinder.nextPos.z, TOLERANCE, "cylinder.nextPos.z")

        // cuboid.nextPos has moved as well, even though it did not move by itself
        assertEquals(5.0001435f, cuboid.nextPos.x, "cuboid.nextPos.x")
        assertEquals(5.5f, cuboid.nextPos.y, "cuboid.nextPos.y")
        assertEquals(4.0f, cuboid.nextPos.z, "cuboid.nextPos.z")

        // cylinder.nextSpeed was changed a little
        assertEquals(0.008857142f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(0.10119778f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(-0.14758009f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")

        // cuboid.nextSpeed was changed a little and moves now roughly in the same direction as the cylinder
        assertEquals(0.082f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.01017951f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(-0.014845118f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // The bodies should no longer collide after bounce
        assertFalse(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should check and bounce cylinder vs cuboid when cylinder is near bottom SE corner`() {
        val cylinder = getCylinder(mass = 7.0f, radius = 2.0f, height = 3.0f)
        val cuboid = getCuboid(mass = 9.0f, sizeX = 2.0f, sizeY = 3.0f, sizeZ = 4.0f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        cylinder.apply {
            pos.set(7.3634f, 8.4634f, 0.499999f)
            applyForces()
        }
        cuboid.apply {
            pos.set(5.0f, 5.5f, 4.0f)
            applyForces()
        }

        assertFalse(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should not collide")

        // Add a force to the cylinder, and make sure then length in z is greater than the length in XY
        cylinder.apply {
            addForce(-48.0f, -48.0f, 70.0f) // abs(fz) > sqrt(fx*fx + fy*fy) ≈ 67.89
            applyForces()
        }

        // The bodies should now collide, and since fz was greater, it should take this as a hit to the bottom/top.
        assertTrue(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        assertEquals(HitArea.TOP_FACE, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM_FACE, hit.area2, "hit.area2")

        // Reset, and add a force to the cylinder, this time making sure than fz is smaller
        cylinder.apply {
            stopAllMotion()
            addForce(-48.0f, -48.0f, 20.0f)
            applyForces()
        }
        cuboid.stopAllMotion()

        // The bodies should collide, and since fz was smaller, it should take this as a hit to the cuboid side.
        assertTrue(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2") // could also have been SOUTH_FACE

        // Reset, and try the first check again at an initial position that already has a z overlap.
        cylinder.apply {
            stopAllMotion()
            pos.set(7.364f, 8.464f, 0.7f)
            applyForces()
        }
        cuboid.stopAllMotion()
        assertFalse(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should not collide")
        cylinder.apply {
            addForce(-48.0f, -48.0f, 70.0f) // abs(fz) > sqrt(fx*fx + fy*fy) ≈ 67.89
            applyForces()
        }

        // The bodies should collide, but now that we have an initial z overlap, it can no longer be bottom/top
        assertTrue(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2")

        assertEquals(6.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(2.101389f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-1.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        assertEquals(7.362095f, cylinder.nextPos.x, TOLERANCE, "cylinder.nextPos.x")
        assertEquals(8.462095f, cylinder.nextPos.y, TOLERANCE, "cylinder.nextPos.y")
        assertEquals(0.70277774f, cylinder.nextPos.z, TOLERANCE, "cylinder.nextPos.z")

        assertEquals(5.0f, cuboid.nextPos.x, "cuboid.nextPos.x") // unchanged
        assertEquals(5.5f, cuboid.nextPos.y, "cuboid.nextPos.y") // unchanged
        assertEquals(4.0f, cuboid.nextPos.z, "cuboid.nextPos.z") // unchanged

        assertEquals(-0.11428572f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(-0.11428572f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(0.16666667f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")

        assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x") // unchanged
        assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y") // unchanged
        assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z") // unchanged

        // Bounce the bodies
        assertTrue(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(cylinder, cuboid, hit)
        bnc.bounce(cylinder, cuboid, hit)
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2")

        assertEquals(6.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(7.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(2.101389f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        // The hitNormal12 should point from b1 to b2
        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(-1.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // cylinder.nextPos has moved a little
        assertEquals(7.362095f, cylinder.nextPos.x, TOLERANCE, "cylinder.nextPos.x")
        assertEquals(8.463312f, cylinder.nextPos.y, TOLERANCE, "cylinder.nextPos.y")
        assertEquals(0.70277774f, cylinder.nextPos.z, TOLERANCE, "cylinder.nextPos.z")

        // cuboid.nextPos was also pushed a little, even though it did not move by itself
        assertEquals(5.0f, cuboid.nextPos.x, "cuboid.nextPos.x")
        assertEquals(5.498783f, cuboid.nextPos.y, "cuboid.nextPos.y")
        assertEquals(4.0f, cuboid.nextPos.z, "cuboid.nextPos.z")

        // cylinder.nextSpeed was changed a little
        assertEquals(-0.10119778f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(-0.008857142f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(0.14758009f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")

        // cuboid.nextSpeed was changed a little and moves now roughly in the same direction as the cylinder
        assertEquals(-0.01017951f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(-0.082f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.014845118f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // The bodies should no longer collide after bounce
        assertFalse(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should force cylinder and cuboid apart when they collide at their orig pos with a clear dir of separation`() {
        val cylinder = getCylinder(mass = 7.0f, radius = 2.0f, height = 1.0f)
        val cuboid = getCuboid(mass = 9.0f, sizeX = 2.0f, sizeY = 2.0f, sizeZ = 2.0f)
        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        cylinder.apply {
            pos.set(10.0f, 10.0f, 10.0f)
            applyForces()
        }
        cuboid.apply {
            pos.set(9.0f, 11.0f, 9.0f)
            applyForces()
        }

        assertTrue(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(cylinder, cuboid, hit)
        bnc.bounce(cylinder, cuboid, hit)

        // Since the positions of the bodies are not identical, there is a clear direction of separation, and the result
        // should not be random even though it is also a bit arbitrary since there is a large overlap in all directions.
        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

        assertEquals(8.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(9.75f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(1.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x") // north face from cylinder's point of view
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        // Position was moved just along the x-axis (north-south axis)
        assertEquals(7.999975f, cylinder.nextPos.x, TOLERANCE, "cylinder.nextPos.x")
        assertEquals(10.0f, cylinder.nextPos.y, TOLERANCE, "cylinder.nextPos.y")
        assertEquals(10.0f, cylinder.nextPos.z, TOLERANCE, "cylinder.nextPos.z")

        assertEquals(11.000025f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(11.0f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(9.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        // Speed wasn't changed at all since no force was involved
        assertEquals(0.0f, cylinder.nextSpeed.x, TOLERANCE, "cylinder.nextSpeed.x")
        assertEquals(0.0f, cylinder.nextSpeed.y, TOLERANCE, "cylinder.nextSpeed.y")
        assertEquals(0.0f, cylinder.nextSpeed.z, TOLERANCE, "cylinder.nextSpeed.z")

        assertEquals(0.0f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.0f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        assertFalse(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should not mistake the hit area for a cuboid side near the edges of the TOP face`() {
        val cylinder = getCylinder(mass = 20.0f, radius = 0.5f, height = 1.0f)
        val cuboid = getCuboid(mass = LARGE_MASS, sizeX = 1.0f, sizeY = 1.0f, sizeZ = 1.0f)
        val chk = CheckCylinderVsCuboid()
        val hit = MutableHitResult()

        cylinder.apply {
            pos.set(22.492413f, 9.504805f, 2.500001f)
            speed.set(-0.1391983f, 0.08817075f, -0.16350001f)
            applyForces()
        }
        cuboid.apply {
            pos.set(21.5f, 9.5f, 1.5f)
            speed.set(0.0f, 0.0f, 0.0f)
            applyForces()
        }

        assertTrue(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should collide")

        assertEquals(HitArea.BOTTOM_FACE, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2")

        assertEquals(22.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(9.506274f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(2.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(0.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(-1.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")
    }

    @Test
    fun `should properly handle collisions in situation 1`() {
        val cylinder = getCylinder(mass = 64.0f, radius = 0.25f, height = 1.6f).apply {
            pos.set(178.25668f, 203.53041f, 7.1275916f)
            speed.set(-4.993949f, -3.892018f, 0.17770779f)
            applyForces()
        }
        val cuboid = getCuboid(mass = LARGE_MASS, sizeX = 1.0f, sizeY = 1.0f, sizeZ = 1.0f).apply {
            pos.set(177.5f, 203.5f, 6.5f)
            applyForces()
        }

        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val bnc = BounceCylinderVsCuboid()
        val hit = MutableHitResult()

        assertTrue(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should collide")

        assertEquals(HitArea.SIDE, hit.area1, "hit.area1")
        assertEquals(HitArea.SOUTH_FACE, hit.area2, "hit.area2")

        assertEquals(178.0f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(203.46555f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(6.6652765f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(-1.0f, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x")
        assertEquals(0.0f, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y")
        assertEquals(0.0f, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z")

        sep.separate(cylinder, cuboid, hit)
        bnc.bounce(cylinder, cuboid, hit)
        assertFalse(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should properly handle collisions in situation 2`() {
        val cylinder = getCylinder(mass = 20.0f, radius = 0.45f, height = 1.0f).apply {
            pos.set(135.3745f, 290.45026f, 1.5000732f)
            speed.set(-3.1316957f, -0.40986443f, 0.0f)
            applyForces()
        }
        val cuboid = getCuboid(mass = LARGE_MASS, sizeX = 1.0f, sizeY = 1.0f, sizeZ = 1.0f).apply {
            pos.set(135.5f, 289.5f, 1.5f)
            applyForces()
        }

        val chk = CheckCylinderVsCuboid()
        val sep = SeparateCylinderVsCuboid()
        val hit = MutableHitResult()

        assertFalse(chk.check(cylinder, cylinder.pos, cuboid, cuboid.pos, hit), "should not collide at orig pos")
        assertTrue(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should collide at nextPos")

        sep.separate(cylinder, cuboid, hit)
        assertFalse(chk.check(cylinder, cylinder.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    companion object {
        private const val TOLERANCE = 0.000001f
    }
}
