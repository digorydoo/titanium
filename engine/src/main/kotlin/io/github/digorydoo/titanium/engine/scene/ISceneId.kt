package io.github.digorydoo.titanium.engine.scene

interface ISceneId {
    val value: Int
    fun createScene(): Scene
}
