package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.NameArray
import org.w3c.dom.Element

class NameArrayReader(node: Element): XMLTreeReader(node) {
    fun read(): NameArray {
        val result = NameArray()

        checkAttributes(arrayOf("id", "count"))
        result.id = getMandatoryAttr("id")
        result.count = getMandatoryIntAttr("count")

        requireChildless()

        result.names.addAll(getValuesList())
        require(result.names.size == result.count) { "count=${result.count}, but names.size=${result.names.size}" }

        return result
    }
}
