package ch.digorydoo.titanium.engine.shader

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.texture.Texture

abstract class PaperRenderer: Renderer {
    abstract class Delegate {
        abstract val frameSize: Point2f // unscaled size of the frame in retro coordinates
        abstract val renderPos: Point3f
        abstract val tex: Texture?
        open val opacity = 1.0f // 1=opaque; currently only implemented for BlendMode.ADD
        open val origin = Point2f() // origin of hotspot in tex coordinates
        open val rotationPhi = 0.0f // rotation around Z-axis, in Radians
        open val rotationRho = 0.0f // rotation around Y-axis (2nd rotation), in Radians
        open val scaleFactor = Point2f(1.0f, 1.0f)
        open val texOffset = Point2f() // offset in texture pixels
        open val multColour = Colour.white // will be multiplied to texture
    }
}
