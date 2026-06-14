package io.github.digorydoo.titanium.engine.behaviours

import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.gel.Behaviour
import io.github.digorydoo.titanium.engine.gel.GraphicElement

/**
 * This class implements a gel behaviour that aligns the gel with one of the boundaries of the screen. Use this for UI
 * gels only, as it does not make sense in 3D space.
 */
class Align(
    private val gel: GraphicElement,
    private val alignment: Alignment,
    private val delegate: Delegate,
): Behaviour {
    enum class Anchor { TOP_LEFT, TOP_CENTRE, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_CENTRE, BOTTOM_RIGHT }

    open class Alignment(
        open val anchor: Anchor,
        open val xOffset: Int = 0,
        open val yOffset: Int = 0,
        open val marginLeft: Int = 0,
        open val marginTop: Int = 0,
        open val marginRight: Int = 0,
        open val marginBottom: Int = 0,
    ) {
        constructor(
            base: Alignment,
            anchor: Anchor? = null,
            xOffset: Int? = null,
            yOffset: Int? = null,
            marginLeft: Int? = null,
            marginTop: Int? = null,
            marginRight: Int? = null,
            marginBottom: Int? = null,
        ): this(
            anchor = base.anchor,
            xOffset = base.xOffset,
            yOffset = base.yOffset,
            marginLeft = base.marginLeft,
            marginTop = base.marginTop,
            marginRight = base.marginRight,
            marginBottom = base.marginBottom,
        )
    }

    // These values are not part of the Alignment object, because the Alignment is often decided by the caller, while
    // the width and height should come from the gel that is being aligned.
    interface Delegate {
        val width: Int
        val height: Int
    }

    private val screenSizeDp = App.resolutionMgr.screenSizeDp

    fun animate() {
        val x = alignment.xOffset + when (alignment.anchor) {
            Anchor.TOP_LEFT,
            Anchor.BOTTOM_LEFT,
            -> alignment.marginLeft

            Anchor.TOP_CENTRE,
            Anchor.BOTTOM_CENTRE,
            -> screenSizeDp.x / 2 - delegate.width / 2

            Anchor.TOP_RIGHT,
            Anchor.BOTTOM_RIGHT,
            -> screenSizeDp.x - delegate.width - alignment.marginRight
        }
        val y = alignment.yOffset + when (alignment.anchor) {
            Anchor.TOP_LEFT,
            Anchor.TOP_CENTRE,
            Anchor.TOP_RIGHT,
            -> alignment.marginTop

            Anchor.BOTTOM_LEFT,
            Anchor.BOTTOM_CENTRE,
            Anchor.BOTTOM_RIGHT,
            -> screenSizeDp.y - delegate.height - alignment.marginBottom
        }
        gel.moveTo(x.toFloat(), y.toFloat(), gel.pos.z)
    }
}
