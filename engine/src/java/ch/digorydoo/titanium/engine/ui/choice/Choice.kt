package ch.digorydoo.titanium.engine.ui.choice

import ch.digorydoo.titanium.engine.ui.button.IButtonGel

sealed class Choice {
    abstract val itemText: String // for variable choice items, this is the invariable prefix
    abstract val autoDismiss: Boolean // true = close menu as soon as an item is selected
    var gel: IButtonGel? = null // will be set by DlgBuilder
}
