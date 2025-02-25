package ch.digorydoo.titanium.import_asset.collada

import ch.digorydoo.kutils.tty.Kokuban
import ch.digorydoo.kutils.tty.ShellCommandError
import ch.digorydoo.titanium.engine.file.MyDataOutputStream
import ch.digorydoo.titanium.import_asset.Options
import ch.digorydoo.titanium.import_asset.Options.Verbosity
import ch.digorydoo.titanium.import_asset.WriterStats
import ch.digorydoo.titanium.import_asset.collada.data.ColladaData
import ch.digorydoo.titanium.import_asset.collada.data.VisualSceneNode
import java.io.File

class ColladaFileConverter(private val options: Options) {
    private val kokuban = Kokuban()

    fun convertFiles() {
        if (options.outDir.isEmpty()) {
            throw ShellCommandError("The output directory has not been set.")
        }

        val dstDir = File(options.outDir)

        if (!dstDir.isDirectory) {
            throw ShellCommandError("Not a directory: ${options.outDir}")
        }

        val srcFileNames = options.extraArgs
            .filter { it.isNotEmpty() }
            .sorted()

        if (srcFileNames.isEmpty()) {
            throw ShellCommandError("The list of source files is empty.")
        }

        if (options.verbosity == Verbosity.VERBOSE) {
            kokuban.text("Processing ${srcFileNames.size} files").println()
        }

        srcFileNames.forEach { colladaFilePath ->
            val colladaFile = File(colladaFilePath)
            val outFile = File(dstDir.path + File.separator + colladaFile.nameWithoutExtension + ".msh")

            if (options.verbosity != Verbosity.QUIET) {
                kokuban.faint.text("${colladaFile.path} ").plain.print()
            }

            var skip = false

            if (outFile.exists()) {
                if (!options.overwrite) {
                    throw ShellCommandError("File already exists!")
                } else if (options.onlyNewer) {
                    if (outFile.lastModified() > colladaFile.lastModified()) {
                        skip = true
                    }
                }
            }

            if (skip) {
                if (options.verbosity != Verbosity.QUIET) {
                    kokuban.green.text("SKIP").plain.println()
                }
            } else {
                val data = read(colladaFile)

                if (options.verbosity != Verbosity.QUIET) {
                    kokuban.green.text("OK").plain.println()
                }

                write(data, outFile)
            }
        }
    }

    private fun read(f: File): ColladaData {
        val result: ColladaData

        try {
            val stream = f.inputStream()
            val parser = ColladaFileParser()
            result = parser.parse(stream)
        } catch (e: Exception) {
            kokuban.red.bold.text("FAILED").plain.println()
            throw e
        }

        return result
    }

    private fun write(data: ColladaData, outFile: File) {
        if (options.verbosity != Verbosity.QUIET) {
            kokuban.faint.text("   ${outFile.path} ").plain.print()
        }

        val accessor = ColladaDataAccessor(data)
        var stats: WriterStats? = null

        try {
            MyDataOutputStream.use(outFile) { stream ->
                val writer = MeshFileWriter(stream, accessor)
                writer.write()
                stats = writer.stats
            }
        } catch (e: Exception) {
            kokuban.red.bold.text("FAILED").plain.println()
            throw e
        }

        if (options.verbosity != Verbosity.QUIET) {
            kokuban.green.text("OK").plain.print()

            if (options.verbosity == Verbosity.VERBOSE) {
                println()
                printGeometryInfo(accessor)
            }

            stats?.let { printStats(it) }
        }
    }

    private fun printGeometryInfo(accessor: ColladaDataAccessor) {
        val scene = accessor.getActiveVisualScene()
        scene.nodes.forEach { printGeometryInfo(it, "   ") }
        println()
    }

    private fun printGeometryInfo(node: VisualSceneNode, indent: String) {
        if (options.verbosity == Verbosity.VERBOSE) {
            println("${indent}${node.name}")
            node.children.forEach { printGeometryInfo(it, "$indent   ") }
        }
    }

    private fun printStats(stats: WriterStats) {
        if (options.verbosity == Verbosity.VERBOSE) {
            println(stats.basicStats().prependIndent("   ") + "\n")
            println(stats.materialStats().prependIndent("   "))
            println()
        } else {
            println(" ${stats.minimal()}")
        }
    }
}
