package io.github.digorydoo.titanium.import_asset.collada.reader

import io.github.digorydoo.titanium.import_asset.XMLTreeReader
import io.github.digorydoo.titanium.import_asset.collada.data.MeshAccessorParam
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
