package ch.digorydoo.titanium.engine.physics.rigid_body

import ch.digorydoo.kutils.point.MutablePoint3f
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class CollideSphereVsSphereTest {
    private fun getTestBody(
        pos: MutablePoint3f,
        elasticity: Float,
        friction: Float,
        mass: Float,
        gravity: Boolean,
    ): RigidBody {
        // Since RigidBody is a sealed class, I can't derive a TestBody class from it. Instead, just create a
        // FixedSphereBody and pass it back as RigidBody.
        return FixedSphereBody(
            name = "TestBody",
            initialPos = pos,
            elasticity = elasticity,
            friction = friction,
            mass = mass,
            gravity = gravity,
            radius = 1.0f,
        )
    }

    @Test
    fun `should correctly initialize its internal values`() {
        val b1 = getTestBody(
            pos = MutablePoint3f(4.0f, 5.0f, 6.0f),
            mass = 7.0f,
            gravity = false,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        assertEquals("TestBody", b1.name, "name")

        assertEquals(4.0f, b1.pos.x, "pos.x")
        assertEquals(5.0f, b1.pos.y, "pos.y")
        assertEquals(6.0f, b1.pos.z, "pos.z")

        assertEquals(7.0f, b1.mass, "mass")
        assertFalse(b1.gravity, "gravity")
        assertEquals(0.5f, b1.elasticity, "elasticity")
        assertEquals(0.2f, b1.friction, "friction")

        assertEquals(0.0f, b1.speed.x, "speed.x")
        assertEquals(0.0f, b1.speed.y, "speed.y")
        assertEquals(0.0f, b1.speed.z, "speed.z")

        assertEquals(0.0f, b1.nextPos.x, "nextPos.x")
        assertEquals(0.0f, b1.nextPos.y, "nextPos.y")
        assertEquals(0.0f, b1.nextPos.z, "nextPos.z")

        assertEquals(0.0f, b1.nextSpeed.x, "nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, "nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, "nextSpeed.z")

        assertEquals(0.0f, b1.speedBeforeCollisions.x, "speedBeforeCollisions.x")
        assertEquals(0.0f, b1.speedBeforeCollisions.y, "speedBeforeCollisions.y")
        assertEquals(0.0f, b1.speedBeforeCollisions.z, "speedBeforeCollisions.z")

        assertNull(b1.normDirOfSpeedBeforeCollisions, "normDirOfSpeedBeforeCollisions")
    }

    @Test
    fun `should correctly update its values when forces are applied`() {
        val b1 = getTestBody(
            pos = MutablePoint3f(4.0f, 5.0f, 6.0f),
            mass = 7.0f,
            gravity = false,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        // Set initial speed, and add two forces
        b1.speed.set(1.5f, 0.0f, 0.0f)
        b1.addForce(0.0f, 330.0f, -0.2f)
        b1.addForce(0.0f, 3.0f, 4.2f)

        // The resulting force should be the sum of the two
        assertEquals(0.0f, b1.resultingForce.x, "resultingForce.x")
        assertEquals(333.0f, b1.resultingForce.y, TOLERANCE, "resultingForce.y")
        assertEquals(4.0f, b1.resultingForce.z, TOLERANCE, "resultingForce.z")

        // Pretend it's the end of anim phase 1 and prepare for collisions
        b1.applyForces()

        // The resulting force was cleared
        assertEquals(0.0f, b1.resultingForce.x, "resultingForce.x")
        assertEquals(0.0f, b1.resultingForce.y, "resultingForce.y")
        assertEquals(0.0f, b1.resultingForce.z, "resultingForce.z")

        // Speed is still what we've just set
        assertEquals(1.5f, b1.speed.x, "speed.x")
        assertEquals(0.0f, b1.speed.y, "speed.y")
        assertEquals(0.0f, b1.speed.z, "speed.z")

        // nextPos has been moved a bit
        assertEquals(4.025f, b1.nextPos.x, TOLERANCE, "nextPos.x")
        assertEquals(5.013214f, b1.nextPos.y, TOLERANCE, "nextPos.y")
        assertEquals(6.000159f, b1.nextPos.z, TOLERANCE, "nextPos.z")

        // nextSpeed is different to speed, because a force was also applied
        assertEquals(1.5f, b1.nextSpeed.x, TOLERANCE, "nextSpeed.x") // unchanged, because resultingForce.x was zero
        assertEquals(0.7928572f, b1.nextSpeed.y, TOLERANCE, "nextSpeed.y")
        assertEquals(0.00952381f, b1.nextSpeed.z, TOLERANCE, "nextSpeed.z")

        // speedBeforeCollisions has been set to nextSpeed
        assertEquals(b1.nextSpeed.x, b1.speedBeforeCollisions.x, "speedBeforeCollisions.x")
        assertEquals(b1.nextSpeed.y, b1.speedBeforeCollisions.y, "speedBeforeCollisions.y")
        assertEquals(b1.nextSpeed.z, b1.speedBeforeCollisions.z, "speedBeforeCollisions.z")

        // normDirOfSpeedBeforeCollisions has been set to a properly normalised vector
        assertNotNull(b1.normDirOfSpeedBeforeCollisions, "normDirOfSpeedBeforeCollisions")
        assertEquals(1.0f, b1.normDirOfSpeedBeforeCollisions!!.length(), TOLERANCE, "length of normDirOfSpeed")
        assertEquals(0.884081f, b1.normDirOfSpeedBeforeCollisions!!.x, TOLERANCE, "normDirOfSpeedBeforeCollisions.x")
        assertEquals(0.46730003f, b1.normDirOfSpeedBeforeCollisions!!.y, TOLERANCE, "normDirOfSpeedBeforeCollisions.y")
        assertEquals(0.005613213f, b1.normDirOfSpeedBeforeCollisions!!.z, TOLERANCE, "normDirOfSpeedBeforeCollisions.z")
    }

    @Test
    fun `should correctly move the body when told to do so`() {
        val b1 = getTestBody(
            pos = MutablePoint3f(4.0f, 5.0f, 6.0f),
            mass = 7.0f,
            gravity = false,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        // Set initial speed, and add two forces
        b1.speed.set(1.5f, 0.0f, 0.0f)
        b1.addForce(0.0f, 330.0f, -0.2f)
        b1.addForce(0.0f, 3.0f, 4.2f)

        // Pretend it's the end of anim phase 1
        b1.applyForces()

        // nextSpeed was updated according to forces
        assertEquals(1.5f, b1.nextSpeed.x, TOLERANCE, "nextSpeed.x")
        assertEquals(0.7928572f, b1.nextSpeed.y, TOLERANCE, "nextSpeed.y")
        assertEquals(0.00952381f, b1.nextSpeed.z, TOLERANCE, "nextSpeed.z")

        // nextPos was updated according to nextSpeed
        assertEquals(4.025f, b1.nextPos.x, TOLERANCE, "nextPos.x")
        assertEquals(5.013214f, b1.nextPos.y, TOLERANCE, "nextPos.y")
        assertEquals(6.000159f, b1.nextPos.z, TOLERANCE, "nextPos.z")

        // Pretend there was a collision that changed nextPos and nextSpeed a bit
        b1.nextSpeed.x += 0.42f
        b1.nextSpeed.z += 0.42f

        // Collision phase finished, and we tell the body to move
        b1.move()

        // nextSpeed is still how the collisions changed it
        assertEquals(1.5f + 0.42f, b1.nextSpeed.x, TOLERANCE, "nextSpeed.x")
        assertEquals(0.7928572f, b1.nextSpeed.y, TOLERANCE, "nextSpeed.y")
        assertEquals(0.00952381f + 0.42f, b1.nextSpeed.z, TOLERANCE, "nextSpeed.z")

        // speed was set to nextSpeed by move()
        assertEquals(b1.nextSpeed.x, b1.speed.x, "speed.x")
        assertEquals(b1.nextSpeed.y, b1.speed.y, "speed.y")
        assertEquals(b1.nextSpeed.z, b1.speed.z, "speed.z")

        // speedBeforeCollisions is the previous value of nextSpeed before collisions
        assertEquals(1.5f, b1.speedBeforeCollisions.x, "speedBeforeCollisions.x")
        assertEquals(0.7928572f, b1.speedBeforeCollisions.y, "speedBeforeCollisions.y")
        assertEquals(0.00952381f, b1.speedBeforeCollisions.z, "speedBeforeCollisions.z")

        assertNotNull(b1.normDirOfSpeedBeforeCollisions, "normDirOfSpeedBeforeCollisions")
        assertEquals(1.0f, b1.normDirOfSpeedBeforeCollisions!!.length(), TOLERANCE, "length of normDirOfSpeed")
        assertEquals(0.884081f, b1.normDirOfSpeedBeforeCollisions!!.x, TOLERANCE, "normDirOfSpeedBeforeCollisions.x")
        assertEquals(0.46730003f, b1.normDirOfSpeedBeforeCollisions!!.y, TOLERANCE, "normDirOfSpeedBeforeCollisions.y")
        assertEquals(0.005613213f, b1.normDirOfSpeedBeforeCollisions!!.z, TOLERANCE, "normDirOfSpeedBeforeCollisions.z")

        // resultingForce was cleared by applyForces()
        assertEquals(0.0f, b1.resultingForce.x, "resultingForce.x")
        assertEquals(0.0f, b1.resultingForce.y, "resultingForce.y")
        assertEquals(0.0f, b1.resultingForce.z, "resultingForce.z")

        // nextPos is still the position computed by applyForces()
        assertEquals(4.025f, b1.nextPos.x, TOLERANCE, "nextPos.x")
        assertEquals(5.013214f, b1.nextPos.y, TOLERANCE, "nextPos.y")
        assertEquals(6.000159f, b1.nextPos.z, TOLERANCE, "nextPos.z")

        // pos was set to nextPos by move
        assertEquals(b1.nextPos.x, b1.pos.x, "pos.x")
        assertEquals(b1.nextPos.y, b1.pos.y, "pos.y")
        assertEquals(b1.nextPos.z, b1.pos.z, "pos.z")
    }

    @Test
    fun `should correctly stop all motion when told to do so`() {
        val b1 = getTestBody(
            pos = MutablePoint3f(4.0f, 5.0f, 6.0f),
            mass = 7.0f,
            gravity = false,
            elasticity = 0.5f,
            friction = 0.2f,
        )

        // Set initial speed, and add two forces
        b1.speed.set(1.5f, 0.0f, 0.0f)
        b1.addForce(0.0f, 330.0f, -0.2f)
        b1.addForce(0.0f, 3.0f, 4.2f)

        // Pretend it's the end of anim phase 1, and move the body
        b1.applyForces()
        b1.move()

        // Add a new force
        b1.addForce(7.2f, 0.0f, 0.0f)

        // Now we tell it to stop
        b1.stopAllMotion()

        // speed was cleared
        assertEquals(0.0f, b1.speed.x, "speed.x")
        assertEquals(0.0f, b1.speed.y, "speed.y")
        assertEquals(0.0f, b1.speed.z, "speed.z")

        // nextSpeed was cleared
        assertEquals(0.0f, b1.nextSpeed.x, "nextSpeed.x")
        assertEquals(0.0f, b1.nextSpeed.y, "nextSpeed.y")
        assertEquals(0.0f, b1.nextSpeed.z, "nextSpeed.z")

        // speedBeforeCollisions was cleared
        assertEquals(0.0f, b1.speedBeforeCollisions.x, "speedBeforeCollisions.x")
        assertEquals(0.0f, b1.speedBeforeCollisions.y, "speedBeforeCollisions.y")
        assertEquals(0.0f, b1.speedBeforeCollisions.z, "speedBeforeCollisions.z")
        assertNull(b1.normDirOfSpeedBeforeCollisions, "normDirOfSpeedBeforeCollisions")

        // resultingForce was cleared
        assertEquals(0.0f, b1.resultingForce.x, "resultingForce.x")
        assertEquals(0.0f, b1.resultingForce.y, "resultingForce.y")
        assertEquals(0.0f, b1.resultingForce.z, "resultingForce.z")

        // nextPos is the position computed by applyForces
        assertEquals(4.025f, b1.nextPos.x, TOLERANCE, "nextPos.x")
        assertEquals(5.013214f, b1.nextPos.y, TOLERANCE, "nextPos.y")
        assertEquals(6.000159f, b1.nextPos.z, TOLERANCE, "nextPos.z")

        // pos was set to nextPos by move
        assertEquals(b1.nextPos.x, b1.pos.x, "pos.x")
        assertEquals(b1.nextPos.y, b1.pos.y, "pos.y")
        assertEquals(b1.nextPos.z, b1.pos.z, "pos.z")
    }

    companion object {
        private const val TOLERANCE = 0.000001f
    }
}
