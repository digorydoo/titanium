package io.github.digorydoo.titanium.engine.scene

import io.github.digorydoo.titanium.engine.brick.BrickVolume
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.core.FrameCounter
import io.github.digorydoo.titanium.engine.core.GameLoop
import io.github.digorydoo.titanium.engine.gel.AbstrPlayerGel
import io.github.digorydoo.titanium.engine.gel.GelLayer
import io.github.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.i18n.EngineTextId

class ActiveSceneContent: GameLoop.Tick {
    var bricks: BrickVolume? = null
    var player: AbstrPlayerGel? = null
    lateinit var scene: Scene // will be init'ed when Main loads the first scene
    var sceneTicket = 0L; private set // incremented each time a scnene is loaded
    var isLoading = false; private set

    private val mainCollidableLayer = GelLayer()
    private val mainNonCollidableLayer = GelLayer()
    private val menuBackdropLayer = GelLayer()
    private val uiBelowDlgLayer = GelLayer()
    private val uiAboveDlgLayer = GelLayer()
    private val stellarObjectsLayer = GelLayer()

    private val allLayers = arrayOf(
        mainCollidableLayer,
        mainNonCollidableLayer,
        menuBackdropLayer,
        uiBelowDlgLayer,
        uiAboveDlgLayer,
        stellarObjectsLayer,
    )

    private val adaptLightingCounter = FrameCounter.everyNthSecond(SECONDS_BETWEEN_ADAPT_LIGHTING)

    fun beginLoading() {
        isLoading = true
        player = null
        bricks?.free()
        bricks = null
        sceneTicket++

        forEachGel { _, gel -> gel.setZombie() }

        scene = object: Scene(
            id = null,
            EngineTextId.LOADING,
            fileNameStem = "",
            Lighting.fineDay1200,
            lightingFollowsStoryTime = false,
            hasSky = false,
            hasShadows = false,
        ) {}
    }

    fun finishLoading() {
        require(isLoading)
        isLoading = false
    }

    override fun tick(token: GameLoop.Token) {
        App.dlg.tick(token)

        mainCollidableLayer.tick(token)
        mainNonCollidableLayer.tick(token)
        menuBackdropLayer.tick(token)
        uiBelowDlgLayer.tick(token)
        uiAboveDlgLayer.tick(token)
        App.gameMenu.tick(token)
        App.editor.tick(token)
        App.camera.tick(token)
        stellarObjectsLayer.tick(token) // must happen after camera.tick()
        App.actions.tick(token)
        App.hud.tick(token) // must happen after actions.tick()

        if (!isLoading) {
            if (scene.lightingFollowsStoryTime && adaptLightingCounter.next() == 0) {
                scene.lighting.adaptToStoryTime()
            }

            App.spawnMgr.tick(token)
        }
    }

    fun renderShadows(@Suppress("unused") token: GameLoop.Token) {
        // When coming here, the target framebuffer is the ShadowBuffer.
        App.shadowBuffer.prepareProjection()
        bricks?.renderShadows()
        mainCollidableLayer.renderShadows()
        mainNonCollidableLayer.renderShadows()
    }

    fun renderRegular(@Suppress("unused") token: GameLoop.Token) {
        // Render solid objects
        bricks?.renderSolid()
        mainCollidableLayer.renderSolid()
        mainNonCollidableLayer.renderSolid()
        App.sky.render() // rendering the sky after mainLayer is beneficial for performance due to early depth tests

        // Render transparent objects
        bricks?.renderTransparent()
        mainCollidableLayer.renderTransparent()
        mainNonCollidableLayer.renderTransparent()
        stellarObjectsLayer.renderTransparent()

        // Render the UI on top
        menuBackdropLayer.renderSolid()
        uiBelowDlgLayer.renderSolid()
        uiAboveDlgLayer.renderSolid()

        menuBackdropLayer.renderTransparent()
        uiBelowDlgLayer.renderTransparent()
        uiAboveDlgLayer.renderTransparent()
    }

    private fun layer(kind: LayerKind) = when (kind) {
        LayerKind.MAIN_COLLIDABLE -> mainCollidableLayer
        LayerKind.MAIN_NON_COLLIDABLE -> mainNonCollidableLayer
        LayerKind.MENU_BACKDROP -> menuBackdropLayer
        LayerKind.UI_BELOW_DLG -> uiBelowDlgLayer
        LayerKind.UI_ABOVE_DLG -> uiAboveDlgLayer
        LayerKind.STELLAR_OBJECTS -> stellarObjectsLayer
    }

    fun add(gel: GraphicElement, kind: LayerKind) {
        layer(kind).add(gel)
    }

    fun find(predicate: (GraphicElement) -> Boolean): GraphicElement? {
        var result: GraphicElement? = null

        allLayers.forEach { layer ->
            if (result == null) {
                layer.forEachGel {
                    if (result == null && predicate(it)) {
                        result = it
                    }
                }
            }
        }

        return result
    }

    fun forEachGel(lambda: (GelLayer, GraphicElement) -> Unit) {
        allLayers.forEach { layer ->
            layer.forEachGel { lambda(layer, it) }
        }
    }

    fun forEachGelInMainLayer(lambda: (GraphicElement) -> Unit) {
        mainCollidableLayer.forEachGel(lambda)
        mainNonCollidableLayer.forEachGel(lambda)
    }

    fun forEachIndexedGelInCollidableLayer(lambda: (Int, GraphicElement) -> Unit) {
        mainCollidableLayer.forEachGelIndexed(lambda)
    }

    fun forEachIndexedGelInCollidableLayer(startIdx: Int, lambda: (Int, GraphicElement) -> Unit) {
        mainCollidableLayer.forEachGelIndexed(startIdx, lambda)
    }

    companion object {
        // const val STORY_TIME_REAL_TIME_RATIO = 6400.0f
        // private const val SECONDS_BETWEEN_ADAPT_LIGHTING = 0.1f

        // const val STORY_TIME_REAL_TIME_RATIO = 500.0f
        // private const val SECONDS_BETWEEN_ADAPT_LIGHTING = 0.2f

        // const val STORY_TIME_REAL_TIME_RATIO = 100.0f
        // private const val SECONDS_BETWEEN_ADAPT_LIGHTING = 0.4f

        const val STORY_TIME_REAL_TIME_RATIO = 1.0f
        private const val SECONDS_BETWEEN_ADAPT_LIGHTING = 10.0f // does not need to happen on every frame
    }
}
