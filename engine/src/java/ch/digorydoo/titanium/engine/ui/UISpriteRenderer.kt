package ch.digorydoo.titanium.engine.ui

import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.point.Point3f
import ch.digorydoo.titanium.engine.shader.Renderer
import ch.digorydoo.titanium.engine.texture.Texture

abstract class UISpriteRenderer: Renderer {
    abstract class Delegate {
        abstract val frameSize: Point2f // unscaled size of the frame in retro coordinates
        abstract val renderPos: Point3f
        abstract val tex: Texture?
        open val brightness = 1.0f // 0=black, 1=normal, 2=white
        open val opacity = 1.0f // 0=invisible, 1=opaque
        open val rotation = 0.0f // rotation, in Radians
        open val scaleFactor = Point2f(1.0f, 1.0f)
        open val texOffset = Point2f.zero // offset in texture pixels
        open val texScaleFactor = Point2f(1.0f, 1.0f)
    }
}
