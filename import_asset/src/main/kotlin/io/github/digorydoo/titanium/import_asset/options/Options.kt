package io.github.digorydoo.titanium.import_asset.options

import kotlin.system.exitProcess

sealed class Options {
    enum class Verbosity { NORMAL, QUIET, VERBOSE }

    protected abstract fun readCmdLine(args: Array<String>)

    companion object {
        protected const val BRICK_TEXTURES_ACTION = "brick-textures"
        protected const val COLLADA_ACTION = "collada"

        fun fromCmdLine(args: Array<String>): Options {
            val action = args.firstOrNull()

            val options = when (action) {
                BRICK_TEXTURES_ACTION -> ImportBrickTexturesOptions()
                COLLADA_ACTION -> ImportColladaOptions()
                else -> {
                    if (action != null && !action.startsWith("-")) {
                        println("Unknown action: $action. Use -h for help.")
                    } else {
                        printUsage()
                    }
                    exitProcess(1)
                }
            }

            options.readCmdLine(args.drop(1).toTypedArray())
            return options
        }

        private fun printUsage() {
            println(
                """
                USAGE: import-asset <command> <additional arguments>
                where <command> is one of:

                   $BRICK_TEXTURES_ACTION: Put together multiple brick texture PNG files into one large PNG.
                   $COLLADA_ACTION: Import Collada 3D object files.

                """.trimIndent()
            )
            println(
                """
                For additional information about each command, type:
                $ import-asset <command> -h
                """.trimIndent()
            )
        }
    }
}
