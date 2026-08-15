package io.github.digorydoo.titanium.engine.mesh

import ch.digorydoo.kutils.string.indentLines
import java.nio.FloatBuffer

class MeshGeometry(
    val id: Int,
    val positions: FloatBuffer,
    val normals: FloatBuffer,
    val texCoords: FloatBuffer?,
    // TODO val tex: Texture?
    val material: MeshMaterial,
) {
    override fun toString() =
        indentLines(
            arrayOf(
                "Geometry {",
                "id=$id",
                "positions.limit = ${positions.limit()}",
                "normals.limit = ${normals.limit()}",
                "texCoords.limit = ${texCoords?.limit()}",
                "material=$material",
                "}",
            )
        )
}
