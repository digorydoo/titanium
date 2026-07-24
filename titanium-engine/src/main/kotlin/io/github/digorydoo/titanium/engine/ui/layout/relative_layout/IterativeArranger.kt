package io.github.digorydoo.titanium.engine.ui.layout.relative_layout

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.vector.MutableVector2f
import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.ui.layout.LayoutArranger.Delegate
import io.github.digorydoo.titanium.engine.ui.layout.relative_layout.PreArranger.MinMax
import io.github.digorydoo.titanium.engine.ui.layout.relative_layout.RelativeLayoutArranger.ChildLayout
import kotlin.math.round

internal class IterativeArranger(private val delegate: Delegate) {
    private class EdgeDelta {
        var left = 0f
        var top = 0f
        var right = 0f
        var bottom = 0f

        override fun toString() = "($left, $top, $right, $bottom)"
    }

    fun arrange(layout: RelativeLayout<*>, nodeMap: NodeMap, layoutInnerMinMax: MinMax) {
        val layoutInnerSize = MutableVector2f(layoutInnerMinMax.minWidth, layoutInnerMinMax.minHeight)
        var iteration = 0
        var deepArrangeCount = 0
        val delta = EdgeDelta()
        var step = 1f
        var lastSumOfSqrErrors = Float.POSITIVE_INFINITY

        do {
            var sumOfSqrErrors = 0f
            var done = false

            log("iteration #$iteration")
            log("   layoutInnerSize=$layoutInnerSize")

            layout.children.forEachIndexed { idx, child ->
                val (relativePos, size) = child.computed
                val node = nodeMap[child]
                log("   child #$idx relPos=$relativePos, size=$size")

                // Compute the resulting force from all the constraints pulling at the child

                delta.apply {
                    left = 0f
                    top = 0f
                    right = 0f
                    bottom = 0f
                }

                applyConstraints(delta, child, layoutInnerSize, node)
                log("      delta from constraints $delta")

                // The goal is to reduce delta to zero

                val sqrError = (delta.left * delta.left) +
                    (delta.top * delta.top) +
                    (delta.right * delta.right) +
                    (delta.bottom * delta.bottom)

                sumOfSqrErrors += sqrError

                // Update position and size

                delta.left *= step
                delta.top *= step
                delta.right *= step
                delta.bottom *= step
                log("      final delta $delta")

                relativePos.x += delta.left
                relativePos.y += delta.top
                size.x = (size.x + delta.right - delta.left).coerceAtLeast(0f)
                size.y = (size.y + delta.bottom - delta.top).coerceAtLeast(0f)
                log("      new relPos=$relativePos, size=$size")
            }

            log("   sumOfSqrErrors=$sumOfSqrErrors")

            if (sumOfSqrErrors <= MAX_SQR_ERROR || iteration >= MAX_NUM_ITERATIONS - 1) {
                // Constraints seem settled, try to arrange children
                done = arrangeChildren(layout, nodeMap)

                if (++deepArrangeCount > MAX_DEEP_ARRANGE_COUNT) {
                    log("      max number of deep arrangements reached!")
                    break
                }
            }

            if (done) break

            if (sumOfSqrErrors > lastSumOfSqrErrors) {
                step *= STEP_DECREASE // arrangement might be oscillating due to children's own inner constraints
                log("   step decreased to $step")
            } else if (step < 1f) {
                step = minOf(1f, step + STEP_INCREASE)
                log("   step increase to $step")
            }

            lastSumOfSqrErrors = sumOfSqrErrors
            updateContentBox(layout, layoutInnerMinMax, layoutInnerSize, final = false)
        } while (++iteration < MAX_NUM_ITERATIONS)

        // Set the layout's final outer size

        updateContentBox(layout, layoutInnerMinMax, layoutInnerSize, final = true)
        layout.computed.size.set(
            layoutInnerSize.x + layout.paddingLeft + layout.paddingRight,
            layoutInnerSize.y + layout.paddingTop + layout.paddingBottom,
        )
    }

