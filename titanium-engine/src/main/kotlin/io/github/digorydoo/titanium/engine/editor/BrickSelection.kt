package io.github.digorydoo.titanium.engine.editor

import ch.digorydoo.kutils.box.Boxi
import ch.digorydoo.kutils.box.MutableBoxi
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.sign
import ch.digorydoo.kutils.vector.MutableVector3f
import ch.digorydoo.kutils.vector.MutableVector3i
import io.github.digorydoo.titanium.engine.brick.BrickVolume.Companion.WORLD_BRICK_SIZE
import io.github.digorydoo.titanium.engine.brick.BrickVolume.Companion.brickToWorld
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.editor.cursor.CursorGelHolder

internal class BrickSelection(private val cursor: CursorGelHolder) {
    private val sel = MutableBoxi() // may be reversed
    private val tip = MutableVector3f() // this is where the selection gets extended

    fun get(): Boxi = sel
    fun getUnreversed(): Boxi = sel.newUnreversed()

    fun set(x: Int, y: Int, z: Int) =
        set(x, y, z, x + 1, y + 1, z + 1)

    fun set(b: Boxi) =
        set(b.x0, b.y0, b.z0, b.x1, b.y1, b.z1)

    fun set(x0: Int, y0: Int, z0: Int, x1: Int, y1: Int, z1: Int) {
        sel.set(x0, y0, z0, x1, y1, z1)
        clampToVolume()
        updateTip()
        selectionChanged()
    }

    private fun updateTip() {
        tip.x = sel.x1.toFloat() - 1
        tip.y = sel.y1.toFloat() - 1
        tip.z = sel.z1.toFloat() - 1
    }

    private fun selectionChanged() {
        cursor.brickSelectionChanged(get())
        val p = getTipPosInWorldCoords()
        App.camera.setTarget(p)
    }

    fun getTipPosInBrickCoords(centre: MutableVector3i) {
        centre.x = tip.x.toInt() + (if (sel.x0 < sel.x1) 0 else 1)
        centre.y = tip.y.toInt() + (if (sel.y0 < sel.y1) 0 else 1)
        centre.z = tip.z.toInt() + (if (sel.z0 < sel.z1) 0 else 1)
    }

    fun getTipPosInWorldCoords() =
        MutableVector3f().also {
            brickToWorld(
                tip.x.toInt() + (if (sel.x0 < sel.x1) 0 else 1),
                tip.y.toInt() + (if (sel.y0 < sel.y1) 0 else 1),
                tip.z.toInt() + (if (sel.z0 < sel.z1) 0 else 1),
                it
            )
            it.x += 0.5f * WORLD_BRICK_SIZE
            it.y += 0.5f * WORLD_BRICK_SIZE
            it.z += 0.5f * WORLD_BRICK_SIZE
        }

    fun collapseSelection() {
        set(
            tip.x.toInt() + (if (sel.x0 < sel.x1) 0 else 1),
            tip.y.toInt() + (if (sel.y0 < sel.y1) 0 else 1),
            tip.z.toInt() + (if (sel.z0 < sel.z1) 0 else 1),
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
                sel.x0 < sel.x1 -> tip.x.toInt()
                else -> tip.x.toInt() + 1
            }

            val y = when {
                dy < 0 -> ur.y0
                dy > 0 -> ur.y1 - 1
                sel.y0 < sel.y1 -> tip.y.toInt()
                else -> tip.y.toInt() + 1
            }

            val z = when {
                dz < 0 -> ur.z0
                dz > 0 -> ur.z1 - 1
                sel.z0 < sel.z1 -> tip.z.toInt()
                else -> tip.z.toInt() + 1
            }

            set(x, y, z)
        }
    }

    fun move(dx: Int, dy: Int, dz: Int) {
        var x0 = sel.x0 + dx
        var x1 = sel.x1 + dx

        if (x0 < x1) {
            if (x0 < 0 || x1 > App.bricks.xsize) {
                x0 = sel.x0
                x1 = sel.x1
            }
        } else {
            if (x1 < 0 || x0 > App.bricks.xsize) {
                x0 = sel.x0
                x1 = sel.x1
            }
        }

        var y0 = sel.y0 + dy
        var y1 = sel.y1 + dy

        if (y0 < y1) {
            if (y0 < 0 || y1 > App.bricks.ysize) {
                y0 = sel.y0
                y1 = sel.y1
            }
        } else {
            if (y1 < 0 || y0 > App.bricks.ysize) {
                y0 = sel.y0
                y1 = sel.y1
            }
        }

        var z0 = sel.z0 + dz
        var z1 = sel.z1 + dz

        if (z0 < z1) {
            if (z0 < 0 || z1 > App.bricks.zsize) {
                z0 = sel.z0
                z1 = sel.z1
            }
        } else {
            if (z1 < 0 || z0 > App.bricks.zsize) {
                z0 = sel.z0
                z1 = sel.z1
            }
        }

        set(x0, y0, z0, x1, y1, z1)
    }

    fun extend(dx: Int, dy: Int, dz: Int) {
        var ax = sel.x0
        var ay = sel.y0
        var az = sel.z0

        var bx = tip.x.toInt() + dx
        var by = tip.y.toInt() + dy
        var bz = tip.z.toInt() + dz

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
