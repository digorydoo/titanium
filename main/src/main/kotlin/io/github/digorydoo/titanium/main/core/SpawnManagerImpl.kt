package io.github.digorydoo.titanium.main.core

import io.github.digorydoo.titanium.engine.gel.SpawnManager
import io.github.digorydoo.titanium.game.core.SpawnObjType

class SpawnManagerImpl: SpawnManager() {
    override val spawnObjTypeList = SpawnObjType.entries.map { it.id }
}
