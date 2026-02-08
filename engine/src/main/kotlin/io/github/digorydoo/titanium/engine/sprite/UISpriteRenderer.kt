package io.github.digorydoo.titanium.engine.sprite

import ch.digorydoo.kutils.vector.Vector2f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.shader.Renderer
import io.github.digorydoo.titanium.engine.texture.Texture

abstract class UISpriteRenderer: Renderer {
    abstract class Delegate {
        abstract val frameSize: Vector2f // unscaled size of the frame in dip
        abstract val renderPos: Vector3f
        abstract val tex: Texture?
        open val brightness = 1.0f // 0=black, 1=normal, 2=white
        open val opacity = 1.0f // 0=invisible, 1=opaque
        open val renderPosIsNormalised = false // false=dp, true=normalised screen coords
        open val rotation = 0.0f // rotation, in Radians
        open val scaleFactor = Vector2f(1.0f, 1.0f)
        open val texOffset = Vector2f.zero // offset in texture pixels
        open val texScaleFactor = Vector2f(1.0f, 1.0f)
    }
}
