package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.BindVertexInput
import org.w3c.dom.Element

class BindVertexInputReader(node: Element): XMLTreeReader(node) {
    fun read(): BindVertexInput {
        val result = BindVertexInput()

        checkAttributes(arrayOf("semantic", "input_semantic", "input_set"))
        result.semantic = getMandatoryAttr("semantic")
        result.inputSemantic = getMandatoryAttr("input_semantic")
        result.inputSet = getMandatoryIntAttr("input_set")

        requireChildless()
        return result
    }
}
