package ch.digorydoo.titanium.main.sound

import ch.digorydoo.kutils.utils.Log
import ch.digorydoo.titanium.engine.core.App
import ch.digorydoo.titanium.engine.file.MyDataInputStream
import java.io.File

class WAVFileReader private constructor(private val stream: MyDataInputStream) {
    class WAVData(val arr: ByteArray, val samplingFreq: Int, val numChannels: Int, val bytesPerSample: Int)

    fun read(): WAVData {
        stream.readExpected("RIFF")
        stream.skipBytes(4) // size of WAVE chunk including data

        stream.readExpected("WAVE")
        stream.readExpected("fmt ")
        stream.readExpectedInt32LittleEndian(16) // chunk size
        stream.readExpectedInt16LittleEndian(WAV_PCM)

        val numChannels = stream.readInt16LittleEndian()

        require(numChannels == 1 || numChannels == 2) {
            "Expected either mono or stereo, but got numChannels=$numChannels"
        }

        val samplingFreq = stream.readInt32LittleEndian()

        stream.skipBytes(4) // avg bytes per second, should be samplingFreq * bytesPerSample * numChannels
        stream.skipBytes(2) // block align, what to do with this??

        val bitsPerSample = stream.readInt16LittleEndian() // bits per sample

        require(bitsPerSample == 8 || bitsPerSample == 16) {
            "Expected either 8bit or 16bit, but got bitsPerSample=$bitsPerSample"
        }

        val bytesPerSample = bitsPerSample / 8

        stream.readExpected("data")
        val dataNumBytes = stream.readInt32LittleEndian()

        Log.info(
            TAG,
            "   " +
                arrayOf(
                    "${bitsPerSample}bit",
                    when (numChannels) {
                        1 -> "mono"
                        2 -> "stereo"
                        else -> "$numChannels channels"
                    },
                    "${samplingFreq}Hz",
                    "data $dataNumBytes bytes",
                ).joinToString(" ")
        )

        val arr = stream.readBytes(dataNumBytes)

        return WAVData(arr, samplingFreq, numChannels, bytesPerSample)
    }

    companion object {
        private val TAG = Log.Tag("WAVFileReader")
        private const val WAV_PCM = 0x0001
        //        private const val WAV_IEEE_FLOAT = 0x0003
        //        private const val WAV_ALAW = 0x0006
        //        private const val WAV_MULAW = 0x0007
        //        private const val WAV_EXTENDED = 0xFFFE

        fun read(fname: String): WAVData {
            val path = App.assets.pathToSound(fname)
            val file = File(path)
            return MyDataInputStream.use(file) { stream ->
                WAVFileReader(stream).read()
            }
        }
    }
}
