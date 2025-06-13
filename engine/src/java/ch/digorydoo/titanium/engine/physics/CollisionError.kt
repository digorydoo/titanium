package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody

internal sealed class CollisionError: Exception()

internal class GelCrashedInInnerLoopError(val gel: GraphicElement, val wrappedError: Exception):
    CollisionError()

internal class BodyWasPushedTooFarFromItsOrigPos(
    val body: RigidBody,
    val distance: Float,
    val other: RigidBody,
    val collideAtOrigPos: Boolean,
): CollisionError()

internal class BodyWasPushedTooFarRelatively(
    val body: RigidBody,
    val distance: Float,
    val nextPosBefore: Point3f,
    val other: RigidBody,
    val collideAtOrigPos: Boolean,
): CollisionError()

internal class SeparationFailed(
    val body1: RigidBody,
    val nextPos1Before: Point3f,
    val body2: RigidBody,
    val nextPos2Before: Point3f,
): CollisionError()
