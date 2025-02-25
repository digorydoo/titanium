package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Controller
import org.w3c.dom.Element

class LibControllersReader(node: Element): XMLTreeReader(node) {
    fun read(): List<Controller> {
        val list = mutableListOf<Controller>()
        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "controller" -> list.add(ControllerReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return list
    }
}
