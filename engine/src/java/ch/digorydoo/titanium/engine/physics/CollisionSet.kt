package ch.digorydoo.titanium.engine.physics

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.gel.GraphicElement

internal class CollisionSet {
    private val list = mutableListOf<Collision>()

    fun clear() {
        list.clear()
    }

    // Also searches reverse order
    private fun find(gel1: GraphicElement, gel2: GraphicElement) = list.find {
        it is GelCollision && ((it.gel1 == gel1 && it.gel2 == gel2) || (it.gel1 == gel2 && it.gel2 == gel1))
    }

    private fun find(gel: GraphicElement, brickCoords: Point3i) = list.find {
        it is BrickCollision && it.gel == gel && it.brickCoords.x == brickCoords.x &&
            it.brickCoords.y == brickCoords.y && it.brickCoords.z == brickCoords.z
    }

    fun add(gel1: GraphicElement, gel2: GraphicElement, hit: HitResult) {
        if (find(gel1, gel2) != null) {
            return // do not add duplicates
        }

        list.add(
            GelCollision(
                gel1,
                gel2,
                hitPt = Point3f(hit.hitPt), // hit may be a MutableHitResult, so we need to clone
                hitNormal12 = Point3f(hit.hitNormal12),
                area1 = hit.area1,
                area2 = hit.area2,
            )
        )
    }

    fun add(gel: GraphicElement, brickCoords: Point3i, shape: BrickShape, material: BrickMaterial, hit: HitResult) {
        if (find(gel, brickCoords) != null) {
            return // do not add duplicates
        }

        list.add(
            BrickCollision(
                gel,
                brickCoords = Point3i(brickCoords), // brickCoords may be a MutablePoint3i, so we need to clone
                shape = shape,
                material = material,
                hitPt = Point3f(hit.hitPt), // hit may be a MutableHitResult, so we need to clone
                hitNormal12 = Point3f(hit.hitNormal12),
                area1 = hit.area1,
                area2 = hit.area2,
            )
        )
    }

    fun forEach(lambda: (collision: Collision) -> Unit) {
        list.forEach(lambda)
    }
}
