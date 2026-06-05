package io.github.digorydoo.titanium.engine.ui.dlg_item

import io.github.digorydoo.titanium.engine.gel.GelLayer
import io.github.digorydoo.titanium.engine.ui.dialogue.DlgItemDef

sealed interface DlgItemGel {
    val def: DlgItemDef
    val btnWidth: Int
    val btnHeight: Int
    var hilited: Boolean
    val autoDismiss: Boolean
    val height: Int
    var scrollOffset: Float

    fun onCreate(layer: GelLayer.LayerKind?)
    fun setZombie()
    fun moveTo(x: Int, y: Int, z: Int)
    fun show()
    fun hide()
    fun fadeOut()

    companion object {
        const val SELECT_DELAY_SECONDS = 0.3f
        const val FADE_DELAY_SECONDS = 0.3f
    }
}

interface CanAnimateSelectAndThen {
    fun animateSelectAndThen(callback: () -> Unit)
}

interface CanToggle {
    fun toggle()
}

interface CanIncrementDecrement {
    fun increment(smallStep: Boolean)
    fun decrement(smallStep: Boolean)
}
