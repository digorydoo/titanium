package io.github.digorydoo.titanium.engine.sprite

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.vector.Vector2f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.shader.Renderer

abstract class UISolidRenderer: Renderer {
    abstract class Delegate {
        abstract val frameSize: Vector2f // unscaled size of the frame in dip
        abstract val renderPos: Vector3f
        open val colour = Colour.white
    }
}
