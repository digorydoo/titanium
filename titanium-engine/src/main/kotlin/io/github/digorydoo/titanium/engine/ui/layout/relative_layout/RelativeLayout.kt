package io.github.digorydoo.titanium.engine.ui.layout.relative_layout

import io.github.digorydoo.titanium.engine.ui.layout.Layout
import io.github.digorydoo.titanium.engine.ui.layout.LayoutDsl
import io.github.digorydoo.titanium.engine.ui.layout.LayoutParams

class RelativeLayoutParams: LayoutParams {
    var marginLeft = 0f
    var marginTop = 0f
    var marginRight = 0f
    var marginBottom = 0f

    fun margin(m: Float) {
        marginLeft = m
        marginTop = m
        marginRight = m
        marginBottom = m
    }

    var alignParentLeft = false
    var alignParentTop = false
    var alignParentRight = false
    var alignParentBottom = false

    var centreHorizontally = false
    var centreVertically = false

    var leftOf: Layout<RelativeLayoutParams, *>? = null
    var rightOf: Layout<RelativeLayoutParams, *>? = null
    var above: Layout<RelativeLayoutParams, *>? = null
    var below: Layout<RelativeLayoutParams, *>? = null

    var minWidth = 0f
    var minHeight = 0f
    var maxWidth = Float.POSITIVE_INFINITY
    var maxHeight = Float.POSITIVE_INFINITY
}

@LayoutDsl
class RelativeLayout<ParentParams: LayoutParams> private constructor(
    parentParams: ParentParams,
): Layout<ParentParams, RelativeLayoutParams>(parentParams) {
    override fun createParams() = RelativeLayoutParams()

    companion object {
        fun <ParentParams: LayoutParams> build(params: ParentParams, lambda: RelativeLayout<ParentParams>.() -> Unit) =
            RelativeLayout(params).apply { lambda() }
    }
}
