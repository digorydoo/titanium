package ch.digorydoo.titanium.engine.editor

import ch.digorydoo.kutils.box.Boxi
import ch.digorydoo.kutils.box.MutableBoxi
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.sign
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.MutablePoint3i
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.brickToWorld
import ch.digorydoo.titanium.engine.core.App

class Selection(private val onChange: () -> Unit) {
    private val sel = MutableBoxi() // may be reversed
    private val pos = MutablePoint3f() // this is where the selection gets extended

    fun get(): Boxi = sel
    fun getUnreversed(): Boxi = sel.newUnreversed()

    fun set(x: Int, y: Int, z: Int) =
        set(x, y, z, x + 1, y + 1, z + 1)

    fun set(b: Boxi) =
        set(b.x0, b.y0, b.z0, b.x1, b.y1, b.z1)

    fun set(x0: Int, y0: Int, z0: Int, x1: Int, y1: Int, z1: Int) {
        sel.set(x0, y0, z0, x1, y1, z1)
        clampToVolume()
        updatePos()
        onChange()
    }

    private fun updatePos() {
        pos.x = sel.x1.toFloat() - 1
        pos.y = sel.y1.toFloat() - 1
        pos.z = sel.z1.toFloat() - 1
    }

    fun getPosCentreInBrickCoords(centre: MutablePoint3i) {
        centre.x = pos.x.toInt() + (if (sel.x0 < sel.x1) 0 else 1)
        centre.y = pos.y.toInt() + (if (sel.y0 < sel.y1) 0 else 1)
        centre.z = pos.z.toInt() + (if (sel.z0 < sel.z1) 0 else 1)
    }

    fun getPosCentreInWorldCoords() =
        MutablePoint3f().also {
            brickToWorld(
                pos.x.toInt() + (if (sel.x0 < sel.x1) 0 else 1),
                pos.y.toInt() + (if (sel.y0 < sel.y1) 0 else 1),
                pos.z.toInt() + (if (sel.z0 < sel.z1) 0 else 1),
                it
            )
            it.x += 0.5f * WORLD_BRICK_SIZE
            it.y += 0.5f * WORLD_BRICK_SIZE
            it.z += 0.5f * WORLD_BRICK_SIZE
        }

    fun collapseSelection() {
        set(
            pos.x.toInt() + (if (sel.x0 < sel.x1) 0 else 1),
            pos.y.toInt() + (if (sel.y0 < sel.y1) 0 else 1),
            pos.z.toInt() + (if (sel.z0 < sel.z1) 0 else 1),
        )
    }

    fun collapseAndMove(dx: Int, dy: Int, dz: Int) {
        val ur = getUnreversed()

        if (ur.xsize == 1 && ur.ysize == 1 && ur.zsize == 1) {
            // Just move
            set(ur.x0 + dx, ur.y0 + dy, ur.z0 + dz)
        } else {
            // Collapse selection in the direction of dx, dy, dz

            val x = when {
                dx < 0 -> ur.x0
                dx > 0 -> ur.x1 - 1
                sel.x0 < sel.x1 -> pos.x.toInt()
                else -> pos.x.toInt() + 1
            }

            val y = when {
                dy < 0 -> ur.y0
                dy > 0 -> ur.y1 - 1
                sel.y0 < sel.y1 -> pos.y.toInt()
                else -> pos.y.toInt() + 1
            }

            val z = when {
                dz < 0 -> ur.z0
                dz > 0 -> ur.z1 - 1
                sel.z0 < sel.z1 -> pos.z.toInt()
                else -> pos.z.toInt() + 1
            }

            set(x, y, z)
        }
    }

    fun move(dx: Int, dy: Int, dz: Int) {
        var newX0 = sel.x0 + dx
        var newX1 = sel.x1 + dx
        val xrange = 0 ..< App.bricks.xsize

        if (newX0 !in xrange || newX1 !in xrange) {
            newX0 = sel.x0
            newX1 = sel.x1
        }

        var newY0 = sel.y0 + dy
        var newY1 = sel.y1 + dy
        val yrange = 0 ..< App.bricks.ysize

        if (newY0 !in yrange || newY1 !in yrange) {
            newY0 = sel.y0
            newY1 = sel.y1
        }

        var newZ0 = sel.z0 + dz
        var newZ1 = sel.z1 + dz
        val zrange = 0 ..< App.bricks.zsize

        if (newZ0 !in zrange || newZ1 !in zrange) {
            newZ0 = sel.z0
            newZ1 = sel.z1
        }

        set(newX0, newY0, newZ0, newX1, newY1, newZ1)
    }

    fun extend(dx: Int, dy: Int, dz: Int) {
        var ax = sel.x0
        var ay = sel.y0
        var az = sel.z0

        var bx = pos.x.toInt() + dx
        var by = pos.y.toInt() + dy
        var bz = pos.z.toInt() + dz

        if (ax == bx + 1) {
            bx += dx
            ax -= sign(dx)
        }

        if (ay == by + 1) {
            by += dy
            ay -= sign(dy)
        }

        if (az == bz + 1) {
            bz += dz
            az -= sign(dz)
        }

        set(ax, ay, az, bx + 1, by + 1, bz + 1)
    }

    fun forEachBrick(lambda: (x: Int, y: Int, z: Int) -> Unit) {
        val ur = getUnreversed()
        for (z in ur.z0 ..< ur.z1) {
            for (y in ur.y0 ..< ur.y1) {
                for (x in ur.x0 ..< ur.x1) {
                    lambda(x, y, z)
                }
            }
        }
    }

    private fun clampToVolume() {
        val pf = App.bricks

        if (sel.x0 < sel.x1) {
            sel.x0 = clamp(sel.x0, 0, pf.xsize - 1)
            sel.x1 = clamp(sel.x1, 1, pf.xsize)
        } else {
            sel.x0 = clamp(sel.x0, 1, pf.xsize)
            sel.x1 = clamp(sel.x1, 0, pf.xsize - 1)
        }

        if (sel.y0 < sel.y1) {
            sel.y0 = clamp(sel.y0, 0, pf.ysize - 1)
            sel.y1 = clamp(sel.y1, 1, pf.ysize)
        } else {
            sel.y0 = clamp(sel.y0, 1, pf.ysize)
            sel.y1 = clamp(sel.y1, 0, pf.ysize - 1)
        }

        if (sel.z0 < sel.z1) {
            sel.z0 = clamp(sel.z0, 0, pf.zsize - 1)
            sel.z1 = clamp(sel.z1, 1, pf.zsize)
        } else {
            sel.z0 = clamp(sel.z0, 1, pf.zsize)
            sel.z1 = clamp(sel.z1, 0, pf.zsize - 1)
        }
    }
}
