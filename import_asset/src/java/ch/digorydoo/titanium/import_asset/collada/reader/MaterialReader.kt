package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Material
import org.w3c.dom.Element

class MaterialReader(node: Element): XMLTreeReader(node) {
    fun read(): Material {
        val material = Material()

        checkAttributes(arrayOf("id", "name"))
        material.id = getMandatoryAttr("id")
        material.name = getMandatoryAttr("name")

        forEachChild { child ->
            when (child.nodeName) {
                "instance_effect" -> {
                    require(material.instanceEffect == null) { "<instance_effect> cannot appear more than once!" }
                    material.instanceEffect = InstanceReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return material
    }
}
