package io.github.digorydoo.titanium.engine.ui.game_menu

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.rect.Recti
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.core.FIXED_ASPECT_RATIO
import io.github.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import io.github.digorydoo.titanium.engine.input.gamepad.GamepadBtn
import io.github.digorydoo.titanium.engine.input.keyboard.KeyboardKey
import io.github.digorydoo.titanium.engine.texture.ImageData
import io.github.digorydoo.titanium.engine.ui.UIAreaGel
import io.github.digorydoo.titanium.engine.ui.tab.MenuTabDescriptor
import io.github.digorydoo.titanium.engine.ui.tab.MenuTabGel
import io.github.digorydoo.titanium.engine.ui.tab.MenuTabIndicatorGel
import io.github.digorydoo.titanium.engine.ui.tab.MenuTabPage

abstract class GameMenu {
    var isShown = false; private set
    var screenshotWhenOpened: ImageData? = null; private set

    protected abstract var topic: IGameMenuTopic
    protected abstract val firstTopic: IGameMenuTopic
    protected abstract val lastTopic: IGameMenuTopic
    protected abstract fun indexOf(t: IGameMenuTopic): Int
    protected abstract fun forEachTopic(lambda: (t: IGameMenuTopic) -> Unit)
    protected abstract fun makePage(topic: IGameMenuTopic): MenuTabPage

    private val tabs = mutableListOf<MenuTabDescriptor>()
    private var indicator: MenuTabIndicatorGel? = null
    private var topArea: UIAreaGel? = null
    private var contentArea: UIAreaGel? = null
    private var aboutToShow = false

    fun animate() {
        // Dialogues may be started from GameMenu, so if isInDlgMode or intermissions.anyRunning is set, we don't close
        // the GameMenu, but we must not handle its keyboard events either.
        if (App.dlg.isInDlgMode || App.intermissions.anyRunning || App.editor.isShown || App.isAboutToTakeScreenshot) {
            return
        }

        val input = App.input

        when {
            input.checkPressedOnce(GamepadBtn.OPEN_MENU_LEFT) -> showOrSwitchOrDismiss(firstTopic)
            input.checkPressedOnce(GamepadBtn.OPEN_MENU_RIGHT) -> showOrSwitchOrDismiss(lastTopic)
            input.checkPressedOnce(KeyboardKey.ESCAPE) -> toggleShow()
        }

        if (!isShown) return

        when {
            input.dismissBtn.checkPressedOnce() -> dismiss()
            input.checkPressedOnce(GamepadBtn.REAR_UPPER_LEFT) -> switchTo(topic.previous())
            input.checkPressedOnce(GamepadBtn.REAR_UPPER_RIGHT) -> switchTo(topic.next())
            input.checkPressedOnce(KeyboardKey.HOME) -> switchTo(firstTopic)
            input.checkPressedOnce(KeyboardKey.END) -> switchTo(lastTopic)
            input.checkPressedOnce(KeyboardKey.TAB) -> cycleThroughTopics(reverse = input.shiftIsDown)

            // These may need to go away once we have horizontally arranged elements on the page
            input.checkPressedOnce(GamepadBtn.HAT_LEFT) -> switchTo(topic.previous())
            input.checkPressedOnce(GamepadBtn.HAT_RIGHT) -> switchTo(topic.next())
            input.checkPressedOnce(KeyboardKey.ARROW_LEFT) -> switchTo(topic.previous())
            input.checkPressedOnce(KeyboardKey.ARROW_RIGHT) -> switchTo(topic.next())
        }

        tabs.forEach { it.page.animate() }
    }

    private fun show(initialTopic: IGameMenuTopic) {
        if (isShown || aboutToShow || App.isAboutToTakeScreenshot) return
        aboutToShow = true

        App.screenshots.take { screenshot ->
            if (aboutToShow) {
                isShown = true
                aboutToShow = false
                screenshotWhenOpened = screenshot
                makeGels()
                topic = initialTopic
                indicator?.selectedIdx = indexOf(initialTopic)
                val newTab = tabs[indexOf(initialTopic)]
                newTab.page.show()
            }
        }
    }

    fun dismiss() {
        aboutToShow = false // if a screenshot is being taken, we will ignore it
        if (!isShown) return
        removeGels()
        System.gc() // now seems a good time
        isShown = false
        screenshotWhenOpened = null
    }

