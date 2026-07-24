package io.github.digorydoo.titanium.engine.ui.layout.linear_layout

import io.github.digorydoo.titanium.engine.ui.layout.Layout
import io.github.digorydoo.titanium.engine.ui.layout.LayoutDsl
import io.github.digorydoo.titanium.engine.ui.layout.LayoutParams

class LinearLayoutParams: LayoutParams {
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

    var weight = 1f
    var minWidth = 0f
    var minHeight = 0f
    var maxWidth = Float.POSITIVE_INFINITY
    var maxHeight = Float.POSITIVE_INFINITY
}

@LayoutDsl
class LinearLayout<ParentParams: LayoutParams> private constructor(
    val orientation: Orientation,
    parentParams: ParentParams,
): Layout<ParentParams, LinearLayoutParams>(parentParams) {
    enum class Orientation { HORIZONTAL, VERTICAL }
    enum class Align { START, END, CENTRE, STRETCH }
    enum class Justify { WRAP_CONTENT, MATCH_PARENT }

    var align = Align.START
    var justify = Justify.MATCH_PARENT

    override fun createParams() = LinearLayoutParams()

    companion object {
        fun <ParentParams: LayoutParams> build(
            orientation: Orientation,
            params: ParentParams,
            lambda: LinearLayout<ParentParams>.() -> Unit,
        ) = LinearLayout(orientation, params).apply { lambda() }
    }
}
