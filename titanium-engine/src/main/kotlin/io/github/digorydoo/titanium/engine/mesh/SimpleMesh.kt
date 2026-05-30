package io.github.digorydoo.titanium.engine.mesh

import io.github.digorydoo.titanium.engine.texture.Texture
import java.nio.FloatBuffer

class SimpleMesh(
    val positions: FloatBuffer,
    val normals: FloatBuffer,
    val tex: Texture?,
    val texCoords: FloatBuffer?,
    var material: MeshMaterial,
)
