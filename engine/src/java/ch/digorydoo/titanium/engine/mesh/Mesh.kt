package ch.digorydoo.titanium.engine.mesh

import ch.digorydoo.kutils.string.indentLines
import java.nio.FloatBuffer

class Mesh(val divisions: List<MeshDivision>) {
    constructor(positions: FloatBuffer, normals: FloatBuffer, material: MeshMaterial):
        this(
            listOf(
                MeshDivision(
                    listOf(
                        MeshNode(
                            id = "",
                            tex = null,
                            transform = null,
                            geometry = Geometry(
                                positions = positions,
                                normals = normals,
                                texCoords = null,
                            ),
                            children = null,
                        )
                    ),
                    material
                )
            )
        )

    override fun toString() =
        indentLines(
            arrayOf(
                "Mesh {",
                "divisions = [${indentLines(divisions.joinToString(", "))}]",
                "}",
            )
        )
}
