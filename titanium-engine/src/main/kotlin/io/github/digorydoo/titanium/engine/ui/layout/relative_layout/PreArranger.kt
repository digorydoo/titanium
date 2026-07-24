package io.github.digorydoo.titanium.engine.ui.layout.relative_layout

import io.github.digorydoo.titanium.engine.ui.layout.Layout

/**
 * This class computes the minimal size of the layout, sets the children to their minimal size, and places them
 * according to their alignment constraints. This should solve the layout quickly if none of the constraints interact,
 * and it should give a good starting point for RelativeLayoutArranger for layouts whose constraints do interact.
 */
internal class PreArranger {
    class MinMax(val minWidth: Float, val maxWidth: Float, val minHeight: Float, val maxHeight: Float) {
        override fun toString() = "(x in $minWidth .. $maxWidth, y in $minHeight .. $maxHeight)"
    }

    private var minChildLeft = 0f
    private var minChildTop = 0f
    private var maxChildRight = 0f
    private var maxChildBottom = 0f

    /**
     * @return MinMax representing layout minimal and maximal inner size (inside padding)
     */
    fun setInitialPosAndSizes(
        layout: RelativeLayout<*>,
        minWidth: Float,
        maxWidth: Float,
        minHeight: Float,
        maxHeight: Float,
        nodeMap: NodeMap,
    ): MinMax {
        val xPadding = layout.paddingLeft + layout.paddingRight
        val yPadding = layout.paddingTop + layout.paddingBottom

        var layoutInnerMinWidth = (minWidth - xPadding).coerceAtLeast(0f)
        var layoutInnerMinHeight = (minHeight - yPadding).coerceAtLeast(0f)

        val children = layout.children

        if (children.isNotEmpty()) {
            // Set initial child size and position. Children which are completely unaligned won't be touched by
            // alignChildren, so setting an initial position here will give them a defined position.

            minChildLeft = 0f // left of child margin
            minChildTop = 0f
            maxChildRight = 0f
            maxChildBottom = 0f

            for (child in children) {
                val params = child.params
                val (relativePos, size) = child.computed
                relativePos.set(params.marginLeft, params.marginTop)
                size.set(
                    maxOf(params.minWidth, child.minContentWidth),
                    maxOf(params.minHeight, child.minContentHeight),
                )
                maxChildRight = maxOf(maxChildRight, relativePos.x + size.x + params.marginRight)
                maxChildBottom = maxOf(maxChildBottom, relativePos.y + size.y + params.marginBottom)
            }

            // Align children in a layout whose size is (0; 0)

            alignChildren(children, nodeMap, 0f, 0f)

            // Negative coordinates must have come from chains starting at the positive edge (assuming negative margins
            // are disallowed at the border). Thus, moving the positive edge should also move those children towards the
            // origin. The required size is the maximum of the two extremes.

            layoutInnerMinWidth = maxOf(layoutInnerMinWidth, maxChildRight, -minChildLeft)
            layoutInnerMinHeight = maxOf(layoutInnerMinHeight, maxChildBottom, -minChildTop)

            // Adjust child sizes to evenly distribute flexible width and height within this layout size

            for (child in children) {
                val node = nodeMap[child]
                node.computeIdealSize(layoutInnerMinWidth, layoutInnerMinHeight)
                child.computed.size.set(node.idealSize)

                // FIXME
                //  Arrange child, then check if size became larger; if so, update arrangedMinSize.
                //  Updating arrangedMinSize means change needs to propagate through graph in order to update
                //  min*Towards*End values as well.
                //  If any arrangedMinSize was updated, recompute ideal size and arrange children again.
                //  Finally, also update lastArrangedSize.
            }

            // Align children again

            alignChildren(children, nodeMap, layoutInnerMinWidth, layoutInnerMinHeight)
        }

        return MinMax(
            minWidth = layoutInnerMinWidth,
            maxWidth = (maxWidth - xPadding).coerceAtLeast(layoutInnerMinWidth),
            minHeight = layoutInnerMinHeight,
            maxHeight = (maxHeight - yPadding).coerceAtLeast(layoutInnerMinHeight),
        )
    }

