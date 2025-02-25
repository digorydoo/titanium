package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.InstanceController
import org.w3c.dom.Element

class InstanceControllerReader(node: Element): XMLTreeReader(node) {
    fun read(): InstanceController {
        val result = InstanceController()

        checkAttributes(arrayOf("url"))
        result.url = getMandatoryAttr("url")

        forEachChild { child ->
            when (child.nodeName) {
                "skeleton" -> {
                    require(result.skeleton == null) { "<skeleton> cannot appear more than once!" }
                    result.skeleton = SidValueReader(child).read()
                }
                "bind_material" -> {
                    require(result.bindMaterial == null) { "<bind_material> cannot appear more than once" }
                    result.bindMaterial = BindMaterialReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
