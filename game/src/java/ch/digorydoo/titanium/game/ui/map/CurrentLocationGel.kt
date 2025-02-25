package ch.digorydoo.titanium.game.ui.map

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer

class CurrentLocationGel: GraphicElement() {
    override val inDialog = Visibility.FROZEN_VISIBLE
    override val inMenu = Visibility.ACTIVE
    override val inEditor = Visibility.ACTIVE

    private val tex = App.textures.getOrCreateTexture("ui-map-current-location.png")
    private val frameSize = Point2f(tex?.width ?: 0, tex?.height ?: 0)
    private val scaleFactor = Point2f(0.75f, 0.75f)
    private val renderPos = MutablePoint3f()
    var rotationPhi = 0.0f

    override val renderer = App.factory.createUISpriteRenderer(
        object: UISpriteRenderer.Delegate() {
            override val frameSize = this@CurrentLocationGel.frameSize
            override val renderPos
                get() = this@CurrentLocationGel.renderPos.apply {
                    set(pos.x - scaleFactor.x * frameSize.x / 2, pos.y - scaleFactor.y * frameSize.y / 2, pos.z)
                }
            override val tex = this@CurrentLocationGel.tex
            override val scaleFactor = this@CurrentLocationGel.scaleFactor
            override val rotation get() = this@CurrentLocationGel.rotationPhi
        },
        antiAliasing = true
    )

    override fun onRemoveZombie() {
        renderer.free()
    }
}
