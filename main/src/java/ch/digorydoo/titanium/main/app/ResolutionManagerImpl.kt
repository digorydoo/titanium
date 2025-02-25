package ch.digorydoo.titanium.main.app

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.kutils.point.MutablePoint2i
import ch.digorydoo.kutils.point.Point2f
import ch.digorydoo.kutils.point.Point2i
import ch.digorydoo.kutils.rect.MutableRecti
import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.kutils.string.toPrecision
import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.core.App.Companion.FIXED_ASPECT_RATIO
import ch.digorydoo.titanium.engine.core.App.Companion.MILLIMETRES_PER_INCH
import ch.digorydoo.titanium.engine.core.ResolutionManager
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.opengl.GL30.glViewport
import kotlin.math.max

class ResolutionManagerImpl: ResolutionManager() {
    class MonitorWithPtr(name: String, val ptr: Long): Monitor(name)
    class MonitorAndResolution(val monitor: Long, val res: Resolution)

    var physicalAspectRatio = 1.0f; private set // aspect ratio of monitor or window

    val screenSizeDp: Point2i get() = _screenSizeDp
    private val _screenSizeDp = MutablePoint2i()

    val dpToGlFactor: Point2f get() = _dpToGlFactor
    private val _dpToGlFactor = MutablePoint2f()

    val viewPort: Recti get() = _viewPort
    private val _viewPort = MutableRecti()

    private var windowWidth = 0
    private var windowHeight = 0
    private var windowSizeIgnoreArgs = true
    private var origVideoMode: GLFWVidMode? = null
    private var monitorOfOrigVideoMode = 0L

    fun setFullscreenMode(enable: Boolean) {
        if (enable) {
            setFullscreenAndUpdatePrefs()
        } else {
            setWindowModeAndUpdatePrefs()
        }
    }

    override fun setWindowModeAndUpdatePrefs() {
        Log.info("Entering window mode")

        val app = App.singleton as AppImpl
        val window = app.window

        // Unfortunately, GLFW cannot set the monitor of a non-fullscreen window. We will base our default width
        // and height on the primary monitor, hoping that the window will get placed there.

        val monitor = glfwGetPrimaryMonitor()

        val videoMode = when (monitor == monitorOfOrigVideoMode) {
            true -> origVideoMode // monitor is currently in full-screen
            false -> glfwGetVideoMode(monitor)
        }

        val screenWidth: Int
        val screenHeight: Int

        if (videoMode == null) {
            Log.warn("Cannot determine resolution of primary monitor! Falling back to defaults")
            screenWidth = 640
            screenHeight = 480
        } else {
            screenWidth = videoMode.width()
            screenHeight = videoMode.height()
        }

        windowWidth = (screenWidth * 0.75f).toInt()
        windowHeight = (windowWidth / FIXED_ASPECT_RATIO).toInt()
        windowSizeIgnoreArgs = true // callback should ignore args and use the above instead

        val left = (screenWidth - windowWidth) / 2
        val top = (screenHeight - windowHeight) / 2
        glfwSetWindowMonitor(window, 0L, left, top, windowWidth, windowHeight, GLFW_DONT_CARE)

        app.prefs.fullscreen = false
        app.onEnterWindowMode()
    }

    /**
     * Figures out what monitor and resolution to use, then enters full-screen mode. If prefs.autoPickMonitorAndRes is
     * true, it tries to use the monitor and resolution from prefs, otherwise it tries to determine both automatically.
     * If window is still 0L, we're at init time.
     */
    override fun setFullscreenAndUpdatePrefs() {
        val app = App.singleton as AppImpl
        val window = app.window

        val monAndRes = when {
            window == 0L -> pickMonitorAndResolutionForFullscreen(0L, null)
            else -> pickMonitorAndResolutionForFullscreen(monitorOfOrigVideoMode, origVideoMode)
        }

        setFullscreenAndUpdatePrefs(monAndRes.monitor, monAndRes.res)
    }

    override fun setFullscreenAndUpdatePrefs(monitor: Monitor, resolution: Resolution) {
        val ptr = (monitor as? MonitorWithPtr)?.ptr ?: 0L
        setFullscreenAndUpdatePrefs(ptr, resolution)
    }

