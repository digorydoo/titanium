package ch.digorydoo.titanium.engine.physics.strategy

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.engine.brick.BrickFaceCovering
import ch.digorydoo.titanium.engine.brick.IBrickFaceCoveringRetriever
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody.Companion.LARGE_MASS
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cuboid.BounceSphereVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cuboid.CheckSphereVsCuboid
import ch.digorydoo.titanium.engine.physics.strategy.sphere_vs_cuboid.SeparateSphereVsCuboid
import ch.digorydoo.titanium.engine.utils.Direction
import ch.digorydoo.titanium.engine.utils.assertGreaterThan
import ch.digorydoo.titanium.engine.utils.assertLessThan
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CollideSphereVsCuboidTest {
    private fun getSphere(mass: Float, radius: Float, elasticity: Float = 0.8f, friction: Float = 0.5f) =
        FixedSphereBody(
            "sphere",
            initialPos = Point3f.zero,
            mass = mass,
            radius = radius,
            gravity = false,
            elasticity = elasticity,
            friction = friction,
        )

    private fun getCuboid(
        mass: Float,
        sizeX: Float,
        sizeY: Float,
        sizeZ: Float,
        elasticity: Float = 0.5f,
        friction: Float = 0.72f,
    ) =
        FixedCuboidBody(
            "cuboid",
            initialPos = Point3f.zero,
            mass = mass,
            sizeX = sizeX,
            sizeY = sizeY,
            sizeZ = sizeZ,
            gravity = false,
            elasticity = elasticity,
            friction = friction,
        )

    @Test
    fun `should correctly check sphere against cuboid`() {
        val b1 = getSphere(mass = 10.0f, radius = 0.5f)
        val b2 = getCuboid(mass = 11.0f, sizeX = 0.4f, sizeY = 0.3f, sizeZ = 0.2f)

        val chk = CheckSphereVsCuboid()
        val hit = MutableHitResult()

        fun check() = chk.check(b1, b1.nextPos, b2, b2.nextPos, hit)

        fun shouldCollideAt(p1: Point3f, p2: Point3f, area: HitArea, expectedHitPt: Point3f, expectedNormal: Point3f) {
            b1.nextPos.set(p1)
            b2.nextPos.set(p2)

            assertTrue(check(), "should collide at $p1 vs $p2")
            assertEquals(HitArea.UNSPECIFIED, hit.area1, "area1 should be UNSPECIFIED")
            assertEquals(area, hit.area2, "area2 should be $area")

            assertEquals(expectedHitPt.x, hit.hitPt.x, TOLERANCE, "hitPt.x ($area)")
            assertEquals(expectedHitPt.y, hit.hitPt.y, TOLERANCE, "hitPt.y ($area)")
            assertEquals(expectedHitPt.z, hit.hitPt.z, TOLERANCE, "hitPt.z ($area)")

            assertEquals(expectedNormal.x, hit.hitNormal12.x, TOLERANCE, "hitNormal12.x ($area)")
            assertEquals(expectedNormal.y, hit.hitNormal12.y, TOLERANCE, "hitNormal12.y ($area)")
            assertEquals(expectedNormal.z, hit.hitNormal12.z, TOLERANCE, "hitNormal12.z ($area)")

            val n = hit.hitNormal12
            val len = sqrt(n.x * n.x + n.y * n.y + n.z * n.z)
            assertLessThan(len, 1.01f, "hitNormal12")
            assertGreaterThan(len, 0.99f, "hitNormal12")
        }

        fun shouldNotCollideAt(p1: Point3f, p2: Point3f) {
            b1.nextPos.set(p1)
            b2.nextPos.set(p2)
            assertFalse(check(), "should not collide at $p1 vs $p2, but collided with area ${hit.area2}")
        }

        // Check the situation of complete overlap
        shouldCollideAt(
            Point3f(10.0f, 10.0f, 10.0f),
            Point3f(10.0f, 10.0f, 10.0f),
            HitArea.NORTH_FACE, // this is arbitrary since we have a hit on all sides
            Point3f(9.8f, 10.0f, 10.0f), // seems odd, but correct since the hit point is always on the colliding face
            Direction.southVector // points from sphere to cuboid, i.e. into the cuboid
        )

        val x = 10.0f
        val y = 10.0f
        val z = 10.0f
        val r = b1.radius
        val xsouth = x + b2.sizeX / 2
        val xnorth = x - b2.sizeX / 2
        val yeast = y + b2.sizeY / 2
        val ywest = y - b2.sizeY / 2
        val ztop = z + b2.sizeZ / 2
        val zbottom = z - b2.sizeZ / 2

        // North face
        run {
            // Centre
            shouldCollideAt(
                Point3f(xnorth - r + 0.000001f, y, z),
                Point3f(x, y, z),
                HitArea.NORTH_FACE,
                Point3f(9.8f, y, z),
                Direction.southVector,
            )
            shouldNotCollideAt(Point3f(xnorth - r - 0.000001f, y, z), Point3f(x, y, z))

            // Close to the top
            shouldCollideAt(
                Point3f(xnorth - r + 0.000001f, y, ztop - 0.001f),
                Point3f(x, y, z),
                HitArea.NORTH_FACE,
                Point3f(9.8f, y, 10.099f),
                Direction.southVector,
            )
            shouldNotCollideAt(Point3f(xnorth - r + 0.000001f, y, ztop + 0.001f), Point3f(x, y, z))

            // Close to the bottom
            shouldCollideAt(
                Point3f(xnorth - r + 0.000001f, y, zbottom + 0.001f),
                Point3f(x, y, z),
                HitArea.NORTH_FACE,
                Point3f(9.8f, y, 9.901f),
                Direction.southVector,
            )
            shouldNotCollideAt(Point3f(xnorth - r + 0.000001f, y, zbottom - 0.001f), Point3f(x, y, z))
        }

        // East face
        run {
            // Centre
            shouldCollideAt(
                Point3f(x, yeast + r - 0.000001f, z),
                Point3f(x, y, z),
                HitArea.EAST_FACE,
                Point3f(x, 10.15f, z),
                Direction.westVector,
            )
            shouldNotCollideAt(Point3f(x, yeast + r + 0.000001f, z), Point3f(x, y, z))

            // Close to the top
            shouldCollideAt(
                Point3f(x, yeast + r - 0.000001f, ztop - 0.001f),
                Point3f(x, y, z),
                HitArea.EAST_FACE,
                Point3f(x, 10.15f, 10.099f),
                Direction.westVector,
            )
            shouldNotCollideAt(Point3f(x, yeast + r - 0.000001f, ztop + 0.001f), Point3f(x, y, z))

            // Close to the bottom
            shouldCollideAt(
                Point3f(x, yeast + r - 0.000001f, zbottom + 0.001f),
                Point3f(x, y, z),
                HitArea.EAST_FACE,
                Point3f(x, 10.15f, 9.901f),
                Direction.westVector,
            )
            shouldNotCollideAt(Point3f(x, yeast + r - 0.000001f, zbottom - 0.001f), Point3f(x, y, z))
        }

        // South face
        run {
            // Centre
            shouldCollideAt(
                Point3f(xsouth + r - 0.000001f, y, z),
                Point3f(x, y, z),
                HitArea.SOUTH_FACE,
                Point3f(10.2f, y, z),
                Direction.northVector,
            )
            shouldNotCollideAt(Point3f(xsouth + r + 0.000001f, y, z), Point3f(x, y, z))

            // Close to the top
            shouldCollideAt(
                Point3f(xsouth + r - 0.000001f, y, ztop - 0.001f),
                Point3f(x, y, z),
                HitArea.SOUTH_FACE,
                Point3f(10.2f, y, 10.099f),
                Direction.northVector,
            )
            shouldNotCollideAt(Point3f(xsouth + r - 0.000001f, y, ztop + 0.001f), Point3f(x, y, z))

            // Close to the bottom
            shouldCollideAt(
                Point3f(xsouth + r - 0.000001f, y, zbottom + 0.001f),
                Point3f(x, y, z),
                HitArea.SOUTH_FACE,
                Point3f(10.2f, y, 9.901f),
                Direction.northVector,
            )
            shouldNotCollideAt(Point3f(xsouth + r - 0.000001f, y, zbottom - 0.001f), Point3f(x, y, z))
        }

        // West face
        run {
            // Centre
            shouldCollideAt(
                Point3f(x, ywest - r + 0.000001f, z),
                Point3f(x, y, z),
                HitArea.WEST_FACE,
                Point3f(x, 9.85f, z),
                Direction.eastVector,
            )
            shouldNotCollideAt(Point3f(x, ywest - r - 0.000001f, z), Point3f(x, y, z))

            // Close to the top
            shouldCollideAt(
                Point3f(x, ywest - r + 0.000001f, ztop - 0.001f),
                Point3f(x, y, z),
                HitArea.WEST_FACE,
                Point3f(x, 9.85f, 10.099f),
                Direction.eastVector,
            )
            shouldNotCollideAt(Point3f(x, ywest - r + 0.000001f, ztop + 0.001f), Point3f(x, y, z))

            // Close to the bottom
            shouldCollideAt(
                Point3f(x, ywest - r + 0.000001f, zbottom + 0.001f),
                Point3f(x, y, z),
                HitArea.WEST_FACE,
                Point3f(x, 9.85f, 9.901f),
                Direction.eastVector,
            )
            shouldNotCollideAt(Point3f(x, ywest - r + 0.000001f, zbottom - 0.001f), Point3f(x, y, z))
        }

        // Top face
        run {
            // Centre
            shouldCollideAt(
                Point3f(x, y, ztop + r - 0.000001f),
                Point3f(x, y, z),
                HitArea.TOP_FACE,
                Point3f(x, y, 10.1f),
                Direction.downVector,
            )
            shouldNotCollideAt(Point3f(x, y, ztop + r + 0.000001f), Point3f(x, y, z))

            // Close to the north
            shouldCollideAt(
                Point3f(xnorth, y, ztop + r - 0.000001f),
                Point3f(x, y, z),
                HitArea.TOP_FACE,
                Point3f(9.8f, y, 10.1f),
                Direction.downVector,
            )
            shouldNotCollideAt(Point3f(xnorth, y, ztop + r + 0.000001f), Point3f(x, y, z))

            // Close to the east
            shouldCollideAt(
                Point3f(x, yeast, ztop + r - 0.000001f),
                Point3f(x, y, z),
                HitArea.TOP_FACE,
                Point3f(x, 10.15f, 10.1f),
                Direction.downVector,
            )
            shouldNotCollideAt(Point3f(x, yeast, ztop + r + 0.000001f), Point3f(x, y, z))

            // Close to the south
            shouldCollideAt(
                Point3f(xsouth, y, ztop + r - 0.000001f),
                Point3f(x, y, z),
                HitArea.TOP_FACE,
                Point3f(10.2f, y, 10.1f),
                Direction.downVector,
            )
            shouldNotCollideAt(Point3f(xsouth, y, ztop + r + 0.000001f), Point3f(x, y, z))

            // Close to the west
            shouldCollideAt(
                Point3f(x, ywest, ztop + r - 0.000001f),
                Point3f(x, y, z),
                HitArea.TOP_FACE,
                Point3f(x, 9.85f, 10.1f),
                Direction.downVector,
            )
            shouldNotCollideAt(Point3f(x, ywest, ztop + r + 0.000001f), Point3f(x, y, z))
        }

        // Bottom face
        run {
            // Centre
            shouldCollideAt(
                Point3f(x, y, zbottom - r + 0.000001f),
                Point3f(x, y, z),
                HitArea.BOTTOM_FACE,
                Point3f(x, y, 9.9f),
                Direction.upVector,
            )
            shouldNotCollideAt(Point3f(x, y, zbottom - r - 0.000001f), Point3f(x, y, z))

            // Close to the north
            shouldCollideAt(
                Point3f(xnorth, y, zbottom - r + 0.000001f),
                Point3f(x, y, z),
                HitArea.BOTTOM_FACE,
                Point3f(9.8f, y, 9.9f),
                Direction.upVector,
            )
            shouldNotCollideAt(Point3f(xnorth, y, zbottom - r - 0.000001f), Point3f(x, y, z))

            // Close to the east
            shouldCollideAt(
                Point3f(x, yeast, zbottom - r + 0.000001f),
                Point3f(x, y, z),
                HitArea.BOTTOM_FACE,
                Point3f(x, 10.15f, 9.9f),
                Direction.upVector,
            )
            shouldNotCollideAt(Point3f(x, yeast, zbottom - r - 0.000001f), Point3f(x, y, z))

            // Close to the south
            shouldCollideAt(
                Point3f(xsouth, y, zbottom - r + 0.000001f),
                Point3f(x, y, z),
                HitArea.BOTTOM_FACE,
                Point3f(10.2f, y, 9.9f),
                Direction.upVector,
            )
            shouldNotCollideAt(Point3f(xsouth, y, zbottom - r - 0.000001f), Point3f(x, y, z))

            // Close to the west
            shouldCollideAt(
                Point3f(x, ywest, zbottom - r + 0.000001f),
                Point3f(x, y, z),
                HitArea.BOTTOM_FACE,
                Point3f(x, 9.85f, 9.9f),
                Direction.upVector,
            )
            shouldNotCollideAt(Point3f(x, ywest, zbottom - r - 0.000001f), Point3f(x, y, z))
        }

        // Top south-east corner
        run {
            // When the sphere's centre point is aligned to the corner in two axes
            shouldCollideAt(
                Point3f(xsouth + r - 0.000001f, yeast, ztop),
                Point3f(x, y, z),
                HitArea.SOUTH_FACE,
                Point3f(10.2f, 10.15f, 10.1f),
                Direction.northVector,
            )
            shouldNotCollideAt(Point3f(xsouth + r + 0.000001f, yeast, ztop), Point3f(x, y, z))
            shouldCollideAt(
                Point3f(xsouth, yeast + r - 0.000001f, ztop),
                Point3f(x, y, z),
                HitArea.EAST_FACE,
                Point3f(10.2f, 10.15f, 10.1f),
                Direction.westVector,
            )
            shouldNotCollideAt(Point3f(xsouth, yeast + r + 0.000001f, ztop), Point3f(x, y, z))
            shouldCollideAt(
                Point3f(xsouth, yeast, ztop + r - 0.000001f),
                Point3f(x, y, z),
                HitArea.TOP_FACE,
                Point3f(10.2f, 10.15f, 10.1f),
                Direction.downVector,
            )
            shouldNotCollideAt(Point3f(xsouth, yeast, ztop + r + 0.000001f), Point3f(x, y, z))

            // When the centre point isn't perfectly aligned with the corner in any axis
            shouldCollideAt(
                Point3f(xsouth + r - 0.22f, yeast + r - 0.209f, ztop + r - 0.206f),
                Point3f(x, y, z),
                HitArea.TOP_FACE,
                Point3f(10.2f, 10.15f, 10.1f),
                Direction.downVector,
            )
            shouldCollideAt(
                Point3f(xsouth + r - 0.209f, yeast + r - 0.206f, ztop + r - 0.22f),
                Point3f(x, y, z),
                HitArea.EAST_FACE,
                Point3f(10.2f, 10.15f, 10.1f),
                Direction.westVector,
            )
            shouldCollideAt(
                Point3f(xsouth + r - 0.206f, yeast + r - 0.22f, ztop + r - 0.209f),
                Point3f(x, y, z),
                HitArea.SOUTH_FACE,
                Point3f(10.2f, 10.15f, 10.1f),
                Direction.northVector,
            )
        }

        // Bottom north-west corner
        run {
            // When the sphere's centre point is aligned to the corner in two axes
            shouldCollideAt(
                Point3f(xnorth - r + 0.000001f, ywest, zbottom),
                Point3f(x, y, z),
                HitArea.NORTH_FACE,
                Point3f(9.8f, 9.85f, 9.9f),
                Direction.southVector,
            )
            shouldNotCollideAt(Point3f(xnorth - r - 0.000001f, ywest, zbottom), Point3f(x, y, z))
            shouldCollideAt(
                Point3f(xnorth, ywest - r + 0.000001f, zbottom),
                Point3f(x, y, z),
                HitArea.WEST_FACE,
                Point3f(9.8f, 9.85f, 9.9f),
                Direction.eastVector,
            )
            shouldNotCollideAt(Point3f(xnorth, ywest - r - 0.000001f, zbottom), Point3f(x, y, z))
            shouldCollideAt(
                Point3f(xnorth, ywest, zbottom - r + 0.000001f),
                Point3f(x, y, z),
                HitArea.BOTTOM_FACE,
                Point3f(9.8f, 9.85f, 9.9f),
                Direction.upVector,
            )
            shouldNotCollideAt(Point3f(xnorth, ywest, zbottom - r - 0.000001f), Point3f(x, y, z))

            // When the centre point isn't perfectly aligned with the corner in any axis
            shouldCollideAt(
                Point3f(xnorth - r + 0.22f, ywest - r + 0.209f, zbottom - r + 0.206f),
                Point3f(x, y, z),
                HitArea.BOTTOM_FACE,
                Point3f(9.8f, 9.85f, 9.9f),
                Direction.upVector,
            )
            shouldCollideAt(
                Point3f(xnorth - r + 0.209f, ywest - r + 0.206f, zbottom - r + 0.22f),
                Point3f(x, y, z),
                HitArea.WEST_FACE,
                Point3f(9.8f, 9.85f, 9.9f),
                Direction.eastVector,
            )
            shouldCollideAt(
                Point3f(xnorth - r + 0.206f, ywest - r + 0.22f, zbottom - r + 0.209f),
                Point3f(x, y, z),
                HitArea.NORTH_FACE,
                Point3f(9.8f, 9.85f, 9.9f),
                Direction.southVector,
            )
        }
    }

    @Test
    fun `should correctly bounce the sphere off the cuboid's NORTH face`() {
        val sphere = getSphere(mass = 9.0f, radius = 0.25f)
        val cuboid = getCuboid(mass = 11.0f, sizeX = 0.5f, sizeY = 0.5f, sizeZ = 0.5f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        // The two should not collide at their initial position
        sphere.pos.set(1.0f, 10.1f, 10.0f)
        cuboid.pos.set(1.501f, 10.0f, 10.0f)
        sphere.applyForces()
        cuboid.applyForces()
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should not collide")

        // Add forces and compute nextSpeed
        sphere.addForce(20.0f, -9.0f, 0.0f)
        cuboid.addForce(-20.0f, 9.0f, 0.0f)
        sphere.applyForces()
        cuboid.applyForces()

        assertEquals(1.0006173f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.099723f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(1.500495f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(10.000227f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(0.03703704f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.016666668f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(-0.030303033f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.013636365f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // The two should now collide
        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")

        // The two should no longer collide after separation
        sep.separate(sphere, cuboid, hit)
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

        assertEquals(1.250495f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.099723f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(1.0005311f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.099723f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(1.5005811f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(10.000227f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        // nextSpeed should still be the previous values
        assertEquals(0.03703704f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.016666668f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(-0.030303033f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.013636365f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        bnc.bounce(sphere, cuboid, hit)

        // Now nextSpeed should be the speed after bounce
        assertEquals(-0.02370371f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.0033333348f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(0.019393943f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.0027272739f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")
    }

    @Test
    fun `should correctly bounce the sphere off the cuboid's EAST face`() {
        val sphere = getSphere(mass = 9.0f, radius = 0.25f)
        val cuboid = getCuboid(mass = 11.0f, sizeX = 0.5f, sizeY = 0.5f, sizeZ = 0.5f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        // The two should not collide at their initial position
        sphere.pos.set(10.1f, 1.501f, 10.0f)
        cuboid.pos.set(10.0f, 1.0f, 10.0f)
        sphere.applyForces()
        cuboid.applyForces()
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should not collide")

        // Add forces and compute nextSpeed
        sphere.addForce(-9.0f, -20.0f, 0.0f)
        cuboid.addForce(9.0f, 20.0f, 0.0f)
        sphere.applyForces()
        cuboid.applyForces()

        assertEquals(10.099723f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(1.5003828f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.000227f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(1.0005051f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(-0.016666668f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.03703704f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(0.013636365f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.030303033f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // The two should now collide
        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2")

        assertEquals(10.099723f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(1.2505051f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(10.099723f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(1.500469f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.000227f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(1.0004189f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(-0.0033333348f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.02370371f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(0.0027272739f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(-0.019393943f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // After bounce, the two should no longer collide
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should correctly bounce the sphere off the cuboid's SOUTH face`() {
        val sphere = getSphere(mass = 9.0f, radius = 0.25f)
        val cuboid = getCuboid(mass = 11.0f, sizeX = 0.5f, sizeY = 0.5f, sizeZ = 0.5f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        // The two should not collide at their initial position
        sphere.pos.set(1.501f, 10.1f, 10.0f)
        cuboid.pos.set(1.0f, 10.0f, 10.0f)
        sphere.applyForces()
        cuboid.applyForces()
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should not collide")

        // Add forces and compute nextSpeed
        sphere.addForce(-20.0f, -9.0f, 0.0f)
        cuboid.addForce(20.0f, 9.0f, 0.0f)
        sphere.applyForces()
        cuboid.applyForces()

        assertEquals(1.5003828f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.099723f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(1.0005051f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(10.000227f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(-0.03703704f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.016666668f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(0.030303033f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.013636365f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // The two should now collide
        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.SOUTH_FACE, hit.area2, "hit.area2")

        assertEquals(1.2505051f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.099723f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(1.500469f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.099723f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(1.0004189f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(10.000227f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(0.02370371f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.0033333348f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(-0.019393943f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.0027272739f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // After bounce, the two should no longer collide
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should correctly bounce the sphere off the cuboid's WEST face`() {
        val sphere = getSphere(mass = 9.0f, radius = 0.25f)
        val cuboid = getCuboid(mass = 11.0f, sizeX = 0.5f, sizeY = 0.5f, sizeZ = 0.5f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        // The two should not collide at their initial position
        sphere.pos.set(10.1f, 1.0f, 10.0f)
        cuboid.pos.set(10.0f, 1.501f, 10.0f)
        sphere.applyForces()
        cuboid.applyForces()
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should not collide")

        // Add forces and compute nextSpeed
        sphere.addForce(-9.0f, 20.0f, 0.0f)
        cuboid.addForce(9.0f, -20.0f, 0.0f)
        sphere.applyForces()
        cuboid.applyForces()

        assertEquals(10.099723f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(1.0006173f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.000227f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(1.500495f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(-0.016666668f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.03703704f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(0.013636365f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(-0.030303033f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // The two should now collide
        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.WEST_FACE, hit.area2, "hit.area2")

        assertEquals(10.099723f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(1.250495f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(10.099723f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(1.0005311f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.000227f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(1.5005811f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(-0.0033333348f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.02370371f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(0.0027272739f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.019393943f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // After bounce, the two should no longer collide
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should correctly bounce the sphere off the cuboid's TOP face`() {
        val sphere = getSphere(mass = 9.0f, radius = 0.25f)
        val cuboid = getCuboid(mass = 11.0f, sizeX = 0.5f, sizeY = 0.5f, sizeZ = 0.5f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        // The two should not collide at their initial position
        sphere.pos.set(10.1f, 10.0f, 1.501f)
        cuboid.pos.set(10.0f, 10.0f, 1.0f)
        sphere.applyForces()
        cuboid.applyForces()
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should not collide")

        // Add forces and compute nextSpeed
        sphere.addForce(-9.0f, 0.0f, -20.0f)
        cuboid.addForce(9.0f, 0.0f, 20.0f)
        sphere.applyForces()
        cuboid.applyForces()

        assertEquals(10.099723f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.0f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(1.5003828f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.000227f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(10.0f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(1.0005051f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(-0.016666668f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(-0.03703704f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(0.013636365f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.0f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.030303033f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // The two should now collide
        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2")

        assertEquals(10.099723f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(1.2505051f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(10.099723f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.0f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(1.500469f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.000227f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(10.0f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(1.0004189f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(-0.0033333348f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")

        assertEquals(0.0027272739f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.0f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")

        // The sphere is standing on top of the cuboid, and the difference in z speed is small.
        // The strategy is expected to set the sphere's z speed to the cuboid's before bouncing.
        assertEquals(0.030303035f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")
        assertEquals(0.030303035f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // After bounce, the two should no longer collide
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should correctly bounce the sphere off the cuboid's BOTTOM face`() {
        val sphere = getSphere(mass = 9.0f, radius = 0.25f)
        val cuboid = getCuboid(mass = 11.0f, sizeX = 0.5f, sizeY = 0.5f, sizeZ = 0.5f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        // The two should not collide at their initial position
        sphere.pos.set(10.1f, 10.0f, 1.0f)
        cuboid.pos.set(10.0f, 10.0f, 1.501f)
        sphere.applyForces()
        cuboid.applyForces()
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should not collide")

        // Add forces and compute nextSpeed
        sphere.addForce(-9.0f, 0.0f, 20.0f)
        cuboid.addForce(9.0f, 0.0f, -20.0f)
        sphere.applyForces()
        cuboid.applyForces()

        assertEquals(10.099723f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.0f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(1.0006173f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.000227f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(10.0f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(1.500495f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(-0.016666668f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.03703704f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(0.013636365f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.0f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(-0.030303033f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // The two should now collide
        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM_FACE, hit.area2, "hit.area2")

        assertEquals(10.099723f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(1.250495f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(10.099723f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.0f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(1.0005311f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.000227f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(10.0f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(1.5005811f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(-0.0033333348f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(-0.02370371f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(0.0027272739f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.0f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.019393943f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // After bounce, the two should no longer collide
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should correctly bounce the sphere off the cuboid's SOUTH-EAST edge, z-centre`() {
        val sphere = getSphere(mass = 9.0f, radius = 0.25f)
        val cuboid = getCuboid(mass = 11.0f, sizeX = 0.5f, sizeY = 0.5f, sizeZ = 0.5f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        // The two should not collide at their initial position
        sphere.pos.set(10.4196f, 1.4339f, 10.0f)
        cuboid.pos.set(10.0f, 1.0f, 10.0f)
        sphere.applyForces()
        cuboid.applyForces()
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should not collide")

        // Add forces and compute nextSpeed
        sphere.addForce(-4.0f, -4.0f, 1.0f)
        cuboid.addForce(4.0f, 4.0f, -1.0f)
        sphere.applyForces()
        cuboid.applyForces()

        assertEquals(10.4194765f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(1.4337765f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.0000305f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.000101f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(1.000101f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(9.999975f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(-0.0074074077f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.0074074077f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0018518519f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(0.0060606063f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.0060606063f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(-0.0015151516f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // The two should now collide
        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2") // also SOUTH_FACE, but this one's slightly closer

        assertEquals(10.250101f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(1.250101f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0000305f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(10.4194765f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(1.4339039f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.0000305f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.000101f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(0.9999736f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(9.999975f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(-0.0048203617f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.004740741f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0012050904f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(0.0039439322f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(-0.0038787883f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(-0.0009859831f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // After bounce, the two should no longer collide
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should correctly bounce the sphere off the cuboid's NORTH-WEST edge, z-centre`() {
        val sphere = getSphere(mass = 9.0f, radius = 0.25f)
        val cuboid = getCuboid(mass = 11.0f, sizeX = 0.5f, sizeY = 0.5f, sizeZ = 0.5f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        // The two should not collide at their initial position
        sphere.pos.set(10.0f, 1.0f, 10.0f)
        cuboid.pos.set(10.4241f, 1.4295f, 10.0f)
        sphere.applyForces()
        cuboid.applyForces()
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should not collide")

        // Add forces and compute nextSpeed
        sphere.addForce(4.0f, 4.0f, 1.0f)
        cuboid.addForce(-4.0f, -4.0f, -1.0f)
        sphere.applyForces()
        cuboid.applyForces()

        assertEquals(10.000123f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(1.0001235f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.0000305f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.423999f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(1.429399f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(9.999975f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(0.0074074077f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(0.0074074077f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0018518519f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(-0.0060606063f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(-0.0060606063f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(-0.0015151516f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // The two should now collide
        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.WEST_FACE, hit.area2, "hit.area2") // also NORTH_FACE, but this one's slightly closer

        assertEquals(10.173999f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(1.179399f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.0000305f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(10.000123f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(0.9999209f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.0000305f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.423999f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(1.4296017f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(9.999975f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(0.0048203617f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.004740741f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0012050904f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(-0.0039439322f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.0038787883f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(-0.0009859831f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // After bounce, the two should no longer collide
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should correctly bounce the sphere off the cuboid's BOTTOM NORTH-EAST corner`() {
        val sphere = getSphere(mass = 9.0f, radius = 0.25f)
        val cuboid = getCuboid(mass = 11.0f, sizeX = 0.5f, sizeY = 0.5f, sizeZ = 0.5f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        // The two should not collide at their initial position
        sphere.pos.set(10.0f, 1.394f, 10.0f)
        cuboid.pos.set(10.392f, 1.0f, 10.397f)
        sphere.applyForces()
        cuboid.applyForces()
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should not collide")

        // Add forces and compute nextSpeed
        sphere.addForce(2.0f, -2.0f, 2.0f)
        cuboid.addForce(-2.0f, 2.0f, -2.0f)
        sphere.applyForces()
        cuboid.applyForces()

        assertEquals(10.000062f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(1.3939383f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.000062f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.39195f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(1.0000505f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.39695f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(0.0037037039f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.0037037039f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(0.0037037039f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(-0.0030303032f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.0030303032f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(-0.0030303032f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // The two should now collide
        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM_FACE, hit.area2, "hit.area2") // also NORTH_FACE and EAST_FACE

        assertEquals(10.14195f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(1.2500505f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.14695f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(10.000062f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(1.3939383f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(9.999887f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.39195f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(1.0000505f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.397124f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(0.002760895f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
        assertEquals(-0.002760895f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
        assertEquals(-0.0023703706f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

        assertEquals(-0.002258914f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
        assertEquals(0.002258914f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
        assertEquals(0.0019393942f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

        // After bounce, the two should no longer collide
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should force the bodies apart when they collide initially at the cuboid's NORTH face`() {
        val sphere = getSphere(mass = 13.0f, radius = 0.3f)
        val cuboid = getCuboid(mass = 8.0f, sizeX = 0.42f, sizeY = 0.42f, sizeZ = 0.42f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        sphere.apply {
            pos.set(10.0f, 10.002f, 10.003f)
            applyForces()
        }
        cuboid.apply {
            pos.set(10.5f, 10.001f, 10.004f)
            applyForces()
        }

        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

        assertEquals(10.29f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.002f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.003f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(9.994975f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.002f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.003f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.505025f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(10.001f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.004f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(0.0f, sphere.nextSpeed.x, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, "sphere.nextSpeed.z")

        assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x")
        assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z")

        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should force the bodies apart when they collide initially at the cuboid's EAST face`() {
        val sphere = getSphere(mass = 13.0f, radius = 0.3f)
        val cuboid = getCuboid(mass = 8.0f, sizeX = 0.42f, sizeY = 0.42f, sizeZ = 0.42f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        sphere.apply {
            pos.set(10.002f, 10.5f, 10.003f)
            applyForces()
        }
        cuboid.apply {
            pos.set(10.001f, 10.0f, 10.004f)
            applyForces()
        }

        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2")

        assertEquals(10.002f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.21f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.003f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(10.002f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.505025f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.003f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.001f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(9.994975f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.004f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(0.0f, sphere.nextSpeed.x, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, "sphere.nextSpeed.z")

        assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x")
        assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z")

        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should force the bodies apart when they collide initially at the cuboid's SOUTH face`() {
        val sphere = getSphere(mass = 13.0f, radius = 0.3f)
        val cuboid = getCuboid(mass = 8.0f, sizeX = 0.42f, sizeY = 0.42f, sizeZ = 0.42f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        sphere.apply {
            pos.set(10.5f, 10.002f, 10.003f)
            applyForces()
        }
        cuboid.apply {
            pos.set(10.0f, 10.001f, 10.004f)
            applyForces()
        }

        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.SOUTH_FACE, hit.area2, "hit.area2")

        assertEquals(10.21f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.002f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.003f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(10.505025f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.002f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.003f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(9.994975f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(10.001f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.004f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(0.0f, sphere.nextSpeed.x, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, "sphere.nextSpeed.z")

        assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x")
        assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z")

        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should force the bodies apart when they collide initially at the cuboid's WEST face`() {
        val sphere = getSphere(mass = 13.0f, radius = 0.3f)
        val cuboid = getCuboid(mass = 8.0f, sizeX = 0.42f, sizeY = 0.42f, sizeZ = 0.42f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        sphere.apply {
            pos.set(10.002f, 10.0f, 10.003f)
            applyForces()
        }
        cuboid.apply {
            pos.set(10.001f, 10.5f, 10.004f)
            applyForces()
        }

        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.WEST_FACE, hit.area2, "hit.area2")

        assertEquals(10.002f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.29f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.003f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(10.002f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(9.994975f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.003f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.001f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(10.505025f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.004f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(0.0f, sphere.nextSpeed.x, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, "sphere.nextSpeed.z")

        assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x")
        assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z")

        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should force the bodies apart when they collide initially at the cuboid's BOTTOM face`() {
        val sphere = getSphere(mass = 13.0f, radius = 0.3f)
        val cuboid = getCuboid(mass = 8.0f, sizeX = 0.42f, sizeY = 0.42f, sizeZ = 0.42f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        sphere.apply {
            pos.set(10.002f, 10.003f, 10.0f)
            applyForces()
        }
        cuboid.apply {
            pos.set(10.001f, 10.004f, 10.5f)
            applyForces()
        }

        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.BOTTOM_FACE, hit.area2, "hit.area2")

        assertEquals(10.002f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.003f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.29f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(10.002f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.003f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(9.994975f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.001f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(10.004f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(10.505025f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(0.0f, sphere.nextSpeed.x, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, "sphere.nextSpeed.z")

        assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x")
        assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z")

        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should force the bodies apart when they collide initially at the cuboid's TOP face`() {
        val sphere = getSphere(mass = 13.0f, radius = 0.3f)
        val cuboid = getCuboid(mass = 8.0f, sizeX = 0.42f, sizeY = 0.42f, sizeZ = 0.42f, elasticity = 0.8f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        sphere.apply {
            pos.set(10.003f, 10.002f, 10.5f)
            applyForces()
        }
        cuboid.apply {
            pos.set(10.004f, 10.001f, 10.0f)
            applyForces()
        }

        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")
        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2")

        assertEquals(10.003f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(10.002f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(10.21f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        assertEquals(10.003f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(10.002f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(10.505025f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(10.004f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(10.001f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(9.994975f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

        assertEquals(0.0f, sphere.nextSpeed.x, "sphere.nextSpeed.x")
        assertEquals(0.0f, sphere.nextSpeed.y, "sphere.nextSpeed.y")
        assertEquals(0.0f, sphere.nextSpeed.z, "sphere.nextSpeed.z")

        assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x")
        assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y")
        assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z")

        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should force the bodies apart when the sphere is contained inside the cuboid`() {
        val sphere = getSphere(mass = 0.5f, radius = 0.25f, elasticity = 0.9f, friction = 0.02f)
        val cuboid =
            getCuboid(mass = LARGE_MASS, sizeX = 1.0f, sizeY = 1.0f, sizeZ = 1.0f, elasticity = 0.7f, friction = 0.6f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        sphere.apply {
            pos.set(22.250002f, 5.749998f, 2.250002f)
            applyForces()
        }
        cuboid.apply {
            pos.set(22.5f, 5.5f, 2.5f)
            applyForces()
        }

        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide")

        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2")

        assertEquals(22.250002f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(5.749998f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(3.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")

        // The sphere is moved to the cuboid's top
        assertEquals(22.250002f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
        assertEquals(5.749998f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
        assertEquals(3.25005f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

        assertEquals(22.5f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
        assertEquals(5.5f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
        assertEquals(2.5f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")
    }

    @Test
    fun `should properly handle collisions in situation 1`() {
        val sphere = getSphere(mass = 0.5f, radius = 0.25f, elasticity = 0.9f, friction = 0.02f)
        val cuboid =
            getCuboid(mass = LARGE_MASS, sizeX = 1.0f, sizeY = 1.0f, sizeZ = 1.0f, elasticity = 0.7f, friction = 0.6f)

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val bnc = BounceSphereVsCuboid()
        val hit = MutableHitResult()

        sphere.apply {
            pos.set(31.0f, 4.0f, 2.25f)
            speed.set(1.5f, 1.5f, -0.16f)
            applyForces()
        }
        cuboid.apply {
            pos.set(31.5f, 4.5f, 1.5f)
            applyForces()
        }

        var numFaceCoveringsRequested = 0

        // mock brick volume
        val bricks = object: IBrickFaceCoveringRetriever {
            override fun getBrickFaceCovering(
                brickCoords: Point3i,
                faceNormalX: Float,
                faceNormalY: Float,
                faceNormalZ: Float,
            ): BrickFaceCovering {
                numFaceCoveringsRequested++
                return if (faceNormalZ < 0.0f) {
                    BrickFaceCovering.FULLY_COVERED
                } else {
                    BrickFaceCovering.NOT_COVERED
                }
            }
        }

        assertTrue(
            chk.check(
                sphere,
                sphere.nextPos.x,
                sphere.nextPos.y,
                sphere.nextPos.z,
                body1IsBrick = false,
                cuboid,
                cuboid.nextPos.x,
                cuboid.nextPos.y,
                cuboid.nextPos.z,
                body2IsBrick = true,
                bricks,
                brickCoords = Point3i(31, 4, 1),
                hit
            ),
            "should collide"
        )
        assertEquals(3, numFaceCoveringsRequested, "numFaceCoveringsRequested")
        assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
        assertEquals(HitArea.TOP_FACE, hit.area2, "hit.area2")

        assertEquals(31.025f, hit.hitPt.x, TOLERANCE, "hitPt.x")
        assertEquals(4.025f, hit.hitPt.y, TOLERANCE, "hitPt.y")
        assertEquals(2.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

        sep.separate(sphere, cuboid, hit)
        bnc.bounce(sphere, cuboid, hit)
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    @Test
    fun `should properly handle collisions in situation 2`() {
        val sphere = getSphere(mass = 0.52f, radius = 0.25f).apply {
            pos.set(145.04475f, 290.29944f, 1.250008f)
            speed.set(-3.8090732f, -3.1481676f, 0.0f)
            applyForces()
        }

        val cuboid = getCuboid(mass = LARGE_MASS, sizeX = 1.0f, sizeY = 1.0f, sizeZ = 1.0f).apply {
            pos.set(144.5f, 289.5f, 1.5f)
            applyForces()
        }

        val chk = CheckSphereVsCuboid()
        val sep = SeparateSphereVsCuboid()
        val hit = MutableHitResult()

        assertFalse(chk.check(sphere, sphere.pos, cuboid, cuboid.pos, hit), "should not collide at orig pos")
        assertTrue(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should collide at nextPos")

        sep.separate(sphere, cuboid, hit)
        assertFalse(chk.check(sphere, sphere.nextPos, cuboid, cuboid.nextPos, hit), "should no longer collide")
    }

    companion object {
        private const val TOLERANCE = 0.000001f
    }
}
