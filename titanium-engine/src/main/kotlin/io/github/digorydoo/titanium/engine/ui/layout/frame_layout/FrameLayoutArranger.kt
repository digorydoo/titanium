package io.github.digorydoo.titanium.engine.ui.layout.frame_layout

import ch.digorydoo.kutils.math.clamp
import io.github.digorydoo.titanium.engine.ui.layout.LayoutArranger
import kotlin.math.max

internal class FrameLayoutArranger(delegate: Delegate): LayoutArranger<FrameLayoutParams, FrameLayout<*>>(delegate) {
    override fun arrange(layout: FrameLayout<*>, minWidth: Float, maxWidth: Float, minHeight: Float, maxHeight: Float) {
        // A FrameLayout usually has one child at most. If there are more children, they'll overlap.
        // The FrameLayout wraps its content as tightly as possible.
        // If the child ends up smaller than the FrameLayout's min size, it will be centred.

        val xPadding = layout.paddingLeft + layout.paddingRight
        val yPadding = layout.paddingTop + layout.paddingBottom
        val childMaxWidth = (maxWidth - xPadding).coerceAtLeast(0f)
        val childMaxHeight = (maxHeight - yPadding).coerceAtLeast(0f)
        var maxComputedChildWidth = 0f
        var maxComputedChildHeight = 0f

        layout.children.forEach { child ->
            delegate.arrange(
                child,
                0f,
                childMaxWidth,
                0f,
                childMaxHeight,
            )
            val childSize = child.computed.size
            maxComputedChildWidth = max(maxComputedChildWidth, childSize.x)
            maxComputedChildHeight = max(maxComputedChildHeight, childSize.y)
        }

        // The computed size is the layout's outer box covering the padding.
        val layoutSize = layout.computed.size

        layoutSize.set(
            clamp(maxComputedChildWidth + xPadding, minWidth, maxWidth),
            clamp(maxComputedChildHeight + yPadding, minHeight, maxHeight),
        )

        val cx = (layoutSize.x - xPadding).coerceAtLeast(0f) / 2f
        val cy = (layoutSize.y - yPadding).coerceAtLeast(0f) / 2f

        layout.children.forEach { child ->
            val childComputed = child.computed
            val childSize = childComputed.size

            // The child's relativePos is relative to the parent's inner box inside the padding.
            childComputed.relativePos.set(cx - childSize.x / 2f, cy - childSize.y / 2f)
        }
    }
}
