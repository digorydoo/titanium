package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.LightExtra
import org.w3c.dom.Element

class LightExtraReader(node: Element): XMLTreeReader(node) {
    fun read(): LightExtra {
        val extra = LightExtra()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "technique" -> {
                    require(extra.technique == null) { "<technique> cannot appear more than once!" }
                    extra.technique = LightExtraTechniqueReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return extra
    }
}
