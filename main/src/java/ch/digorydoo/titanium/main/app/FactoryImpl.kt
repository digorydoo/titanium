package ch.digorydoo.titanium.main.app

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.BrickModelData
import ch.digorydoo.titanium.engine.core.Factory
import ch.digorydoo.titanium.engine.mesh.MeshRenderer
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.shader.Renderer.BlendMode
import ch.digorydoo.titanium.engine.sky.SkydomeRenderer
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.engine.ui.UISpriteRenderer
import ch.digorydoo.titanium.game.core.SpawnObjType
import ch.digorydoo.titanium.game.gel.pickup.PickupSpawnPt
import ch.digorydoo.titanium.game.gel.static_mesh.StaticMeshSpawnPt
import ch.digorydoo.titanium.game.gel.static_paper.StaticPaperSpawnPt
import ch.digorydoo.titanium.game.gel.street_lamp.StreetLampSpawnPt
import ch.digorydoo.titanium.game.gel.test.TestSpawnPt
import ch.digorydoo.titanium.main.shader.MeshRendererImpl
import ch.digorydoo.titanium.main.shader.PaperRendererImpl
import ch.digorydoo.titanium.main.shader.SkydomeRendererImpl
import ch.digorydoo.titanium.main.shader.UISpriteRendererImpl
import ch.digorydoo.titanium.main.shader.bricks.BrickVolumeRendererImpl

class FactoryImpl: Factory {
    override fun createBrickVolumeRenderer(translation: Point3f, tex: Texture, modelData: BrickModelData) =
        BrickVolumeRendererImpl(translation, tex, modelData)

    override fun createSkydomeRenderer(props: SkydomeRenderer.Delegate) =
        SkydomeRendererImpl(props)

    override fun createMeshRenderer(
        props: MeshRenderer.Delegate,
        antiAliasing: Boolean,
        cullFace: Boolean,
        depthTest: Boolean,
    ) = MeshRendererImpl(
        props,
        antiAliasing = antiAliasing,
        cullFace = cullFace,
        depthTest = depthTest,
    )

    override fun createPaperRenderer(
        props: PaperRenderer.Delegate,
        antiAliasing: Boolean,
        blendMode: BlendMode,
        depthTest: Boolean,
        stellarObject: Boolean,
    ) = PaperRendererImpl(
        props,
        antiAliasing = antiAliasing,
        blendMode = blendMode,
        depthTest = depthTest,
        stellarObject = stellarObject,
    )

    override fun createUISpriteRenderer(props: UISpriteRenderer.Delegate, antiAliasing: Boolean) =
        UISpriteRendererImpl(props, antiAliasing = antiAliasing)

    override fun createSpawnPt(raw: Map<String, String>) =
        when (SpawnObjType.fromString(raw["spawnObjType"]!!)) {
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
            SpawnObjType.STREET_LAMP_TRADITIONAL -> StreetLampSpawnPt(raw, StreetLampSpawnPt.Kind.TRADITIONAL)
            SpawnObjType.VASE -> PickupSpawnPt(raw, PickupSpawnPt.Kind.VASE)
            SpawnObjType.TEST_GEL -> TestSpawnPt(raw)
        }
}
