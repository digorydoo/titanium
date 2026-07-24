package io.github.digorydoo.titanium.engine.ui.layout.relative_layout

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.vector.MutableVector2f
import io.github.digorydoo.titanium.engine.ui.layout.relative_layout.RelativeLayoutArranger.ChildLayout
import kotlin.math.floor

internal class Node(val child: ChildLayout /* FIXME make private */) {
    class UnidirectionalCycleException(stamp: Int, name: String):
        Exception("RelativeLayout contains unidirectional cycle, detected at child '$name', stamp=$stamp")

    val params = child.params // keep child private, because the term `child` is confusing in the context of Node
    val computed = child.computed
    val minContentWidth get() = child.minContentWidth
    val minContentHeight get() = child.minContentHeight

    val left = mutableSetOf<Node>() // nodes directly linked to the left of this node
    val top = mutableSetOf<Node>()
    val right = mutableSetOf<Node>()
    val bottom = mutableSetOf<Node>()

    // FIXME are those still needed?
    val isLeftConnected get() = params.alignParentLeft || left.isNotEmpty()
    val isTopConnected get() = params.alignParentTop || top.isNotEmpty()
    val isRightConnected get() = params.alignParentRight || right.isNotEmpty()
    val isBottomConnected get() = params.alignParentBottom || bottom.isNotEmpty()

    // FIXME are those still needed?
    var numNodesFromLeftEnd = 0 // left end does not necessarily mean parent left
    var numNodesFromTopEnd = 0
    var numNodesFromRightEnd = 0
    var numNodesFromBottomEnd = 0

    var minWidthTowardsLeftEnd = 0f // includes the child's own marginLeft
    var minHeightTowardsTopEnd = 0f
    var minWidthTowardsRightEnd = 0f
    var minHeightTowardsBottomEnd = 0f

    var stamp = 0 // used during tree traversal

    val idealSize = MutableVector2f()
    val lastArrangedSize = MutableVector2f(Float.NaN, Float.NaN)
    val arrangedMinSize = MutableVector2f()

    fun <Saved> forEachNodeInTree(
        getChildren: (Node) -> Collection<Node>,
        depthOfRoot: Int = 0,
        onReach: (Node, Int) -> Saved,
        onAscend: (Node, Saved) -> Unit,
    ) {
        val saved = onReach(this, depthOfRoot)

        for (child in getChildren(this)) {
            child.forEachNodeInTree(getChildren, depthOfRoot + 1, onReach, onAscend)
        }

        onAscend(this, saved)
    }

    fun <Saved> forEachNodeInTree(
        stamp: Int,
        getChildren: (Node) -> Collection<Node>,
        depthOfRoot: Int = 0,
        onReach: (Node, Int) -> Saved,
        onAscend: (Node, Saved) -> Unit,
    ) {
        if (this.stamp == stamp) throw UnidirectionalCycleException(stamp, child.name)
        this.stamp = stamp

        val saved = onReach(this, depthOfRoot)

        for (child in getChildren(this)) {
            child.forEachNodeInTree(stamp, getChildren, depthOfRoot + 1, onReach, onAscend)
        }

        onAscend(this, saved)
    }

    fun computeIdealSize(layoutInnerWidth: Float, layoutInnerHeight: Float) {
        val minWidth = maxOf(arrangedMinSize.x, params.minWidth, child.minContentWidth)
        val minHeight = maxOf(arrangedMinSize.y, params.minHeight, child.minContentHeight)

        // FIXME The min*Towards*End values currently can become incorrect, because they're not adjusted when
        //  arrangedMinSize is updated. This means distribution of sizes may be incorrect when a child's inner
        //  constraints forces it to occupy more space then minContent size.

        // Compute how much flexible width and height the layout has for this chain of nodes.
        // Note that minWidthTowardsLeftEnd already has our params.marginLeft incorporated, same goes for other margins.
        val flexibleWidth = layoutInnerWidth - (minWidthTowardsLeftEnd + minWidth + minWidthTowardsRightEnd)
        val flexibleHeight = layoutInnerHeight - (minHeightTowardsTopEnd + minHeight + minHeightTowardsBottomEnd)

        // Evenly distribute flexible width and height among the nodes of the chain
        val idealWidth = floor(minWidth + flexibleWidth / (1 + numNodesFromLeftEnd + numNodesFromRightEnd))
        val idealHeight = floor(minHeight + flexibleHeight / (1 + numNodesFromTopEnd + numNodesFromBottomEnd))

        idealSize.x = clamp(idealWidth, minWidth, params.maxWidth)
        idealSize.y = clamp(idealHeight, minHeight, params.maxHeight)
    }
}
