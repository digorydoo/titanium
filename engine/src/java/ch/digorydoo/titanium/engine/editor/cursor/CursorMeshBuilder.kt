package ch.digorydoo.titanium.engine.editor.cursor

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.utils.toFloatBuffer
import ch.digorydoo.titanium.engine.editor.cursor.CursorGel.Kind
import ch.digorydoo.titanium.engine.mesh.Mesh
import ch.digorydoo.titanium.engine.mesh.MeshMaterial

class CursorMeshBuilder(private val kind: Kind) {
    fun build(): Mesh {
        val positions = buildPositions().toFloatBuffer()
        val normals = buildNormals().toFloatBuffer()
        return Mesh(positions, normals, MeshMaterial.BLACK_CLOTH)
    }

    private fun buildPositions(): FloatArray {
        val xa = -0.01f
        val xb = 0.5f
        val xc = 1.01f
        val ya = -0.01f
        val yb = 0.5f
        val yc = 1.01f
        val za = 0.01f
        val zb = 0.5f
        val zc = -0.01f
        val zd = -0.5f
        return when (kind) {
            Kind.UPPER_NW -> floatArrayOf(
                xa, ya, za,
                xa, ya, zd,
                xb, ya, za,

                xa, ya, za,
                xa, yb, za,
                xa, ya, zd,

                xa, ya, za,
                xb, ya, za,
                xa, yb, za,

                xb, ya, za,
                xa, ya, zd,
                xa, yb, za,
            )
            Kind.UPPER_NE -> floatArrayOf(
                xa, yc, za,
                xb, yc, za,
                xa, yc, zd,

                xa, yc, za,
                xa, yc, zd,
                xa, yb, za,

                xa, yc, za,
                xa, yb, za,
                xb, yc, za,

                xb, yc, za,
                xa, yb, za,
                xa, yc, zd,
            )
            Kind.UPPER_SW -> floatArrayOf(
                xc, ya, za,
                xb, ya, za,
                xc, ya, zd,

                xc, ya, za,
                xc, ya, zd,
                xc, yb, za,

                xc, ya, za,
                xc, yb, za,
                xb, ya, za,

                xb, ya, za,
                xc, yb, za,
                xc, ya, zd,
            )
            Kind.UPPER_SE -> floatArrayOf(
                xc, yc, za,
                xc, yc, zd,
                xb, yc, za,

                xc, yc, za,
                xc, yb, za,
                xc, yc, zd,

                xc, yc, za,
                xb, yc, za,
                xc, yb, za,

                xb, yc, za,
                xc, yc, zd,
                xc, yb, za,
            )
            Kind.LOWER_NW -> floatArrayOf(
                xa, ya, zc,
                xb, ya, zc,
                xa, ya, zb,

                xa, ya, zc,
                xa, ya, zb,
                xa, yb, zc,

                xa, ya, zc,
                xa, yb, zc,
                xb, ya, zc,

                xb, ya, zc,
                xa, yb, zc,
                xa, ya, zb,
            )
            Kind.LOWER_NE -> floatArrayOf(
                xa, yc, zc,
                xa, yc, zb,
                xb, yc, zc,

                xa, yc, zc,
                xa, yb, zc,
                xa, yc, zb,

                xa, yc, zc,
                xb, yc, zc,
                xa, yb, zc,

                xb, yc, zc,
                xa, yc, zb,
                xa, yb, zc,
            )
            Kind.LOWER_SW -> floatArrayOf(
                xc, ya, zc,
                xc, ya, zb,
                xb, ya, zc,

                xc, ya, zc,
                xc, yb, zc,
                xc, ya, zb,

                xc, ya, zc,
                xb, ya, zc,
                xc, yb, zc,

                xb, ya, zc,
                xc, ya, zb,
                xc, yb, zc,
            )
            Kind.LOWER_SE -> floatArrayOf(
                xc, yc, zc,
                xb, yc, zc,
                xc, yc, zb,

                xc, yc, zc,
                xc, yc, zb,
                xc, yb, zc,

                xc, yc, zc,
                xc, yb, zc,
                xb, yc, zc,

                xb, yc, zc,
                xc, yb, zc,
                xc, yc, zb,
            )
        }
    }

    private fun buildNormals(): FloatArray {
        val nd = MutablePoint3f(1.0f, 1.0f, -1.0f).normalize()
        return when (kind) {
            Kind.UPPER_NW -> floatArrayOf(
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,

                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,

                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,

                nd.x, nd.y, nd.z,
                nd.x, nd.y, nd.z,
                nd.x, nd.y, nd.z,
            )
            Kind.UPPER_NE -> floatArrayOf(
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,

                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,

                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,

                nd.x, -nd.y, nd.z,
                nd.x, -nd.y, nd.z,
                nd.x, -nd.y, nd.z,
            )
            Kind.UPPER_SW -> floatArrayOf(
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,

                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,

                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,

                -nd.x, nd.y, nd.z,
                -nd.x, nd.y, nd.z,
                -nd.x, nd.y, nd.z,
            )
            Kind.UPPER_SE -> floatArrayOf(
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,

                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,

                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,

                -nd.x, -nd.y, nd.z,
                -nd.x, -nd.y, nd.z,
                -nd.x, -nd.y, nd.z,
            )
            Kind.LOWER_NW -> floatArrayOf(
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
            Kind.LOWER_NE -> floatArrayOf(
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
            Kind.LOWER_SW -> floatArrayOf(
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
            Kind.LOWER_SE -> floatArrayOf(
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
