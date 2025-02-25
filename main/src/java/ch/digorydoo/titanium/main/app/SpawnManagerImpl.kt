package ch.digorydoo.titanium.main.app

import ch.digorydoo.titanium.engine.gel.SpawnManager
import ch.digorydoo.titanium.game.core.SpawnObjType

class SpawnManagerImpl: SpawnManager() {
    override val spawnObjTypeList = SpawnObjType.entries.map { it.id }
}
