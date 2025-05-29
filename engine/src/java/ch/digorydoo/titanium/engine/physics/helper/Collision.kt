package ch.digorydoo.titanium.engine.physics.helper

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.gel.GraphicElement

internal sealed class Collision: HitResult

internal class GelCollision(
    val gel1: GraphicElement,
    val gel2: GraphicElement,
    override val hitPt: Point3f,
    override val hitNormal12: Point3f, // normal at hitPt pointing from body1 to body2
    override val area1: HitArea, // refers to gel1
    override val area2: HitArea, // refers to gel2
): Collision()

internal class BrickCollision(
    val gel: GraphicElement,
    val brickCoords: Point3i,
    val shape: BrickShape,
    val material: BrickMaterial,
    override val hitPt: Point3f,
    override val hitNormal12: Point3f, // normal at hitPt pointing from gel to brick
    override val area1: HitArea, // refers to gel
    override val area2: HitArea, // refers to brick
): Collision()
