package io.github.digorydoo.titanium.engine.mesh

import ch.digorydoo.kutils.string.indentLines

class ComplexMesh(val geometries: List<MeshGeometry>, val nodes: List<MeshNode>, val skeleton: Skeleton?) {
    override fun toString() =
        indentLines(
            arrayOf(
                "ComplexMesh {",
                "geometries = [${indentLines(geometries.joinToString(", "))}]",
                "nodes = [${indentLines(nodes.joinToString(", "))}]",
                "skeleton = $skeleton",
                "}",
            )
        )
}
