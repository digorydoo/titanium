package ch.digorydoo.titanium.main.font

import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.math.threshold
import ch.digorydoo.kutils.rect.MutableRecti
import ch.digorydoo.kutils.utils.toByteBuffer
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.font.FontManager.FontName
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype.*
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.pow

class Font private constructor(
    val info: STBTTFontinfo,
    @Suppress("unused") private val rawFont: ByteBuffer, // needs to stay, see comments below
    val scale: Float,
    val ascent: Int,
    val descent: Int,
    val lineGap: Int,
    val charGap: Float,
    val filter: (v: Float) -> Float,
) {
    class CharMetrics(val advanceWidth: Int, @Suppress("unused") val leftSideBearing: Int)

    val baseLine get() = (ascent * scale).toInt()

    private val charMetrics = mutableMapOf<Int, CharMetrics>()

    fun getOrMeasureChar(codePoint: Int): CharMetrics {
        val entry = charMetrics[codePoint]

        if (entry != null) {
            return entry
        }

        stbtt_GetCodepointHMetrics(info, codePoint, tmpIntArr1, tmpIntArr2)

        return CharMetrics(
            advanceWidth = tmpIntArr1[0],
            leftSideBearing = tmpIntArr2[0]
        ).also {
            charMetrics[codePoint] = it
        }
    }

    fun getBoundingBoxOfChar(codePoint: Int, xshift: Float, yshift: Float, r: MutableRecti) {
        stbtt_GetCodepointBitmapBoxSubpixel(
            info,
            codePoint,
            scale,
            scale,
            xshift,
            yshift,
            tmpIntArr1,
            tmpIntArr2,
            tmpIntArr3,
            tmpIntArr4,
        )

        r.left = tmpIntArr1[0]
        r.top = tmpIntArr2[0]
        r.right = tmpIntArr3[0]
        r.bottom = tmpIntArr4[0]
    }

    companion object {
        private val tmpIntArr1 = intArrayOf(0)
        private val tmpIntArr2 = intArrayOf(0)
        private val tmpIntArr3 = intArrayOf(0)
        private val tmpIntArr4 = intArrayOf(0)

        fun load(fontName: FontName): Font {
            val info = STBTTFontinfo(ByteBuffer.allocateDirect(STBTTFontinfo.SIZEOF))

            // uno-public-domain.ttf: nice and thin, but needs to be rather large; no umlauts
            // very-fine-serif-non-commercial.ttf: works, needs to be rather large; no umlauts
            // xball-non-commercial.ttf: nice and bold, use for speech, { value -> value.pow(2.0f) }
            // zakirahs-hand-bold-non-commercial.ttf: nice and thin, needs to be rather large; no umlauts

            val fname = when (fontName) {
                FontName.DIALOG_FONT,
                FontName.SNACKBAR_FONT,
                    -> "zakirahs-hand-bold-non-commercial.ttf"

                FontName.SMALL_UI_FONT,
                FontName.TOPIC_FONT,
                    -> "xball-non-commercial.ttf"
            }

            val pixelHeight = when (fontName) {
                FontName.DIALOG_FONT -> 27.0f
                FontName.SNACKBAR_FONT -> 23.0f
                FontName.SMALL_UI_FONT -> 18.0f
                FontName.TOPIC_FONT -> 22.0f
            }

            val charGap = when (fontName) {
                FontName.DIALOG_FONT -> -0.3f // slightly condensed
                FontName.SNACKBAR_FONT -> -0.2f // slightly condensed
                FontName.SMALL_UI_FONT -> 1.0f // slightly extended
                FontName.TOPIC_FONT -> 0.5f
            }

            val filter: (v: Float) -> Float = when (fontName) {
                FontName.DIALOG_FONT,
                FontName.SNACKBAR_FONT,
                    -> { value ->
                    // sharpen the anti-aliasing border slightly
                    val v = value.pow(2.0f)
                    val t = threshold(v, 0.2f)
                    lerp(v, t, 0.3f)
                }

                FontName.SMALL_UI_FONT,
                FontName.TOPIC_FONT,
                    -> { value -> value }
            }

            val path = App.assets.pathToFont(fname)
            val file = File(path)
            val rawFont = file.readBytes().toByteBuffer()
            stbtt_InitFont(info, rawFont)

            val scale = stbtt_ScaleForPixelHeight(info, pixelHeight)
            stbtt_GetFontVMetrics(info, tmpIntArr1, tmpIntArr2, tmpIntArr3)

            // The rawFont needs to be retained in our Font structure, otherwise the garbage collector may delete it,
            // even though info still holds a C-pointer to it!

            return Font(
                info,
                rawFont,
                scale,
                ascent = tmpIntArr1[0],
                descent = tmpIntArr2[0],
                lineGap = tmpIntArr3[0],
                charGap,
                filter
            )
        }
    }
}
