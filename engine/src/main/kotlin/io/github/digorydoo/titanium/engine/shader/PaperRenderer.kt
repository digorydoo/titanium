package io.github.digorydoo.titanium.engine.shader

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.vector.Vector2f
import ch.digorydoo.kutils.vector.Vector3f
import io.github.digorydoo.titanium.engine.texture.Texture

abstract class PaperRenderer: Renderer {
    abstract class Delegate {
        abstract val frameSize: Vector2f // unscaled size of the frame in retro coordinates
        abstract val renderPos: Vector3f
        abstract val tex: Texture?
        open val opacity = 1.0f // 1=opaque; currently only implemented for BlendMode.ADD
        open val origin = Vector2f.zero // origin of hotspot in tex coordinates
        open val rotationPhi = 0.0f // rotation around Z-axis, in Radians
        open val rotationRho = 0.0f // rotation around Y-axis (2nd rotation), in Radians
        open val scaleFactor = Vector2f(1.0f, 1.0f)
        open val texOffset = Vector2f.zero // offset in texture pixels
        open val multColour = Colour.white // will be multiplied to texture
    }
}
