package io.github.digorydoo.titanium.engine.ui.layout

abstract class LayoutArranger<C: LayoutParams, L: Layout<*, C>>(protected val delegate: Delegate) {
    interface Delegate {
        fun arrange(layout: Layout<*, *>, minWidth: Float, maxWidth: Float, minHeight: Float, maxHeight: Float)
    }

    abstract fun arrange(layout: L, minWidth: Float, maxWidth: Float, minHeight: Float, maxHeight: Float)
}
