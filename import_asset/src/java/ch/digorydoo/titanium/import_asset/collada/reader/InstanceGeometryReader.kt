package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.InstanceGeometry
import org.w3c.dom.Element

class InstanceGeometryReader(node: Element): XMLTreeReader(node) {
    fun read(): InstanceGeometry {
        val inst = InstanceGeometry()

        checkAttributes(arrayOf("url", "name"))
        inst.url = getMandatoryAttr("url")
        inst.name = getOptionalAttr("name") ?: ""

        forEachChild { child ->
            when (child.nodeName) {
                "bind_material" -> {
                    require(inst.bindMaterial == null) { "<bind_material> cannot appear more than once!" }
                    inst.bindMaterial = BindMaterialReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return inst
    }
}