    private fun applyConstraints(delta: EdgeDelta, child: ChildLayout, layoutInnerSize: Vector2f, node: Node) {
        val (relativePos, size) = child.computed
        val arrangedMinSize = node.arrangedMinSize
        log("      computeResultingForce arrangedMinSize=$arrangedMinSize")

        val childPosX = relativePos.x
        val childPosY = relativePos.y
        val childSizeX = size.x
        val childSizeY = size.y

        val params = child.params
        val marginLeft = params.marginLeft
        val marginTop = params.marginTop
        val marginRight = params.marginRight
        val marginBottom = params.marginBottom

        val childMinWidth = maxOf(arrangedMinSize.x, params.minWidth, child.minContentWidth)
        val childMinHeight = maxOf(arrangedMinSize.y, params.minHeight, child.minContentHeight)

        // We suppress negative margins at the border, otherwise OVERFLOW_CORRECTION would keep increasing the box.

        val marginLeftNotNeg = marginLeft.coerceAtLeast(0f)
        val marginTopNotNeg = marginTop.coerceAtLeast(0f)
        val marginRightNotNeg = marginRight.coerceAtLeast(0f)
        val marginBottomNotNeg = marginBottom.coerceAtLeast(0f)

        // Child's left border

        var dx: Float

        if (params.alignParentLeft) {
            dx = marginLeftNotNeg - childPosX
            log("      align parent left dx=$dx")
        } else if (childPosX - marginLeftNotNeg < 0f) {
            dx = (marginLeftNotNeg - childPosX) * OVERFLOW_FORCE
            log("      left border overflow dx=$dx")
        } else {
            dx = 0f
            params.rightOf?.let { other ->
                val otherComputed = other.computed
                val otherPos = otherComputed.relativePos
                val otherSize = otherComputed.size
                val mergedMargin = maxOf(other.params.marginRight, marginLeft)
                dx = otherPos.x + otherSize.x + mergedMargin - childPosX
                log("      rightOf dx=$dx")
            }
        }

        delta.left += dx

        if (!node.isRightConnected) {
            delta.right += dx
        }

        // Child's top border

        var dy: Float

        if (params.alignParentTop) {
            dy = marginTopNotNeg - childPosY
            log("      align parent top dy=$dy")
        } else if (childPosY - marginTopNotNeg < 0f) {
            dy = (marginTopNotNeg - childPosY) * OVERFLOW_FORCE
            log("      top border overflow dy=$dy")
        } else {
            dy = 0f
            params.below?.let { other ->
                val otherComputed = other.computed
                val otherPos = otherComputed.relativePos
                val otherSize = otherComputed.size
                val mergedMargin = maxOf(other.params.marginBottom, marginTop)
                dy = otherPos.y + otherSize.y + mergedMargin - childPosY
                log("      below dy=$dy")
            }
        }

        delta.top += dy

        if (!node.isBottomConnected) {
            delta.bottom += dy
        }

        // Child's right border

        if (params.alignParentRight) {
            dx = layoutInnerSize.x - marginRightNotNeg - (childPosX + childSizeX)
            log("      align parent right dx=$dx")
        } else if (childPosX + childSizeX + marginRightNotNeg > layoutInnerSize.x) {
            dx = (layoutInnerSize.x - marginRightNotNeg - (childPosX + childSizeX)) * OVERFLOW_FORCE
            log("      right border overflow dx=$dx")
        } else {
            dx = 0f
            params.leftOf?.let { other ->
                val otherPos = other.computed.relativePos
                val mergedMargin = maxOf(other.params.marginLeft, marginRight)
                dx = otherPos.x - mergedMargin - (childPosX + childSizeX)
                log("      leftOf dx=$dx")
            }
        }

        delta.right += dx

        if (!node.isLeftConnected) {
            delta.left += dx
        }

        // Child's bottom border

        if (params.alignParentBottom) {
            dy = layoutInnerSize.y - marginBottomNotNeg - (childPosY + childSizeY)
            log("      align parent bottom dy=$dy")
        } else if (childPosY + childSizeY + marginBottomNotNeg > layoutInnerSize.y) {
            dy = (layoutInnerSize.y - marginBottomNotNeg - (childPosY + childSizeY)) * OVERFLOW_FORCE
            log("      bottom border overflow dy=$dy")
        } else {
            dy = 0f
            params.above?.let { other ->
                val otherPos = other.computed.relativePos
                val mergedMargin = maxOf(other.params.marginTop, marginBottom)
                dy = otherPos.y - mergedMargin - (childPosY + childSizeY)
                log("      above dy=$dy")
            }
        }

        delta.bottom += dy

        if (!node.isTopConnected) {
            delta.top += dy
        }

        // Centring

        if (params.centreHorizontally) {
            dx = layoutInnerSize.x / 2f - (childPosX + childSizeX / 2f) + marginLeftNotNeg - marginRightNotNeg
            delta.left += dx
            delta.right += dx
        }

        if (params.centreVertically) {
            dy = layoutInnerSize.y / 2f - (childPosY + childSizeY / 2f) + marginTopNotNeg - marginBottomNotNeg
            delta.top += dy
            delta.bottom += dy
        }

        // Keep child size within constraints

        dx = (clamp(childSizeX, childMinWidth, params.maxWidth) - childSizeX) / 2f
        delta.left -= dx
        delta.right += dx

        dy = (clamp(childSizeY, childMinHeight, params.maxHeight) - childSizeY) / 2f
        delta.top -= dy
        delta.bottom += dy

        log("      size constraint correction ($dx, $dy)")

        // Push towards ideal size to evenly distribute flexible size

        node.computeIdealSize(layoutInnerSize.x, layoutInnerSize.y)
        val idealSize = node.idealSize
        log("      ideal size $idealSize")

        dx = DISTRIBUTE_SIZES_FORCE * (idealSize.x - childSizeX) / 2f
        delta.left -= dx
        delta.right += dx

        dy = DISTRIBUTE_SIZES_FORCE * (idealSize.y - childSizeY) / 2f
        delta.top -= dy
        delta.bottom += dy

        log("      ideal size correction ($dx, $dy)")
    }

