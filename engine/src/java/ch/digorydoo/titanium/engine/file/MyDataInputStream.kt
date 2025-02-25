package ch.digorydoo.titanium.engine.file

import ch.digorydoo.kutils.point.Point3f
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File

class MyDataInputStream private constructor(private val input: DataInputStream) {
    fun readMarker(): FileMarker {
        val us = readUShort16()
        return FileMarker.fromUShort(us)
    }

    fun readExpected(marker: FileMarker) {
        val us = readUShort16()
        require(us == marker.value) { "Marker $marker: Expected value ${marker.value}, read $us" }
    }

    fun readExpected(marker: String) {
        val s = readUTF8(marker.length)
        require(s == marker) { "Expected string \"$marker\", read \"$s\"" }
    }

    fun readExpectedInt32LittleEndian(expected: Int) {
        val i = readInt32LittleEndian()
        require(i == expected) { "Expected Little Endian Int32 $expected, read $i" }
    }

    fun readExpectedInt16LittleEndian(expected: Int) {
        val i = readInt16LittleEndian()
        require(i == expected) { "Expected Little Endian Int16 $expected, read $i" }
    }

    fun readUShort16(): UShort {
        return input.readUnsignedShort().toUShort()
    }

    fun readInt32LittleEndian(): Int {
        val l = input.readInt().toLong() // big endian
        val a = (l and 0xFF).shl(24)
        val b = (l and 0xFF00).shl(8)
        val c = (l and 0xFF0000).shr(8)
        val d = (l and 0xFF000000).shr(24)
        return (a or b or c or d).toInt()
    }

    fun readInt32(): Int {
        return input.readInt()
    }

    fun readInt16LittleEndian(): Int {
        val l = input.readShort().toLong() // big endian
        val a = (l and 0xFF).shl(8)
        val b = (l and 0xFF00).shr(8)
        return (a or b).toInt()
    }

    fun readInt16(): Int {
        return input.readShort().toInt()
    }

    fun readUInt16(): UInt {
        return input.readUnsignedShort().toUInt()
    }

    private fun readUInt8(): UInt {
        return input.readByte().toUInt()
    }

    fun readBoolean(): Boolean {
        val ui = readUInt8()
        require(ui == 0u || ui == 1u) { "Expected 0 or 1, but read $ui" }
        return ui != 0u
    }

    fun readPoint3f(): Point3f {
        val x = input.readFloat()
        val y = input.readFloat()
        val z = input.readFloat()
        return Point3f(x, y, z)
    }

    fun readFloat(): Float {
        return input.readFloat()
    }

    fun readFloatArray(): FloatArray {
        val size = readInt32()
        val arr = FloatArray(size)

        for (i in 0 ..< size) {
            arr[i] = input.readFloat()
        }

        return arr
    }

    fun readByteArray(): ByteArray {
        val size = readInt32()
        val arr = ByteArray(size)

        for (i in 0 ..< size) {
            arr[i] = input.readByte()
        }

        return arr
    }

    fun readUInt16ArrayAsInt(): IntArray {
        val size = readUInt16().toInt()
        val arr = IntArray(size)

        for (i in 0 ..< size) {
            arr[i] = readUInt16().toInt()
        }

        return arr
    }

    fun readInt32Array(): IntArray {
        val size = readInt32()
        val arr = IntArray(size)

        for (i in 0 ..< size) {
            arr[i] = readInt32()
        }

        return arr
    }

    fun readUTF8(): String {
        return input.readUTF()
    }

    private fun readUTF8(fixedLen: Int): String {
        val buf = ByteArray(fixedLen)
        val read = input.read(buf)
        require(read == fixedLen) { "Unexpected EOF!" }
        return String(buf, Charsets.UTF_8)
    }

    fun readBytes(count: Int): ByteArray {
        require(count >= 0) { "readBytes: count is negative: $count" }
        val ba = ByteArray(count)
        val n = input.read(ba)
        require(n == count)
        return ba
    }

    fun skipBytes(count: Int) {
        input.skipBytes(count)
    }

    companion object {
        fun <T> use(file: File, lambda: (stream: MyDataInputStream) -> T): T =
            file.inputStream()
                .let { BufferedInputStream(it) }
                .let { DataInputStream(it) }
                .use { lambda(MyDataInputStream(it)) }
    }
}
