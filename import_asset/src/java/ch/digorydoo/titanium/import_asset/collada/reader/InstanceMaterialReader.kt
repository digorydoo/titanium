package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.InstanceMaterial
import org.w3c.dom.Element

class InstanceMaterialReader(node: Element): XMLTreeReader(node) {
    fun read(): InstanceMaterial {
        val inst = InstanceMaterial()

        checkAttributes(arrayOf("symbol", "target"))
        inst.symbol = getMandatoryAttr("symbol")
        inst.target = getMandatoryAttr("target")

        forEachChild { child ->
            when (child.nodeName) {
                "bind_vertex_input" -> {
                    require(inst.bindVertexInput == null) { "<bind_vertex_input> cannot appear more than once!" }
                    inst.bindVertexInput = BindVertexInputReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return inst
    }
}
