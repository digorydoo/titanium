package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.NodeExtra
import org.w3c.dom.Element

class NodeExtraReader(node: Element): XMLTreeReader(node) {
    fun read(): NodeExtra {
        val extra = NodeExtra()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "technique" -> {
                    require(extra.technique == null) { "<technique> cannot appear more than once!" }
                    extra.technique = NodeExtraTechniqueReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return extra
    }
}
