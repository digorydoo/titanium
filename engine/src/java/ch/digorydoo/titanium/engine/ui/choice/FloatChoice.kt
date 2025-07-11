package ch.digorydoo.titanium.engine.ui.choice

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.i18n.ITextId

typealias OnFloatChangeListener = (newValue: Float) -> Unit

class FloatChoice(
    prefix: String,
    val initialValue: Float,
    val minValue: Float = Float.NEGATIVE_INFINITY,
    val maxValue: Float = Float.POSITIVE_INFINITY,
    val step: Float = 1.0f,
    val smallStep: Float = step,
    val modulo: Float? = null,
    val onChange: OnFloatChangeListener?,
): Choice() {
    @Suppress("unused")
    constructor(
        prefix: ITextId,
        initialValue: Float,
        minValue: Float = Float.NEGATIVE_INFINITY,
        maxValue: Float = Float.POSITIVE_INFINITY,
        step: Float = 1.0f,
        smallStep: Float = step,
        modulo: Float? = null,
        onChange: OnFloatChangeListener?,
    ): this(App.i18n.getString(prefix), initialValue, minValue, maxValue, step, smallStep, modulo, onChange)

    override val itemText = prefix
    override val autoDismiss = false
    var curValue = initialValue
}
