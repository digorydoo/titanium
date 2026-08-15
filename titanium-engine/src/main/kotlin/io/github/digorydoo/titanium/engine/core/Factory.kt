package io.github.digorydoo.titanium.engine.core

import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.kstruct.KstructMap
import io.github.digorydoo.titanium.engine.brick.BrickModelData
import io.github.digorydoo.titanium.engine.brick.BrickVolumeRenderer
import io.github.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.titanium.engine.mesh.ComplexMeshRenderer
import io.github.digorydoo.titanium.engine.mesh.SimpleMeshRenderer
import io.github.digorydoo.titanium.engine.shader.PaperRenderer
import io.github.digorydoo.titanium.engine.shader.Renderer.BlendMode
import io.github.digorydoo.titanium.engine.sky.SkydomeRenderer
import io.github.digorydoo.titanium.engine.sprite.UICircularProgressRenderer
import io.github.digorydoo.titanium.engine.sprite.UISolidRenderer
import io.github.digorydoo.titanium.engine.sprite.UISpriteRenderer
import io.github.digorydoo.titanium.engine.sprite.UISwishFadeRenderer
import io.github.digorydoo.titanium.engine.texture.Texture

/**
 * The factory is accessible through App.factory, and is used to instantiate various objects, whose implementation are
 * not part of the engine.
 */
interface Factory {
    fun createBrickVolumeRenderer(translation: Vector3f, tex: Texture, modelData: BrickModelData): BrickVolumeRenderer
    fun createSkydomeRenderer(delegate: SkydomeRenderer.Delegate): SkydomeRenderer

    fun createSimpleMeshRenderer(
        delegate: SimpleMeshRenderer.Delegate,
        antiAliasing: Boolean = false,
        cullFace: Boolean = true,
        depthTest: Boolean = true,
    ): SimpleMeshRenderer

    fun createComplexMeshRenderer(
        delegate: ComplexMeshRenderer.Delegate,
        antiAliasing: Boolean = false,
        cullFace: Boolean = true,
        depthTest: Boolean = true,
        hasSkeleton: Boolean = false,
    ): ComplexMeshRenderer

    fun createPaperRenderer(
        delegate: PaperRenderer.Delegate,
        antiAliasing: Boolean = false,
        blendMode: BlendMode = BlendMode.NONE,
        depthTest: Boolean = true,
        stellarObject: Boolean = false,
    ): PaperRenderer

    fun createUISpriteRenderer(delegate: UISpriteRenderer.Delegate, antiAliasing: Boolean = false): UISpriteRenderer
    fun createUICircularProgressRenderer(delegate: UICircularProgressRenderer.Delegate): UICircularProgressRenderer
    fun createUISolidRenderer(delegate: UISolidRenderer.Delegate): UISolidRenderer
    fun createUISwishFadeRenderer(delegate: UISwishFadeRenderer.Delegate): UISwishFadeRenderer
    fun createSpawnPt(raw: KstructMap): SpawnPt

    // createScene is inside SceneId
}
