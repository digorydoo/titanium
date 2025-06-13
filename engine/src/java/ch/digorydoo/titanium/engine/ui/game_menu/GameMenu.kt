package ch.digorydoo.titanium.engine.ui.game_menu

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
import ch.digorydoo.titanium.engine.input.gamepad.GamepadBtn
import ch.digorydoo.titanium.engine.input.keyboard.KeyboardKey
import ch.digorydoo.titanium.engine.texture.ImageData
import ch.digorydoo.titanium.engine.ui.UIAreaGel
import ch.digorydoo.titanium.engine.ui.tab.MenuTabDescriptor
import ch.digorydoo.titanium.engine.ui.tab.MenuTabGel
import ch.digorydoo.titanium.engine.ui.tab.MenuTabIndicatorGel
import ch.digorydoo.titanium.engine.ui.tab.MenuTabPage

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

    fun animate() {
        if (App.dlg.hasActiveDlg || App.editor.isShown || App.isAboutToTakeScreenshot) return

        val input = App.input

        when {
            input.isPressedOnce(GamepadBtn.OPEN_MENU_LEFT) -> showOrSwitchOrDismiss(firstTopic)
            input.isPressedOnce(GamepadBtn.OPEN_MENU_RIGHT) -> showOrSwitchOrDismiss(lastTopic)
            input.isPressedOnce(KeyboardKey.ESCAPE) -> toggleShow()
        }

        if (!isShown) return

        when {
            input.dismissBtn.pressedOnce -> dismiss()
            input.isPressedOnce(GamepadBtn.REAR_UPPER_LEFT) -> switchTo(topic.previous())
            input.isPressedOnce(GamepadBtn.REAR_UPPER_RIGHT) -> switchTo(topic.next())
            input.isPressedOnce(KeyboardKey.HOME) -> switchTo(firstTopic)
            input.isPressedOnce(KeyboardKey.END) -> switchTo(lastTopic)
            input.isPressedOnce(KeyboardKey.TAB) -> cycleThroughTopics(reverse = input.shiftPressed)

            // These may need to go away once we have horizontally arranged elements on the page
            input.isPressedOnce(GamepadBtn.HAT_LEFT) -> switchTo(topic.previous())
            input.isPressedOnce(GamepadBtn.HAT_RIGHT) -> switchTo(topic.next())
            input.isPressedOnce(KeyboardKey.ARROW_LEFT) -> switchTo(topic.previous())
            input.isPressedOnce(KeyboardKey.ARROW_RIGHT) -> switchTo(topic.next())
        }

        tabs.forEach { it.page.animate() }
    }

    private fun show(initialTopic: IGameMenuTopic) {
        if (isShown) return

        // FIXME make sure show is not called again while waiting for the screenshot!

        App.screenshot.take { screenshot ->
            isShown = true
            screenshotWhenOpened = screenshot
            makeGels()
            topic = initialTopic
            indicator?.selectedIdx = indexOf(initialTopic)
            val newTab = tabs[indexOf(initialTopic)]
            newTab.page.show()
        }
    }

    fun dismiss() {
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
                App.textures.createTexture(screenshot.width, screenshot.height).apply {
                    drawInto {
                        drawImage(screenshot, dstX = 0, dstY = 0, colourMultiplier = 0.5f)
                        blur3x3()
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
        ).also { App.content.add(it, LayerKind.MENU_BACKDROP) }

        require(topArea == null)
        topArea = UIAreaGel(
            bgColour = topAreaBgColour,
            marginLeft = 0,
            marginRight = 0,
            marginTop = 0,
            height = TOP_AREA_HEIGHT,
        ).also { App.content.add(it, LayerKind.MENU_BACKDROP) }

        require(indicator == null)
        indicator = MenuTabIndicatorGel(tabs).also { App.content.add(it, LayerKind.UI_BELOW_DLG) }

        require(tabs.isEmpty())
        var left = TAB_MARGIN_LEFT

        forEachTopic { topic ->
            val gel = MenuTabGel(topic.textId, posX = left.toInt(), posY = TAB_MARGIN_TOP.toInt())
                .also { App.content.add(it, LayerKind.UI_BELOW_DLG) }

            val page = makePage(topic)

            val x = gel.pos.x.toInt()
            val y = gel.pos.y.toInt()
            val bounds = Recti(left = x, top = y, right = x + gel.width, bottom = y + gel.height)

            tabs.add(MenuTabDescriptor(topic.textId, gel, bounds, page))

            left = bounds.right.toFloat() + TAB_SPACING
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
        const val SAVEGAME_THUMBNAIL_WIDTH = 128
        const val SAVEGAME_THUMBNAIL_HEIGHT = (SAVEGAME_THUMBNAIL_WIDTH / App.FIXED_ASPECT_RATIO).toInt()
        private const val TAB_MARGIN_LEFT = 32.0f
        private const val TAB_MARGIN_TOP = 13.0f
        private const val TAB_SPACING = 24.0f
        private const val TOP_AREA_HEIGHT = 48
        private val topAreaBgColour = Colour(0.0f, 0.0f, 0.0f, 0.42f)
    }
}
