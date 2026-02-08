package io.github.digorydoo.titanium.engine.ui.tab

import ch.digorydoo.kutils.rect.Recti
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.i18n.ITextId

class MenuTabDescriptor(
    private val textId: ITextId,
    val gel: MenuTabGel,
    val bounds: Recti,
    val page: MenuTabPage,
) {
    val displayText get() = App.i18n.getString(textId)
}
