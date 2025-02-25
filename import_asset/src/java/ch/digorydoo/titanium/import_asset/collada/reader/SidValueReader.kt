package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.SidValue
import org.w3c.dom.Element

class SidValueReader(node: Element): XMLTreeReader(node) {
    fun read(): SidValue {
        val sv = SidValue()

        checkAttributes(arrayOf("sid", "type"))
        sv.sid = getOptionalAttr("sid") ?: ""
        sv.type = getOptionalAttr("type") ?: ""

        requireChildless()
        sv.value = getValue()

        return sv
    }
}
