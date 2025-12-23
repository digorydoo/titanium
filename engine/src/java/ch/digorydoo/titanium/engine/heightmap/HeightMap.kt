package ch.digorydoo.titanium.engine.heightmap

import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.newFloatBuffer
import ch.digorydoo.titanium.engine.mesh.MeshMaterial
import ch.digorydoo.titanium.engine.mesh.SimpleMesh
import ch.digorydoo.titanium.engine.texture.Texture
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

class HeightMap(
    val filename: String,
    val numSamplesX: Int,
    val numSamplesY: Int,
    var xsize: Float,
    var ysize: Float,
    val zvalues: FloatBuffer,
    val material: MeshMaterial,
) {
    class TriangulatedHeightMap(val positions: FloatBuffer, val normals: FloatBuffer, val texCoords: FloatBuffer)

    init {
        require(numSamplesX > 1)
        require(numSamplesY > 1)
        require(zvalues.limit() == numSamplesX * numSamplesY)
    }

    var mesh: SimpleMesh? = null; private set

    fun getAt(x: Int, y: Int): Float =
        zvalues.get(x + numSamplesX * y)

    fun setAt(x: Int, y: Int, z: Float) {
        zvalues.put(x + numSamplesX * y, z)
    }

    fun setMesh(triangulated: TriangulatedHeightMap, tex: Texture?) {
        mesh = SimpleMesh(triangulated.positions, triangulated.normals, tex, triangulated.texCoords, material)
    }

    fun getWorldCoords(x: Int, y: Int, centre: Point3f, rotationPhi: Float, worldCoords: MutablePoint3f) {
        require(x >= 0 && x < numSamplesX)
        require(y >= 0 && y < numSamplesY)
        val height = zvalues.get(x + numSamplesX * y)

        val wx = centre.x + (x.toFloat() / (numSamplesX - 1) - 0.5f) * xsize
        val wy = centre.y + (y.toFloat() / (numSamplesY - 1) - 0.5f) * ysize

        val cphi = cos(rotationPhi)
        val sphi = sin(rotationPhi)

        worldCoords.set(
            cphi * wx - sphi * wy,
            sphi * wx + cphi * wy,
            centre.z + height,
        )
    }

    /**
     * May be called from a coroutine!
     * FIXME inefficient! use triangle strips instead of triangle array!
     */
    fun triangulate(smooth: Boolean): TriangulatedHeightMap {
        // positions have 2 triangles à 3 pts à 3 components, normals are always per position
        val numPos = (numSamplesX - 1) * (numSamplesY - 1) * 2 * 3 * 3
        val positions = newFloatBuffer(numPos).apply { position(0) }
        val normals = newFloatBuffer(numPos).apply { position(0) }

        when {
            smooth -> triangulateSmooth(positions, normals)
            else -> triangulateFlat(positions, normals)
        }

        // Triangulation is expected to write the entire buffer
        require(positions.position() == numPos)
        require(normals.position() == numPos)

        return TriangulatedHeightMap(positions, normals, computeTexCoords())
    }

    private fun triangulateFlat(positions: FloatBuffer, normals: FloatBuffer) {
        val p0 = MutablePoint3f()
        val p1 = MutablePoint3f()
        val p2 = MutablePoint3f()
        val p3 = MutablePoint3f()

        val n = MutablePoint3f()
        val lastX = numSamplesX - 1
        val lastY = numSamplesY - 1
        var y1 = -0.5f * ysize // initial value for outer loop

        for (y in 0 ..< lastY) {
            val y0 = y1 // value from previous iteration
            y1 = ((y + 1).toFloat() / lastY - 0.5f) * ysize
            var x1 = -0.5f * xsize // initial value for inner loop

            for (x in 0 ..< lastX) {
                val x0 = x1 // value from previous iteration
                x1 = ((x + 1).toFloat() / lastX - 0.5f) * xsize

                p0.set(x0, y0, zvalues.get(x + numSamplesX * y))
                p1.set(x1, y0, zvalues.get(x + 1 + numSamplesX * y))
                p2.set(x0, y1, zvalues.get(x + numSamplesX * (y + 1)))
                p3.set(x1, y1, zvalues.get(x + 1 + numSamplesX * (y + 1)))

                positions.apply {
                    // Triangle 1
                    put(p0.x); put(p0.y); put(p0.z)
                    put(p1.x); put(p1.y); put(p1.z)
                    put(p2.x); put(p2.y); put(p2.z)

                    // Triangle 2
                    put(p1.x); put(p1.y); put(p1.z)
                    put(p3.x); put(p3.y); put(p3.z)
                    put(p2.x); put(p2.y); put(p2.z)
                }

                // Triangle 1
                n.setToNormal(p0, p1, p2)
                normals.apply {
                    put(n.x); put(n.y); put(n.z)
                    put(n.x); put(n.y); put(n.z)
                    put(n.x); put(n.y); put(n.z)
                }

                // Triangle 2
                n.setToNormal(p1, p3, p2)
                normals.apply {
                    put(n.x); put(n.y); put(n.z)
                    put(n.x); put(n.y); put(n.z)
                    put(n.x); put(n.y); put(n.z)
                }
            }
        }
    }

    fun triangulateSmooth(positions: FloatBuffer, normals: FloatBuffer) {
        val p0 = MutablePoint3f()
        val p1 = MutablePoint3f()
        val p2 = MutablePoint3f()
        val p3 = MutablePoint3f()

        // We cumulate the normals from all triangles that use that position, then normalise, and emit the normals in
        // a second pass. If this turns out to be too inefficient for large height maps, we could do it in a single
        // pass that does not require an extra array, by using:
        //    ∆x := xsize / (numSamplesX - 1)
        //    ∆y := ysize / (numSamplesY - 1)
        //    ∂z/∂x = z(x + 1, y) - z(x - 1, y) / 2∆x
        //    ∂z/∂y = z(x, y + 1) - z(x, y - 1) / 2∆y
        //    n ≈ normalise(-∂z/∂x, -∂z/∂y, 1)
        // But it isn't clear to me if this would be accurate enough.

        val normalsArr = Array(numSamplesX) { Array(numSamplesY) { MutablePoint3f() } }

        val n = MutablePoint3f()
        val lastX = numSamplesX - 1
        val lastY = numSamplesY - 1
        var y1 = -0.5f * ysize // initial value for outer loop

        for (y in 0 ..< lastY) {
            val y0 = y1 // value from previous iteration
            y1 = ((y + 1).toFloat() / lastY - 0.5f) * ysize
            var x1 = -0.5f * xsize // initial value for inner loop

            for (x in 0 ..< lastX) {
                val x0 = x1 // value from previous iteration
                x1 = ((x + 1).toFloat() / lastX - 0.5f) * xsize

                p0.set(x0, y0, zvalues.get(x + numSamplesX * y))
                p1.set(x1, y0, zvalues.get(x + 1 + numSamplesX * y))
                p2.set(x0, y1, zvalues.get(x + numSamplesX * (y + 1)))
                p3.set(x1, y1, zvalues.get(x + 1 + numSamplesX * (y + 1)))

                positions.apply {
                    // Triangle 1
                    put(p0.x); put(p0.y); put(p0.z)
                    put(p1.x); put(p1.y); put(p1.z)
                    put(p2.x); put(p2.y); put(p2.z)

                    // Triangle 2
                    put(p1.x); put(p1.y); put(p1.z)
                    put(p3.x); put(p3.y); put(p3.z)
                    put(p2.x); put(p2.y); put(p2.z)
                }

                val n0 = normalsArr[x][y]
                val n1 = normalsArr[x + 1][y]
                val n2 = normalsArr[x][y + 1]
                val n3 = normalsArr[x + 1][y + 1]

                // Triangle 1
                n.setToNormal(p0, p1, p2)
                n0.add(n)
                n1.add(n)
                n2.add(n)

                // Triangle 2
                n.setToNormal(p1, p3, p2)
                n1.add(n)
                n3.add(n)
                n2.add(n)
            }
        }

        // Normal data was added from multiple triangles, so we need to re-normalise
        normalsArr.forEach { arr ->
            arr.forEach { it.normalize() }
        }

        for (y in 0 ..< lastY) {
            for (x in 0 ..< lastX) {
                val n0 = normalsArr[x][y]
                val n1 = normalsArr[x + 1][y]
                val n2 = normalsArr[x][y + 1]
                val n3 = normalsArr[x + 1][y + 1]

                normals.apply {
                    put(n0.x); put(n0.y); put(n0.z)
                    put(n1.x); put(n1.y); put(n1.z)
                    put(n2.x); put(n2.y); put(n2.z)
                }
                normals.apply {
                    put(n1.x); put(n1.y); put(n1.z)
                    put(n3.x); put(n3.y); put(n3.z)
                    put(n2.x); put(n2.y); put(n2.z)
                }
            }
        }
    }

    private fun computeTexCoords(): FloatBuffer {
        // texCoords have 2 triangles à 3 pts à 2 components
        val texCoords = newFloatBuffer((numSamplesX - 1) * (numSamplesY - 1) * 2 * 3 * 2).apply { position(0) }

        var rel0x: Float
        var rel0y: Float
        var rel1x: Float
        var rel1y = 0.0f // becomes the previous value of the first iteration

        for (y in 0 ..< numSamplesY - 1) {
            rel0y = rel1y // previous value
            rel1y = (y + 1).toFloat() / (numSamplesY - 1)
            rel1x = 0.0f

            for (x in 0 ..< numSamplesX - 1) {
                rel0x = rel1x // previous value
                rel1x = (x + 1).toFloat() / (numSamplesX - 1)

                texCoords.apply {
                    // Triangle 1
                    put(rel0x); put(rel0y)
                    put(rel0x); put(rel1y)
                    put(rel1x); put(rel0y)

                    // Triangle 2
                    put(rel1x); put(rel0y)
                    put(rel0x); put(rel1y)
                    put(rel1x); put(rel1y)
                }
            }
        }

        require(texCoords.position() == texCoords.limit())
        return texCoords
    }
}
