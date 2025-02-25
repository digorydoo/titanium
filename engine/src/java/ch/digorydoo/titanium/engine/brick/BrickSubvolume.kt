package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.kutils.box.Boxi
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.texture.Texture

class BrickSubvolume(private val volume: BrickVolume, models: BrickModelHolder, private val box: Boxi, tex: Texture) {
    private val buffer = BrickBuffer(box.xsize, box.ysize, box.zsize)

    private val worldOrigin = Point3f(
        box.x0.toFloat() * Brick.WORLD_BRICK_SIZE,
        box.y0.toFloat() * Brick.WORLD_BRICK_SIZE,
        box.z0.toFloat() * Brick.WORLD_BRICK_SIZE,
    )

    private val modelData = BrickModelData(this, models)

    val numPositions get() = modelData.numPositions
    val numNormals get() = modelData.numNormals
    val numTexCoords get() = modelData.numTexCoords

    private val tempBrick = Brick()

    private val renderer = App.factory.createBrickVolumeRenderer(worldOrigin, tex, modelData)

    fun distanceFromCamera() =
        box.distanceFrom(App.camera.sourcePos)

    fun overlaps(anotherBox: Boxi) =
        box.overlaps(anotherBox)

    private fun isInBounds(x: Int, y: Int, z: Int) =
        x in 0 ..< box.xsize && y in 0 ..< box.ysize && z in 0 ..< box.zsize

    fun setBrick(x: Int, y: Int, z: Int, brick: Brick, acrossBounds: Boolean = false) {
        if (isInBounds(x, y, z)) {
            buffer.set(x, y, z, brick)
        } else if (acrossBounds) {
            volume.setAtBrickCoord(box.x0 + x, box.y0 + y, box.z0 + z, brick)
        }
    }

    fun getBrick(pt: Point3i, brick: Brick, acrossBounds: Boolean = false) =
        getBrick(pt.x, pt.y, pt.z, brick, acrossBounds)

    fun getBrick(x: Int, y: Int, z: Int, brick: Brick, acrossBounds: Boolean = false): Boolean =
        if (isInBounds(x, y, z)) {
            buffer.get(x, y, z, brick)
            true
        } else if (acrossBounds) {
            volume.getAtBrickCoord(box.x0 + x, box.y0 + y, box.z0 + z, brick)
        } else {
            brick.setInvalid()
            false
        }

    fun getShape(pt: Point3i, acrossBounds: Boolean = false) =
        getShape(pt.x, pt.y, pt.z, acrossBounds)

    fun getShape(x: Int, y: Int, z: Int, acrossBounds: Boolean = false): BrickShape? =
        if (isInBounds(x, y, z)) {
            buffer.getShape(x, y, z)
        } else if (!acrossBounds) {
            null
        } else if (volume.getAtBrickCoord(box.x0 + x, box.y0 + y, box.z0 + z, tempBrick)) {
            tempBrick.shape
        } else {
            null
        }

    fun getMaterial(x: Int, y: Int, z: Int, acrossBounds: Boolean = false): BrickMaterial? =
        if (isInBounds(x, y, z)) {
            buffer.getMaterial(x, y, z)
        } else if (!acrossBounds) {
            null
        } else if (volume.getAtBrickCoord(box.x0 + x, box.y0 + y, box.z0 + z, tempBrick)) {
            tempBrick.material
        } else {
            null
        }

    fun forEachBrick(lambda: (brick: Brick, ix: Int, iy: Int, iz: Int) -> Unit) {
        val brick = Brick()

        for (iz in 0 ..< box.zsize) {
            for (iy in 0 ..< box.ysize) {
                for (ix in 0 ..< box.xsize) {
                    buffer.get(ix, iy, iz, brick)
                    lambda(brick, ix, iy, iz)
                }
            }
        }
    }

    fun update() {
        renderer.prepare()
    }

    fun renderShadows() {
        renderer.renderShadows()
    }

    fun renderSolid() {
        renderer.renderSolid()
    }

    fun renderTransparent() {
        renderer.renderTransparent()
    }

    fun free() {
        renderer.free()
    }

    fun heightAt(worldX: Float, worldY: Float, relBrickCoords: Point3i): Float? {
        val h = modelData.heightAt(worldX - worldOrigin.x, worldY - worldOrigin.y, relBrickCoords)
        return if (h == null) null else worldOrigin.z + h
    }

    override fun toString() =
        "BrickSubvolume(box=$box, worldOrigin=$worldOrigin)"
}
