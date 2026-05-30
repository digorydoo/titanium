package io.github.digorydoo.titanium.import_asset.collada

import io.github.digorydoo.titanium.import_asset.WriterStats
import io.github.digorydoo.titanium.import_asset.collada.data.ColladaData
import io.github.digorydoo.titanium.import_asset.collada.data.VisualSceneNode
import io.github.digorydoo.titanium.import_asset.options.ImportColladaOptions
import io.github.digorydoo.titanium.import_asset.options.Options.Verbosity
import io.github.digorydoo.kokuban.ShellCommandError
import io.github.digorydoo.kokuban.ttyFaint
import io.github.digorydoo.kokuban.ttyGreen
import io.github.digorydoo.kokuban.ttyRed
import java.io.File

class ColladaFileConverter(private val options: ImportColladaOptions) {
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
            println("Processing ${srcFileNames.size} files")
        }

        srcFileNames.forEach { colladaFilePath ->
            val colladaFile = File(colladaFilePath)
            val outFile = File(dstDir.path + File.separator + colladaFile.nameWithoutExtension + ".msh")

            if (options.verbosity != Verbosity.QUIET) {
                print(ttyFaint("${colladaFile.path} "))
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
                    println(ttyGreen("SKIP"))
                }
            } else {
                val data = read(colladaFile)

                if (options.verbosity != Verbosity.QUIET) {
                    println(ttyGreen("OK"))
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
            System.err.println(ttyRed("FAILED"))
            throw e
        }

        return result
    }

    private fun write(data: ColladaData, outFile: File) {
        if (options.verbosity != Verbosity.QUIET) {
            print(ttyFaint("   ${outFile.path} "))
        }

        val accessor = ColladaDataAccessor(data)
        var stats: WriterStats

        try {
            stats = MeshFileWriter.write(accessor, outFile)
        } catch (e: Exception) {
            System.err.println(ttyRed("FAILED"))
            throw e
        }

        if (options.verbosity != Verbosity.QUIET) {
            print(ttyGreen("OK"))

            if (options.verbosity == Verbosity.VERBOSE) {
                println()
                printGeometryInfo(accessor)
            }

            stats.let { printStats(it) }
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
