package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Animation
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
