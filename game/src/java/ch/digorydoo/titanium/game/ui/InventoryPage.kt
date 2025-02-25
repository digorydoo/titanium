package ch.digorydoo.titanium.game.ui

import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.gel.TextGel
import ch.digorydoo.titanium.engine.ui.tab.MenuTabPage

class InventoryPage: MenuTabPage {
    private var message: TextGel? = null

    fun makeGels() {
        require(message == null)
        message = TextGel(
            "The InventoryPage",
            alignment = Align.Alignment(marginLeft = 100, marginTop = 120)
        ).also {
            App.content.add(it, LayerKind.UI_BELOW_DLG)
            it.hide()
        }
    }

    override fun removeGels() {
        require(message != null)
        message?.setZombie()
        message = null
    }

    override fun show() {
        message?.show()
    }

    override fun hide() {
        message?.hide()
    }

    override fun animate() {
    }
}
