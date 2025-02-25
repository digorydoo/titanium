package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Color
import org.w3c.dom.Element

class ColorReader(node: Element): XMLTreeReader(node) {
    fun read(): Color {
        val c = Color()

        checkAttributes(arrayOf("sid"))
        c.sid = getMandatoryAttr("sid")

        requireChildless()

        val values = getValuesList()
            .map { it.toFloatOrNull() ?: throw Exception("Color component is not a float: $it") }

        require(values.size == 3 || values.size == 4) { "Color is expected to consist of exactly 3 or 4 components" }

        c.red = values[0]
        c.green = values[1]
        c.blue = values[2]
        c.alpha = if (values.size == 4) values[3] else null

        return c
    }
}
