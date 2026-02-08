package io.github.digorydoo.titanium.engine.core

import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.brick.BrickModelData
import io.github.digorydoo.titanium.engine.brick.BrickVolumeRenderer
import io.github.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.titanium.engine.mesh.ComplexMeshRenderer
import io.github.digorydoo.titanium.engine.mesh.SimpleMeshRenderer
import io.github.digorydoo.titanium.engine.shader.PaperRenderer
import io.github.digorydoo.titanium.engine.shader.Renderer.BlendMode
import io.github.digorydoo.titanium.engine.sky.SkydomeRenderer
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer
import io.github.digorydoo.titanium.engine.texture.Texture
import io.github.digorydoo.kstruct.KstructMap

/**
 * The factory is accessible through App.factory, and is used to instantiate various objects, whose implementation are
 * not part of the engine.
 */
interface Factory {
    fun createBrickVolumeRenderer(translation: Vector3f, tex: Texture, modelData: BrickModelData): BrickVolumeRenderer
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
