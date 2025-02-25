package ch.digorydoo.titanium.engine.brick.bar

import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.brick.zz_various.BlockModel

class UprightDblBarSouthModel: AbstrBrickModel() {
    private val pillarSE = BlockModel(relInsetNorthFace = THIN_INSET, relInsetWestFace = THIN_INSET)
    private val pillarSW = BlockModel(relInsetNorthFace = THIN_INSET, relInsetEastFace = THIN_INSET)

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)
        pillarSE.prepare(ix, iy, iz, subvolume)
        pillarSW.prepare(ix, iy, iz, subvolume)
    }

    override fun heightAt(x: Float, y: Float) =
        pillarSE.heightAt(x, y) ?: pillarSW.heightAt(x, y)

    override fun tesselateUpFace(tess: Tesselator) {
        pillarSE.tesselateUpFace(tess)
        pillarSW.tesselateUpFace(tess)
    }

    override fun tesselateDownFace(tess: Tesselator) {
        pillarSE.tesselateDownFace(tess)
        pillarSW.tesselateDownFace(tess)
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        pillarSE.tesselateNorthFace(tess)
        pillarSW.tesselateNorthFace(tess)
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        pillarSE.tesselateSouthFace(tess)
        pillarSW.tesselateSouthFace(tess)
    }

    override fun tesselateEastFace(tess: Tesselator) {
        pillarSE.tesselateEastFace(tess)
        pillarSW.tesselateEastFace(tess)
    }

    override fun tesselateWestFace(tess: Tesselator) {
        pillarSE.tesselateWestFace(tess)
        pillarSW.tesselateWestFace(tess)
    }
}
