package ch.digorydoo.titanium.engine.brick.bar

import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.brick.zz_various.BlockModel

class UprightDblBarEastModel: AbstrBrickModel() {
    private val pillarNE = BlockModel(relInsetSouthFace = THIN_INSET, relInsetWestFace = THIN_INSET)
    private val pillarSE = BlockModel(relInsetNorthFace = THIN_INSET, relInsetWestFace = THIN_INSET)

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)
        pillarNE.prepare(ix, iy, iz, subvolume)
        pillarSE.prepare(ix, iy, iz, subvolume)
    }

    override fun heightAt(x: Float, y: Float) =
        pillarNE.heightAt(x, y) ?: pillarSE.heightAt(x, y)

    override fun tesselateUpFace(tess: Tesselator) {
        pillarNE.tesselateUpFace(tess)
        pillarSE.tesselateUpFace(tess)
    }

    override fun tesselateDownFace(tess: Tesselator) {
        pillarNE.tesselateDownFace(tess)
        pillarSE.tesselateDownFace(tess)
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        pillarNE.tesselateNorthFace(tess)
        pillarSE.tesselateNorthFace(tess)
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        pillarNE.tesselateSouthFace(tess)
        pillarSE.tesselateSouthFace(tess)
    }

    override fun tesselateEastFace(tess: Tesselator) {
        pillarNE.tesselateEastFace(tess)
        pillarSE.tesselateEastFace(tess)
    }

    override fun tesselateWestFace(tess: Tesselator) {
        pillarNE.tesselateWestFace(tess)
        pillarSE.tesselateWestFace(tess)
    }
}
