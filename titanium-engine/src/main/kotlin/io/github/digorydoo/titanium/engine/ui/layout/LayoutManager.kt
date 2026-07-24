package io.github.digorydoo.titanium.engine.ui.layout

import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.vector.MutableVector2i
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.core.GameLoop
import io.github.digorydoo.titanium.engine.gel.GelLayer
import io.github.digorydoo.titanium.engine.ui.layout.relative_layout.RelativeLayout

class LayoutManager: GameLoop.Tick {
    object RootLayoutParams: LayoutParams
    typealias RootLayout = RelativeLayout<RootLayoutParams>

    private class LayoutAndState(val layout: RootLayout) {
        var arrangingNeeded = false
        var shown = false
    }

    private val arrangers = LayoutArrangerHolder()
    private val registeredLayouts = mutableListOf<LayoutAndState>()
    private val screenSizeOfLastTick = MutableVector2i()
    private val visitor = LayoutVisitor()

    fun build(lambda: RootLayout.() -> Unit) =
        RelativeLayout.build(RootLayoutParams, lambda)

    fun show(layout: RootLayout) {
        var state = registeredLayouts.find { it.layout == layout }

        if (state == null) {
            // Register new layout

            visitor.forEachGelInTree(layout) { gel ->
                require(!gel.onCreateCalled) { "Gel's onCreate has already been called: $gel" }
                gel.onCreate(GelLayer.LayerKind.UI_BELOW_DLG)
            }

            state = LayoutAndState(layout)
            registeredLayouts.add(state)

            arrangers.arrange(layout)

            state.shown = true
            state.arrangingNeeded = false
        } else if (!state.shown) {
            // Show hidden layout.
            visitor.forEachGelInTree(layout) { it.show() }
            state.shown = true
            state.arrangingNeeded = true
        }
    }

    fun hide(layout: RootLayout) {
        val state = registeredLayouts.find { it.layout == layout }

        if (state == null) {
            Log.warn(TAG, "Cannot hide a layout that has not been registered yet.")
        } else {
            state.shown = false
            state.arrangingNeeded = false
        }
    }

    fun discard(layout: RootLayout) {
        val state = registeredLayouts.find { it.layout == layout }

        if (state == null) {
            Log.warn(TAG, "Cannot discard a layout that has not been registered yet.")
        } else {
            visitor.forEachGelInTree(layout) { it.setZombie() }
            registeredLayouts.remove(state)
        }
    }

    fun requestArranging(layout: RootLayout) {
        val state = registeredLayouts.find { it.layout == layout }

        if (state == null) {
            Log.error(TAG, "Cannot request arraning for a layout that has not been registered yet.")
        } else {
            state.arrangingNeeded = true
        }
    }

    override fun tick(token: GameLoop.Token) {
        val screenSize = App.resolutionMgr.screenSizeDp
        val sizeChanged = screenSizeOfLastTick.x != screenSize.x || screenSizeOfLastTick.y != screenSize.y

        if (sizeChanged) {
            screenSizeOfLastTick.set(screenSize)
        }

        registeredLayouts.forEach { state ->
            if (sizeChanged || state.arrangingNeeded) {
                arrangers.arrange(state.layout)
                state.arrangingNeeded = false
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("LayoutManager")
    }
}
