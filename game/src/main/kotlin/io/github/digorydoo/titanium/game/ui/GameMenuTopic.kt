package io.github.digorydoo.titanium.game.ui

import io.github.digorydoo.titanium.engine.i18n.ITextId
import io.github.digorydoo.titanium.engine.ui.game_menu.IGameMenuTopic
import io.github.digorydoo.titanium.game.i18n.GameTextId

enum class GameMenuTopic(override val textId: ITextId): IGameMenuTopic {
    MAP(GameTextId.MAP),
    QUESTS(GameTextId.QUESTS),
    PROFILE(GameTextId.PROFILE),
    INVENTORY(GameTextId.INVENTORY),
    ACHIEVEMENTS(GameTextId.ACHIEVEMENTS),
    OPTIONS(GameTextId.OPTIONS),
    ;

    override fun previous() = entries[(entries.indexOf(this) - 1 + entries.size) % entries.size]
    override fun next() = entries[(entries.indexOf(this) + 1) % entries.size]
}
