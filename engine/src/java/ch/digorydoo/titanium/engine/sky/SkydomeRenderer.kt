package ch.digorydoo.titanium.engine.sky

import ch.digorydoo.titanium.engine.shader.Renderer
import java.nio.FloatBuffer

abstract class SkydomeRenderer: Renderer {
    interface Delegate {
        val positions: FloatBuffer
        val scaleFactor: Float
    }
}
