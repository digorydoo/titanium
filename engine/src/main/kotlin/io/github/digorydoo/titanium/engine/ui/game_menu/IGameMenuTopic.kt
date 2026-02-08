package io.github.digorydoo.titanium.engine.ui.game_menu

import io.github.digorydoo.titanium.engine.i18n.ITextId

interface IGameMenuTopic {
    val textId: ITextId
    fun previous(): IGameMenuTopic
    fun next(): IGameMenuTopic
}
