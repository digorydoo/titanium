package ch.digorydoo.titanium.engine.brick

/**
 * Brick run is used for ramp runs that span across multiple bricks.
 */
class BrickRun {
    var length = 0; private set
    var isFirstOfRun = false; private set
    val from = Floor()
    val to = Floor()

    private var bricks = mutableListOf<Brick>()

    fun getNSRun(
        x: Int,
        y: Int,
        z: Int,
        shapeOfRun: BrickShape,
        subvolume: BrickSubvolume,
        fromDownside: Boolean = false,
        toDownside: Boolean = false,
    ) {
        bricks.clear()
        var fromX = x

        while (true) {
            subvolume.getBrick(fromX - 1, y, z, from.brick, acrossBounds = true)
            if (!from.brick.isValid()) break
            if (from.brick.shape != shapeOfRun) break
            fromX--
        }

        var toX = fromX
        bricks.add(Brick().also { subvolume.getBrick(fromX, y, z, it, acrossBounds = true) })

        while (true) {
            subvolume.getBrick(toX + 1, y, z, to.brick, acrossBounds = true)
            if (!to.brick.isValid()) break
            if (to.brick.shape != shapeOfRun) break
            bricks.add(Brick(to.brick))
            toX++
        }

        from.set(fromX, y, z, subvolume, fromDownside)
        to.set(toX, y, z, subvolume, toDownside)

        isFirstOfRun = x == fromX
        length = toX - fromX + 1
        require(length == bricks.size)
    }

    fun getWERun(
        x: Int,
        y: Int,
        z: Int,
        shapeOfRun: BrickShape,
        subvolume: BrickSubvolume,
        fromDownside: Boolean = false,
        toDownside: Boolean = false,
    ) {
        bricks.clear()
        var fromY = y

        while (true) {
            subvolume.getBrick(x, fromY - 1, z, from.brick, acrossBounds = true)
            if (!from.brick.isValid()) break
            if (from.brick.shape != shapeOfRun) break
            fromY--
        }

        var toY = fromY
        bricks.add(Brick().also { subvolume.getBrick(x, fromY, z, it, acrossBounds = true) })

        while (true) {
            subvolume.getBrick(x, toY + 1, z, to.brick, acrossBounds = true)
            if (!to.brick.isValid()) break
            if (to.brick.shape != shapeOfRun) break
            bricks.add(Brick(to.brick))
            toY++
        }

        from.set(x, fromY, z, subvolume, fromDownside)
        to.set(x, toY, z, subvolume, toDownside)

        isFirstOfRun = y == fromY
        length = toY - fromY + 1
        require(length == bricks.size)
    }

    fun forEachBrick(lambda: (index: Int, brick: Brick) -> Unit) =
        bricks.forEachIndexed(lambda)

    fun getBrick(i: Int) =
        bricks.getOrNull(i) ?: Brick()
}
