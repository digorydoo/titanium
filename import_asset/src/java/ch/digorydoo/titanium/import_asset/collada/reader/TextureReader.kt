package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.Texture
import org.w3c.dom.Element

class TextureReader(node: Element): XMLTreeReader(node) {
    fun read(): Texture {
        val result = Texture()
        checkAttributes(arrayOf("texture", "texcoord"))

        result.texName = getMandatoryAttr("texture")
        result.texCoordName = getMandatoryAttr("texcoord")

        requireChildless()
        return result
    }
}
