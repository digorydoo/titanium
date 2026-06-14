package io.github.digorydoo.titanium.engine.camera

enum class CameraInertia(
    val id: Int,
    val sourceAccel: Float,
    val sourceNormalBrake: Float,
    val sourceStrongBrake: Float,
    val targetAccel: Float,
    val targetNormalBrake: Float,
    val targetStrongBrake: Float,
    val angleAccel: Float,
    val angleNormalBrake: Float,
    val angleStrongBrake: Float,
    val distanceAccel: Float,
    val distanceNormalBrake: Float,
    val distanceStrongBrake: Float,
) {
    NORMAL(
        id = 1,
        sourceAccel = 1.5f,
        sourceNormalBrake = 0.25f,
        sourceStrongBrake = 0.30f,
        targetAccel = 0.8f,
        targetNormalBrake = 0.20f,
        targetStrongBrake = 0.25f,
        angleAccel = 2.9f,
        angleNormalBrake = 0.25f,
        angleStrongBrake = 0.30f,
        distanceAccel = 0.5f,
        distanceNormalBrake = 0.25f,
        distanceStrongBrake = 0.30f,
    ),
    HIGH(
        id = 2,
        sourceAccel = 0.17f,
        sourceNormalBrake = 0.10f,
        sourceStrongBrake = 0.15f,
        targetAccel = 0.12f,
        targetNormalBrake = 0.10f,
        targetStrongBrake = 0.15f,
        angleAccel = 0.42f,
        angleNormalBrake = 0.15f,
        angleStrongBrake = 0.20f,
        distanceAccel = 0.12f,
        distanceNormalBrake = 0.10f,
        distanceStrongBrake = 0.15f,
    );

    companion object {
        fun fromIntOrNull(id: Int) =
            entries.find { it.id == id }
    }
}
