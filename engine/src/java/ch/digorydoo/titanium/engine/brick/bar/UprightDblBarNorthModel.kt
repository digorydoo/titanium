package ch.digorydoo.titanium.engine.brick.bar

import ch.digorydoo.titanium.engine.brick.AbstrBrickModel
import ch.digorydoo.titanium.engine.brick.BrickSubvolume
import ch.digorydoo.titanium.engine.brick.Tesselator
import ch.digorydoo.titanium.engine.brick.zz_various.BlockModel

class UprightDblBarNorthModel: AbstrBrickModel() {
    private val pillarNE = BlockModel(relInsetSouthFace = THIN_INSET, relInsetWestFace = THIN_INSET)
    private val pillarNW = BlockModel(relInsetSouthFace = THIN_INSET, relInsetEastFace = THIN_INSET)

    override fun prepare(ix: Int, iy: Int, iz: Int, subvolume: BrickSubvolume) {
        super.prepare(ix, iy, iz, subvolume)
        pillarNE.prepare(ix, iy, iz, subvolume)
        pillarNW.prepare(ix, iy, iz, subvolume)
    }

    override fun heightAt(x: Float, y: Float) =
        pillarNE.heightAt(x, y) ?: pillarNW.heightAt(x, y)

    override fun tesselateUpFace(tess: Tesselator) {
        pillarNE.tesselateUpFace(tess)
        pillarNW.tesselateUpFace(tess)
    }

    override fun tesselateDownFace(tess: Tesselator) {
        pillarNE.tesselateDownFace(tess)
        pillarNW.tesselateDownFace(tess)
    }

    override fun tesselateNorthFace(tess: Tesselator) {
        pillarNE.tesselateNorthFace(tess)
        pillarNW.tesselateNorthFace(tess)
    }

    override fun tesselateSouthFace(tess: Tesselator) {
        pillarNE.tesselateSouthFace(tess)
        pillarNW.tesselateSouthFace(tess)
    }

    override fun tesselateEastFace(tess: Tesselator) {
        pillarNE.tesselateEastFace(tess)
        pillarNW.tesselateEastFace(tess)
    }

    override fun tesselateWestFace(tess: Tesselator) {
        pillarNE.tesselateWestFace(tess)
        pillarNW.tesselateWestFace(tess)
    }
}
