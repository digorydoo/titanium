package ch.digorydoo.titanium.main.core

import ch.digorydoo.titanium.engine.core.ProcessManager
import org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose

class ProcessManagerImpl: ProcessManager() {
    override fun exit() {
        glfwSetWindowShouldClose(Main.window, true)
    }
}
