package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.LambertValue
import org.w3c.dom.Element

class LambertValueReader(node: Element, private val requiredSid: String): XMLTreeReader(node) {
    fun read(): LambertValue {
        val result = LambertValue()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "float" -> {
                    val value = SidValueReader(child).read()
                    require(value.sid == requiredSid) { "sid is ${value.sid}, expected $requiredSid" }
                    result.value = value.floatValue
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }
}
