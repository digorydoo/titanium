package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.LightDirectional
import org.w3c.dom.Element

class LightDirectionalReader(node: Element): XMLTreeReader(node) {
    fun read(): LightDirectional {
        val ld = LightDirectional()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "color" -> {
                    require(ld.color == null) { "<color> not expected to appear more than once!" }
                    ld.color = ColorReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return ld
    }
}
