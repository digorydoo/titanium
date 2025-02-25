package ch.digorydoo.titanium.engine.camera

import ch.digorydoo.kutils.matrix.Matrix4f
import ch.digorydoo.kutils.matrix.MutableMatrix4f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.camera.CameraProps.Kind
import ch.digorydoo.titanium.engine.core.App.Companion.FIXED_ASPECT_RATIO
import ch.digorydoo.titanium.engine.core.App.Companion.WORLD_TO_GL_FACTOR
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * This class implements a projection matrix. Both perspective and orthogonal projection are supported.
 */
class Projection {
    private val _matrix = MutableMatrix4f()
    val matrix get() = _matrix as Matrix4f

    fun recompute(props: CameraProps) {
        when (props.kind) {
            Kind.PERSPECTIVE -> computePerspective(props.phi.current, props.rho.current, props.sourcePos.current)
            Kind.ORTHOGONAL -> computeOrthogonal(props.phi.current, props.rho.current, props.sourcePos.current)
        }
    }

    private fun computePerspective(phi: Float, rho: Float, sourcePos: Point3f) {
        val sphi = sin(phi)
        val cphi = cos(phi)
        val srho = sin(rho)
        val crho = cos(rho)

        val ys = 1.0 / tan(FOV * 0.5 * PI / 180.0)
        val xs = ys / FIXED_ASPECT_RATIO
        val fn = PERSPECTIVE_Z_FAR - PERSPECTIVE_Z_NEAR
        val p1 = -PERSPECTIVE_Z_FAR / fn
        val p2 = -(PERSPECTIVE_Z_FAR * PERSPECTIVE_Z_NEAR) / fn
        val p3 = -1.0f * WORLD_TO_GL_FACTOR

        val ma = cphi * xs
        val me = sphi * crho * ys
        val mi = sphi * srho * p1
        val mm = sphi * srho * p2

        val mb = -sphi * xs
        val mf = cphi * crho * ys
        val mj = cphi * srho * p1
        val mn = cphi * srho * p2

        val mc = 0.0
        val mg = -srho * ys
        val mk = crho * p1
        val mo = crho * p2

        val md = 0.0
        val mh = 0.0
        val ml = p1 * p3 - 1.0
        val mp = p2 * p3

        // The projection has the eye point behind the projection plane. To prevent from being able to see behind walls,
        // we move the camera a bit forward.

        val cameraDirX = -sphi * srho
        val cameraDirY = -cphi * srho
        val cameraDirZ = -crho

        val ms = WORLD_TO_GL_FACTOR
        val mu = (sourcePos.x + EYE_OFFSET * cameraDirX) * ms
        val mv = (sourcePos.y + EYE_OFFSET * cameraDirY) * ms
        val mw = (sourcePos.z + EYE_OFFSET * cameraDirZ) * ms

        val a = ma * ms
        val b = mb * ms
        val c = mc * ms
        val d = -ma * mu - mb * mv - mc * mw + md
        val e = me * ms
        val f = mf * ms
        val g = mg * ms
        val h = -me * mu - mf * mv - mg * mw + mh
        val i = mi * ms
        val j = mj * ms
        val k = mk * ms
        val l = -mi * mu - mj * mv - mk * mw + ml
        val m = mm * ms
        val n = mn * ms
        val o = mo * ms
        val p = -mm * mu - mn * mv - mo * mw + mp

        // Z_NEAR and Z_FAR have influence on the perspective. To get the clipping planes right, we scale and translate
        // z again after the projection.
        val zscale = 0.05
        val zoff = 0.95

        _matrix.writableBuffer.apply {
            position(0)

            put(a.toFloat())
            put(e.toFloat())
            put((i * zscale + m * zoff).toFloat())
            put(m.toFloat())

            put(b.toFloat())
            put(f.toFloat())
            put((j * zscale + n * zoff).toFloat())
            put(n.toFloat())

            put(c.toFloat())
            put(g.toFloat())
            put((k * zscale + o * zoff).toFloat())
            put(o.toFloat())

            put(d.toFloat())
            put(h.toFloat())
            put((l * zscale + p * zoff).toFloat())
            put(p.toFloat())
        }
    }

    fun computeOrthogonal(phi: Float, rho: Float, sourcePos: Point3f, zoom: Float = 1.0f) {
        val sphi = sin(phi)
        val cphi = cos(phi)
        val srho = sin(rho)
        val crho = cos(rho)

        val sphi2 = sphi * 2.0f
        val srho2 = srho * 2.0f
        val cphi2 = cphi * 2.0f

        val zoomXY = ORTHO_ZOOM_FACTOR * zoom

        val xmin = sourcePos.x - zoomXY
        val xmax = sourcePos.x + zoomXY
        val ymin = sourcePos.y - zoomXY
        val ymax = sourcePos.y + zoomXY
        val zmin = sourcePos.z - ORTHO_ZOOM_FACTOR
        val zmax = sourcePos.z + ORTHO_ZOOM_FACTOR

        val xa = xmax + xmin
        val ya = ymax + ymin
        val za = zmax + zmin

        val xb = xmax - xmin
        val yb = ymax - ymin
        val zb = zmax - zmin

        val xyb = xb * yb
        val xyzb = xyb * zb
        val xayb = xa * yb
        val xbya = xb * ya

        val ar = FIXED_ASPECT_RATIO
        val arcrho = ar * crho
        val zbarcrho = zb * arcrho

        val p = -(ORTHO_Z_FAR + ORTHO_Z_NEAR) / 2.0f * WORLD_TO_GL_FACTOR
        val q = 2.0f / ((ORTHO_Z_FAR - ORTHO_Z_NEAR) * WORLD_TO_GL_FACTOR)

        _matrix.writableBuffer.apply {
            position(0)
            put(cphi2 / xb)
            put((arcrho * sphi2) / xb)
            put(-(q * srho * sphi2) / xb)
            put(0.0f)

            put(-sphi2 / yb)
            put((arcrho * cphi2) / yb)
            put(-(q * srho2 * cphi) / yb)
            put(0.0f)

            put(0.0f)
            put(-(ar * srho2) / zb)
            put(-(q * crho * 2.0f) / zb)
            put(0.0f)

            put((xbya * sphi - xayb * cphi) / xyb)
            put((xyb * za * ar * srho - xbya * zbarcrho * cphi - xayb * zbarcrho * sphi) / xyzb)
            put((xayb * zb * q * srho * sphi + xbya * zb * q * srho * cphi + xyb * za * q * crho + xyzb * q * p) / xyzb)
            put(1.0f)
        }
    }

    companion object {
        private const val PERSPECTIVE_Z_NEAR = 1.0
        private const val PERSPECTIVE_Z_FAR = 30.0
        private const val FOV = 55.0 // degrees
        private const val EYE_OFFSET = 1.0f // metres

        private const val ORTHO_ZOOM_FACTOR = WORLD_TO_GL_FACTOR * 150.0f // zoom out to make up for no perspective
        private const val ORTHO_Z_NEAR = -28.0f // maps to GL z = -1
        private const val ORTHO_Z_FAR = 99.0f // maps to GL z = 1
    }
}
