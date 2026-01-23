package ch.digorydoo.titanium.main.core

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.brick.BrickModelData
import ch.digorydoo.titanium.engine.core.Factory
import ch.digorydoo.titanium.engine.gel.SpawnPt
import ch.digorydoo.titanium.engine.heightmap.HeightMapSpawnPt
import ch.digorydoo.titanium.engine.mesh.ComplexMeshRenderer
import ch.digorydoo.titanium.engine.mesh.SimpleMeshRenderer
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.shader.Renderer.BlendMode
import ch.digorydoo.titanium.engine.sky.SkydomeRenderer
import ch.digorydoo.titanium.engine.sprite.UISpriteRenderer
import ch.digorydoo.titanium.engine.texture.Texture
import ch.digorydoo.titanium.game.core.SpawnObjType
import ch.digorydoo.titanium.game.gel.ball.BallSpawnPt
import ch.digorydoo.titanium.game.gel.door.DoorSpawnPt
import ch.digorydoo.titanium.game.gel.static_mesh.StaticMeshSpawnPt
import ch.digorydoo.titanium.game.gel.static_paper.StaticPaperSpawnPt
import ch.digorydoo.titanium.game.gel.street_lamp.StreetLampSpawnPt
import ch.digorydoo.titanium.game.gel.test.TestSpawnPt
import ch.digorydoo.titanium.game.gel.vase.VaseSpawnPt
import ch.digorydoo.titanium.main.brick.BrickVolumeRendererImpl
import ch.digorydoo.titanium.main.mesh.ComplexMeshRendererImpl
import ch.digorydoo.titanium.main.mesh.SimpleMeshRendererImpl
import ch.digorydoo.titanium.main.shader.PaperRendererImpl
import ch.digorydoo.titanium.main.shader.SkydomeRendererImpl
import ch.digorydoo.titanium.main.shader.UISpriteRendererImpl
import io.github.digorydoo.kstruct.KstructMap

class FactoryImpl: Factory {
    override fun createBrickVolumeRenderer(translation: Point3f, tex: Texture, modelData: BrickModelData) =
        BrickVolumeRendererImpl(translation, tex, modelData)

    override fun createSkydomeRenderer(props: SkydomeRenderer.Delegate) =
        SkydomeRendererImpl(props)

    override fun createSimpleMeshRenderer(
        props: SimpleMeshRenderer.Delegate,
        antiAliasing: Boolean,
        cullFace: Boolean,
        depthTest: Boolean,
    ) = SimpleMeshRendererImpl(
        props,
        antiAliasing = antiAliasing,
        cullFace = cullFace,
        depthTest = depthTest,
    )

    override fun createComplexMeshRenderer(
        props: ComplexMeshRenderer.Delegate,
        antiAliasing: Boolean,
        cullFace: Boolean,
        depthTest: Boolean,
    ) = ComplexMeshRendererImpl(
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

    override fun createSpawnPt(raw: KstructMap): SpawnPt {
        val rawType = raw["type"]?.stringOrNull() ?: throw Exception("Missing type in raw spawn pt def")
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
