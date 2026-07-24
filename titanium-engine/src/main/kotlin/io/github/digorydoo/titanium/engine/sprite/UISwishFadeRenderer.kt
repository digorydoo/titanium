package io.github.digorydoo.titanium.engine.sprite

import ch.digorydoo.kutils.vector.Vector2f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.shader.Renderer

abstract class UISwishFadeRenderer: Renderer {
    abstract class Delegate {
        abstract val frameSize: Vector2f // unscaled size of the frame in dip
        abstract val renderPos: Vector3f
        abstract val progress: Float // 0..1
    }
}
