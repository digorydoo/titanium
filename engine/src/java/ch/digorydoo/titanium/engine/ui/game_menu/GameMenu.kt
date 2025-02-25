package ch.digorydoo.titanium.engine.ui.game_menu

import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.rect.Recti
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GelLayer.LayerKind
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

        val input = App.input.values

        when {
            input.menuLeft.pressedOnce -> show(firstTopic)
            input.menuRight.pressedOnce -> show(lastTopic)
            input.actionB.pressedOnce -> if (isShown) dismiss()

            input.escape.pressedOnce -> {
                when {
                    isShown -> dismiss()
                    input.alt.pressed -> show(firstTopic)
                    else -> show(lastTopic)
                }
            }
        }

        if (!isShown) return

        when {
            input.hatLeft.pressedWithRepeat -> show(topic.previous())
            input.hatRight.pressedWithRepeat -> show(topic.next())
            input.ljoyLeft.pressedWithRepeat -> show(topic.previous())
            input.ljoyRight.pressedWithRepeat -> show(topic.next())
        }

        tabs.forEach { it.page.animate() }
    }

    fun show(newTopic: IGameMenuTopic) {
        if (isShown) {
            // Menu is already shown. Just switch tabs.

            val prevTopic = topic
            topic = newTopic
            indicator?.selectedIdx = indexOf(newTopic)

            if (prevTopic != newTopic) {
                val prevTab = tabs[indexOf(prevTopic)]
                val newTab = tabs[indexOf(newTopic)]
                prevTab.page.hide()
                newTab.page.show()
            }
        } else {
            // Menu is not yet shown. Take a screenshot, then create gels.

            App.screenshot.take { screenshot ->
                isShown = true
                screenshotWhenOpened = screenshot
                makeGels()
                topic = newTopic
                indicator?.selectedIdx = indexOf(newTopic)
                val newTab = tabs[indexOf(newTopic)]
                newTab.page.show()
            }
        }
    }

    fun dismiss() {
        if (!isShown) return
        removeGels()
        System.gc() // now seems a good time
        isShown = false
        screenshotWhenOpened = null
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
        private const val TAB_MARGIN_TOP = 18.0f
        private const val TAB_SPACING = 24.0f
        private const val TOP_AREA_HEIGHT = 48
        private val topAreaBgColour = Colour(0.0f, 0.0f, 0.0f, 0.42f)
    }
}
