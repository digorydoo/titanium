package ch.digorydoo.titanium.game.ui

import ch.digorydoo.titanium.engine.ui.game_menu.GameMenu
import ch.digorydoo.titanium.engine.ui.game_menu.IGameMenuTopic
import ch.digorydoo.titanium.game.ui.GameMenuTopic.*
import ch.digorydoo.titanium.game.ui.map.MapPage
import ch.digorydoo.titanium.game.ui.options.OptionsPage

class GameMenuImpl: GameMenu() {
    override var topic: IGameMenuTopic = OPTIONS
    override val firstTopic: IGameMenuTopic = GameMenuTopic.entries.first()
    override val lastTopic: IGameMenuTopic = GameMenuTopic.entries.last()
    override fun indexOf(t: IGameMenuTopic) = GameMenuTopic.entries.indexOf(t)
    override fun forEachTopic(lambda: (t: IGameMenuTopic) -> Unit) = GameMenuTopic.entries.forEach(lambda)

    override fun makePage(topic: IGameMenuTopic) = when (topic as? GameMenuTopic) {
        MAP -> MapPage().also { it.makeGels() }
        PROFILE -> ProfilePage().also { it.makeGels() }
        INVENTORY -> InventoryPage().also { it.makeGels() }
        QUESTS -> QuestsPage().also { it.makeGels() }
        ACHIEVEMENTS -> AchievementsPage().also { it.makeGels() }
        OPTIONS -> OptionsPage().also { it.makeGels() }
        null -> throw Exception("Topic $topic is not a GameMenuTopic")
    }
}
