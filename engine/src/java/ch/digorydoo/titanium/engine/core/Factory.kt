package ch.digorydoo.titanium.engine.core

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.BrickModelData
import ch.digorydoo.titanium.engine.brick.BrickVolumeRenderer
import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.mesh.MeshRenderer
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.shader.Renderer.BlendMode
import ch.digorydoo.titanium.engine.sky.SkydomeRenderer
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer

/**
 * The factory is accessible through App.factory, and is used to instantiate various objects, whose implementation are
 * not part of the engine.
 */
interface Factory {
    fun createBrickVolumeRenderer(translation: Point3f, tex: Texture, modelData: BrickModelData): BrickVolumeRenderer
    fun createSkydomeRenderer(props: SkydomeRenderer.Delegate): SkydomeRenderer

    fun createMeshRenderer(
        props: MeshRenderer.Delegate,
        antiAliasing: Boolean = false,
        cullFace: Boolean = true,
        depthTest: Boolean = true,
    ): MeshRenderer

    fun createPaperRenderer(
        props: PaperRenderer.Delegate,
        antiAliasing: Boolean = false,
        blendMode: BlendMode = BlendMode.NONE,
        depthTest: Boolean = true,
        stellarObject: Boolean = false,
    ): PaperRenderer

    fun createUISpriteRenderer(props: UISpriteRenderer.Delegate, antiAliasing: Boolean = false): UISpriteRenderer
    fun createSpawnPt(raw: Map<String, String>): SpawnPt

    // createScene is inside SceneId
}
