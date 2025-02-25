package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.MeshAccessorParam
import org.w3c.dom.Element

class MeshAccessorParamReader(node: Element): XMLTreeReader(node) {
    fun read(): MeshAccessorParam {
        val param = MeshAccessorParam()

        checkAttributes(arrayOf("name", "type"))
        param.name = getMandatoryAttr("name")
        param.type = getMandatoryAttr("type")

        requireChildless()
        return param
    }
}
