package ch.digorydoo.titanium.engine.mesh

import ch.digorydoo.titanium.engine.texture.Texture
import java.nio.FloatBuffer

class SimpleMesh(
    val positions: FloatBuffer,
    val normals: FloatBuffer,
    val tex: Texture?,
    val texCoords: FloatBuffer?,
    var material: MeshMaterial,
)