    /**
     * Directly enters full-screen mode for a given monitor and resolution.
     */
    private fun setFullscreenAndUpdatePrefs(monitor: Long, resolution: Resolution) {
        val app = App.singleton as AppImpl
        val window = app.window

        val resX = resolution.numPixelsX
        val resY = resolution.numPixelsY
        Log.info("Entering fullscreen: monitor=$monitor, res=${resX}x${resY}")

        if (monitor != monitorOfOrigVideoMode) {
            origVideoMode = glfwGetVideoMode(monitor)
            monitorOfOrigVideoMode = monitor
            Log.info("Original resolution was ${origVideoMode?.width()}x${origVideoMode?.height()}")
        }

        windowWidth = resX
        windowHeight = resY
        windowSizeIgnoreArgs = true // callback should ignore args and use the above instead

        glfwSetWindowMonitor(window, monitor, 0, 0, resX, resY, GLFW_DONT_CARE)

        app.prefs.fullscreen = true
        app.prefs.nameOfMonitor = glfwGetMonitorName(monitor) ?: ""
        app.prefs.fullscreenResX = resolution.numPixelsX
        app.prefs.fullscreenResY = resolution.numPixelsY

        app.onEnterFullscreen()
    }

    fun onFramebufferSize(widthArg: Int, heightArg: Int) {
        // The arguments width and height may be wrong when this callback is called the
        // first time. (They're wrong on my MacBook Pro, they're correct on my Mac Mini.)
        // So, we use windowWidth and windowHeight instead initially.

        val fbWidth = if (windowSizeIgnoreArgs) windowWidth else widthArg
        val fbHeight = if (windowSizeIgnoreArgs) windowHeight else heightArg
        windowSizeIgnoreArgs = false // use the arguments in subsequent window sizing events

        Log.info("onFramebufferSize: fbWidth=$fbWidth, fbHeight=$fbHeight")
        updateViewport(fbWidth, fbHeight)
    }

    private fun updateViewport() {
        val app = App.singleton as AppImpl
        val tmp1 = IntArray(size = 1)
        val tmp2 = IntArray(size = 1)
        glfwGetFramebufferSize(app.window, tmp1, tmp2)
        updateViewport(tmp1[0], tmp2[0])
    }

