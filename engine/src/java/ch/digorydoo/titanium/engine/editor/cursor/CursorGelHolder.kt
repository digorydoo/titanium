package ch.digorydoo.titanium.engine.editor.cursor

import ch.digorydoo.kutils.box.Boxi
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.titanium.engine.brick.BrickVolume.Companion.brickToWorld
import ch.digorydoo.titanium.engine.editor.EditorState
import ch.digorydoo.titanium.engine.editor.EditorState.EditMode
import ch.digorydoo.titanium.engine.editor.cursor.CursorGel.Kind.*
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind

internal class CursorGelHolder(private val state: EditorState) {
    private var smallUpperNWGel: CursorGel? = null
    private var smallUpperNEGel: CursorGel? = null
    private var smallUpperSWGel: CursorGel? = null
    private var smallUpperSEGel: CursorGel? = null

    private var smallLowerNWGel: CursorGel? = null
    private var smallLowerNEGel: CursorGel? = null
    private var smallLowerSWGel: CursorGel? = null
    private var smallLowerSEGel: CursorGel? = null

    private var bigUpperNWGel: CursorGel? = null
    private var bigUpperNEGel: CursorGel? = null
    private var bigUpperSWGel: CursorGel? = null
    private var bigUpperSEGel: CursorGel? = null

    private var bigLowerNWGel: CursorGel? = null
    private var bigLowerNEGel: CursorGel? = null
    private var bigLowerSWGel: CursorGel? = null
    private var bigLowerSEGel: CursorGel? = null

    private var shown = false

    init {
        state.addObserver {
            if (shown) show() // hide/show gels based on edit mode
        }
    }

