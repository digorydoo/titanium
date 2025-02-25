package ch.digorydoo.titanium.engine.behaviours

import ch.digorydoo.kutils.matrix.MutableMatrix4f
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.kutils.point.MutablePoint4f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * This class implements a gel behaviour that turns the gel towards the camera. This can be used in combination with
 * a PaperRenderer to place a 2D sprite in the 3D space.
 */
class TurnTowardsCamera(
    private val delegate: Delegate,
    private val usePosition: Boolean = false, // false=just turn with camera phi and rho; true=more accurate
    private val keepUpright: Boolean = false,
    private val keepBehind: Float = 0.0f, // a value > 0 will move the gel behind the "centre"
): GraphicElement.Behaviour {
    abstract class Delegate {
        open var rotationPhi = 0.0f
        open var rotationRho = 0.0f // will be set when keepUpright is false
        open val centre: Point3f? = null // set this to the gel's pos when keepBehind > 0 or usePosition is true
        val renderPos = MutablePoint3f() // will be updated when keepBehind > 0
    }

    private val matPhi = MutableMatrix4f { 0.0f }
    private val matRho = MutableMatrix4f { 0.0f }
    private val tmpPt1 = MutablePoint4f()
    private val tmpPt2 = MutablePoint4f()

    override fun animate() {
        val centre = delegate.centre
        val phi: Float
        val rho: Float

        if (usePosition) {
            require(centre != null) { "Centre must be set when usePosition is true" }
            val cam = App.camera.sourcePos
            val dx = centre.x - cam.x
            val dy = centre.y - cam.y
            val dz = centre.z - cam.z
            phi = atan2(dy, dx)

            if (keepUpright) {
                rho = 0.0f
            } else {
                val rx = dx * cos(-phi) - dy * sin(-phi)
                rho = atan2(dz, rx)
            }
        } else {
            phi = -App.camera.currentPhi + 0.5f * Math.PI.toFloat()
            rho = if (keepUpright) 0.0f else -App.camera.currentRho + 0.5f * Math.PI.toFloat()
        }

        delegate.rotationPhi = phi
        delegate.rotationRho = rho

        if (keepBehind > 0) {
            require(centre != null) { "Centre must be set when keepBehind > 0" }

            matPhi.setRotationZ(phi)
            matRho.setRotationY(rho)

            tmpPt1.set(keepBehind, 0.0f, 0.0f, 1.0f)
            tmpPt2.setMultiplied(matRho, tmpPt1)
            tmpPt1.setMultiplied(matPhi, tmpPt2)

            delegate.renderPos.set(
                centre.x + tmpPt1.x,
                centre.y + tmpPt1.y,
                centre.z + tmpPt1.z,
            )
        }
    }
}
