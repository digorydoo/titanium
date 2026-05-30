package io.github.digorydoo.titanium.engine.sky

import io.github.digorydoo.titanium.engine.shader.Renderer
import java.nio.FloatBuffer

abstract class SkydomeRenderer: Renderer {
    interface Delegate {
        val positions: FloatBuffer
        val scaleFactor: Float
    }
}
