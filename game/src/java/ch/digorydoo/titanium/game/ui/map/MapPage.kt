package ch.digorydoo.titanium.game.ui.map

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.ui.tab.MenuTabPage
import kotlin.math.PI

class MapPage: MenuTabPage {
    private var mapGel: MapGel? = null
    private var curLocationGel: CurrentLocationGel? = null

    fun makeGels() {
        require(mapGel == null)
        mapGel = MapGel().also {
            App.content.add(it, LayerKind.UI_BELOW_DLG)
            it.hide()
            it.moveTo(App.screenWidthDp / 2 - it.width / 2, 100.0f, 0.0f)
        }

        require(curLocationGel == null)
        curLocationGel = CurrentLocationGel().also { loc ->
            App.content.add(loc, LayerKind.UI_BELOW_DLG)
            loc.hide()
            loc.moveTo(MutablePoint3f().apply { worldCoordToMap(App.player?.pos ?: App.camera.targetPos, this) })
            loc.rotationPhi = (3.0 * PI / 2.0).toFloat() - App.camera.currentPhi // TODO use player orientation
        }
    }

    private fun worldCoordToMap(worldCoord: Point3f, result: MutablePoint3f) {
        val mapGel = mapGel
        require(mapGel != null)
        result.x = mapGel.pos.x + MAP_MARGIN + ((mapGel.width - 2.0f * MAP_MARGIN) * worldCoord.y / App.bricks.ysize)
        result.y = mapGel.pos.y + MAP_MARGIN + ((mapGel.height - 2.0f * MAP_MARGIN) * worldCoord.x / App.bricks.xsize)
        result.z = 0.0f
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
        private const val MAP_MARGIN = 16
    }
}
