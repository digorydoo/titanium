package ch.digorydoo.titanium.main.opengl

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.GL_INVALID_FRAMEBUFFER_OPERATION

fun checkGLError(getCtx: (() -> String)? = null) {
    val err = glGetError()

    if (err != GL_NO_ERROR) {
        throw Exception("${glErrorToString(err)} ${if (getCtx == null) "" else " (${getCtx()})"}")
    }
}

private fun glErrorToString(err: Int) = when (err) {
    GL_INVALID_ENUM -> "GL_INVALID_ENUM"
    GL_INVALID_VALUE -> "GL_INVALID_VALUE"
    GL_INVALID_OPERATION -> "GL_INVALID_OPERATION"
    GL_STACK_OVERFLOW -> "GL_STACK_OVERFLOW"
    GL_STACK_UNDERFLOW -> "GL_STACK_UNDERFLOW"
    GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY"
    GL_INVALID_FRAMEBUFFER_OPERATION -> "GL_INVALID_FRAMEBUFFER_OPERATION"
    else -> "GL error $err (unmapped in MyGLUtils.kt)"
}
