package ch.digorydoo.titanium.engine.ui.choice

import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.i18n.ITextId

class TextChoice(
    override val itemText: String,
    override val autoDismiss: Boolean,
    private val onSelectLambda: () -> Unit,
): Choice() {
    constructor(itemText: String, onSelect: () -> Unit):
        this(itemText, true, onSelect)

    constructor(itemText: ITextId, onSelect: () -> Unit):
        this(App.i18n.getString(itemText), true, onSelect)

    constructor(itemText: ITextId, autoDismiss: Boolean, onSelect: () -> Unit):
        this(App.i18n.getString(itemText), autoDismiss, onSelect)

    override val canSelect = true

    override fun onSelect() {
        onSelectLambda()
    }
}