    private fun alignChildren(
        children: List<Layout<RelativeLayoutParams, *>>,
        nodeMap: NodeMap,
        layoutInnerWidth: Float,
        layoutInnerHeight: Float,
    ) {
        for (child in children) {
            val params = child.params
            val node = nodeMap[child]
            val (relativePos, size) = child.computed
            var followLeft = false
            var followRight = false

            if (params.alignParentRight) {
                relativePos.x = layoutInnerWidth - params.marginRight - size.x
                followLeft = true
            }

            if (params.alignParentLeft) {
                // Even though we initialized child positions at the origin, the child may have moved due to conflicting
                // constraints, e.g. when the child is also part of a chain starting from the right. Such conflicts are
                // not solved here. We force the child back to the border to get the minimal layout size correct.
                relativePos.x = params.marginLeft
                followRight = true
            }

            if (params.centreHorizontally) {
                relativePos.x = layoutInnerWidth / 2f - size.x / 2f + (params.marginLeft - params.marginRight) / 2f
                followLeft = true
                followRight = true
            }

            if (followLeft) {
                alignChainTowardsLeft(node)
            }

            if (followRight) {
                alignChainTowardsRight(node)
            }

            var followTop = false
            var followBottom = false

            if (params.alignParentBottom) {
                relativePos.y = layoutInnerHeight - params.marginBottom - size.y
                followTop = true
            }

            if (params.alignParentTop) {
                relativePos.y = params.marginTop
                followBottom = true
            }

            if (params.centreVertically) {
                relativePos.y = layoutInnerHeight / 2f - size.y / 2f + (params.marginTop - params.marginBottom) / 2f
                followTop = true
                followBottom = true
            }

            if (followTop) {
                alignChainTowardsTop(node)
            }

            if (followBottom) {
                alignChainTowardsBottom(node)
            }
        }
    }

    private fun alignChainTowardsRight(root: Node) {
        var x = root.computed.relativePos.x
        var marginRightOfPrev = 0f

        // We already know that chains are without cycles (NodeMap ensures this) and may be followed in a tree-like
        // descent starting from the edge. The root node itself is included in the iteration and always reached.
        root.forEachNodeInTree(
            getChildren = { it.right },
            onReach = { n, _ ->
                val saved = Pair(x, marginRightOfPrev)
                val pi = n.params
                val ki = n.computed

                if (n !== root) {
                    x += maxOf(marginRightOfPrev, pi.marginLeft)
                    ki.relativePos.x = x
                }

                x += ki.size.x
                maxChildRight = maxOf(maxChildRight, x + pi.marginRight)
                marginRightOfPrev = pi.marginRight
                saved // we'll see this again in onAscend
            },
            onAscend = { _, saved ->
                @Suppress("AssignedValueIsNeverRead") // linter bug
                x = saved.first
                @Suppress("AssignedValueIsNeverRead") // linter bug
                marginRightOfPrev = saved.second
            }
        )
    }

    private fun alignChainTowardsLeft(root: Node) {
        var x = root.computed.relativePos.x
        var marginLeftOfPrev = 0f

        root.forEachNodeInTree(
            getChildren = { it.left },
            onReach = { n, _ ->
                val saved = Pair(x, marginLeftOfPrev)
                val pi = n.params
                val ki = n.computed

                if (n !== root) {
                    x -= maxOf(marginLeftOfPrev, pi.marginRight) + ki.size.x
                    ki.relativePos.x = x
                }

                minChildLeft = minOf(minChildLeft, x - pi.marginLeft)
                marginLeftOfPrev = pi.marginLeft
                saved
            },
            onAscend = { _, saved ->
                @Suppress("AssignedValueIsNeverRead") // linter bug
                x = saved.first
                @Suppress("AssignedValueIsNeverRead") // linter bug
                marginLeftOfPrev = saved.second
            }
        )
    }

    private fun alignChainTowardsBottom(root: Node) {
        var y = root.computed.relativePos.y
        var marginBottomOfPrev = 0f

        root.forEachNodeInTree(
            getChildren = { it.bottom },
            onReach = { n, _ ->
                val saved = Pair(y, marginBottomOfPrev)
                val pi = n.params
                val ki = n.computed

                if (n !== root) {
                    y += maxOf(marginBottomOfPrev, pi.marginTop)
                    ki.relativePos.y = y
                }

                y += ki.size.y
                maxChildBottom = maxOf(maxChildBottom, y + pi.marginBottom)
                marginBottomOfPrev = pi.marginBottom
                saved
            },
            onAscend = { _, saved ->
                @Suppress("AssignedValueIsNeverRead") // linter bug
                y = saved.first
                @Suppress("AssignedValueIsNeverRead") // linter bug
                marginBottomOfPrev = saved.second
            }
        )
    }

    private fun alignChainTowardsTop(root: Node) {
        var y = root.computed.relativePos.y
        var marginTopOfPrev = 0f

        root.forEachNodeInTree(
            getChildren = { it.top },
            onReach = { n, _ ->
                val saved = Pair(y, marginTopOfPrev)
                val pi = n.params
                val ki = n.computed

                if (n !== root) {
                    y -= maxOf(marginTopOfPrev, pi.marginBottom) + ki.size.y
                    ki.relativePos.y = y
                }

                minChildTop = minOf(minChildTop, y - pi.marginTop)
                marginTopOfPrev = pi.marginTop
                saved
            },
            onAscend = { _, saved ->
                @Suppress("AssignedValueIsNeverRead") // linter bug
                y = saved.first
                @Suppress("AssignedValueIsNeverRead") // linter bug
                marginTopOfPrev = saved.second
            }
        )
    }
}
