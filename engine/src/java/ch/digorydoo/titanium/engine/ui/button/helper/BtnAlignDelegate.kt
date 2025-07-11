package ch.digorydoo.titanium.engine.ui.button.helper

import ch.digorydoo.titanium.engine.behaviours.Align
import ch.digorydoo.titanium.engine.ui.button.IButtonGel

class BtnAlignDelegate(val btn: IButtonGel, val alignment: Align.Alignment): Align.Delegate() {
    override val anchor = alignment.anchor
    override val xOffset = alignment.xOffset
    override val yOffset get() = btn.scrollOffset.toInt() + alignment.yOffset
    override val marginLeft = alignment.marginLeft
    override val marginTop = alignment.marginTop
    override val marginRight = alignment.marginRight
    override val marginBottom = alignment.marginBottom
    override val width = btn.btnWidth
    override val height = btn.btnHeight

    override fun setPos(x: Int, y: Int) {
        btn.moveTo(x, y, 0)
    }
}
