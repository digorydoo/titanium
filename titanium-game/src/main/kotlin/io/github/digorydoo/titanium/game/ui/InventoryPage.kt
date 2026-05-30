package io.github.digorydoo.titanium.game.ui

import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import io.github.digorydoo.titanium.engine.gel.TextGel
import io.github.digorydoo.titanium.engine.ui.tab.MenuTabPage

class InventoryPage: MenuTabPage {
    private var message: TextGel? = null

    fun makeGels() {
        require(message == null)
        message = TextGel(
            "The InventoryPage",
            alignment = Align.Alignment(marginLeft = 100, marginTop = 120)
        ).also {
            it.onCreate(LayerKind.UI_BELOW_DLG)
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