    fun createGels() {
        require(smallUpperNWGel == null)
        require(smallUpperNEGel == null)
        require(smallUpperSWGel == null)
        require(smallUpperSEGel == null)

        require(smallLowerNWGel == null)
        require(smallLowerNEGel == null)
        require(smallLowerSWGel == null)
        require(smallLowerSEGel == null)

        require(bigUpperNWGel == null)
        require(bigUpperNEGel == null)
        require(bigUpperSWGel == null)
        require(bigUpperSEGel == null)

        require(bigLowerNWGel == null)
        require(bigLowerNEGel == null)
        require(bigLowerSWGel == null)
        require(bigLowerSEGel == null)

        // The cursor is part of the scene and checks depth values. Hence, it cannot be in the UI_BELOW_DLG layer,
        // otherwise transparent objects (which render before UI) would hide the cursor when they're in front.

        smallUpperNWGel = CursorGel(SMALL_UPPER_NW).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }
        smallUpperNEGel = CursorGel(SMALL_UPPER_NE).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }
        smallUpperSWGel = CursorGel(SMALL_UPPER_SW).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }
        smallUpperSEGel = CursorGel(SMALL_UPPER_SE).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }

        smallLowerNWGel = CursorGel(SMALL_LOWER_NW).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }
        smallLowerNEGel = CursorGel(SMALL_LOWER_NE).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }
        smallLowerSWGel = CursorGel(SMALL_LOWER_SW).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }
        smallLowerSEGel = CursorGel(SMALL_LOWER_SE).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }

        bigUpperNWGel = CursorGel(BIG_UPPER_NW).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }
        bigUpperNEGel = CursorGel(BIG_UPPER_NE).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }
        bigUpperSWGel = CursorGel(BIG_UPPER_SW).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }
        bigUpperSEGel = CursorGel(BIG_UPPER_SE).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }

        bigLowerNWGel = CursorGel(BIG_LOWER_NW).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }
        bigLowerNEGel = CursorGel(BIG_LOWER_NE).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }
        bigLowerSWGel = CursorGel(BIG_LOWER_SW).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }
        bigLowerSEGel = CursorGel(BIG_LOWER_SE).also { it.onCreate(LayerKind.MAIN_NON_COLLIDABLE) }

        show() // hide/show gels based on edit mode
    }

    fun destroyGels() {
        smallUpperNWGel?.setZombie()
        smallUpperNEGel?.setZombie()
        smallUpperSWGel?.setZombie()
        smallUpperSEGel?.setZombie()

        smallLowerNWGel?.setZombie()
        smallLowerNEGel?.setZombie()
        smallLowerSWGel?.setZombie()
        smallLowerSEGel?.setZombie()

        bigUpperNWGel?.setZombie()
        bigUpperNEGel?.setZombie()
        bigUpperSWGel?.setZombie()
        bigUpperSEGel?.setZombie()

        bigLowerNWGel?.setZombie()
        bigLowerNEGel?.setZombie()
        bigLowerSWGel?.setZombie()
        bigLowerSEGel?.setZombie()

        smallUpperNWGel = null
        smallUpperNEGel = null
        smallUpperSWGel = null
        smallUpperSEGel = null

        smallLowerNWGel = null
        smallLowerNEGel = null
        smallLowerSWGel = null
        smallLowerSEGel = null

        bigUpperNWGel = null
        bigUpperNEGel = null
        bigUpperSWGel = null
        bigUpperSEGel = null

        bigLowerNWGel = null
        bigLowerNEGel = null
        bigLowerSWGel = null
        bigLowerSEGel = null
    }

    fun hide() {
        smallUpperNWGel?.hide()
        smallUpperNEGel?.hide()
        smallUpperSWGel?.hide()
        smallUpperSEGel?.hide()

        smallLowerNWGel?.hide()
        smallLowerNEGel?.hide()
        smallLowerSWGel?.hide()
        smallLowerSEGel?.hide()

        bigUpperNWGel?.hide()
        bigUpperNEGel?.hide()
        bigUpperSWGel?.hide()
        bigUpperSEGel?.hide()

        bigLowerNWGel?.hide()
        bigLowerNEGel?.hide()
        bigLowerSWGel?.hide()
        bigLowerSEGel?.hide()

        shown = false
    }

    fun show() {
        hide()

        when (state.editMode) {
            EditMode.BRICKS -> {
                bigUpperNWGel?.show()
                bigUpperNEGel?.show()
                bigUpperSWGel?.show()
                bigUpperSEGel?.show()
                bigLowerNWGel?.show()
                bigLowerNEGel?.show()
                bigLowerSWGel?.show()
                bigLowerSEGel?.show()
            }
            EditMode.HEIGHT_MAP -> {
                smallUpperNWGel?.show()
                smallUpperNEGel?.show()
                smallUpperSWGel?.show()
                smallUpperSEGel?.show()
                smallLowerNWGel?.show()
                smallLowerNEGel?.show()
                smallLowerSWGel?.show()
                smallLowerSEGel?.show()
            }
        }

        shown = true
    }

    fun brickSelectionChanged(box: Boxi) {
        if (state.editMode != EditMode.BRICKS) return

        val x0: Int
        val y0: Int
        val z0: Int
        val x1: Int
        val y1: Int
        val z1: Int

        if (box.x0 < box.x1) {
            x0 = box.x0
            x1 = box.x1
        } else {
            x0 = box.x1
            x1 = box.x0
        }

        if (box.y0 < box.y1) {
            y0 = box.y0
            y1 = box.y1
        } else {
            y0 = box.y1
            y1 = box.y0
        }

        if (box.z0 < box.z1) {
            z0 = box.z0
            z1 = box.z1
        } else {
            z0 = box.z1
            z1 = box.z0
        }

        val lowerNW = MutablePoint3f().also { brickToWorld(x0, y0, z0, it) }
        val lowerNE = MutablePoint3f().also { brickToWorld(x0, y1, z0, it) }
        val lowerSW = MutablePoint3f().also { brickToWorld(x1, y0, z0, it) }
        val lowerSE = MutablePoint3f().also { brickToWorld(x1, y1, z0, it) }

        val upperNW = MutablePoint3f().also { brickToWorld(x0, y0, z1, it) }
        val upperNE = MutablePoint3f().also { brickToWorld(x0, y1, z1, it) }
        val upperSW = MutablePoint3f().also { brickToWorld(x1, y0, z1, it) }
        val upperSE = MutablePoint3f().also { brickToWorld(x1, y1, z1, it) }

        bigUpperNWGel?.moveTo(upperNW)
        bigUpperNEGel?.moveTo(upperNE)
        bigUpperSWGel?.moveTo(upperSW)
        bigUpperSEGel?.moveTo(upperSE)

        bigLowerNWGel?.moveTo(lowerNW)
        bigLowerNEGel?.moveTo(lowerNE)
        bigLowerSWGel?.moveTo(lowerSW)
        bigLowerSEGel?.moveTo(lowerSE)

        val head = when {
            box.x0 < box.x1 -> when {
                box.y0 < box.y1 -> when {
                    box.z0 < box.z1 -> bigUpperSEGel
                    else -> bigLowerSEGel
                }
                else -> when {
                    box.z0 < box.z1 -> bigUpperSWGel
                    else -> bigLowerSWGel
                }
            }
            else -> when {
                box.y0 < box.y1 -> when {
                    box.z0 < box.z1 -> bigUpperNEGel
                    else -> bigLowerNEGel
                }
                else -> when {
                    box.z0 < box.z1 -> bigUpperNWGel
                    else -> bigLowerNWGel
                }
            }
        }

        bigUpperNWGel?.let { it.setHead(head == it) }
        bigUpperNEGel?.let { it.setHead(head == it) }
        bigUpperSWGel?.let { it.setHead(head == it) }
        bigUpperSEGel?.let { it.setHead(head == it) }

        bigLowerNWGel?.let { it.setHead(head == it) }
        bigLowerNEGel?.let { it.setHead(head == it) }
        bigLowerSWGel?.let { it.setHead(head == it) }
        bigLowerSEGel?.let { it.setHead(head == it) }
    }

    fun heightMapSelectionChanged(r: Recti) {
        if (state.editMode != EditMode.HEIGHT_MAP) return

        val heightMap = state.heightMap ?: return
        val centre = state.heightMapSpawnPt?.pos ?: return
        val rotationPhi = state.heightMapSpawnPt?.rotation ?: 0.0f

        val x0: Int
        val y0: Int
        val x1: Int
        val y1: Int

        if (r.left < r.right) {
            x0 = r.left
            x1 = r.right - 1
        } else {
            x0 = r.right
            x1 = r.left - 1
        }

        if (r.top < r.bottom) {
            y0 = r.top
            y1 = r.bottom - 1
        } else {
            y0 = r.bottom
            y1 = r.top - 1
        }

        val upperNW = MutablePoint3f().also {
            heightMap.getWorldCoords(x0, y0, centre, rotationPhi, it)
            it.x -= SMALL_CURSOR_DELTA
            it.y -= SMALL_CURSOR_DELTA
            it.z += SMALL_CURSOR_DELTA
        }
        val upperNE = MutablePoint3f().also {
            heightMap.getWorldCoords(x0, y1, centre, rotationPhi, it)
            it.x -= SMALL_CURSOR_DELTA
            it.y += SMALL_CURSOR_DELTA
            it.z += SMALL_CURSOR_DELTA
        }
        val upperSW = MutablePoint3f().also {
            heightMap.getWorldCoords(x1, y0, centre, rotationPhi, it)
            it.x += SMALL_CURSOR_DELTA
            it.y -= SMALL_CURSOR_DELTA
            it.z += SMALL_CURSOR_DELTA
        }
        val upperSE = MutablePoint3f().also {
            heightMap.getWorldCoords(x1, y1, centre, rotationPhi, it)
            it.x += SMALL_CURSOR_DELTA
            it.y += SMALL_CURSOR_DELTA
            it.z += SMALL_CURSOR_DELTA
        }

        val lowerNW = MutablePoint3f(upperNW).also { it.z -= 2 * SMALL_CURSOR_DELTA }
        val lowerNE = MutablePoint3f(upperNE).also { it.z -= 2 * SMALL_CURSOR_DELTA }
        val lowerSW = MutablePoint3f(upperSW).also { it.z -= 2 * SMALL_CURSOR_DELTA }
        val lowerSE = MutablePoint3f(upperSE).also { it.z -= 2 * SMALL_CURSOR_DELTA }

        smallUpperNWGel?.moveTo(upperNW)
        smallUpperNEGel?.moveTo(upperNE)
        smallUpperSWGel?.moveTo(upperSW)
        smallUpperSEGel?.moveTo(upperSE)

        smallLowerNWGel?.moveTo(lowerNW)
        smallLowerNEGel?.moveTo(lowerNE)
        smallLowerSWGel?.moveTo(lowerSW)
        smallLowerSEGel?.moveTo(lowerSE)

        smallUpperNWGel?.setHead(false)
        smallUpperNEGel?.setHead(false)
        smallUpperSWGel?.setHead(false)
        smallUpperSEGel?.setHead(false)

        smallLowerNWGel?.setHead(false)
        smallLowerNEGel?.setHead(false)
        smallLowerSWGel?.setHead(false)
        smallLowerSEGel?.setHead(false)

        when {
            r.left < r.right -> when {
                r.top < r.bottom -> {
                    smallUpperSEGel?.setHead(true)
                    smallLowerSEGel?.setHead(true)
                }
                else -> {
                    smallUpperSWGel?.setHead(true)
                    smallLowerSWGel?.setHead(true)
                }
            }
            else -> when {
                r.top < r.bottom -> {
                    smallUpperNEGel?.setHead(true)
                    smallLowerNEGel?.setHead(true)
                }
                else -> {
                    smallUpperNWGel?.setHead(true)
                    smallLowerNWGel?.setHead(true)
                }
            }
        }
    }

    companion object {
        private const val SMALL_CURSOR_DELTA = 0.05f
    }
}
