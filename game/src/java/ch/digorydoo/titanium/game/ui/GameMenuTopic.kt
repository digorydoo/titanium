package ch.digorydoo.titanium.game.ui

import ch.digorydoo.titanium.engine.i18n.ITextId
import ch.digorydoo.titanium.engine.ui.game_menu.IGameMenuTopic
import ch.digorydoo.titanium.game.i18n.GameTextId

enum class GameMenuTopic(override val textId: ITextId): IGameMenuTopic {
    MAP(GameTextId.MAP),
    PROFILE(GameTextId.PROFILE),
    INVENTORY(GameTextId.INVENTORY),
    QUESTS(GameTextId.QUESTS),
    ACHIEVEMENTS(GameTextId.ACHIEVEMENTS),
    OPTIONS(GameTextId.OPTIONS),
    ;

    override fun previous() = entries[(entries.indexOf(this) - 1 + entries.size) % entries.size]
    override fun next() = entries[(entries.indexOf(this) + 1) % entries.size]
}