    private fun updateViewport(fbWidth: Int, fbHeight: Int) {
        val app = App.singleton as AppImpl
        val window = app.window

        val fbAspectRatio = fbWidth.toFloat() / fbHeight
        Log.info("FB is 16:${(16 / fbAspectRatio).toPrecision(1)}")

        val dipSizeX: Int
        val dipSizeY: Int

        val monitor = glfwGetWindowMonitor(window)

        if (monitor == 0L) {
            // This indicates that the window is not full-screen.
            dipSizeX = 1 // fixed, because window may overlap multiple screens
            dipSizeY = 1
            physicalAspectRatio = fbAspectRatio // there is no real physical device in window mode
        } else {
            val tmp1 = IntArray(size = 1)
            val tmp2 = IntArray(size = 1)
            glfwGetMonitorPhysicalSize(monitor, tmp1, tmp2)

            val monMmSizeX = tmp1[0] // in millimetres
            val monMmSizeY = tmp2[0]
            Log.info("Monitor mm size reported as ${monMmSizeX}mm x ${monMmSizeY}mm")

            physicalAspectRatio = monMmSizeX.toFloat() / monMmSizeY
            Log.info("Monitor is 16:${(16 / physicalAspectRatio).toPrecision(1)}")

            if (app.prefs.scaleUI) {
                val mode = glfwGetVideoMode(monitor)
                val monPxSizeX = mode?.width() ?: 0
                val monPxSizeY = mode?.height() ?: 0
                Log.info("Video mode px size reported as ${monPxSizeX}px x ${monPxSizeY}px")

                val dpix = if (monMmSizeX > 0) monPxSizeX.toFloat() * MILLIMETRES_PER_INCH / monMmSizeX else 72.0f
                val dpiy = if (monMmSizeY > 0) monPxSizeY.toFloat() * MILLIMETRES_PER_INCH / monMmSizeY else 72.0f
                Log.info("Estimated dpi is $dpix x $dpiy")

                // We define dipSize as the number of device pixels of the width of a square 1x1 in 96dpi, rounded down to a
                // full number of device pixels, and never less than 1.
                // My old EIZO:     90 dpi -> 1 dip = 1 px
                // My new Samsung: 109 dpi -> 1 dip = 1 px
                // My MacBook Pro: 227 dpi -> 1 dip = 2 px
                dipSizeX = max(1, (dpix / 96.0f).toInt())
                dipSizeY = max(1, (dpiy / 96.0f).toInt())
                Log.info("Using a dip-size of ($dipSizeX, $dipSizeY) device pixels")
            } else {
                Log.info("Using a fixed dip-size of 1x1")
                dipSizeX = 1
                dipSizeY = 1
            }
        }

        val contentWidth: Int
        val contentHeight: Int
        val widthInDip: Int
        val heightInDip: Int

        if (app.prefs.stretchViewport) {
            contentWidth = fbWidth
            contentHeight = fbHeight
            _viewPort.set(0, 0, contentWidth, fbHeight)
            glViewport(_viewPort.left, _viewPort.top, _viewPort.width, _viewPort.height)
            widthInDip = (contentWidth.toFloat() / dipSizeX).toInt()
            heightInDip = (contentHeight.toFloat() / dipSizeY).toInt()
        } else {
            /*
             * NOTE: The following algorithm assumes the monitor scales the resolution to its full physical size. This
             * is not always true. If I choose 640x480 (4:3) as the resolution, my Samsung scales it to full width; but
             * if I choose 1280x960 (4:3 as well), it fits it into its physical size. The algorithm below works
             * correctly for the case of 640x480: Since the scaled image appears as 16:9 (the physical aspect ratio of
             * the monitor), it correctly adds a black margin to the left and right to make it appear as
             * FIXED_ASPECT_RATIO. However, the algorithm fails with 1280x960, incorrectly assuming it appears 16:9
             * while it appears as 4:3, and adds stripes to make it appear even narrower. There is a
             * glfwGetMonitorContentScale, but it always seems to return 1:1. There is now a prefs.stretchViewport to
             * address this problem.
             *
             * Also note that while some other games also use the black margin technique, there are games that support
             * ultra-wide monitors full-screen, making use of the additional width. However, properly supporting this
             * requires lots of testing, because the additional width may reveal stuff that should not be seen. So I'll
             * probably stick with FIXED_ASPECT_RATIO.
             *
             * If you make any changes to this algorithm, be sure to fix ScreenshotManagerImpl as well, because there
             * is a similar algorithm that trims the screenshot to remove the black margin.
             *
             * Three aspect ratios are involved:
             *    - fbAspectRatio: The aspect ratio of the resolution that was chosen, e.g. 4:3 for 640x480
             *    - physAspectRatio: The monitor's physical aspect ratio, e.g. 16:9 for my Samsung
             *    - FIXED_ASPECT_RATIO: The desired aspect ratio of the viewport
             *
             * relRatio will be 1 if the resolution's aspect ratio and the physical aspect ratio are the same,
             * which is always true in window mode.
             */

            val relRatio =
                if (fbAspectRatio < physicalAspectRatio) {
                    physicalAspectRatio / fbAspectRatio
                } else {
                    fbAspectRatio / physicalAspectRatio
                }

            if (fbAspectRatio * relRatio > FIXED_ASPECT_RATIO) {
                contentWidth = (fbHeight * FIXED_ASPECT_RATIO / relRatio).toInt()
                contentHeight = fbHeight
                val gap = fbWidth - contentWidth
                _viewPort.set(gap / 2, 0, gap / 2 + contentWidth, fbHeight)
                glViewport(_viewPort.left, _viewPort.top, _viewPort.width, _viewPort.height)
            } else {
                contentWidth = fbWidth
                contentHeight = (fbWidth / FIXED_ASPECT_RATIO * relRatio).toInt()
                val gap = fbHeight - contentHeight
                _viewPort.set(0, gap / 2, fbWidth, gap / 2 + contentHeight)
                glViewport(_viewPort.left, _viewPort.top, _viewPort.width, _viewPort.height)
            }

            if (relRatio < 1.0f) {
                widthInDip = (contentWidth.toFloat() / relRatio / dipSizeX).toInt()
                heightInDip = (contentHeight.toFloat() / dipSizeY).toInt()
            } else {
                widthInDip = (contentWidth.toFloat() / dipSizeX).toInt()
                heightInDip = (contentHeight.toFloat() / relRatio / dipSizeY).toInt()
            }
        }

        _screenSizeDp.set(widthInDip, heightInDip)
        _dpToGlFactor.x = 2.0f / widthInDip // in GL, screen width is 2 (-1..+1)
        _dpToGlFactor.y = 2.0f / heightInDip

        Log.info("Content size: (${contentWidth}px, ${contentHeight}px)=(${widthInDip}dp, ${heightInDip}dp)")
        app.onViewportUpdated()
    }

