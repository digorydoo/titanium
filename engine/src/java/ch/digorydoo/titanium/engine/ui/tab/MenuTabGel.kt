package ch.digorydoo.titanium.engine.ui.tab

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.font.FontManager.FontName
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.i18n.ITextId
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer

class MenuTabGel(private val textId: ITextId, posX: Int, posY: Int): GraphicElement(posX, posY, 0) {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    private val displayText get() = App.i18n.getString(textId)
    private val textTex = App.textures.createTexture(displayText, font = FontName.TOPIC_FONT)

    private val frameSize = MutablePoint2f(textTex.width, textTex.height)
    val width get() = textTex.width
    val height get() = textTex.height

    override val renderer = App.factory.createUISpriteRenderer(
        object: UISpriteRenderer.Delegate() {
            override val tex = this@MenuTabGel.textTex
            override val frameSize = this@MenuTabGel.frameSize
            override val renderPos = this@MenuTabGel.pos
        }
    )

    override fun onRemoveZombie() {
        renderer.free()
        textTex.freeRequireUnshared()
    }
}
