package ch.digorydoo.titanium.engine.gel

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.physics.RigidBody.Companion.STATIC_MASS

class GelLayer(private val detectCollisions: Boolean = false) {
    enum class LayerKind {
        MAIN_COLLIDABLE, MAIN_NON_COLLIDABLE, MENU_BACKDROP, UI_BELOW_DLG, UI_ABOVE_DLG, STELLAR_OBJECTS
    }

    private val gels = mutableListOf<GraphicElement>()
    private val newGels = mutableListOf<GraphicElement>()

    fun add(gel: GraphicElement) {
        // To make sure that newly added gels are rendered after their animate phases have run,
        // we add them to newGels first and move them to gels in animate().
        newGels.add(gel)
    }

    fun forEachGel(includeNew: Boolean, lambda: (gel: GraphicElement) -> Unit) {
        gels.forEach(lambda)

        if (includeNew) {
            newGels.forEach(lambda)
        }
    }

    fun animate() {
        if (newGels.isNotEmpty()) {
            gels.addAll(newGels)
            newGels.clear()
        }

        gels.forEach { gel ->
            try {
                gel.animatePhase1()
            } catch (e: Exception) {
                Log.error("A gel crashes in animatePhase1: $gel\n${e.stackTraceToString()}")
                gel.setZombie()
            }
        }

        if (detectCollisions) {
            val c = App.collisions

            gels.forEachIndexed { i1, gel1 ->
                if (gel1.canCollide()) {
                    (i1 + 1 ..< gels.size).forEach { i2 ->
                        val gel2 = gels[i2]
                        try {
                            if (gel2.canCollide()) {
                                c.handleCollisions(gel1, gel2)
                            }
                        } catch (e: Exception) {
                            Log.error(
                                "Exception during collision detection: $gel1 vs. $gel2\n${e.stackTraceToString()}"
                            )
                            gel1.setZombie()
                            gel2.setZombie()
                        }
                    }

                    val body = gel1.body

                    if (body != null && body.mass < STATIC_MASS) {
                        c.handleCollisions(gel1, App.bricks)
                    }
                }
            }
        }

        var anyToRemove = false

        gels.forEach { gel ->
            try {
                gel.animatePhase2() // moves the RigidBody according to forces
            } catch (e: Exception) {
                Log.error("A gel crashed in animatePhase2: $gel\n${e.stackTraceToString()}")
                gel.setZombie()
            }

            if (gel.zombie) {
                try {
                    Log.info("About to remove $gel")
                    anyToRemove = true
                    gel.onRemoveZombie()
                    gel.spawnPt?.didRemoveGel()
                } catch (e: Exception) {
                    Log.error("A gel crashed in aboutToRemove/didRemoveGel: $gel\n${e.stackTraceToString()}")
                    gel.setZombie()
                    anyToRemove = true
                }
            }
        }

        if (anyToRemove) {
            gels.removeAll { it.zombie }
        }
    }

    fun renderShadows() {
        gels.forEach { gel ->
            try {
                gel.renderShadows()
            } catch (e: Exception) {
                Log.error("A gel crashed in renderShadows: $gel\n${e.stackTraceToString()}")
                gel.setZombie()
            }
        }
    }

    fun renderSolid() {
        gels.forEach { gel ->
            try {
                gel.renderSolid()
            } catch (e: Exception) {
                Log.error("A gel crashed in renderSolid: $gel\n${e.stackTraceToString()}")
                gel.setZombie()
            }
        }
    }

    fun renderTransparent() {
        gels.forEach { gel ->
            try {
                gel.renderTransparent()
            } catch (e: Exception) {
                Log.error("A gel crashed in renderTransparent: $gel\n${e.stackTraceToString()}")
                gel.setZombie()
            }
        }
    }
}
