package ch.digorydoo.titanium.engine.gel

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.string.zapPackageName
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.brick.BrickMaterial
import ch.digorydoo.titanium.engine.brick.BrickShape
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.physics.HitArea
import ch.digorydoo.titanium.engine.physics.rigid_body.RigidBody
import ch.digorydoo.titanium.engine.shader.Renderer
import kotlinx.coroutines.Job
import kotlin.math.sqrt

/**
 * A GraphicElement is the base class for all animated objects in the game as well as all UI elements.
 */
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

    open val body: RigidBody? = null

    private val _pos = MutablePoint3f(initialPos) // note that the pos will follow body.pos if body is set
    val pos: Point3f get() = _pos

    protected val bodyPosOffset = MutablePoint3f() // relation is: pos = body.pos - bodyPosOffset

    protected var inDialog = Visibility.FROZEN_VISIBLE
    protected var inMenu = Visibility.INVISIBLE
    protected var inEditor = Visibility.FROZEN_VISIBLE

    var hidden = false; protected set // explicitly hidden gels neither animate nor render
    protected var setHiddenOnNextFrameTo: Boolean? = null // sometimes better than directly modifying hidden
    protected var visible = false; private set // will be set during animPhase2() whether this gel will actually render
    protected var visibleOnScreenshots = true
    private var active = true // whether to call onAnimateActive() or onAnimateInactive()

    protected var callOnCreateConcurrently = false
    private var onCreateCalled = false
    private var onCreateJob: Job? = null
    val initialised get() = onCreateCalled && onCreateJob == null

    var zombie = false; private set // true=sprite will be removed in the next frame
    var sqrDistanceToCamera = 0.0; private set
    var encounterRadius = 0.0f; protected set // encounter radius
    protected var allowNegativeZ = false

    protected abstract val renderer: Renderer
    internal val vicinity = MutableGelSet()

    fun moveTo(newPos: Point3f) {
        moveTo(newPos.x, newPos.y, newPos.z)
    }

    fun moveTo(x: Int, y: Int, z: Int) {
        moveTo(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun moveTo(x: Float, y: Float, z: Float) {
        _pos.set(x, y, z)

        val body = body ?: return
        body.stopAllMotion()

        val bx = x + bodyPosOffset.x
        val by = y + bodyPosOffset.y
        val bz = z + bodyPosOffset.z
        body.pos.set(bx, by, bz)
        body.nextPos.set(bx, by, bz)
    }

    fun onCreate(layer: LayerKind?) {
        require(!onCreateCalled)
        onCreateCalled = true

        if (!callOnCreateConcurrently) {
            if (layer != null) {
                App.content.add(this, layer)
            }
            return
        }

        val sceneTicket = App.content.sceneTicket

        require(onCreateJob == null)
        onCreateJob = App.process.runAsyncIO {
            var caught: Exception? = null
            var done: (() -> Unit)? = null

            try {
                done = onCreateConcurrently()
            } catch (e: Exception) {
                caught = e
            }

            App.process.runAtEndOfFrame {
                // We're back in the main thread.
                val gel = this@GraphicElement
                onCreateJob = null // necessary before adding the gel to the layer

                if (caught != null) {
                    Log.error(TAG, "Gel $gel crashed in onCreateConcurrently: ${caught.message}")
                    zombie = true
                } else if (zombie) {
                    // This is not necessarily a bug since it just means someone else must have set it to zombie.
                    Log.info(TAG, "Not adding $gel to layer since it went zombie while loading concurrently")
                } else if (sceneTicket != App.content.sceneTicket) {
                    Log.info(TAG, "Not adding $gel to layer since the scene has changed")
                    zombie = true
                } else if (layer != null) {
                    // FIXME we need a ticket to check if scene hasn't been unloaded in the meantime
                    done?.invoke()
                    App.content.add(gel, layer)
                }
            }
        }
    }

    /**
     * Called inside a coroutine when callOnCreateConcurrently is true. Do not call any GL functions from here. Avoid
     * modifying the gel; apply the changes in the lambda instead.
     * @return A lambda that will be called back on the main thread. Not called when there was an exception, or when
     *    the gel went zombie in the meantime.
     */
    protected open suspend fun onCreateConcurrently(): () -> Unit {
        throw NotImplementedError() // gels that set callOnCreateConcurrently must override this function
    }

    protected open fun onAnimateActive() {}
    protected open fun onAnimateInactive() {}
    protected open fun onAboutToRender() {}

    // All gels must at least dispose of their renderer. See also SpawnPt.didRemoveGel()
    abstract fun onRemoveZombie()

    fun setZombie() {
        zombie = true
        onCreateJob?.cancel()
    }

    // Here we will do most of the animation and compute the forces, but not move the body yet.
    fun animatePhase1() {
        if (zombie) return

        val spawnPt = spawnPt

        if (spawnPt != null) {
            sqrDistanceToCamera = App.camera.sourcePos.sqrDistanceTo(pos)

            if (spawnPt.canAutoDespawn(sqrDistanceToCamera)) {
                Log.info(TAG, "Despawning $this, gel distance=${sqrt(sqrDistanceToCamera).toInt()}m")
                zombie = true
                return
            }
        }

        setHiddenOnNextFrameTo?.let { hidden = it }
        setHiddenOnNextFrameTo = null
        if (hidden) return

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

        body?.applyForces()
    }

    // Here we will move the body, and decide whether the gel will be rendered
    fun animatePhase2() {
        if (!zombie && !hidden && active) {
            val body = body

            if (body != null) {
                body.move()
                val bp = body.pos
                _pos.set(bp.x - bodyPosOffset.x, bp.y - bodyPosOffset.y, bp.z - bodyPosOffset.z)
            }

            if (pos.x.isNaN() || pos.y.isNaN() || pos.z.isNaN()) {
                Log.error(TAG, "Removing gel $this, because its position became NaN: $pos")
                zombie = true
            } else if (!allowNegativeZ && pos.z < 0.0f) {
                Log.warn(TAG, "Gel $this will be removed, because it fell through z=0")
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

        if (visible) {
            onAboutToRender()
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

    /**
     * Subclasses overriding canCollideWithGels or canCollideWithBricks should always call this method to determine
     * whether the gel is actually able to receive collisions at all.
     */
    protected fun canCollide(): Boolean {
        if (zombie || hidden || !active) return false
        val body = body ?: return false // a body is required
        val spawnPt = spawnPt ?: return body.gravity // a gel affected by gravity should usually collide
        return spawnPt.canCollide // let the spawn pt decide
    }

    /**
     * A sword currently held by a character is an example of a gel returning true here but returning false from
     * canCollideWithBricks. The sword can safely ignore bricks, but should be able to hit the opponent.
     */
    open fun canCollideWithGels(): Boolean = canCollide()

    /**
     * A rain drop is an example of a gel returning true here but returning false from canCollideWithGels. The small
     * raindrop may fall through objects, but it should splash on the floor.
     */
    open fun canCollideWithBricks(): Boolean = canCollide()

    /**
     * A fire or poisonous gas is an example of a gel returning false here. Other gels may need to react to collisions
     * with it, but it should be allowed to overlap. A poisonous gas may even return true from canCollideWithBricks and
     * cleverly glide along the floor while not being strictly bounced off the floor.
     */
    open fun shouldBounceOnCollision(): Boolean = true

    /**
     * Called by CollisionManager when the gel collides with another. The other gel will also get a callback.
     */
    open fun onCollide(
        other: GraphicElement,
        myHit: HitArea,
        otherHit: HitArea,
        hitPt: Point3f,
        normalTowardsMe: Point3f,
    ) {
    }

    /**
     * Called by CollisionManager when the gel collides with a brick.
     */
    open fun onCollide(
        shape: BrickShape,
        material: BrickMaterial,
        myHit: HitArea,
        otherHit: HitArea,
        hitPt: Point3f,
        normalTowardsMe: Point3f,
    ) {
    }

    /**
     * Both gels must return true here to allow an encounter. The encounter radius is checked separately, and the
     * implementation of this function should not depend on it as it is irrelevant for other gels encountering this
     * one. A gel that can be encountered but does not encounter any gels should return true here and have an encounter
     * radius of 0.
     */
    open fun canEncounterOrBeFound(): Boolean = false

    /**
     * Called when the other gel gets inside this gel's encounter radius. The other gel's encounter radius is
     * irrelevant. If the other gel has a body, its enclosing radius is taken into account, otherwise the gel's
     * position is used as a point location. This gel's body and enclosing radius are irrelevant.
     */
    open fun onEncounter(other: GraphicElement) {}

    override fun toString() =
        zapPackageName(super.toString())

    companion object {
        private val TAG = Log.Tag("GraphicElement")
    }
}
