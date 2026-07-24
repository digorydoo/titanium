package io.github.digorydoo.titanium.engine.ui.layout.linear_layout

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.sumOfFloat
import ch.digorydoo.kutils.math.sumOfFloatStartingFrom
import ch.digorydoo.kutils.math.sumOfInt
import io.github.digorydoo.titanium.engine.ui.layout.LayoutArranger

internal class VerticalLinearLayoutArranger(
    delegate: Delegate,
): LayoutArranger<LinearLayoutParams, LinearLayout<*>>(delegate) {
    private class Estimate {
        var height = 0f
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

        var y = 0f // child relativePos are relative to content box, i.e. inside parent's padding
        var maxChildWidth = 0f
        var marginBottomOfPrevChild = 0f
        val lastChild = layout.children.lastOrNull()
        val availableWidth = (maxWidth - xPadding).coerceAtLeast(0f)

        val availableHeight = when (justify) {
            LinearLayout.Justify.WRAP_CONTENT -> 0f // no height to distribute; make everything as tight as possible
            LinearLayout.Justify.MATCH_PARENT -> (maxHeight - yPadding).coerceAtLeast(0f)
        }

        val estimates = getInitialEstimates(layout, availableHeight) // based on weights and constraints
        var iteration = 0

        do {
            iteration++
            var repeat = false
            var remainingHeight = availableHeight
            var anyNonPinnedSoFar = false

            for ((idx, child) in layout.children.withIndex()) {
                val params: LinearLayoutParams = child.params
                val estimate = estimates[idx]
                anyNonPinnedSoFar = anyNonPinnedSoFar || !estimate.pinned

                val actualMarginTop = maxOf(marginBottomOfPrevChild, params.marginTop) // merging margins

                val estimatedHeight = when {
                    child === lastChild -> remainingHeight - actualMarginTop - params.marginBottom
                    estimate.height <= 0f -> 0f
                    else -> (remainingHeight * estimate.height /
                        estimates.sumOfFloatStartingFrom(idx) { it.height }) - actualMarginTop
                }

                val desiredHeight = clamp(estimatedHeight, params.minHeight, params.maxHeight)

                val innerMinWidth = when (align) {
                    LinearLayout.Align.STRETCH -> clamp(
                        availableWidth - params.marginLeft - params.marginRight,
                        params.minWidth,
                        params.maxWidth,
                    )
                    else -> params.minWidth
                }

                delegate.arrange(child, innerMinWidth, params.maxWidth, desiredHeight, desiredHeight)

                val childComputed = child.computed
                val childSize = childComputed.size
                val finalChildHeight = clamp(childSize.y, params.minHeight, params.maxHeight)
                val error = desiredHeight - finalChildHeight

                if (
                    iteration < MAX_ITERATIONS_FOR_ARRANGE &&
                    remainingHeight > 0f &&
                    anyNonPinnedSoFar &&
                    !estimate.pinned &&
                    error * error > SQR_ERROR_TOLERANCE
                ) {
                    estimate.height = actualMarginTop + finalChildHeight
                    estimate.pinned = true
                    distributeAmongNonPinned(error, estimates, layout)
                    applyConstraints(estimates, layout)
                    y = 0f
                    maxChildWidth = 0f
                    marginBottomOfPrevChild = 0f
                    repeat = true
                    break
                }

                childComputed.relativePos.y = y + actualMarginTop // x will be decided later
                maxChildWidth = maxOf(maxChildWidth, childSize.x)

                val delta = actualMarginTop + finalChildHeight
                y += delta // childMarginBottom not added yet, may need merging
                remainingHeight = (remainingHeight - delta).coerceAtLeast(0f)

                marginBottomOfPrevChild = params.marginBottom
            }
        } while (repeat)

        // Compute our own size

        val layoutSize = layout.computed.size

        layoutSize.set(
            x = when (align) {
                LinearLayout.Align.STRETCH -> maxWidth
                else -> clamp(
                    xPadding + maxChildWidth +
                        (layout.children.maxOfOrNull { it.params.marginLeft } ?: 0f) +
                        (layout.children.maxOfOrNull { it.params.marginRight } ?: 0f),
                    minWidth,
                    maxWidth
                )
            },
            y = when (justify) {
                LinearLayout.Justify.MATCH_PARENT -> maxHeight
                LinearLayout.Justify.WRAP_CONTENT -> clamp(
                    layout.paddingTop + y + marginBottomOfPrevChild + layout.paddingBottom,
                    minHeight,
                    maxHeight
                )
            },
        )

        // Determine the children's x coordinates

        layout.children.forEach { child ->
            val params = child.params
            child.computed.relativePos.x = when (align) {
                LinearLayout.Align.START -> params.marginLeft
                LinearLayout.Align.STRETCH -> params.marginLeft - params.marginRight
                LinearLayout.Align.END -> layoutSize.x - xPadding - params.marginRight
                LinearLayout.Align.CENTRE -> (layoutSize.x - xPadding) / 2.0f -
                    (child.computed.size.x - params.marginLeft + params.marginRight) / 2.0f
            }
        }
    }

    private fun getInitialEstimates(layout: LinearLayout<*>, availHeight: Float): Array<Estimate> {
        val estimates = Array(layout.children.size) { Estimate() }
        var error = availHeight // because all estimates are currently 0
        var iteration = 0

        do {
            distributeAmongNonPinned(error, estimates, layout)
            applyConstraints(estimates, layout)
            error = availHeight - estimates.sumOfFloat { it.height }
        } while (error * error > SQR_ERROR_TOLERANCE && ++iteration < MAX_ITERATIONS_FOR_INITIAL)

        return estimates
    }

    private fun applyConstraints(estimates: Array<Estimate>, layout: LinearLayout<*>) {
        var marginBottomOfPrevChild = 0f
        val lastChild = layout.children.lastOrNull()

        layout.children.forEachIndexed { idx, child ->
            val params: LinearLayoutParams = child.params
            val estimate = estimates[idx]

            if (!estimate.pinned) {
                val innerMinHeight = maxOf(params.minHeight, child.minContentHeight).coerceAtMost(params.maxHeight)
                val actualMarginTop = maxOf(marginBottomOfPrevChild, params.marginTop) // merging margins
                val marginBottomIfLast = if (child === lastChild) params.marginBottom else 0f

                // The estimate covers the merged top margin; the last estimate covers the bottom margin as well.
                estimate.height = clamp(
                    estimate.height,
                    innerMinHeight + actualMarginTop + marginBottomIfLast,
                    params.maxHeight + actualMarginTop + marginBottomIfLast
                )
            }

            marginBottomOfPrevChild = params.marginBottom
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
                    estimates[idx].height += remaining
                } else {
                    val delta = diff * child.params.weight / sumOfWeights
                    estimates[idx].height += delta
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
