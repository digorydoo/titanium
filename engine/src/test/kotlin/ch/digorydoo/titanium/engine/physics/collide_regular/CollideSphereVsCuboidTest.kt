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
            pos = MutablePoint3f(),
            mass = 10.0f,
            gravity = false,
            radius = 0.5f,
            zOffset = 0.0f,
            elasticity = 0.8f,
            friction = 0.2f,
        )
        val b2 = FixedCuboidBody(
            "b2",
            pos = MutablePoint3f(),
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

    companion object {
        private const val TOLERANCE = 0.000001f

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            Log.ttyOutput = Log.TtyOutput.OFF
        }
    }
}
