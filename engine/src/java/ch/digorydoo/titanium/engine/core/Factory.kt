package ch.digorydoo.titanium.engine.core

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.BrickModelData
import ch.digorydoo.titanium.engine.brick.BrickVolumeRenderer
import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.mesh.ComplexMeshRenderer
import ch.digorydoo.titanium.engine.mesh.SimpleMeshRenderer
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.shader.Renderer.BlendMode
import ch.digorydoo.titanium.engine.sky.SkydomeRenderer
import ch.digorydoo.titanium.engine.sprite.UISpriteRenderer
import ch.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.kstruct.KstructMap

/**
 * The factory is accessible through App.factory, and is used to instantiate various objects, whose implementation are
 * not part of the engine.
 */
interface Factory {
    fun createBrickVolumeRenderer(translation: Point3f, tex: Texture, modelData: BrickModelData): BrickVolumeRenderer
    fun createSkydomeRenderer(props: SkydomeRenderer.Delegate): SkydomeRenderer

    fun createSimpleMeshRenderer(
        props: SimpleMeshRenderer.Delegate,
        antiAliasing: Boolean = false,
        cullFace: Boolean = true,
        depthTest: Boolean = true,
    ): SimpleMeshRenderer

    fun createComplexMeshRenderer(
        props: ComplexMeshRenderer.Delegate,
        antiAliasing: Boolean = false,
        cullFace: Boolean = true,
        depthTest: Boolean = true,
    ): ComplexMeshRenderer

    fun createPaperRenderer(
        props: PaperRenderer.Delegate,
        antiAliasing: Boolean = false,
        blendMode: BlendMode = BlendMode.NONE,
        depthTest: Boolean = true,
        stellarObject: Boolean = false,
    ): PaperRenderer

    fun createUISpriteRenderer(props: UISpriteRenderer.Delegate, antiAliasing: Boolean = false): UISpriteRenderer
    fun createSpawnPt(raw: KstructMap): SpawnPt

    // createScene is inside SceneId
}
