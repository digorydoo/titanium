package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Effect
import org.w3c.dom.Element

class LibEffectsReader(node: Element): XMLTreeReader(node) {
    fun read(): List<Effect> {
        val effects = mutableListOf<Effect>()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "effect" -> effects.add(EffectReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return effects
    }
}
