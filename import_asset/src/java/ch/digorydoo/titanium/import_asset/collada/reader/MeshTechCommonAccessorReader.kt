package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.MeshTechCommonAccessor
import org.w3c.dom.Element

class MeshTechCommonAccessorReader(node: Element): XMLTreeReader(node) {
    fun read(): MeshTechCommonAccessor {
        val accessor = MeshTechCommonAccessor()

        checkAttributes(arrayOf("source", "count", "stride"))
        accessor.source = getMandatoryAttr("source")
        accessor.count = getMandatoryIntAttr("count")
        accessor.stride = getMandatoryIntAttr("stride")

        forEachChild { child ->
            when (child.nodeName) {
                "param" -> accessor.params.add(MeshAccessorParamReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return accessor
    }
}
