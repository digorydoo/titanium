package io.github.digorydoo.titanium.main.core

import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.kstruct.KstructMap
import io.github.digorydoo.titanium.engine.brick.BrickModelData
import io.github.digorydoo.titanium.engine.core.Factory
import io.github.digorydoo.titanium.engine.gel.SpawnPt
import io.github.digorydoo.titanium.engine.heightmap.HeightMapSpawnPt
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
import io.github.digorydoo.titanium.game.core.SpawnObjType
import io.github.digorydoo.titanium.game.gel.ball.BallSpawnPt
import io.github.digorydoo.titanium.game.gel.door.DoorSpawnPt
import io.github.digorydoo.titanium.game.gel.static_mesh.StaticMeshSpawnPt
import io.github.digorydoo.titanium.game.gel.static_paper.StaticPaperSpawnPt
import io.github.digorydoo.titanium.game.gel.street_lamp.StreetLampSpawnPt
import io.github.digorydoo.titanium.game.gel.test.TestSpawnPt
import io.github.digorydoo.titanium.game.gel.vase.VaseSpawnPt
import io.github.digorydoo.titanium.main.renderer.*

class FactoryImpl: Factory {
    override fun createBrickVolumeRenderer(translation: Vector3f, tex: Texture, modelData: BrickModelData) =
        BrickVolumeRendererImpl(translation, tex, modelData)

    override fun createSkydomeRenderer(delegate: SkydomeRenderer.Delegate) =
        SkydomeRendererImpl(delegate)

    override fun createSimpleMeshRenderer(
        delegate: SimpleMeshRenderer.Delegate,
        antiAliasing: Boolean,
        cullFace: Boolean,
        depthTest: Boolean,
    ) = SimpleMeshRendererImpl(
        delegate,
        antiAliasing = antiAliasing,
        cullFace = cullFace,
        depthTest = depthTest,
    )

    override fun createComplexMeshRenderer(
        delegate: ComplexMeshRenderer.Delegate,
        antiAliasing: Boolean,
        cullFace: Boolean,
        depthTest: Boolean,
        hasSkeleton: Boolean,
    ) = ComplexMeshRendererImpl(
        delegate,
        antiAliasing = antiAliasing,
        cullFace = cullFace,
        depthTest = depthTest,
        hasSkeleton = hasSkeleton,
    )

    override fun createPaperRenderer(
        delegate: PaperRenderer.Delegate,
        antiAliasing: Boolean,
        blendMode: BlendMode,
        depthTest: Boolean,
        stellarObject: Boolean,
    ) = PaperRendererImpl(
        delegate,
        antiAliasing = antiAliasing,
        blendMode = blendMode,
        depthTest = depthTest,
        stellarObject = stellarObject,
    )

    override fun createUISpriteRenderer(delegate: UISpriteRenderer.Delegate, antiAliasing: Boolean) =
        UISpriteRendererImpl(delegate, antiAliasing = antiAliasing)

    override fun createUICircularProgressRenderer(delegate: UICircularProgressRenderer.Delegate) =
        UICircularProgressRendererImpl(delegate)

    override fun createUISolidRenderer(delegate: UISolidRenderer.Delegate) =
        UISolidRendererImpl(delegate)

    override fun createUISwishFadeRenderer(delegate: UISwishFadeRenderer.Delegate) =
        UISwishFadeRendererImpl(delegate)

    override fun createSpawnPt(raw: KstructMap): SpawnPt {
        val rawType = raw["type"]?.stringOrNull() ?: throw Exception("Missing type in raw spawnpt def")
        return when (SpawnObjType.fromString(rawType)) {
            // StaticMesh
            SpawnObjType.BENCH_1 -> StaticMeshSpawnPt(raw, StaticMeshSpawnPt.Kind.BENCH_1)
            SpawnObjType.RAILING_1 -> StaticMeshSpawnPt(raw, StaticMeshSpawnPt.Kind.RAILING_1)
            SpawnObjType.RAILING_2 -> StaticMeshSpawnPt(raw, StaticMeshSpawnPt.Kind.RAILING_2)
            SpawnObjType.ROBOT_POLICEMAN -> StaticMeshSpawnPt(raw, StaticMeshSpawnPt.Kind.ROBOT_POLICEMAN)
            SpawnObjType.SIGN_1 -> StaticMeshSpawnPt(raw, StaticMeshSpawnPt.Kind.SIGN_1)
            SpawnObjType.STONE_1 -> StaticMeshSpawnPt(raw, StaticMeshSpawnPt.Kind.STONE_1)

            // StaticPaper
            SpawnObjType.GNARLED_TREE_LARGE -> StaticPaperSpawnPt(raw, StaticPaperSpawnPt.Kind.GNARLED_TREE_LARGE)
            SpawnObjType.GNARLED_TREE_MEDIUM -> StaticPaperSpawnPt(raw, StaticPaperSpawnPt.Kind.GNARLED_TREE_MEDIUM)
            SpawnObjType.GNARLED_TREE_SMALL -> StaticPaperSpawnPt(raw, StaticPaperSpawnPt.Kind.GNARLED_TREE_SMALL)
            SpawnObjType.ROUND_TREE -> StaticPaperSpawnPt(raw, StaticPaperSpawnPt.Kind.ROUND_TREE)

            // other
            SpawnObjType.BALL_R25CM -> BallSpawnPt(raw, BallSpawnPt.Kind.BALL_R25CM)
            SpawnObjType.BALL_R33CM -> BallSpawnPt(raw, BallSpawnPt.Kind.BALL_R33CM)
            SpawnObjType.DOOR_WITH_WOODEN_FRAME -> DoorSpawnPt(raw, DoorSpawnPt.Kind.DOOR_WITH_WOODEN_FRAME)
            SpawnObjType.HEIGHT_MAP -> HeightMapSpawnPt(raw)
            SpawnObjType.STREET_LAMP_TRADITIONAL -> StreetLampSpawnPt(raw, StreetLampSpawnPt.Kind.TRADITIONAL)
            SpawnObjType.VASE_H1M -> VaseSpawnPt(raw, VaseSpawnPt.Kind.VASE_H1M)
            SpawnObjType.TEST_GEL -> TestSpawnPt(raw)
        }
    }
}
