package ch.digorydoo.titanium.engine.behaviours

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.gel.GraphicElement.Behaviour

/**
 * This class implements a gel behaviour that aligns the gel with one of the boundaries of the screen. Use this for UI
 * gels only, as it does not make sense in 3D space.
 */
class Align(private val delegate: Delegate): Behaviour {
    enum class Anchor { TOP_LEFT, TOP_CENTRE, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_CENTRE, BOTTOM_RIGHT }

    open class Alignment(
        open val anchor: Anchor = Anchor.TOP_LEFT,
        open val xOffset: Int = 0,
        open val yOffset: Int = 0,
        open val marginLeft: Int = 0,
        open val marginTop: Int = 0,
        open val marginRight: Int = 0,
        open val marginBottom: Int = 0,
    )

    abstract class Delegate: Alignment() {
        open val width = 0
        open val height = 0
        abstract fun setPos(x: Int, y: Int)
    }

    override fun animate() {
        val x = delegate.xOffset + when (delegate.anchor) {
            Anchor.TOP_LEFT,
            Anchor.BOTTOM_LEFT,
                -> delegate.marginLeft

            Anchor.TOP_CENTRE,
            Anchor.BOTTOM_CENTRE,
                -> App.screenWidthDp / 2 - delegate.width / 2

            Anchor.TOP_RIGHT,
            Anchor.BOTTOM_RIGHT,
                -> App.screenWidthDp - delegate.width - delegate.marginRight
        }
        val y = delegate.yOffset + when (delegate.anchor) {
            Anchor.TOP_LEFT,
            Anchor.TOP_CENTRE,
            Anchor.TOP_RIGHT,
                -> delegate.marginTop

            Anchor.BOTTOM_LEFT,
            Anchor.BOTTOM_CENTRE,
            Anchor.BOTTOM_RIGHT,
                -> App.screenHeightDp - delegate.height - delegate.marginBottom
        }
        delegate.setPos(x, y)
    }
}
