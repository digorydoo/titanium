package io.github.digorydoo.titanium.engine.ui.layout

import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.ui.layout.frame_layout.FrameLayout
import io.github.digorydoo.titanium.engine.ui.layout.frame_layout.FrameLayoutArranger
import io.github.digorydoo.titanium.engine.ui.layout.linear_layout.HorizontalLinearLayoutArranger
import io.github.digorydoo.titanium.engine.ui.layout.linear_layout.LinearLayout
import io.github.digorydoo.titanium.engine.ui.layout.linear_layout.LinearLayout.Orientation
import io.github.digorydoo.titanium.engine.ui.layout.linear_layout.VerticalLinearLayoutArranger
import io.github.digorydoo.titanium.engine.ui.layout.relative_layout.RelativeLayout
import io.github.digorydoo.titanium.engine.ui.layout.relative_layout.RelativeLayoutArranger

internal class LayoutArrangerHolder: LayoutArranger.Delegate {
    private val frameLayoutArranger = FrameLayoutArranger(this)
    private val horizontalLinearLayoutArranger = HorizontalLinearLayoutArranger(this)
    private val verticalLinearLayoutArranger = VerticalLinearLayoutArranger(this)
    private val relativeLayoutArranger = RelativeLayoutArranger(this)
    private var arranging = false

    fun arrange(layout: LayoutManager.RootLayout) {
        require(!arranging) { "Already arranging some layout" }

        try {
            arranging = true // just a sanity check to ensure that deeper levels never call this function
            val resolutionMgr = App.resolutionMgr
            val screenWidth = resolutionMgr.screenSizeDp.x.toFloat()
            val screenHeight = resolutionMgr.screenSizeDp.y.toFloat()
            arrange(layout, 0f, screenWidth, 0f, screenHeight)
        } finally {
            arranging = false
        }

        notifyLayoutUpdated(layout, 0f, 0f)
    }

    override fun arrange(layout: Layout<*, *>, minWidth: Float, maxWidth: Float, minHeight: Float, maxHeight: Float) {
        when (layout) {
            is FrameLayout<*> -> frameLayoutArranger.arrange(layout, minWidth, maxWidth, minHeight, maxHeight)
            is LinearLayout<*> -> when (layout.orientation) {
                Orientation.HORIZONTAL -> horizontalLinearLayoutArranger.arrange(
                    layout,
                    minWidth,
                    maxWidth,
                    minHeight,
                    maxHeight
                )
                Orientation.VERTICAL -> verticalLinearLayoutArranger.arrange(
                    layout,
                    minWidth,
                    maxWidth,
                    minHeight,
                    maxHeight
                )
            }
            is RelativeLayout<*> -> relativeLayoutArranger.arrange(layout, minWidth, maxWidth, minHeight, maxHeight)
            else -> throw Exception("Arranger not implemented for layout: $layout")
        }
    }

    private fun notifyLayoutUpdated(layout: Layout<*, *>, parentInnerLeft: Float, parentInnerTop: Float) {
        val computed = layout.computed
        val relativePos = computed.relativePos
        val size = computed.size
        var left = parentInnerLeft + relativePos.x
        var top = parentInnerTop + relativePos.y
        var right = left + size.x
        var bottom = top + size.y

        layout.background?.onLayoutUpdated(left, top, right, bottom)

        left += layout.paddingLeft
        top += layout.paddingTop
        right -= layout.paddingRight
        bottom -= layout.paddingBottom

        if (left > right) {
            left = (left + right) / 2f
            right = left
        }

        if (top > bottom) {
            top = (top + bottom) / 2f
            bottom = top
        }

        layout.content?.onLayoutUpdated(left, top, right, bottom)

        layout.children.forEach { child ->
            notifyLayoutUpdated(child, left, top)
        }
    }
}
