package ch.digorydoo.titanium.engine.editor.cursor

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.mesh.MeshMaterial
import ch.digorydoo.titanium.engine.mesh.MeshRenderer

class CursorGel(kind: Kind): GraphicElement() {
    enum class Kind { UPPER_NW, UPPER_NE, UPPER_SW, UPPER_SE, LOWER_NW, LOWER_NE, LOWER_SW, LOWER_SE }

    private val mesh = CursorMeshBuilder(kind).build()

    private val renderProps = object: MeshRenderer.Delegate() {
        override val mesh get() = this@CursorGel.mesh
        override val renderPos get() = this@CursorGel.pos
    }

    override val renderer = App.factory.createMeshRenderer(
        renderProps,
        antiAliasing = false,
        cullFace = true,
        depthTest = true,
    )

    override val inDialog = Visibility.FROZEN_VISIBLE
    override val inMenu = Visibility.INVISIBLE
    override val inEditor = Visibility.ACTIVE

    fun setHead(head: Boolean) {
        val division = mesh.divisions.firstOrNull() ?: return
        if (head) {
            division.material = MeshMaterial.WHITE_CLOTH
        } else {
            division.material = MeshMaterial.BLACK_CLOTH
        }
    }

    override fun onRemoveZombie() {
        renderer.free()
    }
}
