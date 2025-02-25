package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Light
import org.w3c.dom.Element

class LibLightsReader(node: Element): XMLTreeReader(node) {
    fun read(): List<Light> {
        val lights = mutableListOf<Light>()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "light" -> lights.add(LightReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return lights
    }
}