    private fun arrangeChildren(layout: RelativeLayout<*>, nodeMap: NodeMap): Boolean {
        var done = true

        log("   ARRANGE")

        layout.children.forEachIndexed { idx, child ->
            val params = child.params
            val (relativePos, size) = child.computed
            val origPosX = relativePos.x
            val origPosY = relativePos.y

            // Snap to integer position and sizes

            relativePos.x = round(relativePos.x)
            relativePos.y = round(relativePos.y)
            size.x = round(size.x)
            size.y = round(size.y)
            log("      child #$idx snapped to relPos=$relativePos, size=$size")

            // Check if the overall layout is affected by the right/bottom edge changing too much

            val desiredWidth = size.x
            val desiredHeight = size.y

            if (origPosX + size.x - (relativePos.x + desiredWidth) !in -0.49f .. 0.49f) done = false
            if (origPosY + size.y - (relativePos.y + desiredHeight) !in -0.49f .. 0.49f) done = false

            // Check if we need to arrange the child

            val node = nodeMap[child]
            val lastArrangedSize = node.lastArrangedSize
            log("      lastArrangedSize=$lastArrangedSize, desired=($desiredWidth, $desiredHeight)")

            if (lastArrangedSize.x != desiredWidth || lastArrangedSize.y != desiredHeight) {
                log("         arrange needed")
                delegate.arrange(child, desiredWidth, desiredWidth, desiredHeight, desiredHeight)

                // Check if the child ended up with a different size due to its own internal constraints

                lastArrangedSize.set(size)
                val effectiveWidth = size.x
                val effectiveHeight = size.y

                if (effectiveWidth != desiredWidth || effectiveHeight != desiredHeight) {
                    done = false

                    log("         width desired=$desiredWidth, effective=$effectiveWidth")
                    log("         height desired=$desiredHeight, effective=$effectiveHeight")

                    if (effectiveWidth > desiredWidth) {
                        // The child wants a width that's larger than desiredWidth, therefore we can assume that
                        // effectiveWidth is the minimum width that satisfies the child's inner constraints. This is
                        // not always true, e.g. a FlexWrapLayout's inner minimum width is a function of the available
                        // height, but it should be a good enough heuristic.
                        node.arrangedMinSize.x = maxOf(node.arrangedMinSize.x, effectiveWidth)
                    }

                    if (effectiveHeight > desiredHeight) {
                        node.arrangedMinSize.y = maxOf(node.arrangedMinSize.y, effectiveHeight)
                    }

                    relativePos.x = round(relativePos.x - (effectiveWidth - desiredWidth) / 2f)
                        .coerceAtLeast(params.marginLeft)
                    relativePos.y = round(relativePos.y - (effectiveHeight - desiredHeight) / 2f)
                        .coerceAtLeast(params.marginTop)
                } else {
                    log("         effective size == desired size")
                }
            } else {
                log("         arrange not needed")
            }
        }

        return done
    }

