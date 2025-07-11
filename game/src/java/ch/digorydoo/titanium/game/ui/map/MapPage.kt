package ch.digorydoo.titanium.game.ui.map

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.ui.tab.MenuTabPage

class MapPage: MenuTabPage {
    var mapScaleFactor = 0.75f; private set
    private var mapGel: MapGel? = null
    private var curLocationGel: CurrentLocationGel? = null

    fun makeGels() {
        require(mapGel == null)
        mapGel = MapGel(this).also {
            it.onCreate(LayerKind.UI_BELOW_DLG)
            it.hide()
            it.moveTo(App.screenWidthDp * 0.5f - MapGel.TEX_WIDTH * 0.5f, MAP_TOP, 0.0f)
        }

        require(curLocationGel == null)
        curLocationGel = CurrentLocationGel(this).also { gel ->
            gel.onCreate(LayerKind.UI_BELOW_DLG)
            gel.hide()
        }
    }

    fun moveGelOnMap(gel: GraphicElement, worldCoord: Point3f) {
        val mapGel = mapGel!!
        val relu = worldCoord.y / App.bricks.ysize
        val relv = worldCoord.x / App.bricks.xsize
        val width = MapGel.TEX_WIDTH * mapScaleFactor
        val height = MapGel.TEX_HEIGHT * mapScaleFactor
        val margin = MAP_MARGIN * mapScaleFactor
        val x = mapGel.pos.x + margin + ((width - 2.0f * margin) * relu)
        val y = mapGel.pos.y + margin + ((height - 2.0f * margin) * relv)
        gel.moveTo(x, y, 0.0f)
    }

    override fun removeGels() {
        require(mapGel != null)
        mapGel?.setZombie()
        mapGel = null

        require(curLocationGel != null)
        curLocationGel?.setZombie()
        curLocationGel = null
    }

    override fun show() {
        mapGel?.show()
        curLocationGel?.show()
    }

    override fun hide() {
        mapGel?.hide()
        curLocationGel?.hide()
    }

    override fun animate() {
    }

    companion object {
        private const val MAP_TOP = 100.0f
        private const val MAP_MARGIN = 16
    }
}
