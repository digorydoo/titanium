package ch.digorydoo.titanium.engine.brick

import ch.digorydoo.kutils.point.Point3i

enum class BrickFaceCovering { NOT_COVERED, FULLY_COVERED, PARTIALLY_COVERED }

interface IBrickFaceCoveringRetriever {
    fun getBrickFaceCovering(
        brickCoords: Point3i,
        faceNormalX: Float,
        faceNormalY: Float,
        faceNormalZ: Float,
    ): BrickFaceCovering
}

class BrickFaceCoveringRetriever(val volume: BrickVolume): IBrickFaceCoveringRetriever {
    private val tempBrick = Brick()

    override fun getBrickFaceCovering(
        brickCoords: Point3i,
        faceNormalX: Float,
        faceNormalY: Float,
        faceNormalZ: Float,
    ): BrickFaceCovering {
        val brickX = brickCoords.x
        val brickY = brickCoords.y
        val brickZ = brickCoords.z

        val neighbourX = (brickX + faceNormalX).toInt()
        val neighbourY = (brickY + faceNormalY).toInt()
        val neighbourZ = (brickZ + faceNormalZ).toInt()

        if (neighbourX == brickX && neighbourY == brickY && neighbourZ == brickZ) {
            throw IllegalArgumentException("Invalid faceNormal")
        }

        volume.getAtBrickCoord(neighbourX, neighbourY, neighbourZ, tempBrick)

        val shapeOfNeighbour = tempBrick.shape
        return when {
            !tempBrick.isValid() -> BrickFaceCovering.NOT_COVERED
            shapeOfNeighbour.relVolume <= 0.1f -> BrickFaceCovering.NOT_COVERED // e.g. UPRIGHT_BAR_NW
            neighbourX > brickX && shapeOfNeighbour.coversNorth -> BrickFaceCovering.FULLY_COVERED
            neighbourX < brickX && shapeOfNeighbour.coversSouth -> BrickFaceCovering.FULLY_COVERED
            neighbourY > brickY && shapeOfNeighbour.coversWest -> BrickFaceCovering.FULLY_COVERED
            neighbourY < brickY && shapeOfNeighbour.coversEast -> BrickFaceCovering.FULLY_COVERED
            neighbourZ > brickZ && shapeOfNeighbour.coversBrickBelow -> BrickFaceCovering.FULLY_COVERED
            neighbourZ < brickZ && shapeOfNeighbour.coversBrickAbove -> BrickFaceCovering.FULLY_COVERED
            else -> BrickFaceCovering.PARTIALLY_COVERED
        }
    }
}
