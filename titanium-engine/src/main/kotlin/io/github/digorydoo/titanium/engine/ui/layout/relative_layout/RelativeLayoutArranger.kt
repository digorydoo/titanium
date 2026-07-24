package io.github.digorydoo.titanium.engine.ui.layout.relative_layout

import io.github.digorydoo.titanium.engine.ui.layout.Layout
import io.github.digorydoo.titanium.engine.ui.layout.LayoutArranger

internal class RelativeLayoutArranger(
    delegate: Delegate,
): LayoutArranger<RelativeLayoutParams, RelativeLayout<*>>(delegate) {
    typealias ChildLayout = Layout<RelativeLayoutParams, *>

    private val preArranger = PreArranger()
    private val iterativeArranger = IterativeArranger(delegate)

    override fun arrange(
        layout: RelativeLayout<*>,
        minWidth: Float,
        maxWidth: Float,
        minHeight: Float,
        maxHeight: Float,
    ) {
        val nodeMap = NodeMap.createFromLayout(layout)
        val minMax = preArranger.setInitialPosAndSizes(layout, minWidth, maxWidth, minHeight, maxHeight, nodeMap)

        // iterativeArranger can be inefficient for complex layouts.
        // I could get rid of it by simply using the initial pos and sizes from pre-arranger.
        // All I would lose is the ability of children to extend their sizes.
        // However, if PreArranger would call delegate.arrange once, even this could be handled.
        iterativeArranger.arrange(layout, nodeMap, minMax)
    }
}
