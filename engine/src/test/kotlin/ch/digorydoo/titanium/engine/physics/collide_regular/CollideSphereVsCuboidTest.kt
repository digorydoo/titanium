package ch.digorydoo.titanium.engine.physics.collide_regular

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.MutableHitResult
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedCuboidBody
import ch.digorydoo.titanium.engine.physics.rigid_body.FixedSphereBody
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CollideSphereVsCuboidTest {
    @Test
    fun `should correctly check sphere against cuboid`() {
        val b1 = FixedSphereBody(
            "b1",
            initialPos = MutablePoint3f(),
            mass = 10.0f,
            gravity = false,
            radius = 0.5f,
            elasticity = 0.8f,
            friction = 0.2f,
        )
        val b2 = FixedCuboidBody(
            "b2",
            initialPos = MutablePoint3f(),
            mass = 11.0f,
            gravity = false,
            elasticity = 0.5f,
            friction = 0.2f,
            sizeX = 0.4f,
            sizeY = 0.3f,
            sizeZ = 0.2f,
        )

        val ck = CollideSphereVsCuboid()
        val hit = MutableHitResult()

        fun check() = ck.checkAndBounceIfNeeded(b1, b2, canBounce = false, hit)

        fun shouldCollideAt(p1: Point3f, p2: Point3f, area: HitArea, expectedHitPt: Point3f) {
            b1.nextPos.set(p1)
            b2.nextPos.set(p2)

            assertTrue(check(), "should collide at $p1 vs $p2")
            assertEquals(HitArea.UNSPECIFIED, hit.area1, "area1 should be UNSPECIFIED")
            assertEquals(area, hit.area2, "area2 should be $area")

            assertEquals(expectedHitPt.x, hit.hitPt.x, TOLERANCE, "hitPt.x ($area)")
            assertEquals(expectedHitPt.y, hit.hitPt.y, TOLERANCE, "hitPt.y ($area)")
            assertEquals(expectedHitPt.z, hit.hitPt.z, TOLERANCE, "hitPt.z ($area)")
        }

        fun shouldNotCollideAt(p1: Point3f, p2: Point3f) {
            b1.nextPos.set(p1)
            b2.nextPos.set(p2)
            assertFalse(check(), "should not collide at $p1 vs $p2, but collided with area ${hit.area2}")
        }

        // Check the sitation of complete overlap
        shouldCollideAt(
            Point3f(10.0f, 10.0f, 10.0f),
            Point3f(10.0f, 10.0f, 10.0f),
            HitArea.NORTH_FACE, // this is arbitrary since we have a hit on all sides
            Point3f(9.8f, 10.0f, 10.0f), // seems odd, but correct since the hit point is always on the colliding face
        )

        val x = 10.0f
        val y = 10.0f
        val z = 10.0f
        val r = b1.radius
        val xsouth = x + b2.size.x / 2
        val xnorth = x - b2.size.x / 2
        val yeast = y + b2.size.y / 2
        val ywest = y - b2.size.y / 2
        val ztop = z + b2.size.z / 2
        val zbottom = z - b2.size.z / 2

        // North face
        run {
            // Centre
            shouldCollideAt(
                Point3f(xnorth - r + 0.000001f, y, z),
                Point3f(x, y, z),
                HitArea.NORTH_FACE,
                Point3f(9.8f, y, z),
            )
            shouldNotCollideAt(Point3f(xnorth - r - 0.000001f, y, z), Point3f(x, y, z))

            // Close to the top
            shouldCollideAt(
                Point3f(xnorth - r + 0.000001f, y, ztop - 0.001f),
                Point3f(x, y, z),
                HitArea.NORTH_FACE,
                Point3f(9.8f, y, 10.099f),
            )
            shouldNotCollideAt(Point3f(xnorth - r + 0.000001f, y, ztop + 0.001f), Point3f(x, y, z))

            // Close to the bottom
            shouldCollideAt(
                Point3f(xnorth - r + 0.000001f, y, zbottom + 0.001f),
                Point3f(x, y, z),
                HitArea.NORTH_FACE,
                Point3f(9.8f, y, 9.901f),
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
            )
            shouldNotCollideAt(Point3f(x, yeast + r + 0.000001f, z), Point3f(x, y, z))

            // Close to the top
            shouldCollideAt(
                Point3f(x, yeast + r - 0.000001f, ztop - 0.001f),
                Point3f(x, y, z),
                HitArea.EAST_FACE,
                Point3f(x, 10.15f, 10.099f),
            )
            shouldNotCollideAt(Point3f(x, yeast + r - 0.000001f, ztop + 0.001f), Point3f(x, y, z))

            // Close to the bottom
            shouldCollideAt(
                Point3f(x, yeast + r - 0.000001f, zbottom + 0.001f),
                Point3f(x, y, z),
                HitArea.EAST_FACE,
                Point3f(x, 10.15f, 9.901f),
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
            )
            shouldNotCollideAt(Point3f(xsouth + r + 0.000001f, y, z), Point3f(x, y, z))

            // Close to the top
            shouldCollideAt(
                Point3f(xsouth + r - 0.000001f, y, ztop - 0.001f),
                Point3f(x, y, z),
                HitArea.SOUTH_FACE,
                Point3f(10.2f, y, 10.099f),
            )
            shouldNotCollideAt(Point3f(xsouth + r - 0.000001f, y, ztop + 0.001f), Point3f(x, y, z))

            // Close to the bottom
            shouldCollideAt(
                Point3f(xsouth + r - 0.000001f, y, zbottom + 0.001f),
                Point3f(x, y, z),
                HitArea.SOUTH_FACE,
                Point3f(10.2f, y, 9.901f),
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
            )
            shouldNotCollideAt(Point3f(x, ywest - r - 0.000001f, z), Point3f(x, y, z))

            // Close to the top
            shouldCollideAt(
                Point3f(x, ywest - r + 0.000001f, ztop - 0.001f),
                Point3f(x, y, z),
                HitArea.WEST_FACE,
                Point3f(x, 9.85f, 10.099f),
            )
            shouldNotCollideAt(Point3f(x, ywest - r + 0.000001f, ztop + 0.001f), Point3f(x, y, z))

            // Close to the bottom
            shouldCollideAt(
                Point3f(x, ywest - r + 0.000001f, zbottom + 0.001f),
                Point3f(x, y, z),
                HitArea.WEST_FACE,
                Point3f(x, 9.85f, 9.901f),
            )
            shouldNotCollideAt(Point3f(x, ywest - r + 0.000001f, zbottom - 0.001f), Point3f(x, y, z))
        }

        // Top face
        run {
            // Centre
            shouldCollideAt(
                Point3f(x, y, ztop + r - 0.000001f),
                Point3f(x, y, z),
                HitArea.TOP,
                Point3f(x, y, 10.1f),
            )
            shouldNotCollideAt(Point3f(x, y, ztop + r + 0.000001f), Point3f(x, y, z))

            // Close to the north
            shouldCollideAt(
                Point3f(xnorth, y, ztop + r - 0.000001f),
                Point3f(x, y, z),
                HitArea.TOP,
                Point3f(9.8f, y, 10.1f),
            )
            shouldNotCollideAt(Point3f(xnorth, y, ztop + r + 0.000001f), Point3f(x, y, z))

            // Close to the east
            shouldCollideAt(
                Point3f(x, yeast, ztop + r - 0.000001f),
                Point3f(x, y, z),
                HitArea.TOP,
                Point3f(x, 10.15f, 10.1f),
            )
            shouldNotCollideAt(Point3f(x, yeast, ztop + r + 0.000001f), Point3f(x, y, z))

            // Close to the south
            shouldCollideAt(
                Point3f(xsouth, y, ztop + r - 0.000001f),
                Point3f(x, y, z),
                HitArea.TOP,
                Point3f(10.2f, y, 10.1f),
            )
            shouldNotCollideAt(Point3f(xsouth, y, ztop + r + 0.000001f), Point3f(x, y, z))

            // Close to the west
            shouldCollideAt(
                Point3f(x, ywest, ztop + r - 0.000001f),
                Point3f(x, y, z),
                HitArea.TOP,
                Point3f(x, 9.85f, 10.1f),
            )
            shouldNotCollideAt(Point3f(x, ywest, ztop + r + 0.000001f), Point3f(x, y, z))
        }

        // Bottom face
        run {
            // Centre
            shouldCollideAt(
                Point3f(x, y, zbottom - r + 0.000001f),
                Point3f(x, y, z),
                HitArea.BOTTOM,
                Point3f(x, y, 9.9f),
            )
            shouldNotCollideAt(Point3f(x, y, zbottom - r - 0.000001f), Point3f(x, y, z))

            // Close to the north
            shouldCollideAt(
                Point3f(xnorth, y, zbottom - r + 0.000001f),
                Point3f(x, y, z),
                HitArea.BOTTOM,
                Point3f(9.8f, y, 9.9f),
            )
            shouldNotCollideAt(Point3f(xnorth, y, zbottom - r - 0.000001f), Point3f(x, y, z))

            // Close to the east
            shouldCollideAt(
                Point3f(x, yeast, zbottom - r + 0.000001f),
                Point3f(x, y, z),
                HitArea.BOTTOM,
                Point3f(x, 10.15f, 9.9f),
            )
            shouldNotCollideAt(Point3f(x, yeast, zbottom - r - 0.000001f), Point3f(x, y, z))

            // Close to the south
            shouldCollideAt(
                Point3f(xsouth, y, zbottom - r + 0.000001f),
                Point3f(x, y, z),
                HitArea.BOTTOM,
                Point3f(10.2f, y, 9.9f),
            )
            shouldNotCollideAt(Point3f(xsouth, y, zbottom - r - 0.000001f), Point3f(x, y, z))

            // Close to the west
            shouldCollideAt(
                Point3f(x, ywest, zbottom - r + 0.000001f),
                Point3f(x, y, z),
                HitArea.BOTTOM,
                Point3f(x, 9.85f, 9.9f),
            )
            shouldNotCollideAt(Point3f(x, ywest, zbottom - r - 0.000001f), Point3f(x, y, z))
        }

        // Top south east corner
        run {
            // When the sphere's centre point is aligned to the corner in two axes
            shouldCollideAt(
                Point3f(xsouth + r - 0.000001f, yeast, ztop),
                Point3f(x, y, z),
                HitArea.SOUTH_FACE,
                Point3f(10.2f, 10.15f, 10.1f),
            )
            shouldNotCollideAt(Point3f(xsouth + r + 0.000001f, yeast, ztop), Point3f(x, y, z))
            shouldCollideAt(
                Point3f(xsouth, yeast + r - 0.000001f, ztop),
                Point3f(x, y, z),
                HitArea.EAST_FACE,
                Point3f(10.2f, 10.15f, 10.1f),
            )
            shouldNotCollideAt(Point3f(xsouth, yeast + r + 0.000001f, ztop), Point3f(x, y, z))
            shouldCollideAt(
                Point3f(xsouth, yeast, ztop + r - 0.000001f),
                Point3f(x, y, z),
                HitArea.TOP,
                Point3f(10.2f, 10.15f, 10.1f),
            )
            shouldNotCollideAt(Point3f(xsouth, yeast, ztop + r + 0.000001f), Point3f(x, y, z))

            // When the centre point isn't perfectly aligned with the corner in any axis
            shouldCollideAt(
                Point3f(xsouth + r - 0.22f, yeast + r - 0.209f, ztop + r - 0.206f),
                Point3f(x, y, z),
                HitArea.TOP,
                Point3f(10.2f, 10.15f, 10.1f),
            )
            shouldCollideAt(
                Point3f(xsouth + r - 0.209f, yeast + r - 0.206f, ztop + r - 0.22f),
                Point3f(x, y, z),
                HitArea.EAST_FACE,
                Point3f(10.2f, 10.15f, 10.1f),
            )
            shouldCollideAt(
                Point3f(xsouth + r - 0.206f, yeast + r - 0.22f, ztop + r - 0.209f),
                Point3f(x, y, z),
                HitArea.SOUTH_FACE,
                Point3f(10.2f, 10.15f, 10.1f),
            )
        }

        // Bottom north west corner
        run {
            // When the sphere's centre point is aligned to the corner in two axes
            shouldCollideAt(
                Point3f(xnorth - r + 0.000001f, ywest, zbottom),
                Point3f(x, y, z),
                HitArea.NORTH_FACE,
                Point3f(9.8f, 9.85f, 9.9f),
            )
            shouldNotCollideAt(Point3f(xnorth - r - 0.000001f, ywest, zbottom), Point3f(x, y, z))
            shouldCollideAt(
                Point3f(xnorth, ywest - r + 0.000001f, zbottom),
                Point3f(x, y, z),
                HitArea.WEST_FACE,
                Point3f(9.8f, 9.85f, 9.9f),
            )
            shouldNotCollideAt(Point3f(xnorth, ywest - r - 0.000001f, zbottom), Point3f(x, y, z))
            shouldCollideAt(
                Point3f(xnorth, ywest, zbottom - r + 0.000001f),
                Point3f(x, y, z),
                HitArea.BOTTOM,
                Point3f(9.8f, 9.85f, 9.9f),
            )
            shouldNotCollideAt(Point3f(xnorth, ywest, zbottom - r - 0.000001f), Point3f(x, y, z))

            // When the centre point isn't perfectly aligned with the corner in any axis
            shouldCollideAt(
                Point3f(xnorth - r + 0.22f, ywest - r + 0.209f, zbottom - r + 0.206f),
                Point3f(x, y, z),
                HitArea.BOTTOM,
                Point3f(9.8f, 9.85f, 9.9f),
            )
            shouldCollideAt(
                Point3f(xnorth - r + 0.209f, ywest - r + 0.206f, zbottom - r + 0.22f),
                Point3f(x, y, z),
                HitArea.WEST_FACE,
                Point3f(9.8f, 9.85f, 9.9f),
            )
            shouldCollideAt(
                Point3f(xnorth - r + 0.206f, ywest - r + 0.22f, zbottom - r + 0.209f),
                Point3f(x, y, z),
                HitArea.NORTH_FACE,
                Point3f(9.8f, 9.85f, 9.9f),
            )
        }
    }

    @Test
    fun `should correctly bounce the sphere off the cuboid`() {
        val sphere = FixedSphereBody(
            "b1",
            initialPos = MutablePoint3f(),
            mass = 9.0f,
            gravity = false,
            radius = 0.25f,
            elasticity = 0.8f,
            friction = 0.2f,
        )
        val cuboid = FixedCuboidBody(
            "b2",
            initialPos = MutablePoint3f(),
            mass = 11.0f,
            gravity = false,
            elasticity = 0.8f,
            friction = 0.2f,
            sizeX = 0.5f,
            sizeY = 0.5f,
            sizeZ = 0.5f,
        )
        val ck = CollideSphereVsCuboid()
        val hit = MutableHitResult()

        // North face
        run {
            // The two should not collide at their initial position
            sphere.pos.set(1.0f, 10.1f, 10.0f)
            cuboid.pos.set(1.501f, 10.0f, 10.0f)
            sphere.applyForces()
            cuboid.applyForces()
            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should not collide")

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
            assertTrue(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = true, hit), "should collide")

            assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
            assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

            assertEquals(1.250495f, hit.hitPt.x, TOLERANCE, "hitPt.x")
            assertEquals(10.099723f, hit.hitPt.y, TOLERANCE, "hitPt.y")
            assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

            assertEquals(1.0003086f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
            assertEquals(10.099861f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
            assertEquals(10.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

            assertEquals(1.5007474f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
            assertEquals(10.0001135f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
            assertEquals(10.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

            assertEquals(-0.02370371f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
            assertEquals(-0.0033333348f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
            assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

            assertEquals(0.019393943f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
            assertEquals(0.0027272739f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
            assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

            // After bounce, the two should no longer collide
            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should no longer collide")
        }

        // East face
        run {
            // The two should not collide at their initial position
            sphere.pos.set(10.1f, 1.501f, 10.0f)
            cuboid.pos.set(10.0f, 1.0f, 10.0f)
            sphere.applyForces()
            cuboid.applyForces()
            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should not collide")

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
            assertTrue(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = true, hit), "should collide")

            assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
            assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2")

            assertEquals(10.099723f, hit.hitPt.x, TOLERANCE, "hitPt.x")
            assertEquals(1.2505051f, hit.hitPt.y, TOLERANCE, "hitPt.y")
            assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

            assertEquals(10.099861f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
            assertEquals(1.5006914f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
            assertEquals(10.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

            assertEquals(10.0001135f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
            assertEquals(1.0002525f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
            assertEquals(10.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

            assertEquals(-0.0033333348f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
            assertEquals(0.02370371f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
            assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

            assertEquals(0.0027272739f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
            assertEquals(-0.019393943f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
            assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

            // After bounce, the two should no longer collide
            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should no longer collide")
        }

        // South face
        run {
            // The two should not collide at their initial position
            sphere.pos.set(1.501f, 10.1f, 10.0f)
            cuboid.pos.set(1.0f, 10.0f, 10.0f)
            sphere.applyForces()
            cuboid.applyForces()
            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should not collide")

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
            assertTrue(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = true, hit), "should collide")

            assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
            assertEquals(HitArea.SOUTH_FACE, hit.area2, "hit.area2")

            assertEquals(1.2505051f, hit.hitPt.x, TOLERANCE, "hitPt.x")
            assertEquals(10.099723f, hit.hitPt.y, TOLERANCE, "hitPt.y")
            assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

            assertEquals(1.5006914f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
            assertEquals(10.099861f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
            assertEquals(10.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

            assertEquals(1.0002525f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
            assertEquals(10.0001135f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
            assertEquals(10.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

            assertEquals(0.02370371f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
            assertEquals(-0.0033333348f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
            assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

            assertEquals(-0.019393943f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
            assertEquals(0.0027272739f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
            assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

            // After bounce, the two should no longer collide
            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should no longer collide")
        }

        // West face
        run {
            // The two should not collide at their initial position
            sphere.pos.set(10.1f, 1.0f, 10.0f)
            cuboid.pos.set(10.0f, 1.501f, 10.0f)
            sphere.applyForces()
            cuboid.applyForces()
            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should not collide")

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
            assertTrue(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = true, hit), "should collide")

            assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
            assertEquals(HitArea.WEST_FACE, hit.area2, "hit.area2")

            assertEquals(10.099723f, hit.hitPt.x, TOLERANCE, "hitPt.x")
            assertEquals(1.250495f, hit.hitPt.y, TOLERANCE, "hitPt.y")
            assertEquals(10.0f, hit.hitPt.z, TOLERANCE, "hitPt.z")

            assertEquals(10.099861f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
            assertEquals(1.0003086f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
            assertEquals(10.0f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

            assertEquals(10.0001135f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
            assertEquals(1.5007474f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
            assertEquals(10.0f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

            assertEquals(-0.0033333348f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
            assertEquals(-0.02370371f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
            assertEquals(0.0f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

            assertEquals(0.0027272739f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
            assertEquals(0.019393943f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
            assertEquals(0.0f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

            // After bounce, the two should no longer collide
            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should no longer collide")
        }

        // Top face
        run {
            // The two should not collide at their initial position
            sphere.pos.set(10.1f, 10.0f, 1.501f)
            cuboid.pos.set(10.0f, 10.0f, 1.0f)
            sphere.applyForces()
            cuboid.applyForces()
            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should not collide")

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
            assertTrue(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = true, hit), "should collide")

            assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
            assertEquals(HitArea.TOP, hit.area2, "hit.area2")

            assertEquals(10.099723f, hit.hitPt.x, TOLERANCE, "hitPt.x")
            assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
            assertEquals(1.2505051f, hit.hitPt.z, TOLERANCE, "hitPt.z")

            assertEquals(10.099861f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
            assertEquals(10.0f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
            assertEquals(1.5006914f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

            assertEquals(10.0001135f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
            assertEquals(10.0f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
            assertEquals(1.0002525f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

            assertEquals(-0.0033333348f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
            assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
            assertEquals(0.02370371f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

            assertEquals(0.0027272739f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
            assertEquals(0.0f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
            assertEquals(-0.019393943f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

            // After bounce, the two should no longer collide
            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should no longer collide")
        }

        // Bottom face
        run {
            // The two should not collide at their initial position
            sphere.pos.set(10.1f, 10.0f, 1.0f)
            cuboid.pos.set(10.0f, 10.0f, 1.501f)
            sphere.applyForces()
            cuboid.applyForces()
            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should not collide")

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
            assertTrue(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = true, hit), "should collide")

            assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
            assertEquals(HitArea.BOTTOM, hit.area2, "hit.area2")

            assertEquals(10.099723f, hit.hitPt.x, TOLERANCE, "hitPt.x")
            assertEquals(10.0f, hit.hitPt.y, TOLERANCE, "hitPt.y")
            assertEquals(1.250495f, hit.hitPt.z, TOLERANCE, "hitPt.z")

            assertEquals(10.099861f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
            assertEquals(10.0f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
            assertEquals(1.0003086f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

            assertEquals(10.0001135f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
            assertEquals(10.0f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
            assertEquals(1.5007474f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

            assertEquals(-0.0033333348f, sphere.nextSpeed.x, TOLERANCE, "sphere.nextSpeed.x")
            assertEquals(0.0f, sphere.nextSpeed.y, TOLERANCE, "sphere.nextSpeed.y")
            assertEquals(-0.02370371f, sphere.nextSpeed.z, TOLERANCE, "sphere.nextSpeed.z")

            assertEquals(0.0027272739f, cuboid.nextSpeed.x, TOLERANCE, "cuboid.nextSpeed.x")
            assertEquals(0.0f, cuboid.nextSpeed.y, TOLERANCE, "cuboid.nextSpeed.y")
            assertEquals(0.019393943f, cuboid.nextSpeed.z, TOLERANCE, "cuboid.nextSpeed.z")

            // After bounce, the two should no longer collide
            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should no longer collide")
        }
    }

    @Test
    fun `should force the bodies apart when there is a clear direction of separation`() {
        val sphere = FixedSphereBody(
            "b1",
            initialPos = MutablePoint3f(),
            mass = 13.0f,
            gravity = false,
            radius = 0.3f,
            elasticity = 0.8f,
            friction = 0.2f,
        )
        val cuboid = FixedCuboidBody(
            "b2",
            initialPos = MutablePoint3f(),
            mass = 8.0f,
            gravity = false,
            elasticity = 0.8f,
            friction = 0.2f,
            sizeX = 0.42f,
            sizeY = 0.42f,
            sizeZ = 0.42f,
        )
        val ck = CollideSphereVsCuboid()
        val hit = MutableHitResult()

        // North face
        run {
            sphere.apply {
                pos.set(10.0f, 10.002f, 10.003f)
                applyForces()
            }
            cuboid.apply {
                pos.set(10.5f, 10.001f, 10.004f)
                applyForces()
            }
            assertTrue(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = true, hit), "should collide")

            assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
            assertEquals(HitArea.NORTH_FACE, hit.area2, "hit.area2")

            assertEquals(10.29f, hit.hitPt.x, TOLERANCE, "hitPt.x")
            assertEquals(10.002f, hit.hitPt.y, TOLERANCE, "hitPt.y")
            assertEquals(10.003f, hit.hitPt.z, TOLERANCE, "hitPt.z")

            assertEquals(9.994999f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
            assertEquals(10.002009f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
            assertEquals(10.002991f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

            assertEquals(10.505001f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
            assertEquals(10.000991f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
            assertEquals(10.004009f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

            assertEquals(0.0f, sphere.nextSpeed.x, "sphere.nextSpeed.x")
            assertEquals(0.0f, sphere.nextSpeed.y, "sphere.nextSpeed.y")
            assertEquals(0.0f, sphere.nextSpeed.z, "sphere.nextSpeed.z")

            assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x")
            assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y")
            assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z")

            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should no longer collide")
        }

        // East face
        run {
            sphere.apply {
                pos.set(10.002f, 10.5f, 10.003f)
                applyForces()
            }
            cuboid.apply {
                pos.set(10.001f, 10.0f, 10.004f)
                applyForces()
            }
            assertTrue(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = true, hit), "should collide")

            assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
            assertEquals(HitArea.EAST_FACE, hit.area2, "hit.area2")

            assertEquals(10.002f, hit.hitPt.x, TOLERANCE, "hitPt.x")
            assertEquals(10.21f, hit.hitPt.y, TOLERANCE, "hitPt.y")
            assertEquals(10.003f, hit.hitPt.z, TOLERANCE, "hitPt.z")

            assertEquals(10.002009f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
            assertEquals(10.505001f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
            assertEquals(10.002991f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

            assertEquals(10.000991f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
            assertEquals(9.994999f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
            assertEquals(10.004009f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

            assertEquals(0.0f, sphere.nextSpeed.x, "sphere.nextSpeed.x")
            assertEquals(0.0f, sphere.nextSpeed.y, "sphere.nextSpeed.y")
            assertEquals(0.0f, sphere.nextSpeed.z, "sphere.nextSpeed.z")

            assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x")
            assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y")
            assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z")

            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should no longer collide")
        }

        // South face
        run {
            sphere.apply {
                pos.set(10.5f, 10.002f, 10.003f)
                applyForces()
            }
            cuboid.apply {
                pos.set(10.0f, 10.001f, 10.004f)
                applyForces()
            }
            assertTrue(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = true, hit), "should collide")

            assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
            assertEquals(HitArea.SOUTH_FACE, hit.area2, "hit.area2")

            assertEquals(10.21f, hit.hitPt.x, TOLERANCE, "hitPt.x")
            assertEquals(10.002f, hit.hitPt.y, TOLERANCE, "hitPt.y")
            assertEquals(10.003f, hit.hitPt.z, TOLERANCE, "hitPt.z")

            assertEquals(10.505001f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
            assertEquals(10.002009f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
            assertEquals(10.002991f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

            assertEquals(9.994999f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
            assertEquals(10.000991f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
            assertEquals(10.004009f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

            assertEquals(0.0f, sphere.nextSpeed.x, "sphere.nextSpeed.x")
            assertEquals(0.0f, sphere.nextSpeed.y, "sphere.nextSpeed.y")
            assertEquals(0.0f, sphere.nextSpeed.z, "sphere.nextSpeed.z")

            assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x")
            assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y")
            assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z")

            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should no longer collide")
        }

        // West face
        run {
            sphere.apply {
                pos.set(10.002f, 10.0f, 10.003f)
                applyForces()
            }
            cuboid.apply {
                pos.set(10.001f, 10.5f, 10.004f)
                applyForces()
            }
            assertTrue(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = true, hit), "should collide")

            assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
            assertEquals(HitArea.WEST_FACE, hit.area2, "hit.area2")

            assertEquals(10.002f, hit.hitPt.x, TOLERANCE, "hitPt.x")
            assertEquals(10.29f, hit.hitPt.y, TOLERANCE, "hitPt.y")
            assertEquals(10.003f, hit.hitPt.z, TOLERANCE, "hitPt.z")

            assertEquals(10.002009f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
            assertEquals(9.994999f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
            assertEquals(10.002991f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

            assertEquals(10.000991f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
            assertEquals(10.505001f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
            assertEquals(10.004009f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

            assertEquals(0.0f, sphere.nextSpeed.x, "sphere.nextSpeed.x")
            assertEquals(0.0f, sphere.nextSpeed.y, "sphere.nextSpeed.y")
            assertEquals(0.0f, sphere.nextSpeed.z, "sphere.nextSpeed.z")

            assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x")
            assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y")
            assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z")

            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should no longer collide")
        }

        // Bottom face
        run {
            sphere.apply {
                pos.set(10.002f, 10.003f, 10.0f)
                applyForces()
            }
            cuboid.apply {
                pos.set(10.001f, 10.004f, 10.5f)
                applyForces()
            }
            assertTrue(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = true, hit), "should collide")

            assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
            assertEquals(HitArea.BOTTOM, hit.area2, "hit.area2")

            assertEquals(10.002f, hit.hitPt.x, TOLERANCE, "hitPt.x")
            assertEquals(10.003f, hit.hitPt.y, TOLERANCE, "hitPt.y")
            assertEquals(10.29f, hit.hitPt.z, TOLERANCE, "hitPt.z")

            assertEquals(10.002009f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
            assertEquals(10.002991f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
            assertEquals(9.994999f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

            assertEquals(10.000991f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
            assertEquals(10.004009f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
            assertEquals(10.505001f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

            assertEquals(0.0f, sphere.nextSpeed.x, "sphere.nextSpeed.x")
            assertEquals(0.0f, sphere.nextSpeed.y, "sphere.nextSpeed.y")
            assertEquals(0.0f, sphere.nextSpeed.z, "sphere.nextSpeed.z")

            assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x")
            assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y")
            assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z")

            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should no longer collide")
        }

        // Top face
        run {
            sphere.apply {
                pos.set(10.003f, 10.002f, 10.5f)
                applyForces()
            }
            cuboid.apply {
                pos.set(10.004f, 10.001f, 10.0f)
                applyForces()
            }
            assertTrue(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = true, hit), "should collide")

            assertEquals(HitArea.UNSPECIFIED, hit.area1, "hit.area1")
            assertEquals(HitArea.TOP, hit.area2, "hit.area2")

            assertEquals(10.003f, hit.hitPt.x, TOLERANCE, "hitPt.x")
            assertEquals(10.002f, hit.hitPt.y, TOLERANCE, "hitPt.y")
            assertEquals(10.21f, hit.hitPt.z, TOLERANCE, "hitPt.z")

            assertEquals(10.002991f, sphere.nextPos.x, TOLERANCE, "sphere.nextPos.x")
            assertEquals(10.002009f, sphere.nextPos.y, TOLERANCE, "sphere.nextPos.y")
            assertEquals(10.505001f, sphere.nextPos.z, TOLERANCE, "sphere.nextPos.z")

            assertEquals(10.004009f, cuboid.nextPos.x, TOLERANCE, "cuboid.nextPos.x")
            assertEquals(10.000991f, cuboid.nextPos.y, TOLERANCE, "cuboid.nextPos.y")
            assertEquals(9.994999f, cuboid.nextPos.z, TOLERANCE, "cuboid.nextPos.z")

            assertEquals(0.0f, sphere.nextSpeed.x, "sphere.nextSpeed.x")
            assertEquals(0.0f, sphere.nextSpeed.y, "sphere.nextSpeed.y")
            assertEquals(0.0f, sphere.nextSpeed.z, "sphere.nextSpeed.z")

            assertEquals(0.0f, cuboid.nextSpeed.x, "cuboid.nextSpeed.x")
            assertEquals(0.0f, cuboid.nextSpeed.y, "cuboid.nextSpeed.y")
            assertEquals(0.0f, cuboid.nextSpeed.z, "cuboid.nextSpeed.z")

            assertFalse(ck.checkAndBounceIfNeeded(sphere, cuboid, canBounce = false, hit), "should no longer collide")
        }
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
