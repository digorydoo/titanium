package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Image
import org.w3c.dom.Element

class ImageReader(node: Element): XMLTreeReader(node) {
    fun read(): Image {
        val img = Image()

        checkAttributes(arrayOf("id", "name"))
        img.id = getMandatoryAttr("id")
        img.name = getMandatoryAttr("name")

        forEachChild { child ->
            when (child.nodeName) {
                "init_from" -> {
                    require(img.path.isEmpty()) { "path has already been set: ${img.path}" }
                    img.path = getChildValue(child)
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return img
    }
}
