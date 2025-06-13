package ch.digorydoo.titanium.engine.scene

import ch.digorydoo.titanium.engine.brick.BrickVolume
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.core.FrameCounter
import ch.digorydoo.titanium.engine.gel.AbstrPlayerGel
import ch.digorydoo.titanium.engine.gel.GelLayer
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.gel.GraphicElement

class ActiveSceneContent(startScene: Scene) {
    var bricks: BrickVolume? = null
    var player: AbstrPlayerGel? = null
    var scene: Scene = startScene
    var isLoading = false

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

    fun animate() {
        App.dlg.handle()

        mainCollidableLayer.animate()
        mainNonCollidableLayer.animate()
        menuBackdropLayer.animate()
        uiBelowDlgLayer.animate()
        uiAboveDlgLayer.animate()
        App.gameMenu.animate()
        App.editor.animate()
        App.camera.animate()
        stellarObjectsLayer.animate() // must happen after camera.animate()
        App.actions.maintain()
        App.hud.animate() // must happen after actions.maintain()

        if (!isLoading) {
            if (scene.lightingFollowsStoryTime && adaptLightingCounter.next() == 0) {
                scene.lighting.adaptToStoryTime()
            }

            App.spawnMgr.spawnGels()
        }
    }

    fun renderShadows() {
        // When coming here, the target framebuffer is the ShadowBuffer.
        App.shadowBuffer.prepareProjection()
        bricks?.renderShadows()
        mainCollidableLayer.renderShadows()
        mainNonCollidableLayer.renderShadows()
    }

    fun renderRegular() {
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

    fun setAllGelsToZombie() {
        allLayers.forEach { layer ->
            layer.forEachGel(includeNew = true) { it.setZombie() }
        }
    }

    fun forEachGelInMainLayerIncludingNew(lambda: (gel: GraphicElement) -> Unit) {
        mainCollidableLayer.forEachGel(includeNew = true, lambda)
        mainNonCollidableLayer.forEachGel(includeNew = true, lambda)
    }

    fun forEachIndexedGelInCollidableLayer(lambda: (i: Int, gel: GraphicElement) -> Unit) {
        mainCollidableLayer.forEachGelIndexed(lambda)
    }

    fun forEachIndexedGelInCollidableLayer(startIdx: Int, lambda: (i: Int, gel: GraphicElement) -> Unit) {
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
