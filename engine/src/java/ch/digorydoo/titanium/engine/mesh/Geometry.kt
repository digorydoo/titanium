package ch.digorydoo.titanium.engine.mesh

import ch.digorydoo.kutils.string.indentLines
import java.nio.FloatBuffer

class Geometry(val positions: FloatBuffer, val normals: FloatBuffer, val texCoords: FloatBuffer?) {
    override fun toString() =
        indentLines(
            arrayOf(
                "Geometry {",
                "positions.limit = ${positions.limit()}",
                "normals.limit = ${normals.limit()}",
                "texCoords.limit = ${texCoords?.limit()}",
                "}",
            )
        )
}
