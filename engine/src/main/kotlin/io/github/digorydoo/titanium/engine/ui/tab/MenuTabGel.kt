package io.github.digorydoo.titanium.engine.ui.tab

import ch.digorydoo.kutils.vector.MutableVector2f
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.font.FontManager.FontName
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.i18n.ITextId
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer

class MenuTabGel(private val textId: ITextId, posX: Int, posY: Int): GraphicElement(posX, posY, 0) {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.ACTIVE
        inEditor = Visibility.ACTIVE
    }

    private val displayText get() = App.i18n.getString(textId)
    private val textTex = App.textures.createTexture(displayText, font = FontName.TOPIC_FONT)

    private val frameSize = MutableVector2f(textTex.width, textTex.height)
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