    private fun updateContentBox(
        layout: RelativeLayout<*>,
        layoutInnerMinMax: MinMax,
        layoutInnerSize: MutableVector2f,
        final: Boolean,
    ) {
        // Compute the area currently occupied by children

        var minChildLeft = Float.POSITIVE_INFINITY
        var minChildTop = Float.POSITIVE_INFINITY
        var maxChildRight = Float.NEGATIVE_INFINITY
        var maxChildBottom = Float.NEGATIVE_INFINITY

        layout.children.forEach { child ->
            val params = child.params
            val (relativePos, size) = child.computed

            minChildLeft = minOf(minChildLeft, relativePos.x - params.marginLeft)
            minChildTop = minOf(minChildTop, relativePos.y - params.marginTop)
            maxChildRight = maxOf(maxChildRight, relativePos.x + size.x + params.marginRight)
            maxChildBottom = maxOf(maxChildBottom, relativePos.y + size.y + params.marginBottom)
        }

        log("   enclosing box ($minChildLeft, $minChildTop, $maxChildRight, $maxChildBottom) final=$final")

        // Move the entire layout towards positive positions (assumes negative margins at border are disallowed)

        if (minChildLeft < 0f || minChildTop < 0f) {
            val dx = maxOf(-minChildLeft, 0f)
            val dy = maxOf(-minChildTop, 0f)

            layout.children.forEach { child ->
                child.computed.relativePos.add(dx, dy)
            }

            minChildLeft += dx
            minChildTop += dy
            maxChildRight += dx
            maxChildBottom += dy
        }

        // Scale the content by a small amount in order to apply a layout tightening force

        if (!final && layout.children.isNotEmpty()) {
            val scaleX = (maxChildRight - TIGHTEN_LAYOUT) / maxChildRight
            val scaleY = (maxChildBottom - TIGHTEN_LAYOUT) / maxChildBottom

            layout.children.forEach { child ->
                val (relativePos, size) = child.computed
                relativePos.x *= scaleX
                relativePos.y *= scaleY
                size.x *= scaleX
                size.y *= scaleY
            }

            maxChildRight -= TIGHTEN_LAYOUT
            maxChildBottom -= TIGHTEN_LAYOUT
        }

        // Adjust the context box

        val w = maxChildRight - minChildLeft
        val h = maxChildBottom - minChildTop

        layoutInnerSize.x = clamp(
            round(
                when {
                    final || w < layoutInnerSize.x -> w
                    else -> lerp(layoutInnerSize.x, w, EXPAND_LAYOUT)
                }
            ),
            layoutInnerMinMax.minWidth,
            layoutInnerMinMax.maxWidth
        )
        layoutInnerSize.y = clamp(
            round(
                when {
                    final || h < layoutInnerSize.y -> h
                    else -> lerp(layoutInnerSize.y, h, EXPAND_LAYOUT)
                }
            ),
            layoutInnerMinMax.minHeight,
            layoutInnerMinMax.maxHeight
        )
    }

    // FIXME remove this once the algorithm works
    private fun log(msg: String) {
        println(msg)
    }

    companion object {
        private const val MAX_NUM_ITERATIONS = 30
        private const val MAX_DEEP_ARRANGE_COUNT = 10
        private const val MAX_SQR_ERROR = 0.49f
        private const val STEP_DECREASE = 0.95f // helps settle oscillating re-arrangements
        private const val STEP_INCREASE = 0.02f // slowly recover after step decrease
        private const val OVERFLOW_FORCE = 0.5f // push children inside current content box
        private const val DISTRIBUTE_SIZES_FORCE = 0.1f // push children towards their ideal size
        private const val EXPAND_LAYOUT = 0.01f // expand layout size if children overlap edge
        private const val TIGHTEN_LAYOUT = 0.1f // dp to shrink layout size every iteration
    }
}
