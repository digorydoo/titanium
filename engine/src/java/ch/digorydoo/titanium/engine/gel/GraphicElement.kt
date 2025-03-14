package ch.digorydoo.titanium.engine.gel

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.string.zapPackageName
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.anim.AnimCycle
import ch.digorydoo.titanium.engine.brick.Brick
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.VicinityList
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody
import ch.digorydoo.titanium.engine.shader.Renderer
import kotlin.math.sqrt

abstract class GraphicElement(open val spawnPt: SpawnPt?, initialPos: Point3f) {
    constructor(): this(null, MutablePoint3f())
    constructor(spawnPt: SpawnPt): this(spawnPt, spawnPt.pos)
    constructor(initialPos: Point3f): this(null, initialPos)
    constructor(x: Float, y: Float, z: Float): this(null, MutablePoint3f(x, y, z))
    constructor(x: Int, y: Int, z: Int): this(null, MutablePoint3f(x, y, z))

    enum class Visibility { ACTIVE, FROZEN_VISIBLE, INVISIBLE }

    interface Behaviour {
        fun animate()
    }

    internal val vicinity = VicinityList() // used by CollisionManager

    val pos = MutablePoint3f(initialPos)
    protected abstract val renderer: Renderer
    open val body: RigidBody? = null
    var cycle: AnimCycle? = null

    var sqrDistanceToCamera = 0.0; private set
    private var hidden = false // hidden gels are neither animated nor rendered
    private var setHiddenOnNextFrameTo: Boolean? = null

    /**
     * Called by CollisionDetector on both gels to check whether their bodies should be bounced. Their bodies are NOT be
     * bounced if at least one of the two gels return false. (E.g. an NPC that should generally collide with stuff
     * may return true for any gel, while a fire that only needs to inform the NPC of the heat returns false to indicate
     * that its shape is not good for bouncing.) The implementation of this function must be efficient and must not
     * have any side effects. This function may be called if gels are in close proximity even if they do not collide.
     * On the other hand, this function may not be called even if gels do collide (which happens if the other gel
     * returned false).
     */
    open fun shouldBounceOnCollision(other: GraphicElement): Boolean {
        return true
    }

    /**
     * Called by CollisionDetector when the gel collides with another. The other gel will also get a callback. If
     * shouldBounceOnCollision returned true for one of the gels, this function will be called after the bodies have
     * been bounced. This function is called even if the bodies were not bounced.
     */
    open fun didCollide(other: GraphicElement, myHit: HitArea, otherHit: HitArea, hitPt: Point3f) {}

    open fun didCollide(brick: Brick, hitPt: Point3f, hitNormal: Point3f) {}

    fun canCollide(): Boolean = when {
        zombie || hidden || body == null -> false
        spawnPt?.canCollide == false -> false
        else -> true
    }

    fun show() {
        setHiddenOnNextFrameTo = false
    }

    fun hide() {
        setHiddenOnNextFrameTo = true
    }

    protected open val inDialog = Visibility.FROZEN_VISIBLE
    protected open val inMenu = Visibility.INVISIBLE
    protected open val inEditor = Visibility.FROZEN_VISIBLE
    protected open val visibleOnScreenshots = true
    protected open val allowNegativeZ = false

    private var active = true // false=disable animation except endless loops
    protected var visible = false; private set // FIXME why is `hidden` not enough
    var zombie = false; private set // true=sprite will be removed in the next frame

    protected open fun onAnimateActive() {}
    protected open fun onAnimateInactive() {}

    // All gels must at least dispose of their renderer. See also SpawnPt.didRemoveGel()
    abstract fun onRemoveZombie()

    fun setZombie() {
        zombie = true
    }

    // Here we will do most of the animation and compute the forces, but not move the body yet.
    fun animatePhase1() {
        if (zombie) return

        setHiddenOnNextFrameTo?.let { hidden = it }
        setHiddenOnNextFrameTo = null
        if (hidden) return

        checkDistanceToCamera()

        active = when {
            App.isAboutToTakeScreenshot -> false
            App.dlg.hasActiveDlg && inDialog != Visibility.ACTIVE -> false
            App.gameMenu.isShown && inMenu != Visibility.ACTIVE -> false
            App.editor.isShown && inEditor != Visibility.ACTIVE -> false
            else -> true
        }

        if (active) {
            onAnimateActive()
        } else {
            onAnimateInactive()
        }

        cycle?.let { cycle ->
            // Endless cycles continue even if the sprite isn't active.
            // Cycles that are not endless can't continue, because they may cause
            // side-effects by calling cycleEnded().
            if (cycle.isEndless || active) {
                cycle.animate()
            }
        }

        body?.applyForces()
    }

    private fun checkDistanceToCamera() {
        sqrDistanceToCamera = App.camera.sourcePos.sqrDistanceTo(pos)

        if (shouldDespawn()) {
            Log.info("Despawning $this, gel distance=${sqrt(sqrDistanceToCamera).toInt()}m")
            setZombie()
        }
    }

    private fun shouldDespawn(): Boolean {
        val spawnPt = spawnPt ?: return false
        return spawnPt.canAutoDespawn(sqrDistanceToCamera)
    }

    // Here we will move the body, and decide whether the gel will be rendered
    fun animatePhase2() {
        if (!zombie && !hidden && active) {
            body?.move()

            if (pos.x.isNaN() || pos.y.isNaN() || pos.z.isNaN()) {
                Log.error("Removing gel $this, because its position became NaN: $pos")
                zombie = true
            } else if (!allowNegativeZ && pos.z < 0.0f) {
                Log.warn("Gel $this will be removed, because it fell through z=0")
                zombie = true
            }
        }

        visible = when {
            hidden -> false
            zombie -> false
            App.isAboutToTakeScreenshot && !visibleOnScreenshots -> false
            App.dlg.hasActiveDlg && inDialog == Visibility.INVISIBLE -> false
            App.gameMenu.isShown && inMenu == Visibility.INVISIBLE -> false
            App.editor.isShown && inEditor == Visibility.INVISIBLE -> false
            else -> true
        }
    }

    fun renderShadows() {
        if (visible && !zombie) {
            renderer.renderShadows()
        }
    }

    fun renderSolid() {
        // We have to test zombie again, because it may have been set by some other instance in the meantime.
        if (visible && !zombie) {
            renderer.renderSolid()
        }
    }

    fun renderTransparent() {
        if (visible && !zombie) {
            renderer.renderTransparent()
        }
    }

    override fun toString() =
        zapPackageName(super.toString())
}
