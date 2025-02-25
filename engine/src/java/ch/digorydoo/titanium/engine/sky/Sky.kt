package ch.digorydoo.titanium.engine.sky

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind

class Sky {
    private var enabled = false
    private var skydome: Skydome? = null
    private var sun: SunGel? = null

    // Called by SceneLoader asynchronously.
    fun load() {
        skydome = Skydome()
        sun = SunGel()
    }

    fun enable() {
        sun?.let { App.content.add(it, LayerKind.STELLAR_OBJECTS) }
        enabled = true
    }

    fun unload() {
        skydome?.free()
        skydome = null
        sun?.setZombie()
        sun = null
        enabled = false
    }

    fun render() {
        if (!enabled || App.gameMenu.isShown) return
        skydome?.render()
    }
}
