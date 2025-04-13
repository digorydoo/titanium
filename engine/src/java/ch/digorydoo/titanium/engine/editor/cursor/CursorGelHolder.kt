package ch.digorydoo.titanium.engine.editor.cursor

import ch.digorydoo.kutils.box.Boxi
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.brickToWorld
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.editor.cursor.CursorGel.Kind.*
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind

class CursorGelHolder {
    private var cursorUpperNW: CursorGel? = null
    private var cursorUpperNE: CursorGel? = null
    private var cursorUpperSW: CursorGel? = null
    private var cursorUpperSE: CursorGel? = null

    private var cursorLowerNW: CursorGel? = null
    private var cursorLowerNE: CursorGel? = null
    private var cursorLowerSW: CursorGel? = null
    private var cursorLowerSE: CursorGel? = null

    fun createGels() {
        require(cursorUpperNW == null)
        require(cursorUpperNE == null)
        require(cursorUpperSW == null)
        require(cursorUpperSE == null)

        require(cursorLowerNW == null)
        require(cursorLowerNE == null)
        require(cursorLowerSW == null)
        require(cursorLowerSE == null)

        // The cursor is part of the scene and checks depth values. Hence, it cannot be in the UI_BELOW_DLG layer,
        // otherwise transparent objects (which render before UI) would hide the cursor when they're in front.

        cursorUpperNW = CursorGel(UPPER_NW).also { App.content.add(it, LayerKind.MAIN_NON_COLLIDABLE) }
        cursorUpperNE = CursorGel(UPPER_NE).also { App.content.add(it, LayerKind.MAIN_NON_COLLIDABLE) }
        cursorUpperSW = CursorGel(UPPER_SW).also { App.content.add(it, LayerKind.MAIN_NON_COLLIDABLE) }
        cursorUpperSE = CursorGel(UPPER_SE).also { App.content.add(it, LayerKind.MAIN_NON_COLLIDABLE) }

        cursorLowerNW = CursorGel(LOWER_NW).also { App.content.add(it, LayerKind.MAIN_NON_COLLIDABLE) }
        cursorLowerNE = CursorGel(LOWER_NE).also { App.content.add(it, LayerKind.MAIN_NON_COLLIDABLE) }
        cursorLowerSW = CursorGel(LOWER_SW).also { App.content.add(it, LayerKind.MAIN_NON_COLLIDABLE) }
        cursorLowerSE = CursorGel(LOWER_SE).also { App.content.add(it, LayerKind.MAIN_NON_COLLIDABLE) }
    }

    fun destroyGels() {
        cursorUpperNW?.setZombie()
        cursorUpperNE?.setZombie()
        cursorUpperSW?.setZombie()
        cursorUpperSE?.setZombie()

        cursorLowerNW?.setZombie()
        cursorLowerNE?.setZombie()
        cursorLowerSW?.setZombie()
        cursorLowerSE?.setZombie()

        cursorUpperNW = null
        cursorUpperNE = null
        cursorUpperSW = null
        cursorUpperSE = null

        cursorLowerNW = null
        cursorLowerNE = null
        cursorLowerSW = null
        cursorLowerSE = null
    }

    fun hide() {
        cursorUpperNW?.hide()
        cursorUpperNE?.hide()
        cursorUpperSW?.hide()
        cursorUpperSE?.hide()
        cursorLowerNW?.hide()
        cursorLowerNE?.hide()
        cursorLowerSW?.hide()
        cursorLowerSE?.hide()
    }

    fun show() {
        cursorUpperNW?.show()
        cursorUpperNE?.show()
        cursorUpperSW?.show()
        cursorUpperSE?.show()
        cursorLowerNW?.show()
        cursorLowerNE?.show()
        cursorLowerSW?.show()
        cursorLowerSE?.show()
    }

    fun updateGels(r: Boxi) {
        val x0: Float
        val y0: Float
        val z0: Float
        val x1: Float
        val y1: Float
        val z1: Float

        if (r.x0 < r.x1) {
            x0 = r.x0.toFloat()
            x1 = (r.x1 - 1).toFloat()
        } else {
            x0 = r.x1.toFloat()
            x1 = (r.x0 - 1).toFloat()
        }

        if (r.y0 < r.y1) {
            y0 = r.y0.toFloat()
            y1 = (r.y1 - 1).toFloat()
        } else {
            y0 = r.y1.toFloat()
            y1 = (r.y0 - 1).toFloat()
        }

        if (r.z0 < r.z1) {
            z0 = r.z0.toFloat()
            z1 = r.z1.toFloat() // FIXME why is this different
        } else {
            z0 = r.z1.toFloat()
            z1 = r.z0.toFloat() // FIXME why is this different
        }

        val upperNW = MutablePoint3f().also { brickToWorld(x0, y0, z1, it) }
        val upperNE = MutablePoint3f().also { brickToWorld(x0, y1, z1, it) }
        val upperSW = MutablePoint3f().also { brickToWorld(x1, y0, z1, it) }
        val upperSE = MutablePoint3f().also { brickToWorld(x1, y1, z1, it) }

        val lowerNW = MutablePoint3f(upperNW).apply { z = z0 }
        val lowerNE = MutablePoint3f(upperNE).apply { z = z0 }
        val lowerSW = MutablePoint3f(upperSW).apply { z = z0 }
        val lowerSE = MutablePoint3f(upperSE).apply { z = z0 }

        cursorUpperNW?.moveTo(upperNW)
        cursorUpperNE?.moveTo(upperNE)
        cursorUpperSW?.moveTo(upperSW)
        cursorUpperSE?.moveTo(upperSE)

        cursorLowerNW?.moveTo(lowerNW)
        cursorLowerNE?.moveTo(lowerNE)
        cursorLowerSW?.moveTo(lowerSW)
        cursorLowerSE?.moveTo(lowerSE)

        val head = when {
            r.x0 < r.x1 -> when {
                r.y0 < r.y1 -> when {
                    r.z0 < r.z1 -> cursorUpperSE
                    else -> cursorLowerSE
                }
                else -> when {
                    r.z0 < r.z1 -> cursorUpperSW
                    else -> cursorLowerSW
                }
            }
            else -> when {
                r.y0 < r.y1 -> when {
                    r.z0 < r.z1 -> cursorUpperNE
                    else -> cursorLowerNE
                }
                else -> when {
                    r.z0 < r.z1 -> cursorUpperNW
                    else -> cursorLowerNW
                }
            }
        }

        cursorUpperNW?.let { it.setHead(head == it) }
        cursorUpperNE?.let { it.setHead(head == it) }
        cursorUpperSW?.let { it.setHead(head == it) }
        cursorUpperSE?.let { it.setHead(head == it) }

        cursorLowerNW?.let { it.setHead(head == it) }
        cursorLowerNE?.let { it.setHead(head == it) }
        cursorLowerSW?.let { it.setHead(head == it) }
        cursorLowerSE?.let { it.setHead(head == it) }
    }
}
