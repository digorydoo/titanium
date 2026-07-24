package io.github.digorydoo.titanium.engine.gel

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.core.GameLoop

class GelLayer: GameLoop.Tick {
    enum class LayerKind {
        MAIN_COLLIDABLE, MAIN_NON_COLLIDABLE, MENU_BACKDROP, UI_BELOW_DLG, UI_ABOVE_DLG, STELLAR_OBJECTS
    }

    private val gels = mutableListOf<GraphicElement>()
    private val newGels = mutableListOf<GraphicElement>()

    fun add(gel: GraphicElement) {
        require(gel.initialised) { "Gel $gel is not properly initialised, cannot add it to layer!" }

        // To make sure that newly added gels are rendered after their animate phases have run,
        // we add them to newGels first and move them to gels in animate().
        newGels.add(gel)
    }

    fun forEachGel(lambda: (gel: GraphicElement) -> Unit) {
        gels.forEach(lambda)
        newGels.forEach(lambda)
    }

    fun forEachGelIndexed(lambda: (i: Int, gel: GraphicElement) -> Unit) {
        gels.forEachIndexed(lambda)
    }

    fun forEachGelIndexed(startIdx: Int, lambda: (i: Int, gel: GraphicElement) -> Unit) {
        (startIdx ..< gels.size).forEach { lambda(it, gels[it]) }
    }

    override fun tick(token: GameLoop.Token) {
        if (newGels.isNotEmpty()) {
            gels.addAll(newGels)
            newGels.clear()
        }

        gels.forEach { gel ->
            try {
                gel.animatePhase1()
            } catch (e: Exception) {
                Log.error(TAG, "A gel crashes in animatePhase1: $gel\n${e.stackTraceToString()}")
                gel.setZombie()
            }
        }

        App.collisions.handleCollisions()
        var anyToRemove = false

        gels.forEach { gel ->
            try {
                gel.animatePhase2() // moves the RigidBody according to forces
            } catch (e: Exception) {
                Log.error(TAG, "A gel crashed in animatePhase2: $gel\n${e.stackTraceToString()}")
                gel.setZombie()
            }

            if (gel.zombie) {
                try {
                    Log.info(TAG, "About to remove $gel")
                    anyToRemove = true
                    gel.onRemoveZombie()
                    gel.spawnPt?.didRemoveGel()
                } catch (e: Exception) {
                    Log.error(TAG, "A gel crashed in aboutToRemove/didRemoveGel: $gel\n${e.stackTraceToString()}")
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
                Log.error(TAG, "A gel crashed in renderShadows: $gel\n${e.stackTraceToString()}")
                gel.setZombie()
            }
        }
    }

    fun renderSolid() {
        gels.forEach { gel ->
            try {
                gel.renderSolid()
            } catch (e: Exception) {
                Log.error(TAG, "A gel crashed in renderSolid: $gel\n${e.stackTraceToString()}")
                gel.setZombie()
            }
        }
    }

    fun renderTransparent() {
        gels.forEach { gel ->
            try {
                gel.renderTransparent()
            } catch (e: Exception) {
                Log.error(TAG, "A gel crashed in renderTransparent: $gel\n${e.stackTraceToString()}")
                gel.setZombie()
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("GelLayer")
    }
}
