package ch.digorydoo.titanium.engine.core

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.point.MutablePoint3f
import ch.digorydoo.titanium.engine.camera.Projection
import ch.digorydoo.titanium.engine.scene.Scene
import kotlin.math.cos
import kotlin.math.sin

abstract class ShadowBuffer {
    private val shadowProjection = Projection()
    val projMatrix get() = shadowProjection.matrix

    // If the camera changes only slightly, shadow inaccuracies become very apparent, so we stick to the same camera
    // settings for a number of frames. This has the drawback that the camera may move outside the shadow area, though.
    private class CameraSettings {
        private val checked = FrameCounter.everyNthSecond(5)
        private var scene: Scene? = null
        var phi = 0.0f
        var rho = 0.0f
        val dir = MutablePoint3f()
        val sourcePos = MutablePoint3f()

        fun update() {
            val frame = checked.next()

            if (frame == 0 || scene != App.scene) {
                scene = App.scene
                val camera = App.camera
                phi = camera.currentPhi
                rho = camera.currentRho
                dir.set(camera.currentDir)
                sourcePos.set(camera.sourcePos)
            }
        }
    }

    private val cameraSettings = CameraSettings()
    private val shadowCentre = MutablePoint3f()

    fun prepareProjection() {
        cameraSettings.update()

        val sunDir = App.scene.lighting.sunDir
        val sunPhi = sunDir.phi
        val sunRho = clamp(sunDir.rho, -MAX_SUN_RHO, MAX_SUN_RHO)
        val sunVector = sunDir.vector

        val turnAmount = TURN_SHADOW_AREA_WITH_CAMERA * cos(sunPhi)
        val moveAmount = MOVE_SHADOW_AREA_AWAY_FROM_CAMERA * cos(cameraSettings.rho)
        val cameraPhi = cameraSettings.phi
        val cameraDir = cameraSettings.dir
        val sourcePos = cameraSettings.sourcePos

        shadowCentre.set(
            sourcePos.x +
                MOVE_SHADOW_AREA_TOWARDS_SUN * sunVector.x +
                moveAmount * cameraDir.x +
                turnAmount * cos(cameraPhi),
            sourcePos.y +
                MOVE_SHADOW_AREA_TOWARDS_SUN * sunVector.y +
                moveAmount * cameraDir.y +
                turnAmount * sin(cameraPhi),
            sourcePos.z
        )

        shadowProjection.computeOrthogonal(sunPhi, sunRho, sourcePos = shadowCentre, SHADOW_ZOOM)
    }

    companion object {
        private const val SHADOW_ZOOM = 2.0f // increases the size of the area covered by the ShadowMap
        private const val MOVE_SHADOW_AREA_TOWARDS_SUN = 10.0f
        private const val MOVE_SHADOW_AREA_AWAY_FROM_CAMERA = 10.0f
        private const val TURN_SHADOW_AREA_WITH_CAMERA = 10.0f
        private const val MAX_SUN_RHO = 1.45f // avoid sunbeams becoming too horizontal
    }
}
