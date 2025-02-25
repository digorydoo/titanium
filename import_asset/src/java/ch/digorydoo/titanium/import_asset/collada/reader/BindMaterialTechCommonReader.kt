package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.BindMaterialTechCommon
import org.w3c.dom.Element

class BindMaterialTechCommonReader(node: Element): XMLTreeReader(node) {
    fun read(): BindMaterialTechCommon {
        val techCommon = BindMaterialTechCommon()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "instance_material" -> {
                    require(techCommon.instanceMaterial == null) { "<instance_material> cannot appear more than once!" }
                    techCommon.instanceMaterial = InstanceMaterialReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return techCommon
    }
}
