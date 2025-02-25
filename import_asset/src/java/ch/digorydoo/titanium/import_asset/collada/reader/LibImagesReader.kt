package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Image
import org.w3c.dom.Element

class LibImagesReader(node: Element): XMLTreeReader(node) {
    fun read(): List<Image> {
        val images = mutableListOf<Image>()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "image" -> images.add(ImageReader(child).read())
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return images
    }
}
