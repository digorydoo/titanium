package ch.digorydoo.titanium.engine.ui.choice

import ch.digorydoo.titanium.engine.ui.button.ButtonGel

abstract class Choice {
    abstract val itemText: String // for variable choice items, this is the invariable prefix
    abstract val autoDismiss: Boolean // true = close menu as soon as an item is selected

    open val canSelect = false
    open val canIncrement = false
    open val canDecrement = false

    var gel: ButtonGel? = null // will be set by DlgBuilder

    open fun onSelect() {}
    open fun onIncrement(useSmallStep: Boolean = false) {}
    open fun onDecrement(useSmallStep: Boolean = false) {}
}
