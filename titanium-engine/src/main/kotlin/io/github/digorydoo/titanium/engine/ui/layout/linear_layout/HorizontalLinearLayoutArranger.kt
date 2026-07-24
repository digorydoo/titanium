package io.github.digorydoo.titanium.engine.ui.layout.linear_layout

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.sumOfFloat
import ch.digorydoo.kutils.math.sumOfFloatStartingFrom
import ch.digorydoo.kutils.math.sumOfInt
import io.github.digorydoo.titanium.engine.ui.layout.LayoutArranger

internal class HorizontalLinearLayoutArranger(
    delegate: Delegate,
): LayoutArranger<LinearLayoutParams, LinearLayout<*>>(delegate) {
    private class Estimate {
        var width = 0f
        var pinned = false
    }

    override fun arrange(
        layout: LinearLayout<*>,
        minWidth: Float,
        maxWidth: Float,
        minHeight: Float,
        maxHeight: Float,
    ) {
        val align = layout.align
        val justify = layout.justify

        val xPadding = layout.paddingLeft + layout.paddingRight
        val yPadding = layout.paddingTop + layout.paddingBottom

        var x = 0f // child relativePos are relative to content box, i.e. inside parent's padding
        var maxChildHeight = 0f
        var marginRightOfPrevChild = 0f
        val lastChild = layout.children.lastOrNull()
        val availableHeight = (maxHeight - yPadding).coerceAtLeast(0f)

        val availableWidth = when (justify) {
            LinearLayout.Justify.WRAP_CONTENT -> 0f // no width to distribute; make everything as tight as possible
            LinearLayout.Justify.MATCH_PARENT -> (maxWidth - xPadding).coerceAtLeast(0f)
        }

        val estimates = getInitialEstimates(layout, availableWidth) // based on weights and constraints
        var iteration = 0

        do {
            iteration++
            var repeat = false
            var remainingWidth = availableWidth
            var anyNonPinnedSoFar = false

            for ((idx, child) in layout.children.withIndex()) {
                val params: LinearLayoutParams = child.params
                val estimate = estimates[idx]
                anyNonPinnedSoFar = anyNonPinnedSoFar || !estimate.pinned

                val actualMarginLeft = maxOf(marginRightOfPrevChild, params.marginLeft) // merging margins

                val estimatedWidth = when {
                    child === lastChild -> remainingWidth - actualMarginLeft - params.marginRight
                    estimate.width <= 0f -> 0f
                    else -> (remainingWidth * estimate.width /
                        estimates.sumOfFloatStartingFrom(idx) { it.width }) - actualMarginLeft
                }

                val desiredWidth = clamp(estimatedWidth, params.minWidth, params.maxWidth)

                val innerMinHeight = when (align) {
                    LinearLayout.Align.STRETCH -> clamp(
                        availableHeight - params.marginTop - params.marginBottom,
                        params.minHeight,
                        params.maxHeight,
                    )
                    else -> params.minHeight
                }

                delegate.arrange(child, desiredWidth, desiredWidth, innerMinHeight, params.maxHeight)

                val childComputed = child.computed
                val childSize = childComputed.size
                val finalChildWidth = clamp(childSize.x, params.minWidth, params.maxWidth)
                val error = desiredWidth - finalChildWidth

                if (
                    iteration < MAX_ITERATIONS_FOR_ARRANGE &&
                    remainingWidth > 0f &&
                    anyNonPinnedSoFar &&
                    !estimate.pinned &&
                    error * error > SQR_ERROR_TOLERANCE
                ) {
                    estimate.width = actualMarginLeft + finalChildWidth
                    estimate.pinned = true
                    distributeAmongNonPinned(error, estimates, layout)
                    applyConstraints(estimates, layout)
                    x = 0f
                    maxChildHeight = 0f
                    marginRightOfPrevChild = 0f
                    repeat = true
                    break
                }

                childComputed.relativePos.x = x + actualMarginLeft // y will be decided later
                maxChildHeight = maxOf(maxChildHeight, childSize.y)

                val delta = actualMarginLeft + finalChildWidth
                x += delta // childMarginRight not added yet, may need merging
                remainingWidth = (remainingWidth - delta).coerceAtLeast(0f)

                marginRightOfPrevChild = params.marginRight
            }
        } while (repeat)

        // Compute our own size

        val layoutSize = layout.computed.size

        layoutSize.set(
            x = when (justify) {
                LinearLayout.Justify.MATCH_PARENT -> maxWidth
                LinearLayout.Justify.WRAP_CONTENT -> clamp(
                    layout.paddingLeft + x + marginRightOfPrevChild + layout.paddingRight,
                    minWidth,
                    maxWidth
                )
            },
            y = when (align) {
                LinearLayout.Align.STRETCH -> maxHeight
                else -> clamp(
                    yPadding + maxChildHeight +
                        (layout.children.maxOfOrNull { it.params.marginTop } ?: 0f) +
                        (layout.children.maxOfOrNull { it.params.marginBottom } ?: 0f),
                    minHeight,
                    maxHeight
                )
            }
        )

        // Determine the children's y coordinates

        layout.children.forEach { child ->
            val params = child.params
            child.computed.relativePos.y = when (align) {
                LinearLayout.Align.START -> params.marginTop
                LinearLayout.Align.STRETCH -> params.marginTop - params.marginBottom
                LinearLayout.Align.END -> layoutSize.y - yPadding - params.marginBottom
                LinearLayout.Align.CENTRE -> (layoutSize.y - yPadding) / 2.0f -
                    (child.computed.size.y - params.marginTop + params.marginBottom) / 2.0f
            }
        }
    }

    private fun getInitialEstimates(layout: LinearLayout<*>, availWidth: Float): Array<Estimate> {
        val estimates = Array(layout.children.size) { Estimate() }
        var error = availWidth // because all estimates are currently 0
        var iteration = 0

        do {
            distributeAmongNonPinned(error, estimates, layout)
            applyConstraints(estimates, layout)
            error = availWidth - estimates.sumOfFloat { it.width }
        } while (error * error > SQR_ERROR_TOLERANCE && ++iteration < MAX_ITERATIONS_FOR_INITIAL)

        return estimates
    }

    private fun applyConstraints(estimates: Array<Estimate>, layout: LinearLayout<*>) {
        var marginRightOfPrevChild = 0f
        val lastChild = layout.children.lastOrNull()

        layout.children.forEachIndexed { idx, child ->
            val params: LinearLayoutParams = child.params
            val estimate = estimates[idx]

            if (!estimate.pinned) {
                val innerMinWidth = maxOf(params.minWidth, child.minContentWidth).coerceAtMost(params.maxWidth)
                val actualMarginLeft = maxOf(marginRightOfPrevChild, params.marginLeft) // merging margins
                val marginRightIfLast = if (child === lastChild) params.marginRight else 0f

                // The estimate covers the merged left margin; the last estimate covers the right margin as well.
                estimate.width = clamp(
                    estimate.width,
                    innerMinWidth + actualMarginLeft + marginRightIfLast,
                    params.maxWidth + actualMarginLeft + marginRightIfLast
                )
            }

            marginRightOfPrevChild = params.marginRight
        }
    }

    private fun distributeAmongNonPinned(diff: Float, estimates: Array<Estimate>, layout: LinearLayout<*>): Boolean {
        val numNotPinned = estimates.sumOfInt { if (it.pinned) 0 else 1 }
        if (numNotPinned == 0) return false

        val sumOfWeights = layout.children.foldIndexed(0f) { idx, result, child ->
            val e = estimates[idx]
            return@foldIndexed result + (if (e.pinned) 0f else child.params.weight)
        }

        if (sumOfWeights <= 0f) return false

        var remaining = diff
        val idxOfLastNotPinned = estimates.indexOfLast { !it.pinned }

        layout.children.forEachIndexed { idx, child ->
            val e = estimates[idx]

            if (!e.pinned) {
                if (idx == idxOfLastNotPinned) {
                    // For the last estimate, we use the remaining diff to make up for any Float inaccuracies.
                    estimates[idx].width += remaining
                } else {
                    val delta = diff * child.params.weight / sumOfWeights
                    estimates[idx].width += delta
                    remaining -= delta
                }
            }
        }

        return true
    }

    companion object {
        private const val SQR_ERROR_TOLERANCE = 1f
        private const val MAX_ITERATIONS_FOR_INITIAL = 10
        private const val MAX_ITERATIONS_FOR_ARRANGE = 10
    }
}