    override fun setAutoPickMonitorAndResAndUpdatePrefs(auto: Boolean) {
        val app = App.singleton as AppImpl
        app.prefs.autoPickMonitorAndRes = auto

        if (auto) {
            setFullscreenMode(app.prefs.fullscreen)
        }
    }

    override fun setScaleUIAndUpdatePrefs(scale: Boolean) {
        val app = App.singleton as AppImpl
        app.prefs.scaleUI = scale
        updateViewport() // recomputes dipSize as a side-effect
    }

    override fun setStretchViewportAndUpdatePrefs(stretch: Boolean) {
        val app = App.singleton as AppImpl
        app.prefs.stretchViewport = stretch
        updateViewport()
    }

    private fun pickMonitorAndResolutionForFullscreen(
        monitorCurrentlyFullscreen: Long,
        origVideoModeOfMonitorCurrentlyFullscreen: GLFWVidMode?,
    ): MonitorAndResolution {
        val list = mutableListOf<MonitorAndResolution>()
        val availableMonitors = getAvailableMonitors()

        if (!App.prefs.autoPickMonitorAndRes) {
            val prefMonitor = App.prefs.nameOfMonitor
            val resX = App.prefs.fullscreenResX
            val resY = App.prefs.fullscreenResY

            if (resX > 0 && resY > 0 && prefMonitor.isNotEmpty()) {
                // Try if we find the monitor and resolution from prefs

                var mon = availableMonitors.find { it.name == prefMonitor }?.ptr

                if (mon != null) {
                    Log.info("Monitor from prefs found: $prefMonitor")
                } else {
                    Log.info("None of connected monitors matches prefs: $prefMonitor")
                    mon = glfwGetPrimaryMonitor() // try the primary monitor instead
                }

                val res = getAvailableResolutions(mon).find { it.numPixelsX == resX && it.numPixelsY == resY }

                if (res != null) {
                    Log.info("Resolution from prefs found: ${resX}x${resY}")
                    return MonitorAndResolution(mon, res)
                }
            }
        }

        availableMonitors.forEach { monitor ->
            val videoModeOfDesktop = getVideoModeOfDesktop(
                monitor.ptr,
                monitorCurrentlyFullscreen,
                origVideoModeOfMonitorCurrentlyFullscreen,
            )
            val monAndRes = findBestResolutionForMonitor(monitor.ptr, videoModeOfDesktop)

            if (monAndRes == null) {
                Log.warn("Ignoring monitor ${monitor.name}, because no suitable resolution was found")
            } else {
                list.add(monAndRes)
            }
        }

        val primaryMonitor = glfwGetPrimaryMonitor()

        if (list.isEmpty()) {
            Log.warn("The list of matching monitors and resolutions is empty! Trying the primary monitor anyway!")

            if (primaryMonitor == 0L) {
                Log.warn("Primary monitor is 0L")
            }

            val videoMode = glfwGetVideoMode(primaryMonitor)

            if (videoMode == null) {
                Log.warn("Video mode of primary monitor is null")
            }

            val res = Resolution(videoMode?.width() ?: 0, videoMode?.height() ?: 0)
            return MonitorAndResolution(primaryMonitor, res)
        } else {
            // If the primary monitor is among the list, take it, otherwise just take the first.
            return list.find { it.monitor == primaryMonitor } ?: list.first()
        }
    }

