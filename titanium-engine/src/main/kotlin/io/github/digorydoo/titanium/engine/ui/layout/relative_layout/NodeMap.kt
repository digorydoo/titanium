package io.github.digorydoo.titanium.engine.ui.layout.relative_layout

import io.github.digorydoo.titanium.engine.ui.layout.relative_layout.RelativeLayoutArranger.ChildLayout

/**
 * This class wraps each child of a RelativeLayout in a Node, and provides access to the root nodes aligned to each of
 * the parent's sides. Nodes which are centred in the parent won't be reachable from those root nodes, and neither will
 * be nodes of disconnected chains.
 *
 * During construction, the distance of each node from both ends of each axis is computed, which will be used by
 * RelativeLayoutArranger to determine the weights when distributing flexible width/height to chains of nodes.
 *
 * Note that axis constraints act independently since above/below constraints tell nothing about horizontal alignment,
 * and leftOf/rightOf constraints tell nothing about vertical alignment. Thus, for one axis, the alignment relationships
 * may be viewed as two trees that may touch at nodes that are reachable from both sides.
 */
internal class NodeMap private constructor(
    private val map: Map<ChildLayout, Node>,
    val alignedToParentLeft: List<Node>,
    val alignedToParentTop: List<Node>,
    val alignedToParentRight: List<Node>,
    val alignedToParentBottom: List<Node>,
) {
    val size get() = map.size
    fun isEmpty() = map.isEmpty()
    operator fun get(child: ChildLayout) = map[child]!!

    companion object {
        fun createFromLayout(layout: RelativeLayout<*>): NodeMap {
            val map = mutableMapOf<ChildLayout, Node>()
            val alignedToParentLeft = mutableListOf<Node>()
            val alignedToParentTop = mutableListOf<Node>()
            val alignedToParentRight = mutableListOf<Node>()
            val alignedToParentBottom = mutableListOf<Node>()

            for (child in layout.children) {
                val params = child.params
                val node = map.getOrPut(child) { Node(child) }

                params.leftOf?.let { otherChild ->
                    val otherNode = map.getOrPut(otherChild) { Node(otherChild) }
                    node.right.add(otherNode) // node is leftOf otherNode, so otherNode is to node's right
                    otherNode.left.add(node)

                }

                params.above?.let { otherChild ->
                    val otherNode = map.getOrPut(otherChild) { Node(otherChild) }
                    node.bottom.add(otherNode) // node is above otherNode, so otherNode is at node's bottom
                    otherNode.top.add(node)
                }

                params.rightOf?.let { otherChild ->
                    val otherNode = map.getOrPut(otherChild) { Node(otherChild) }
                    node.left.add(otherNode) // node is rightOf otherNode, so otherNode is to node's left
                    otherNode.right.add(node)
                }

                params.below?.let { otherChild ->
                    val otherNode = map.getOrPut(otherChild) { Node(otherChild) }
                    node.top.add(otherNode) // node is below otherNode, so otherNode is at node's top
                    otherNode.bottom.add(node)
                }

                node.minWidthTowardsLeftEnd = params.marginLeft
                node.minHeightTowardsTopEnd = params.marginTop
                node.minWidthTowardsRightEnd = params.marginRight
                node.minHeightTowardsBottomEnd = params.marginBottom
            }

            // Count the number of nodes to each node's side, and compute the min width and height on each side

            var stamp = 0 // used to mark nodes as visited and detect cycles

            for (child in layout.children) {
                val params = child.params
                val node = map[child]!!

                if (params.alignParentLeft) {
                    alignedToParentLeft.add(node)
                    stamp++
                    countNodesFromLeftToRight(node, stamp)
                } else if (node.left.isEmpty()) {
                    stamp++
                    countNodesFromLeftToRight(node, stamp)
                }

                if (params.alignParentTop) {
                    alignedToParentTop.add(node)
                    stamp++
                    countNodesFromTopToBottom(node, stamp)
                } else if (node.top.isEmpty()) {
                    stamp++
                    countNodesFromTopToBottom(node, stamp)
                }

                if (params.alignParentRight) {
                    alignedToParentRight.add(node)
                    stamp++
                    countNodesFromRightToLeft(node, stamp)
                } else if (node.right.isEmpty()) {
                    stamp++
                    countNodesFromRightToLeft(node, stamp)
                }

                if (params.alignParentBottom) {
                    alignedToParentBottom.add(node)
                    stamp++
                    countNodesFromBottomToTop(node, stamp)
                } else if (node.bottom.isEmpty()) {
                    stamp++
                    countNodesFromBottomToTop(node, stamp)
                }
            }

            return NodeMap(map, alignedToParentLeft, alignedToParentTop, alignedToParentRight, alignedToParentBottom)
        }

        private fun countNodesFromLeftToRight(root: Node, stamp: Int) {
            var maxDepth = 0
            var width = 0f
            var maxWidth = 0f
            var marginRightOfPrev = 0f
            val getChildren = { node: Node -> node.right }

            @Suppress("AssignedValueIsNeverRead") // linter bug
            root.forEachNodeInTree(
                stamp,
                getChildren,
                onReach = { node, depth ->
                    val saved = Pair(width, marginRightOfPrev)
                    val params = node.params

                    node.numNodesFromLeftEnd = maxOf(node.numNodesFromLeftEnd, depth)
                    maxDepth = maxOf(maxDepth, depth)

                    width += maxOf(marginRightOfPrev, params.marginLeft)
                    node.minWidthTowardsLeftEnd = maxOf(node.minWidthTowardsLeftEnd, width)
                    width += maxOf(node.minContentWidth, params.minWidth)
                    maxWidth = maxOf(maxWidth, width + params.marginRight)
                    marginRightOfPrev = params.marginRight

                    saved // we'll see this again in onAscend
                },
                onAscend = { _, saved ->
                    width = saved.first
                    marginRightOfPrev = saved.second
                }
            )
        }

        private fun countNodesFromRightToLeft(root: Node, stamp: Int) {
            var maxDepth = 0
            var width = 0f
            var maxWidth = 0f
            var marginLeftOfPrev = 0f
            val getChildren = { node: Node -> node.left }

            @Suppress("AssignedValueIsNeverRead") // linter bug
            root.forEachNodeInTree(
                stamp,
                getChildren,
                onReach = { node, depth ->
                    val saved = Pair(width, marginLeftOfPrev)
                    val params = node.params

                    node.numNodesFromRightEnd = maxOf(node.numNodesFromRightEnd, depth)
                    maxDepth = maxOf(maxDepth, depth)

                    width += maxOf(marginLeftOfPrev, params.marginRight)
                    node.minWidthTowardsRightEnd = maxOf(node.minWidthTowardsRightEnd, width)
                    width += maxOf(node.minContentWidth, params.minWidth)
                    maxWidth = maxOf(maxWidth, width + params.marginLeft)
                    marginLeftOfPrev = params.marginLeft

                    saved
                },
                onAscend = { _, saved ->
                    width = saved.first
                    marginLeftOfPrev = saved.second
                }
            )
        }

        private fun countNodesFromTopToBottom(root: Node, stamp: Int) {
            var maxDepth = 0
            var height = 0f
            var maxHeight = 0f
            var marginBottomOfPrev = 0f
            val getChildren = { node: Node -> node.bottom }

            @Suppress("AssignedValueIsNeverRead") // linter bug
            root.forEachNodeInTree(
                stamp,
                getChildren,
                onReach = { node, depth ->
                    val saved = Pair(height, marginBottomOfPrev)
                    val params = node.params

                    node.numNodesFromTopEnd = maxOf(node.numNodesFromTopEnd, depth)
                    maxDepth = maxOf(maxDepth, depth)

                    height += maxOf(marginBottomOfPrev, params.marginTop)
                    node.minHeightTowardsTopEnd = maxOf(node.minHeightTowardsTopEnd, height)
                    height += maxOf(node.minContentHeight, params.minHeight)
                    maxHeight = maxOf(maxHeight, height + params.marginBottom)
                    marginBottomOfPrev = params.marginBottom

                    saved
                },
                onAscend = { _, saved ->
                    height = saved.first
                    marginBottomOfPrev = saved.second
                }
            )
        }

        private fun countNodesFromBottomToTop(root: Node, stamp: Int) {
            var maxDepth = 0
            var height = 0f
            var maxHeight = 0f
            var marginTopOfPrev = 0f
            val getChildren = { node: Node -> node.top }

            @Suppress("AssignedValueIsNeverRead") // linter bug
            root.forEachNodeInTree(
                stamp,
                getChildren,
                onReach = { node, depth ->
                    val saved = Pair(height, marginTopOfPrev)
                    val params = node.params

                    node.numNodesFromBottomEnd = maxOf(node.numNodesFromBottomEnd, depth)
                    maxDepth = maxOf(maxDepth, depth)

                    height += maxOf(marginTopOfPrev, params.marginBottom)
                    node.minHeightTowardsBottomEnd = maxOf(node.minHeightTowardsBottomEnd, height)
                    height += maxOf(node.minContentHeight, params.minHeight)
                    maxHeight = maxOf(maxHeight, height + params.marginTop)
                    marginTopOfPrev = params.marginTop

                    saved
                },
                onAscend = { _, saved ->
                    height = saved.first
                    marginTopOfPrev = saved.second
                }
            )
        }
    }
}
