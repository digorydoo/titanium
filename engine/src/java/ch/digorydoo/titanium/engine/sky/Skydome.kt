package ch.digorydoo.titanium.engine.sky

import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.kutils.utils.toFloatBuffer
import ch.digorydoo.titanium.engine.core.App
import java.nio.FloatBuffer

/**
 * If texture should be added to Skydome, use cubemaps:
 * https://ogldev.org/www/tutorial25/tutorial25.html
 * https://learnopengl.com/Advanced-OpenGL/Cubemaps
 */
class Skydome {
    private class Triangle(val v1: Int, val v2: Int, val v3: Int)

    private val triangles = arrayOf(
        // downside
        Triangle(0, 3, 1),
        Triangle(1, 3, 2),

        // lower strip
        Triangle(0, 16, 4),
        Triangle(16, 0, 1),
        Triangle(16, 1, 5),
        Triangle(5, 1, 17),
        Triangle(17, 1, 2),
        Triangle(17, 2, 6),
        Triangle(6, 2, 18),
        Triangle(18, 2, 3),
        Triangle(18, 3, 7),
        Triangle(7, 3, 19),
        Triangle(19, 3, 0),
        Triangle(19, 0, 4),

        // upper strip
        Triangle(8, 4, 16),
        Triangle(9, 8, 16),
        Triangle(9, 16, 5),
        Triangle(5, 10, 9),
        Triangle(5, 17, 10),
        Triangle(10, 17, 11),
        Triangle(11, 17, 6),
        Triangle(11, 6, 12),
        Triangle(12, 6, 18),
        Triangle(12, 18, 13),
        Triangle(13, 18, 7),
        Triangle(13, 7, 14),
        Triangle(14, 7, 19),
        Triangle(14, 19, 15),
        Triangle(15, 19, 4),
        Triangle(15, 4, 8),

        // upside
        Triangle(20, 8, 9),
        Triangle(20, 9, 10),
        Triangle(20, 10, 11),
        Triangle(20, 11, 12),
        Triangle(20, 12, 13),
        Triangle(20, 13, 14),
        Triangle(20, 14, 15),
        Triangle(20, 15, 8),
    )

    private val positions: FloatBuffer = run {
        val list = mutableListOf<Float>()
        triangles.forEach { tr ->
            val pt1 = cubePts[tr.v1]
            val pt2 = cubePts[tr.v2]
            val pt3 = cubePts[tr.v3]
            list.add(pt1.x)
            list.add(pt1.y)
            list.add(pt1.z)
            list.add(pt2.x)
            list.add(pt2.y)
            list.add(pt2.z)
            list.add(pt3.x)
            list.add(pt3.y)
            list.add(pt3.z)
        }
        list.toFloatBuffer()
    }

    private val renderer = App.factory.createSkydomeRenderer(
        object: SkydomeRenderer.Delegate {
            override val positions get() = this@Skydome.positions
            override val scaleFactor = SKYDOME_SCALE_FACTOR
        }
    )

    fun render() {
        renderer.renderSolid()
    }

    fun free() {
        renderer.free()
    }

    companion object {
        // The box can be quite small, because the renderer always puts it behind all models.
        private const val SKYDOME_SCALE_FACTOR = 20.0f
        private const val Z4 = 0.5f
        private const val X9 = 0.42f
        private val cubePts = arrayOf(
            Point3f(-1.0f, -1.0f, -1.0f), // 0
            Point3f(1.0f, -1.0f, -1.0f), // 1
            Point3f(1.0f, 1.0f, -1.0f), // 2
            Point3f(-1.0f, 1.0f, -1.0f), // 3
            Point3f(-1.0f, -1.0f, Z4), // 4
            Point3f(1.0f, -1.0f, Z4), // 5
            Point3f(1.0f, 1.0f, Z4), // 6
            Point3f(-1.0f, 1.0f, Z4), // 7
            Point3f(-X9, -1.0f, 1.0f), // 8
            Point3f(X9, -1.0f, 1.0f), // 9
            Point3f(1.0f, -X9, 1.0f), // 10
            Point3f(1.0f, X9, 1.0f), // 11
            Point3f(X9, 1.0f, 1.0f), // 12
            Point3f(-X9, 1.0f, 1.0f), // 13
            Point3f(-1.0f, X9, 1.0f), // 14
            Point3f(-1.0f, -X9, 1.0f), // 15
            Point3f(0.0f, -1.0f, Z4), // 16
            Point3f(1.0f, 0.0f, Z4), // 17
            Point3f(0.0f, 1.0f, Z4), // 18
            Point3f(-1.0f, 0.0f, Z4), // 19
            Point3f(0.0f, 0.0f, 1.0f), // 20
        )
    }
}
