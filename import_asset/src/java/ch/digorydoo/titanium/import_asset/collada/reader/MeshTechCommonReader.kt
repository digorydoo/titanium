package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.MeshTechCommon
import org.w3c.dom.Element

class MeshTechCommonReader(node: Element): XMLTreeReader(node) {
    fun read(): MeshTechCommon {
        val techCommon = MeshTechCommon()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "accessor" -> {
                    require(techCommon.accessor == null) { "<accessor> not expected to occur more than once!" }
                    techCommon.accessor = MeshTechCommonAccessorReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return techCommon
    }
}
