package io.github.digorydoo.titanium.engine.ui.layout.frame_layout

import io.github.digorydoo.titanium.engine.ui.layout.Layout
import io.github.digorydoo.titanium.engine.ui.layout.LayoutDsl
import io.github.digorydoo.titanium.engine.ui.layout.LayoutParams

class FrameLayoutParams: LayoutParams

@LayoutDsl
class FrameLayout<ParentParams: LayoutParams> private constructor(
    parentParams: ParentParams,
): Layout<ParentParams, FrameLayoutParams>(parentParams) {
    override fun createParams() = FrameLayoutParams()

    companion object {
        fun <ParentParams: LayoutParams> build(params: ParentParams, lambda: FrameLayout<ParentParams>.() -> Unit) =
            FrameLayout(params).apply { lambda() }
    }
}
