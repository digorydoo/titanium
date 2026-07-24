package io.github.digorydoo.titanium.engine.ui.layout

import ch.digorydoo.kutils.vector.MutableVector2f
import io.github.digorydoo.titanium.engine.ui.layout.frame_layout.FrameLayout
import io.github.digorydoo.titanium.engine.ui.layout.linear_layout.LinearLayout
import io.github.digorydoo.titanium.engine.ui.layout.relative_layout.RelativeLayout

@DslMarker
annotation class LayoutDsl

interface LayoutElement {
    val minWidth: Float
    val minHeight: Float
    fun onLayoutUpdated(left: Float, top: Float, right: Float, bottom: Float)
}

interface LayoutParams

class LayoutComputedValues {
    val relativePos = MutableVector2f() // relative to parent (inside parent padding)
    val size = MutableVector2f() // includes the padding

    operator fun component1() = relativePos
    operator fun component2() = size
}

abstract class Layout<ParentParams: LayoutParams, ChildParams: LayoutParams>(val params: ParentParams) {
    var name = "" // FIXME for debugging purposes; remove later

    var paddingLeft = 0f
    var paddingTop = 0f
    var paddingRight = 0f
    var paddingBottom = 0f

    fun padding(p: Float) {
        paddingLeft = p
        paddingTop = p
        paddingRight = p
        paddingBottom = p
    }

    var background: LayoutElement? = null
    var content: LayoutElement? = null

    val minContentWidth: Float get() = paddingLeft + (content?.minWidth ?: 0f) + paddingRight
    val minContentHeight: Float get() = paddingTop + (content?.minHeight ?: 0f) + paddingBottom

    private val _children = mutableListOf<Layout<ChildParams, *>>()
    val children: List<Layout<ChildParams, *>> get() = _children

    val computed = LayoutComputedValues()

    protected abstract fun createParams(): ChildParams

    fun horizontal(lambda: LinearLayout<ChildParams>.() -> Unit): LinearLayout<ChildParams> {
        val params = createParams()
        val layout = LinearLayout.build(LinearLayout.Orientation.HORIZONTAL, params, lambda)
        _children.add(layout)
        return layout
    }

    fun vertical(lambda: LinearLayout<ChildParams>.() -> Unit): LinearLayout<ChildParams> {
        val params = createParams()
        val layout = LinearLayout.build(LinearLayout.Orientation.VERTICAL, params, lambda)
        _children.add(layout)
        return layout
    }

    fun relative(lambda: RelativeLayout<ChildParams>.() -> Unit): RelativeLayout<ChildParams> {
        val params = createParams()
        val layout = RelativeLayout.build(params, lambda)
        _children.add(layout)
        return layout
    }

    fun frame(lambda: FrameLayout<ChildParams>.() -> Unit): FrameLayout<ChildParams> {
        val params = createParams()
        val layout = FrameLayout.build(params, lambda)
        _children.add(layout)
        return layout
    }
}
