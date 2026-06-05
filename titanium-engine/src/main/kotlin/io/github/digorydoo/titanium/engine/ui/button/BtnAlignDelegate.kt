package io.github.digorydoo.titanium.engine.ui.button

import io.github.digorydoo.titanium.engine.behaviours.Align
import io.github.digorydoo.titanium.engine.ui.dlg_item.DlgItemGel

class BtnAlignDelegate(val btn: DlgItemGel, val alignment: Align.Alignment): Align.Delegate() {
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
