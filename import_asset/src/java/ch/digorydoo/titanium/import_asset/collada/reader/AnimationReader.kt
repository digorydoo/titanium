package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Animation
import org.w3c.dom.Element

class AnimationReader(node: Element): XMLTreeReader(node) {
    fun read(): Animation {
        val result = Animation()

        checkAttributes(arrayOf("id", "name"))
        result.id = getMandatoryAttr("id")
        result.name = getMandatoryAttr("name")

        forEachChild { child ->
            // when (child.nodeName) {
            /* else -> */ throw Exception("Unexpected tag: ${child.nodeName}")
            //}
        }

        return result
    }
}
