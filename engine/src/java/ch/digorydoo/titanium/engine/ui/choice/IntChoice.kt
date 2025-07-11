package ch.digorydoo.titanium.engine.ui.choice

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.i18n.ITextId

typealias OnIntChangeListener = (newValue: Int) -> Unit

class IntChoice(
    prefix: String,
    val initialValue: Int,
    val minValue: Int = 0,
    val maxValue: Int = Int.MAX_VALUE,
    val step: Int = 1,
    val smallStep: Int = step,
    val onChange: OnIntChangeListener?,
): Choice() {
    @Suppress("unused")
    constructor(
        prefix: ITextId,
        initialValue: Int,
        minValue: Int = 0,
        maxValue: Int = Int.MAX_VALUE,
        step: Int = 1,
        smallStep: Int = step,
        onChange: OnIntChangeListener?,
    ): this(App.i18n.getString(prefix), initialValue, minValue, maxValue, step, smallStep, onChange)

    override val itemText = prefix
    override val autoDismiss = false
    var curValue = initialValue
}
