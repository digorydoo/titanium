package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.VertexWeights
import org.w3c.dom.Element

class VertexWeightsReader(node: Element): XMLTreeReader(node) {
    fun read(): VertexWeights {
        val result = VertexWeights()

        checkAttributes(arrayOf("count"))
        result.count = getMandatoryIntAttr("count")

        forEachChild { child ->
            when (child.nodeName) {
                "input" -> {
                    val input = MeshInputReader(child).read()

                    when (input.semantic) {
                        "JOINT" -> {
                            require(result.jointInput == null) { "Multiple <input> with semantic JOINT" }
                            result.jointInput = input
                        }
                        "WEIGHT" -> {
                            require(result.weightInput == null) { "Multiple <input> with semantic WEIGHT" }
                            result.weightInput = input
                        }
                        else -> throw Exception("Unexpected input semantic: ${input.semantic}")
                    }
                }
                "vcount" -> storeValues(child, result.vcount)
                "v" -> storeValues(child, result.vertices)
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return result
    }

    private fun storeValues(child: Element, list: MutableList<Int>) {
        require(list.isEmpty()) { "<${child.nodeName}>: List has already been filled!" }

        list.addAll(
            getValuesList(child).map {
                it.toIntOrNull() ?: throw Exception("<${child.nodeName}>: Expected int, got: $it")
            }
        )
    }
}
