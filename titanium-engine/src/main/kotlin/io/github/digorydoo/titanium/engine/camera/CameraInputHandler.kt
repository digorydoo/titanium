package io.github.digorydoo.titanium.engine.camera

import ch.digorydoo.kutils.math.clamp
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.core.GameTime.Companion.DELTA_TIME
import kotlin.math.abs
import kotlin.math.sign

class CameraInputHandler(private val props: CameraProps) {
    private var origPhi = 0f
    private var origRho = 0f
    private var prevMode = CameraInputMode.OFF

    fun handle() {
        if (App.gameMenu.isShown) return

        val mode = props.inputMode
        if (mode == CameraInputMode.OFF) return

        val input = App.input
        val rightJoy = input.rightJoy

        val prefs = App.prefs
        val swapX = if (prefs.swapCameraX) -1.0f else 1.0f
        val swapY = if (prefs.swapCameraY) -1.0f else 1.0f

        when (mode) {
            CameraInputMode.FULLY_CONTROLLABLE -> {
                val factor = prefs.cameraControlsSpeed.speed * DELTA_TIME
                val dphi = rightJoy.x * PHI_SPEED_WHEN_FULLY_CONTROLLABLE * factor * swapX
                val drho = rightJoy.y * RHO_SPEED_WHEN_FULLY_CONTROLLABLE * factor * swapY

                props.phi.desired += dphi
                props.rho.desired = clamp(props.rho.desired + drho, MIN_RHO, MAX_RHO)
            }
            CameraInputMode.CONSTRAINED -> {
                if (prevMode != CameraInputMode.CONSTRAINED) {
                    origPhi = props.phi.desired
                    origRho = props.rho.desired
                }

                props.phi.desired = origPhi + when {
                    abs(rightJoy.x) > 0.5f -> sign(rightJoy.x) * DELTA_WHEN_CONSTRAINED * swapX
                    else -> 0f
                }
                props.rho.desired = origRho + when {
                    abs(rightJoy.y) > 0.5f -> sign(rightJoy.y) * DELTA_WHEN_CONSTRAINED * swapY
                    else -> 0f
                }
            }
        }

        prevMode = mode
    }

    companion object {
        private const val PHI_SPEED_WHEN_FULLY_CONTROLLABLE = 2.5f
        private const val RHO_SPEED_WHEN_FULLY_CONTROLLABLE = 2.0f
        private const val DELTA_WHEN_CONSTRAINED = 0.1f
        private const val MIN_RHO = -3.0f
        private const val MAX_RHO = -0.09f
    }
}
