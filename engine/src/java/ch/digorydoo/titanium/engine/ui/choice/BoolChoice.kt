package ch.digorydoo.titanium.engine.ui.choice

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.i18n.ITextId

typealias OnBoolChangeListener = (newValue: Boolean) -> Unit

class BoolChoice(prefix: String, initialValue: Boolean, val onChange: OnBoolChangeListener?): Choice() {
    constructor(prefix: ITextId, initialValue: Boolean, onChange: OnBoolChangeListener?):
        this(App.i18n.getString(prefix), initialValue, onChange)

    override val itemText = prefix
    override val autoDismiss = false
    var curValue = initialValue
}