    private fun getVideoModeOfDesktop(
        monitor: Long,
        monitorCurrentlyFullscreen: Long,
        origVideoModeOfMonitorCurrentlyFullscreen: GLFWVidMode?,
    ): GLFWVidMode? {
        return when (monitor == monitorCurrentlyFullscreen) {
            true -> origVideoModeOfMonitorCurrentlyFullscreen ?: run {
                Log.warn("origVideoMode is null, but monitor is monitorCurrentlyFullscreen")
                null
            }
            false -> glfwGetVideoMode(monitor) ?: run {
                Log.warn("glfwGetVideoMode returned null for monitor $monitor")
                null
            }
        }
    }

    private fun findBestResolutionForMonitor(monitor: Long, videoModeOfDesktop: GLFWVidMode?): MonitorAndResolution? {
        require(monitor != 0L)
        val resolutions = getAvailableResolutions(monitor)

        val desktopWidth = videoModeOfDesktop?.width() ?: 0
        val desktopHeight = videoModeOfDesktop?.height() ?: 0

        val resolutionOfDesktop = when (videoModeOfDesktop) {
            null -> -1
            else -> resolutions.indexOfFirst(desktopWidth, desktopHeight).also {
                if (it == -1) {
                    Log.warn(
                        "Cannot find resolution ${desktopWidth}x${desktopHeight} even though it should be in the list"
                    )
                }
            }
        }

        return resolutions.pickBestMatch(resolutionOfDesktop)?.let {
            Log.info("Monitor $monitor: Picked ${it.res}: ${it.reasonOfPick.asText}")
            MonitorAndResolution(monitor, it.res)
        }
    }

    override fun getAvailableMonitors() =
        getAvailableMonitorsPtr().map { ptr ->
            var name = glfwGetMonitorName(ptr)

            if (name.isNullOrEmpty()) {
                name = "Monitor $ptr"
            }

            MonitorWithPtr(name, ptr)
        }

    private fun getAvailableMonitorsPtr(): List<Long> {
        val result = mutableListOf<Long>()
        val monitors = glfwGetMonitors()

        if (monitors == null) {
            Log.warn("glfwGetMonitors returned null! Trying to use primary monitor.")
            result.add(glfwGetPrimaryMonitor())
        } else {
            monitors.position(0)

            repeat(monitors.limit()) {
                val m = monitors.get()

                if (m == 0L) {
                    Log.warn("glfwGetMonitors returned an entry that is 0L")
                } else {
                    result.add(m)
                }
            }
        }

        return result
    }

    override fun getAvailableResolutions(monitor: Monitor): List<Resolution> {
        val monWithPtr = monitor as? MonitorWithPtr ?: return emptyList()
        return getAvailableResolutions(monWithPtr.ptr)
    }

    override fun getRecommendedResolutions(monitor: Monitor): List<Resolution> {
        val monWithPtr = monitor as? MonitorWithPtr ?: return emptyList()
        return getRecommendedResolutions(monWithPtr.ptr)
    }

    private fun getRecommendedResolutions(monitor: Long): List<Resolution> {
        val available = getAvailableResolutions(monitor)
        if (available.isEmpty()) return emptyList()
        val recommended = available.filter { it.isWithinRecommendedBounds() }
        if (recommended.isNotEmpty()) return recommended
        val highestNotRecommended = available.maxByOrNull { it.numPixelsX * it.numPixelsY }
        return listOf(highestNotRecommended!!) // shouldn't be null since available is known to be non-empty
    }

    private fun getAvailableResolutions(monitor: Long): List<Resolution> {
        val result = mutableListOf<Resolution>()
        val modes = glfwGetVideoModes(monitor)

        if (modes != null) {
            modes.position(0)

            repeat(modes.limit()) {
                val m = modes.get()
                result.add(
                    Resolution(
                        numPixelsX = m.width(),
                        numPixelsY = m.height(),
                    )
                )
            }
        }

        // GLFW can report resolutions with multiple refresh rates, but we're only interested in the size.
        return result.distinctBy { "${it.numPixelsX}x${it.numPixelsY}" }
    }
}
