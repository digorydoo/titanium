package io.github.digorydoo.titanium.engine.heightmap

import ch.digorydoo.kutils.vector.MutableVector2f
import io.github.digorydoo.titanium.engine.behaviours.CreateConcurrently
import io.github.digorydoo.titanium.engine.core.App
import io.github.digorydoo.titanium.engine.file.HeightMapFileReader
import io.github.digorydoo.titanium.engine.gel.GraphicElement
import io.github.digorydoo.titanium.engine.heightmap.HeightMap.TriangulatedHeightMap
import io.github.digorydoo.titanium.engine.mesh.SimpleMeshRenderer
import io.github.digorydoo.titanium.engine.texture.ImageData
import io.github.digorydoo.titanium.engine.texture.Texture
import kotlin.reflect.KClass

class HeightMapGel(override val spawnPt: HeightMapSpawnPt): GraphicElement(spawnPt) {
    init {
        inDialog = Visibility.ACTIVE
        inMenu = Visibility.INVISIBLE
        inEditor = Visibility.FROZEN_VISIBLE
    }

    private var tex: Texture? = null
    private val frameSize = MutableVector2f()
    var heightMap: HeightMap? = null; private set

    private val renderProps = object: SimpleMeshRenderer.Delegate() {
        override val renderPos = this@HeightMapGel.pos // shared mutable object
        override val mesh get() = this@HeightMapGel.heightMap?.mesh
        override val rotationPhi = spawnPt.rotation
    }

    override val renderer = App.factory.createSimpleMeshRenderer(renderProps)

    private val createConcurrently = CreateConcurrently(
        this,
        object: CreateConcurrently.Delegate {
            private lateinit var tmpImg: ImageData
            private lateinit var tmpHeightMap: HeightMap
            private lateinit var tmpTriangulated: TriangulatedHeightMap

            override suspend fun onJobStart() {
                // Do not modify gel here! Store everything in temporary variables!
                tmpImg = App.textures.getOrLoadImageDataAsync("test32x32.png")
                tmpHeightMap = HeightMapFileReader.read(spawnPt.filename)
                tmpTriangulated = tmpHeightMap.triangulate(spawnPt.smooth)
            }

            override fun onJobDone() {
                // Back in the main thread
                tex = App.textures.getOrCreateTexture(tmpImg).also {
                    frameSize.set(it.width, it.height)
                }
                tmpHeightMap.setMesh(tmpTriangulated, tex)
                heightMap = tmpHeightMap
            }
        }
    )

    override fun getBehaviour(klass: KClass<*>) = when (klass) {
        CreateConcurrently::class -> createConcurrently
        else -> null
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
