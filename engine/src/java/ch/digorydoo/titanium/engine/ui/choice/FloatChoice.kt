package ch.digorydoo.titanium.engine.ui.choice

import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.i18n.ITextId
import kotlin.math.round

typealias OnFloatChangeListener = (newValue: Float) -> Unit

class FloatChoice(
    prefix: String,
    private val initialValue: Float,
    private val minValue: Float = Float.NEGATIVE_INFINITY,
    private val maxValue: Float = Float.POSITIVE_INFINITY,
    private val step: Float = 1.0f,
    private val smallStep: Float = step,
    private val modulo: Float? = null,
    onChangeLambda: OnFloatChangeListener?,
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
        onChangeLambda: OnFloatChangeListener?,
    ): this(App.i18n.getString(prefix), initialValue, minValue, maxValue, step, smallStep, modulo, onChangeLambda)

    override val itemText = prefix
    override val autoDismiss = false

    var curValue = initialValue; private set

    override val canIncrement get() = curValue < maxValue
    override val canDecrement get() = curValue > minValue

    private val onChangeListeners: MutableList<OnFloatChangeListener> =
        if (onChangeLambda == null) mutableListOf() else mutableListOf(onChangeLambda)

    fun addOnChangeListener(onChange: OnFloatChangeListener) {
        onChangeListeners.add(onChange)
    }

    private fun callOnChangeListeners() {
        onChangeListeners.forEach { it(curValue) }
    }

    override fun onIncrement(useSmallStep: Boolean) {
        val oldValue = curValue
        curValue += if (useSmallStep) smallStep else step
        keepConstraints()

        if (oldValue < initialValue && curValue > initialValue) {
            curValue = initialValue // startValue may be outside step grid or even outside range
        }

        callOnChangeListeners()
    }

    override fun onDecrement(useSmallStep: Boolean) {
        val oldValue = curValue
        curValue -= if (useSmallStep) smallStep else step
        keepConstraints()

        if (oldValue > initialValue && curValue < initialValue) {
            curValue = initialValue
        }

        callOnChangeListeners()
    }

    private fun keepConstraints() {
        if (modulo != null) {
            curValue = (curValue + modulo) % modulo
        }

        curValue = round(curValue / smallStep) * smallStep
        curValue = clamp(curValue, minValue, maxValue)
    }
}
