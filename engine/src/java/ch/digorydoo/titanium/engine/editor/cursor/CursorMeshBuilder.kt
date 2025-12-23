package ch.digorydoo.titanium.engine.editor.cursor

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.toFloatBuffer
import ch.digorydoo.titanium.engine.editor.cursor.CursorGel.Kind
import ch.digorydoo.titanium.engine.mesh.MeshMaterial
import ch.digorydoo.titanium.engine.mesh.SimpleMesh

class CursorMeshBuilder(private val kind: Kind) {
    fun build(): SimpleMesh {
        val positions = buildPositions(kind)
        val normals = buildNormals(kind)
        return SimpleMesh(
            positions.toFloatBuffer(),
            normals.toFloatBuffer(),
            tex = null,
            texCoords = null,
            MeshMaterial.BLACK_CLOTH
        )
    }

    companion object {
        private fun buildPositions(kind: Kind): FloatArray {
            val a = 0.01f
            val b = 0.42f

            return when (kind) {
                Kind.SMALL_UPPER_NW -> buildPositions(Kind.BIG_UPPER_NW).also { arr ->
                    arr.forEachIndexed { i, f ->
                        arr[i] = f / 7.0f
                    }
                }
                Kind.SMALL_UPPER_NE -> buildPositions(Kind.BIG_UPPER_NE).also { arr ->
                    arr.forEachIndexed { i, f ->
                        arr[i] = f / 7.0f
                    }
                }
                Kind.SMALL_UPPER_SW -> buildPositions(Kind.BIG_UPPER_SW).also { arr ->
                    arr.forEachIndexed { i, f ->
                        arr[i] = f / 7.0f
                    }
                }
                Kind.SMALL_UPPER_SE -> buildPositions(Kind.BIG_UPPER_SE).also { arr ->
                    arr.forEachIndexed { i, f ->
                        arr[i] = f / 7.0f
                    }
                }
                Kind.SMALL_LOWER_NW -> buildPositions(Kind.BIG_LOWER_NW).also { arr ->
                    arr.forEachIndexed { i, f ->
                        arr[i] = f / 7.0f
                    }
                }
                Kind.SMALL_LOWER_NE -> buildPositions(Kind.BIG_LOWER_NE).also { arr ->
                    arr.forEachIndexed { i, f ->
                        arr[i] = f / 7.0f
                    }
                }
                Kind.SMALL_LOWER_SW -> buildPositions(Kind.BIG_LOWER_SW).also { arr ->
                    arr.forEachIndexed { i, f ->
                        arr[i] = f / 7.0f
                    }
                }
                Kind.SMALL_LOWER_SE -> buildPositions(Kind.BIG_LOWER_SE).also { arr ->
                    arr.forEachIndexed { i, f ->
                        arr[i] = f / 7.0f
                    }
                }
                Kind.BIG_UPPER_NW -> floatArrayOf(
                    -a, -a, a,
                    -a, -a, -b,
                    b, -a, a,

                    -a, -a, a,
                    -a, b, a,
                    -a, -a, -b,

                    -a, -a, a,
                    b, -a, a,
                    -a, b, a,
                )
                Kind.BIG_UPPER_NE -> floatArrayOf(
                    -a, a, a,
                    b, a, a,
                    -a, a, -b,

                    -a, a, a,
                    -a, a, -b,
                    -a, -b, a,

                    -a, a, a,
                    -a, -b, a,
                    b, a, a,
                )
                Kind.BIG_UPPER_SW -> floatArrayOf(
                    a, -a, a,
                    -b, -a, a,
                    a, -a, -b,

                    a, -a, a,
                    a, -a, -b,
                    a, b, a,

                    a, -a, a,
                    a, b, a,
                    -b, -a, a,
                )
                Kind.BIG_UPPER_SE -> floatArrayOf(
                    a, a, a,
                    a, a, -b,
                    -b, a, a,

                    a, a, a,
                    a, -b, a,
                    a, a, -b,

                    a, a, a,
                    -b, a, a,
                    a, -b, a,
                )
                Kind.BIG_LOWER_NW -> floatArrayOf(
                    -a, -a, -a,
                    b, -a, -a,
                    -a, -a, b,

                    -a, -a, -a,
                    -a, -a, b,
                    -a, b, -a,

                    -a, -a, -a,
                    -a, b, -a,
                    b, -a, -a,
                )
                Kind.BIG_LOWER_NE -> floatArrayOf(
                    -a, a, -a,
                    -a, a, b,
                    b, a, -a,

                    -a, a, -a,
                    -a, -b, -a,
                    -a, a, b,

                    -a, a, -a,
                    b, a, -a,
                    -a, -b, -a,
                )
                Kind.BIG_LOWER_SW -> floatArrayOf(
                    a, -a, -a,
                    a, -a, b,
                    -b, -a, -a,

                    a, -a, -a,
                    a, b, -a,
                    a, -a, b,

                    a, -a, -a,
                    -b, -a, -a,
                    a, b, -a,
                )
                Kind.BIG_LOWER_SE -> floatArrayOf(
                    a, a, -a,
                    -b, a, -a,
                    a, a, b,

                    a, a, -a,
                    a, a, b,
                    a, -b, -a,

                    a, a, -a,
                    a, -b, -a,
                    -b, a, -a,
                )
            }
        }

        private fun buildNormals(kind: Kind): FloatArray {
            val nd = MutablePoint3f(1.0f, 1.0f, -1.0f).normalize()

            return when (kind) {
                Kind.SMALL_UPPER_NW,
                Kind.BIG_UPPER_NW,
                -> floatArrayOf(
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,

                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,

                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                )
                Kind.SMALL_UPPER_NE,
                Kind.BIG_UPPER_NE,
                -> floatArrayOf(
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,

                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,

                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                )
                Kind.SMALL_UPPER_SW,
                Kind.BIG_UPPER_SW,
                -> floatArrayOf(
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,

                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,

                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                )
                Kind.SMALL_UPPER_SE,
                Kind.BIG_UPPER_SE,
                -> floatArrayOf(
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,

                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,

                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                )
                Kind.SMALL_LOWER_NW,
                Kind.BIG_LOWER_NW,
                -> floatArrayOf(
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,

                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,

                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,

                    nd.x, nd.y, -nd.z,
                    nd.x, nd.y, -nd.z,
                    nd.x, nd.y, -nd.z,
                )
                Kind.SMALL_LOWER_NE,
                Kind.BIG_LOWER_NE,
                -> floatArrayOf(
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,

                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,

                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,

                    nd.x, -nd.y, -nd.z,
                    nd.x, -nd.y, -nd.z,
                    nd.x, -nd.y, -nd.z,
                )
                Kind.SMALL_LOWER_SW,
                Kind.BIG_LOWER_SW,
                -> floatArrayOf(
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,

                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,

                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,

                    -nd.x, nd.y, -nd.z,
                    -nd.x, nd.y, -nd.z,
                    -nd.x, nd.y, -nd.z,
                )
                Kind.SMALL_LOWER_SE,
                Kind.BIG_LOWER_SE,
                -> floatArrayOf(
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,

                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,

                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,

                    -nd.x, -nd.y, -nd.z,
                    -nd.x, -nd.y, -nd.z,
                    -nd.x, -nd.y, -nd.z,
                )
            }
        }
    }
}
