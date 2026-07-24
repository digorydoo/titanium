package io.github.digorydoo.titanium.engine.ui.layout

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.titanium.engine.gel.GraphicElement

internal class LayoutVisitor {
    fun forEachLayoutInTree(startWith: Layout<*, *>, lambda: (layout: Layout<*, *>) -> Unit) {
        lambda(startWith)
        startWith.children.forEach { forEachLayoutInTree(it, lambda) }
    }

    fun forEachGelInTree(startWith: Layout<*, *>, lambda: (gel: GraphicElement) -> Unit) {
        forEachLayoutInTree(startWith) { layout ->
            layout.background?.let { background ->
                when {
                    background !is GraphicElement -> {
                        // Unfortunately, Kotlin does not allow us to declare it as GraphicElement & LayoutElement.
                        Log.error(TAG, "Removing background, which is not derived from GraphicElement: $background")
                        layout.background = null
                    }
                    background.zombie -> {
                        // This should not happen. Gels should become zombie only when the layout is discarded as well.
                        Log.error(TAG, "Removing background, which is already a zombie: $background")
                        layout.background = null
                    }
                    else -> lambda(background)
                }
            }

            layout.content?.let { content ->
                when {
                    content !is GraphicElement -> {
                        Log.error(TAG, "Removing content, which is not derived from GraphicElement: $content")
                        layout.content = null
                    }
                    content.zombie -> {
                        Log.error(TAG, "Removing content, which is already a zombie: $content")
                        layout.content = null
                    }
                    else -> lambda(content)
                }
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("LayoutVisitor")
    }
}
