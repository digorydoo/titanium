package io.github.digorydoo.titanium.import_asset.collada.reader

import io.github.digorydoo.titanium.import_asset.XMLTreeReader
import io.github.digorydoo.titanium.import_asset.collada.data.SkinTechCommon
import org.w3c.dom.Element

class SkinTechCommonReader(node: Element): XMLTreeReader(node) {
    fun read(): SkinTechCommon {
        val result = SkinTechCommon()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "accessor" -> {
                    require(result.accessor == null) { "<accessor> cannot appear more than once!" }
                    result.accessor = SkinTechCommonAccessorReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
