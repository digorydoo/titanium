package ch.digorydoo.titanium.engine.heightmap

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.HeightMapFileReader
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.shader.PaperRenderer
import ch.digorydoo.titanium.engine.sprite.FrameCollection

class HeightMapGel(override val spawnPt: HeightMapSpawnPt): GraphicElement(spawnPt) {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.FROZEN_VISIBLE
        callOnCreateConcurrently = true
    }

    private val frames = FrameCollection()
    private val frameOrigin = MutablePoint2f()
    private var heightMap: HeightMap? = null

    private val renderProps = object: PaperRenderer.Delegate() {
        override val renderPos = this@HeightMapGel.pos
        override val frameSize get() = frames.frameSize
        override val tex get() = frames.tex
        override val texOffset get() = frames.texOffset
        override val origin get() = frameOrigin
        override val scaleFactor = MutablePoint2f(1.0f / 36, 1.0f / 36)
    }

    override val renderer = App.factory.createPaperRenderer(renderProps)

    override suspend fun onCreateConcurrently(): () -> Unit {
        val img = App.textures.getOrLoadImageDataAsync("test32x32.png")
        val map = HeightMapFileReader.read(spawnPt.filename)

        return {
            // Back in main thread
            frames.setTexture(img)
            frameOrigin.set(renderProps.frameSize.x / 2, renderProps.frameSize.y)
            heightMap = map
        }
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "HeightMapGel(${spawnPt.id})"
}
