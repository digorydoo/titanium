package io.github.digorydoo.titanium.engine.gel

import ch.digorydoo.kutils.math.accel
import ch.digorydoo.kutils.vector.Vector2f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.sprite.UISwishFadeRenderer

class SwishFadeGel: GraphicElement() {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    private val renderPos = Vector3f(-1f, -1f, 0f)
    private val frameSize = App.resolutionMgr.screenSizeDp.let { Vector2f(it.x + 2, it.y + 2) }

    override val renderer = App.factory.createUISwishFadeRenderer(
        object: UISwishFadeRenderer.Delegate() {
            override val renderPos get() = this@SwishFadeGel.renderPos
            override val frameSize = this@SwishFadeGel.frameSize
            override val progress get() = this@SwishFadeGel.progress
        }
    )

    private var progress = 0.0f
    private var frameCount = 0
    private var startTime = 0f

    override fun onAnimateActive() {
        if (frameCount < SKIP_FRAMES) {
            frameCount++
            return
        } else if (startTime == 0f) {
            startTime = App.time.sessionTime
            return
        }

        val now = App.time.sessionTime
        val rel = (now - startTime) / ANIM_DURATION

        if (rel >= 1.0f) {
            setZombie()
        } else {
            progress = accel(rel, 2.1f)
        }
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "SwishFadeGel"

    companion object {
        private const val SKIP_FRAMES = 3 // skip the first few frames, because the scene will be loading
        private const val ANIM_DURATION = 0.33f // seconds
    }
}
