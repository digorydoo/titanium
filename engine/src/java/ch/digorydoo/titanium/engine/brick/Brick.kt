package ch.digorydoo.titanium.engine.brick

/**
 * This class holds all properties assigned to a given brick positition.
 */
class Brick private constructor(
    var shape: BrickShape,
    var material: BrickMaterial,
    var upFaceIdx: Int,
    var downFaceIdx: Int,
    var northFaceIdx: Int,
    var southFaceIdx: Int,
    var eastFaceIdx: Int,
    var westFaceIdx: Int,
) {
    constructor(): this(
        shape = BrickShape.NONE,
        material = BrickMaterial.GREY_CONCRETE,
        upFaceIdx = -1,
        downFaceIdx = -1,
        northFaceIdx = -1,
        southFaceIdx = -1,
        eastFaceIdx = -1,
        westFaceIdx = -1,
    )

    constructor(other: Brick): this(
        shape = other.shape,
        material = other.material,
        upFaceIdx = other.upFaceIdx,
        downFaceIdx = other.downFaceIdx,
        northFaceIdx = other.northFaceIdx,
        southFaceIdx = other.southFaceIdx,
        eastFaceIdx = other.eastFaceIdx,
        westFaceIdx = other.westFaceIdx,
    )

    fun isValid() =
        shape != BrickShape.NONE

    fun setInvalid() {
        shape = BrickShape.NONE
        material = BrickMaterial.GREY_CONCRETE
        upFaceIdx = -1
        downFaceIdx = -1
        northFaceIdx = -1
        southFaceIdx = -1
        eastFaceIdx = -1
        westFaceIdx = -1
    }

    fun set(other: Brick) {
        shape = other.shape
        material = other.material
        upFaceIdx = other.upFaceIdx
        downFaceIdx = other.downFaceIdx
        northFaceIdx = other.northFaceIdx
        southFaceIdx = other.southFaceIdx
        eastFaceIdx = other.eastFaceIdx
        westFaceIdx = other.westFaceIdx
    }

    override fun toString() =
        "Brick($shape, $material)"
}
