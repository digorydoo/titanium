package ch.digorydoo.titanium.game.ui.map

import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer

class MapGel: GraphicElement() {
    init {
        inDialog = Visibility.FROZEN_VISIBLE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    private val tex = App.textures.getOrCreateTexture("map-town-01.png")
    private val frameSize = Point2f(tex?.width ?: 0, tex?.height ?: 0)
    private val scaleFactor = Point2f(0.75f, 0.75f)

    val width get() = frameSize.x * scaleFactor.x
    val height get() = frameSize.y * scaleFactor.y

    override val renderer = App.factory.createUISpriteRenderer(
        object: UISpriteRenderer.Delegate() {
            override val frameSize = this@MapGel.frameSize
            override val renderPos = this@MapGel.pos
            override val tex = this@MapGel.tex
            override val scaleFactor = this@MapGel.scaleFactor
        },
        antiAliasing = true
    )

    override fun onRemoveZombie() {
        renderer.free()
    }
}
