package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.SkinTechCommonAccessor
import org.w3c.dom.Element

class SkinTechCommonAccessorReader(node: Element): XMLTreeReader(node) {
    fun read(): SkinTechCommonAccessor {
        val result = SkinTechCommonAccessor()

        checkAttributes(arrayOf("source", "count", "stride"))
        result.source = getMandatoryAttr("source")
        result.count = getMandatoryIntAttr("count")
        result.stride = getMandatoryIntAttr("stride")

        forEachChild { child ->
            when (child.nodeName) {
                "param" -> {
                    require(result.param == null) { "<param> cannot appear more than once!" }
                    result.param = MeshAccessorParamReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
