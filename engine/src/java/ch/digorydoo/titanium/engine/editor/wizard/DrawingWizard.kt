package ch.digorydoo.titanium.engine.editor.wizard

import ch.digorydoo.kutils.point.Point3i
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickMaterial.GREY_CONCRETE
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.brick.BrickShape.NONE
import ch.digorydoo.titanium.engine.brick.BrickShapeAndMaterial
import ch.digorydoo.titanium.engine.core.App

internal class DrawingWizard {
    private val northBrick = Brick()
    private val eastBrick = Brick()
    private val southBrick = Brick()
    private val westBrick = Brick()
    private val neBrick = Brick()
    private val nwBrick = Brick()
    private val seBrick = Brick()
    private val swBrick = Brick()
    private val brickBelow = Brick()
    private val northBrickBelow = Brick()
    private val eastBrickBelow = Brick()
    private val southBrickBelow = Brick()
    private val westBrickBelow = Brick()
    private val brickAbove = Brick()
    private val northBrickAbove = Brick()
    private val eastBrickAbove = Brick()
    private val southBrickAbove = Brick()
    private val westBrickAbove = Brick()

    internal class If(
        val north: BrickShape? = null,
        val east: BrickShape? = null,
        val south: BrickShape? = null,
        val west: BrickShape? = null,
        val northIsnt: BrickShape? = null,
        val eastIsnt: BrickShape? = null,
        val southIsnt: BrickShape? = null,
        val westIsnt: BrickShape? = null,
        val ne: BrickShape? = null,
        val nw: BrickShape? = null,
        val se: BrickShape? = null,
        val sw: BrickShape? = null,
        val above: BrickShape? = null,
        val below: BrickShape? = null,
        val aboveIsnt: BrickShape? = null,
        val belowIsnt: BrickShape? = null,
        val northBelow: BrickShape? = null,
        val eastBelow: BrickShape? = null,
        val southBelow: BrickShape? = null,
        val westBelow: BrickShape? = null,
        val northBelowIsnt: BrickShape? = null,
        val eastBelowIsnt: BrickShape? = null,
        val southBelowIsnt: BrickShape? = null,
        val westBelowIsnt: BrickShape? = null,
        val northAbove: BrickShape? = null,
        val eastAbove: BrickShape? = null,
        val southAbove: BrickShape? = null,
        val westAbove: BrickShape? = null,
        val then: BrickShape,
        val mat: BrickMaterial? = null,
    )

    fun getSuggestions(brickPos: Point3i): List<BrickShapeAndMaterial> {
        val x = brickPos.x
        val y = brickPos.y
        val z = brickPos.z

        App.bricks.getAtBrickCoord(x - 1, y, z, northBrick)
        App.bricks.getAtBrickCoord(x + 1, y, z, southBrick)
        App.bricks.getAtBrickCoord(x, y - 1, z, westBrick)
        App.bricks.getAtBrickCoord(x, y + 1, z, eastBrick)
        App.bricks.getAtBrickCoord(x - 1, y + 1, z, neBrick)
        App.bricks.getAtBrickCoord(x - 1, y - 1, z, nwBrick)
        App.bricks.getAtBrickCoord(x + 1, y + 1, z, seBrick)
        App.bricks.getAtBrickCoord(x + 1, y - 1, z, swBrick)
        App.bricks.getAtBrickCoord(x, y, z - 1, brickBelow)
        App.bricks.getAtBrickCoord(x - 1, y, z - 1, northBrickBelow)
        App.bricks.getAtBrickCoord(x + 1, y, z - 1, southBrickBelow)
        App.bricks.getAtBrickCoord(x, y - 1, z - 1, westBrickBelow)
        App.bricks.getAtBrickCoord(x, y + 1, z - 1, eastBrickBelow)
        App.bricks.getAtBrickCoord(x, y, z + 1, brickAbove)
        App.bricks.getAtBrickCoord(x - 1, y, z + 1, northBrickAbove)
        App.bricks.getAtBrickCoord(x + 1, y, z + 1, southBrickAbove)
        App.bricks.getAtBrickCoord(x, y - 1, z + 1, westBrickAbove)
        App.bricks.getAtBrickCoord(x, y + 1, z + 1, eastBrickAbove)

        return wizardRules
            .filter { it.check() }
            .map { it.getResult() }
    }

    private fun If.check() = when {
        north != null && northBrick.shape != north -> false
        east != null && eastBrick.shape != east -> false
        south != null && southBrick.shape != south -> false
        west != null && westBrick.shape != west -> false
        northIsnt != null && northBrick.shape == northIsnt -> false
        eastIsnt != null && eastBrick.shape == eastIsnt -> false
        southIsnt != null && southBrick.shape == southIsnt -> false
        westIsnt != null && westBrick.shape == westIsnt -> false
        ne != null && neBrick.shape != ne -> false
        nw != null && nwBrick.shape != nw -> false
        se != null && seBrick.shape != se -> false
        sw != null && swBrick.shape != sw -> false
        above != null && brickAbove.shape != above -> false
        below != null && brickBelow.shape != below -> false
        aboveIsnt != null && brickAbove.shape == aboveIsnt -> false
        belowIsnt != null && brickBelow.shape == belowIsnt -> false
        northBelow != null && northBrickBelow.shape != northBelow -> false
        eastBelow != null && eastBrickBelow.shape != eastBelow -> false
        southBelow != null && southBrickBelow.shape != southBelow -> false
        westBelow != null && westBrickBelow.shape != westBelow -> false
        northBelowIsnt != null && northBrickBelow.shape == northBelowIsnt -> false
        eastBelowIsnt != null && eastBrickBelow.shape == eastBelowIsnt -> false
        southBelowIsnt != null && southBrickBelow.shape == southBelowIsnt -> false
        westBelowIsnt != null && westBrickBelow.shape == westBelowIsnt -> false
        northAbove != null && northBrickAbove.shape != northAbove -> false
        eastAbove != null && eastBrickAbove.shape != eastAbove -> false
        southAbove != null && southBrickAbove.shape != southAbove -> false
        westAbove != null && westBrickAbove.shape != westAbove -> false
        else -> true
    }

    private fun If.getResult() =
        BrickShapeAndMaterial(
            shape = then,
            material = mat
                ?: (north?.takeIf { it != NONE }?.let { northBrick.material })
                ?: (east?.takeIf { it != NONE }?.let { eastBrick.material })
                ?: (south?.takeIf { it != NONE }?.let { southBrick.material })
                ?: (west?.takeIf { it != NONE }?.let { westBrick.material })
                ?: (ne?.takeIf { it != NONE }?.let { neBrick.material })
                ?: (nw?.takeIf { it != NONE }?.let { nwBrick.material })
                ?: (se?.takeIf { it != NONE }?.let { seBrick.material })
                ?: (sw?.takeIf { it != NONE }?.let { swBrick.material })
                ?: (below?.takeIf { it != NONE }?.let { brickBelow.material })
                ?: (above?.takeIf { it != NONE }?.let { brickAbove.material })
                ?: GREY_CONCRETE
        )
}
