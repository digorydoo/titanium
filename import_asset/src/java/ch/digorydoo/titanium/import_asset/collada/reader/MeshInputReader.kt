package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.MeshInput
import org.w3c.dom.Element

class MeshInputReader(node: Element): XMLTreeReader(node) {
    fun read(): MeshInput {
        val input = MeshInput()

        checkAttributes(arrayOf("semantic", "source", "offset", "set"))
        input.semantic = getMandatoryAttr("semantic")
        input.source = getMandatoryAttr("source")
        input.offset = getOptionalIntAttr("offset") ?: 0
        input.set = getOptionalIntAttr("set") ?: 0

        requireChildless()

        return input
    }
}
