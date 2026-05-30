package io.github.digorydoo.titanium.import_asset.collada.reader

import io.github.digorydoo.titanium.import_asset.XMLTreeReader
import io.github.digorydoo.titanium.import_asset.collada.data.Animation
import org.w3c.dom.Element

class LibAnimationsReader(node: Element): XMLTreeReader(node) {
    fun read(): List<Animation> {
        val result = mutableListOf<Animation>()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "animation" -> result.add(AnimationReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
