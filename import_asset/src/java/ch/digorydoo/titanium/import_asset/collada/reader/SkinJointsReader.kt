package ch.digorydoo.titanium.import_asset.collada.reader

import ch.digorydoo.titanium.import_asset.XMLTreeReader
import ch.digorydoo.titanium.import_asset.collada.data.SkinJoints
import org.w3c.dom.Element

class SkinJointsReader(node: Element): XMLTreeReader(node) {
    fun read(): SkinJoints {
        val joints = SkinJoints()

        checkAttributes(arrayOf())

        forEachChild { child ->
            when (child.nodeName) {
                "input" -> {
                    val input = MeshInputReader(child).read()

                    when (input.semantic) {
                        "JOINT" -> {
                            require(joints.jointInput == null) { "Multiple <input> with semantic JOINT" }
                            joints.jointInput = input
                        }
                        "INV_BIND_MATRIX" -> {
                            require(joints.invBindMatrixInput == null) { "Multiple <input> with semantic INV_BIND_MATRIX" }
                            joints.invBindMatrixInput = input
                        }
                        else -> throw Exception("Unexpected input semantic: ${input.semantic}")
                    }
                }
                else -> throw Exception("Unexpected tag: ${child.nodeName}")
            }
        }

        return joints
    }
}