    private fun switchTo(newTopic: IGameMenuTopic) {
        if (!isShown) return

        val prevTopic = topic
        topic = newTopic
        indicator?.selectedIdx = indexOf(newTopic)

        if (prevTopic != newTopic) {
            val prevTab = tabs[indexOf(prevTopic)]
            val newTab = tabs[indexOf(newTopic)]
            prevTab.page.hide()
            newTab.page.show()
        }
    }

    private fun cycleThroughTopics(reverse: Boolean) {
        if (reverse) {
            switchTo(if (topic == firstTopic) lastTopic else topic.previous())
        } else {
            switchTo(if (topic == lastTopic) firstTopic else topic.next())
        }
    }

    private fun toggleShow() {
        when {
            isShown -> dismiss()
            else -> show(topic) // reopen the topic we had before
        }
    }

    private fun showOrSwitchOrDismiss(newTopic: IGameMenuTopic) {
        when {
            !isShown -> show(newTopic)
            topic != newTopic -> switchTo(newTopic)
            else -> dismiss()
        }
    }

    private fun makeGels() {
        require(contentArea == null)

        val bgTex = screenshotWhenOpened
            ?.let { screenshot ->
                App.textures.createTexture(MENU_BG_WIDTH, MENU_BG_HEIGHT).apply {
                    drawInto {
                        drawImageScaled(screenshot, 0, 0, MENU_BG_WIDTH, MENU_BG_HEIGHT, antiAliasing = true)
                        blur3x3()
                        overlayRect(Recti(0, 0, MENU_BG_WIDTH, MENU_BG_HEIGHT), menuBgColour)
                    }
                }
            }

        contentArea = UIAreaGel(
            bgTex = bgTex,
            marginLeft = -1, // one dp overlap to avoid inaccuracies at border
            marginRight = -1,
            marginTop = -1,
            marginBottom = -1,
            scaleTexToFrameSize = true,
        ).also { it.onCreate(LayerKind.MENU_BACKDROP) }

        require(topArea == null)
        topArea = UIAreaGel(
            bgColour = topAreaBgColour,
            marginLeft = 0,
            marginRight = 0,
            marginTop = 0,
            height = TOP_AREA_HEIGHT,
        ).also { it.onCreate(LayerKind.MENU_BACKDROP) }

        require(indicator == null)
        indicator = MenuTabIndicatorGel(tabs).also { it.onCreate(LayerKind.UI_BELOW_DLG) }

        require(tabs.isEmpty())

        val menuTabGels = mutableMapOf<IGameMenuTopic, MenuTabGel>()
        var left = 0
        var maxRight = 0

        forEachTopic { topic ->
            val gel = MenuTabGel(topic.textId, posX = left, posY = TAB_MARGIN_TOP.toInt())
            menuTabGels[topic] = gel
            val right = left + gel.width
            if (right > maxRight) maxRight = right
            left = right + TAB_SPACING
        }

        // Can't assign this as a member variable, because GameMenu is created too early.
        val screenSizeDp = App.resolutionMgr.screenSizeDp
        left = screenSizeDp.x / 2 - maxRight / 2

        forEachTopic { topic ->
            val gel = menuTabGels[topic]!!
            gel.moveTo(left.toFloat(), gel.pos.y, gel.pos.z)
            gel.onCreate(LayerKind.UI_BELOW_DLG)

            val page = makePage(topic)

            val y = gel.pos.y.toInt()
            val bounds = Recti(left = left, top = y, right = left + gel.width, bottom = y + gel.height)
            tabs.add(MenuTabDescriptor(topic.textId, gel, bounds, page))

            left = bounds.right + TAB_SPACING
        }
    }

    private fun removeGels() {
        require(tabs.isNotEmpty())

        tabs.forEach {
            it.gel.setZombie()
            it.page.removeGels()
        }

        tabs.clear()

        require(indicator != null)
        indicator?.setZombie()
        indicator = null

        require(topArea != null)
        topArea?.setZombie()
        topArea = null

        require(contentArea != null)
        contentArea?.setZombie()
        contentArea = null
    }

    companion object {
        private const val MENU_BG_WIDTH = 256
        private const val MENU_BG_HEIGHT = (MENU_BG_WIDTH / FIXED_ASPECT_RATIO).toInt()
        const val TOP_AREA_HEIGHT = 96
        private const val TAB_MARGIN_TOP = TOP_AREA_HEIGHT - 35.0f
        private const val TAB_SPACING = 32
        private val menuBgColour = Colour(0.19f, 0.16f, 0.11f, 0.64f)
        private val topAreaBgColour = Colour(0.0f, 0.0f, 0.0f, 0.24f)
    }
}
