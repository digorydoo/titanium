package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.LambertTransparent
import ch.digorydoo.titanium.import_asset.collada.data.LambertTransparent.Opaque
import org.w3c.dom.Element

class LambertTransparentReader(node: Element): XMLTreeReader(node) {
    fun read(): LambertTransparent {
        val result = LambertTransparent()
        checkAttributes(arrayOf("opaque"))

        val s = getMandatoryAttr("opaque")

        result.opaque = when (s) {
            "A_ONE" -> Opaque.A_ONE
            else -> throw Exception("Illegal value for opaque: $s")
        }

        forEachChild { child ->
            when (child.nodeName) {
                "color" -> {
                    require(result.color == null) { "<color> cannot appear more than once!" }
                    result.color = ColorReader(child).read()
                    require(result.color?.sid == "alpha") { "sid is ${result.color?.sid}, expected \"alpha\"" }
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
