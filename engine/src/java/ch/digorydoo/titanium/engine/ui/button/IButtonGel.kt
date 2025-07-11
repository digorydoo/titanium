package ch.digorydoo.titanium.engine.ui.button

import ch.digorydoo.titanium.engine.gel.GelLayer

interface IButtonGel {
    val btnWidth: Int
    val btnHeight: Int
    var hilited: Boolean
    val canSelect: Boolean
    val height: Int
    var scrollOffset: Float

    fun onCreate(layer: GelLayer.LayerKind?)
    fun setZombie()
    fun moveTo(x: Int, y: Int, z: Int)
    fun show()
    fun hide()
    fun fadeOut()

    fun select(onBeforeAction: () -> Unit = {}) {}

    fun increment(smallStep: Boolean) {}
    fun decrement(smallStep: Boolean) {}

    companion object {
        const val SELECT_DELAY_SECONDS = 0.3f
        const val FADE_DELAY_SECONDS = 0.3f
    }
}
