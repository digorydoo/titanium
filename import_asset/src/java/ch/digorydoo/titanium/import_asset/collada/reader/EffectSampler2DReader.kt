package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.EffectSampler2D
import org.w3c.dom.Element

class EffectSampler2DReader(node: Element): XMLTreeReader(node) {
    fun read(): EffectSampler2D {
        val result = EffectSampler2D()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "source" -> {
                    require(result.source == null) { "<source> cannot appear more than once!" }
                    result.source = SidValueReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
