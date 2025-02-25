package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.LambertColor
import org.w3c.dom.Element

class LambertColorReader(node: Element, private val requiredSid: String): XMLTreeReader(node) {
    fun read(): LambertColor {
        val result = LambertColor()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "color" -> {
                    require(result.color == null) { "<color> cannot appear more than once!" }
                    result.color = ColorReader(child).read()
                    require(result.color?.sid == requiredSid) { "sid is ${result.color?.sid}, expected $requiredSid" }
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
