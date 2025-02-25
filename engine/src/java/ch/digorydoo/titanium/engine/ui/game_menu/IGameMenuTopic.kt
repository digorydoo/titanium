package ch.digorydoo.titanium.engine.ui.game_menu

import ch.digorydoo.titanium.engine.i18n.ITextId

interface IGameMenuTopic {
    val textId: ITextId
    fun previous(): IGameMenuTopic
    fun next(): IGameMenuTopic
}
