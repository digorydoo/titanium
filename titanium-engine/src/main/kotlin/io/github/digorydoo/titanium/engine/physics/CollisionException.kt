package io.github.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.physics.rigid_body.RigidBody

internal sealed class CollisionException: Exception()

internal class GelCrashedInInnerLoop(val gel: GraphicElement, val wrappedError: Exception):
    CollisionException()

internal class BodyWasPushedTooFarFromItsOrigPos(
    val body: RigidBody,
    val distance: Float,
    val other: RigidBody,
    val collideAtOrigPos: Boolean,
): CollisionException()

internal class BodyWasPushedTooFarRelatively(
    val body: RigidBody,
    val distance: Float,
    val nextPosBefore: Vector3f,
    val other: RigidBody,
    val collideAtOrigPos: Boolean,
): CollisionException()

internal class SeparationFailed(
    val body1: RigidBody,
    val nextPos1Before: Vector3f,
    val body2: RigidBody,
    val nextPos2Before: Vector3f,
): CollisionException()
