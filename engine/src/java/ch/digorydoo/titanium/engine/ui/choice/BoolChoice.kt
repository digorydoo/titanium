package ch.digorydoo.titanium.engine.ui.choice

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.i18n.ITextId

typealias OnBoolChangeListener = (newValue: Boolean) -> Unit

class BoolChoice(prefix: String, initialValue: Boolean, onChangeLambda: OnBoolChangeListener?): Choice() {
    constructor(prefix: ITextId, initialValue: Boolean, onChangeLambda: OnBoolChangeListener?):
        this(App.i18n.getString(prefix), initialValue, onChangeLambda)

    override val itemText = prefix
    override val autoDismiss = false

    var curValue = initialValue; private set

    override val canIncrement get() = !curValue
    override val canDecrement get() = curValue

    private val onChangeListeners: MutableList<OnBoolChangeListener> =
        if (onChangeLambda == null) mutableListOf() else mutableListOf(onChangeLambda)

    fun addOnChangeListener(onChange: OnBoolChangeListener) {
        onChangeListeners.add(onChange)
    }

    private fun callOnChangeListeners() {
        onChangeListeners.forEach { it(curValue) }
    }

    override fun onIncrement(useSmallStep: Boolean) {
        curValue = true
        callOnChangeListeners()
    }

    override fun onDecrement(useSmallStep: Boolean) {
        curValue = false
        callOnChangeListeners()
    }
}
