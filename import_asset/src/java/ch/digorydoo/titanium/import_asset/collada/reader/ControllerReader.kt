package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Controller
import org.w3c.dom.Element

class ControllerReader(node: Element): XMLTreeReader(node) {
    fun read(): Controller {
        val ctrl = Controller()

        checkAttributes(arrayOf("id", "name"))
        ctrl.id = getMandatoryAttr("id")
        ctrl.name = getMandatoryAttr("name")

        forEachChild { child ->
            when (child.nodeName) {
                "skin" -> {
                    require(ctrl.skin == null) { "<skin> cannot appear more than once!" }
                    ctrl.skin = SkinReader(child).read()
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return ctrl
    }
}
