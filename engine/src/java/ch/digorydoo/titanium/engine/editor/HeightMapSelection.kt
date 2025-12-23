package ch.digorydoo.titanium.engine.editor

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.sign
import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.rect.MutableRecti
import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.cursor.CursorGelHolder

internal class HeightMapSelection(private val cursor: CursorGelHolder, private val state: EditorState) {
    private val sel = MutableRecti()
    private val tip = MutablePoint2f() // this is where the selection gets extended

    fun get(): Recti = sel
    fun getUnreversed(): Recti = sel.newUnreversed()

    fun set(x: Int, y: Int) =
        set(x, y, x + 1, y + 1)

    fun set(r: Recti) =
        set(r.left, r.top, r.right, r.bottom)

    fun set(left: Int, top: Int, right: Int, bottom: Int) {
        sel.set(left, top, right, bottom)
        clampToHeightMap()
        updateTip()
        selectionChanged()
    }

    private fun updateTip() {
        tip.x = sel.right.toFloat() - 1
        tip.y = sel.bottom.toFloat() - 1
    }

    private fun selectionChanged() {
        cursor.heightMapSelectionChanged(get())
        val p = getTipPosInWorldCoords()
        App.camera.setTarget(p)
    }

    fun getTipPosInWorldCoords(): MutablePoint3f {
        val result = MutablePoint3f()

        state.heightMapSpawnPt?.let { spawnPt ->
            state.heightMap?.getWorldCoords(
                tip.x.toInt() + (if (sel.left < sel.right) 0 else 1),
                tip.y.toInt() + (if (sel.top < sel.bottom) 0 else 1),
                spawnPt.pos,
                spawnPt.rotation,
                result
            )
        }

        return result
    }

    fun collapseAndMove(dx: Int, dy: Int) {
        val ur = getUnreversed()

        if (ur.width == 1 && ur.height == 1) {
            // Just move
            set(ur.left + dx, ur.top + dy)
        } else {
            // Collapse selection in the direction of dx, dy

            val x = when {
                dx < 0 -> ur.left
                dx > 0 -> ur.right - 1
                sel.left < sel.right -> tip.x.toInt()
                else -> tip.x.toInt() + 1
            }

            val y = when {
                dy < 0 -> ur.top
                dy > 0 -> ur.bottom - 1
                sel.top < sel.bottom -> tip.y.toInt()
                else -> tip.y.toInt() + 1
            }

            set(x, y)
        }
    }

    fun move(dx: Int, dy: Int) {
        val heightMap = state.heightMap ?: return

        var left = sel.left + dx
        var right = sel.right + dx

        if (left < right) {
            if (left < 0 || right > heightMap.numSamplesX) {
                left = sel.left
                right = sel.right
            }
        } else {
            if (right < 0 || left > heightMap.numSamplesX) {
                left = sel.left
                right = sel.right
            }
        }

        var top = sel.top + dy
        var bottom = sel.bottom + dy

        if (top < bottom) {
            if (top < 0 || bottom > heightMap.numSamplesY) {
                top = sel.top
                bottom = sel.bottom
            }
        } else {
            if (bottom < 0 || top > heightMap.numSamplesY) {
                top = sel.top
                bottom = sel.bottom
            }
        }

        set(left, top, right, bottom)
    }

    fun extend(dx: Int, dy: Int) {
        var ax = sel.left
        var ay = sel.top

        var bx = tip.x.toInt() + dx
        var by = tip.y.toInt() + dy

        if (ax == bx + 1) {
            bx += dx
            ax -= sign(dx)
        }

        if (ay == by + 1) {
            by += dy
            ay -= sign(dy)
        }

        set(ax, ay, bx + 1, by + 1)
    }

    private fun clampToHeightMap() {
        val heightMap = state.heightMap ?: return

        if (sel.left < sel.right) {
            sel.left = clamp(sel.left, 0, heightMap.numSamplesX - 1)
            sel.right = clamp(sel.right, 1, heightMap.numSamplesX)
        } else {
            sel.left = clamp(sel.left, 1, heightMap.numSamplesX)
            sel.right = clamp(sel.right, 0, heightMap.numSamplesX - 1)
        }

        if (sel.top < sel.bottom) {
            sel.top = clamp(sel.top, 0, heightMap.numSamplesY - 1)
            sel.bottom = clamp(sel.bottom, 1, heightMap.numSamplesY)
        } else {
            sel.top = clamp(sel.top, 1, heightMap.numSamplesY)
            sel.bottom = clamp(sel.bottom, 0, heightMap.numSamplesY - 1)
        }
    }
}
