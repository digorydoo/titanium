package ch.digorydoo.titanium.engine.heightmap

import ch.digorydoo.kutils.point.MutablePoint2f
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.HeightMapFileReader
import ch.digorydoo.titanium.engine.gel.GraphicElement
import ch.digorydoo.titanium.engine.mesh.SimpleMeshRenderer
import ch.digorydoo.titanium.engine.texture.Texture

class HeightMapGel(override val spawnPt: HeightMapSpawnPt): GraphicElement(spawnPt) {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.FROZEN_VISIBLE
        callOnCreateConcurrently = true
    }

    private var tex: Texture? = null
    private val frameSize = MutablePoint2f()
    var heightMap: HeightMap? = null; private set

    private val renderProps = object: SimpleMeshRenderer.Delegate() {
        override val renderPos = this@HeightMapGel.pos // shared mutable object
        override val mesh get() = this@HeightMapGel.heightMap?.mesh
        override val rotationPhi = spawnPt.rotation
    }

    override val renderer = App.factory.createSimpleMeshRenderer(renderProps)

    override suspend fun onCreateConcurrently(): () -> Unit {
        val img = App.textures.getOrLoadImageDataAsync("test32x32.png")
        val theHeightMap = HeightMapFileReader.read(spawnPt.filename)
        val triangulated = theHeightMap.triangulate(spawnPt.smooth)

        return {
            // Back in main thread
            tex = App.textures.getOrCreateTexture(img)
            frameSize.set(tex?.width ?: 0, tex?.height ?: 0)
            heightMap = theHeightMap
            theHeightMap.setMesh(triangulated, tex)
        }
    }

    fun heightMapChanged() {
        val heightMap = heightMap ?: return
        val triangulated = heightMap.triangulate(spawnPt.smooth)
        heightMap.setMesh(triangulated, tex)
    }

    fun replaceHeightMap(newHeightMap: HeightMap) {
        heightMap = newHeightMap
        heightMapChanged()
    }

    override fun onRemoveZombie() {
        renderer.free()
    }

    override fun toString() = "HeightMapGel(${spawnPt.id})"
}
