package ch.digorydoo.titanium.engine.editor.cursor

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.mesh.MeshMaterial
import ch.digorydoo.titanium.engine.mesh.SimpleMeshRenderer

class CursorGel(kind: Kind): GraphicElement() {
    enum class Kind {
        BIG_UPPER_NW,
        BIG_UPPER_NE,
        BIG_UPPER_SW,
        BIG_UPPER_SE,
        BIG_LOWER_NW,
        BIG_LOWER_NE,
        BIG_LOWER_SW,
        BIG_LOWER_SE,
        SMALL_UPPER_NW,
        SMALL_UPPER_NE,
        SMALL_UPPER_SW,
        SMALL_UPPER_SE,
        SMALL_LOWER_NW,
        SMALL_LOWER_NE,
        SMALL_LOWER_SW,
        SMALL_LOWER_SE,
    }

    init {
        inDialog = Visibility.FROZEN_VISIBLE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.ACTIVE
    }

    private val mesh = CursorMeshBuilder(kind).build()

    private val renderProps = object: SimpleMeshRenderer.Delegate() {
        override val mesh get() = this@CursorGel.mesh
        override val renderPos get() = this@CursorGel.pos
    }

    override val renderer = App.factory.createSimpleMeshRenderer(
        renderProps,
        antiAliasing = false,
        cullFace = false,
        depthTest = true,
    )

    fun setHead(head: Boolean) {
        mesh.material = if (head) MeshMaterial.WHITE_CLOTH else MeshMaterial.BLACK_CLOTH
    }

    fun show() {
        setHiddenOnNextFrameTo = false
    }

    fun hide() {
        setHiddenOnNextFrameTo = true
    }

    override fun onRemoveZombie() {
        renderer.free()
    }
}
