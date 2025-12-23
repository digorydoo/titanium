package ch.digorydoo.titanium.engine.mesh

import ch.digorydoo.kutils.string.indentLines

class ComplexMesh(val divisions: List<MeshDivision>) {
    override fun toString() =
        indentLines(
            arrayOf(
                "ComplexMesh {",
                "divisions = [${indentLines(divisions.joinToString(", "))}]",
                "}",
            )
        )
}
