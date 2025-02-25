package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Instance
import org.w3c.dom.Element

class InstanceReader(node: Element): XMLTreeReader(node) {
    fun read(): Instance {
        val inst = Instance()

        checkAttributes(arrayOf("url", "name"))
        inst.url = getMandatoryAttr("url")
        inst.name = getOptionalAttr("name") ?: ""

        requireChildless()
        return inst
    }
}
