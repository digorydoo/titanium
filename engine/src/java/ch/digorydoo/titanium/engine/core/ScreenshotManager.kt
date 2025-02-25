package ch.digorydoo.titanium.engine.core

import ch.digorydoo.titanium.engine.image.ImageData

abstract class ScreenshotManager {
    protected interface Listener {
        fun onScreenshot(screenshot: ImageData)
    }

    protected val listeners = mutableListOf<Listener>()

    fun take(lambda: (screenshot: ImageData) -> Unit) {
        listeners.add(
            object: Listener {
                override fun onScreenshot(screenshot: ImageData) {
                    lambda(screenshot)
                }
            }
        )
    }
}
