package io.github.digorydoo.titanium.collect_intl

import io.github.digorydoo.kokuban.ShellCommandError
import io.github.digorydoo.kokuban.toPrettyString
import java.io.File
import java.util.*
import kotlin.system.exitProcess

private enum class SupportedLang {
    EN, DE;

    val suffix = "_${name.lowercase()}.intl"
}

// Data class, because we're using destructuing.
private data class FileAndExtras(val file: File, val pathWithoutSuffix: String)

private class GroupedIntlFiles(val en: File, val de: File)
private class ParsedIntlFiles(val en: Properties, val de: Properties, val raw: GroupedIntlFiles)

// Must stay a data class, because we're relying on equals behaviour!
private data class Translations(val en: String, val de: String)

private fun checkFilesAndDirs(options: Options) {
    fun checkDir(dir: File) {
        when {
            dir.exists() -> when {
                !dir.isDirectory -> throw ShellCommandError("Not a directory: ${dir.path}")
                options.clean -> {
                    println("Cleaning: ${dir.path}")
                    dir.delete()
                    dir.mkdir()
                }
            }
            else -> dir.mkdir()
        }
    }

    checkDir(options.srcOutputDir)
    checkDir(options.resOutputDir)

    options.inputFiles.forEach {
        when {
            it.extension != "intl" -> throw ShellCommandError("Unexpected file ending: ${it.name}")
            !it.exists() -> throw ShellCommandError("Input file does not exist or not accessible: ${it.path}")
            it.isDirectory -> throw ShellCommandError("Input file is a directory: ${it.path}")
        }
    }
}

private fun divideInputsPerLang(inputFiles: List<File>): Map<SupportedLang, List<FileAndExtras>> {
    val map = mutableMapOf<SupportedLang, MutableList<FileAndExtras>>()

    inputFiles.forEach { file ->
        var pickedLang: SupportedLang? = null

        SupportedLang.entries.forEach { lang ->
            if (file.name.endsWith(lang.suffix)) {
                pickedLang = lang
            }
        }

        if (pickedLang == null) {
            throw ShellCommandError("File with unsupported suffix in input: ${file.path}")
        } else {
            val list = map[pickedLang] ?: mutableListOf<FileAndExtras>().also { map[pickedLang] = it }
            val pathWithoutSuffix = file.path.let { it.substring(0, it.length - pickedLang.suffix.length) }
            list.add(FileAndExtras(file, pathWithoutSuffix))
        }
    }

    return map
}

private fun groupInputs(inputsPerLang: Map<SupportedLang, List<FileAndExtras>>): List<GroupedIntlFiles> {
    val problems = mutableListOf<String>()
    val grouped = mutableMapOf<String, MutableMap<SupportedLang, File>>()

    SupportedLang.entries.forEach { lang ->
        inputsPerLang[lang]?.forEach { (file, pathWithoutSuffix) ->
            val map = grouped[pathWithoutSuffix]
                ?: mutableMapOf<SupportedLang, File>().also { grouped[pathWithoutSuffix] = it }

            if (map[lang] != null) {
                problems.add("File provided more than once: ${file.path}")
            } else {
                map[lang] = file
            }
        }
    }

    val result = mutableListOf<GroupedIntlFiles>()

    grouped.entries.forEach { (pathWithoutSuffix, map) ->
        val en = map[SupportedLang.EN]
        val de = map[SupportedLang.DE]

        when {
            en == null -> problems.add("Missing ${pathWithoutSuffix}${SupportedLang.EN.suffix}")
            de == null -> problems.add("Missing ${pathWithoutSuffix}${SupportedLang.DE.suffix}")
            else -> result.add(GroupedIntlFiles(en = en, de = de))
        }
    }

    if (problems.isNotEmpty()) {
        throw ShellCommandError("Not all translation files were provided!\n${problems.joinToString("\n")}")
    }

    return result
}

private fun parseInputs(inputs: List<GroupedIntlFiles>): List<ParsedIntlFiles> {
    fun read(file: File): Properties {
        val props = Properties()
        try {
            file.bufferedReader(Charsets.UTF_8).use {
                props.load(it)
            }
            return props
        } catch (e: Exception) {
            throw ShellCommandError("Failed to parse: ${file.path}\n${e.message}")
        }
    }

    return inputs.map { input ->
        ParsedIntlFiles(
            en = read(input.en),
            de = read(input.de),
            raw = input
        )
    }
}

private fun collectTranslations(inputs: List<ParsedIntlFiles>): Map<String, Translations> {
    class Problem(val file: File, val msg: String)

    val problems = mutableListOf<Problem>()
    val result = mutableMapOf<String, Translations>()

    fun Properties.getAndCheckProperty(key: String, file: File): String {
        val value = getProperty(key)?.trim()
        if (value == null) {
            problems.add(Problem(file, "Missing key $key"))
        } else if (value.isEmpty()) {
            problems.add(Problem(file, "Key $key with empty value"))
        }
        return value ?: ""
    }

    val validKeys = Regex("[A-Z][_A-Z0-9]*") // lowercase not allowed

    inputs.forEach { input ->
        val keys: Set<String> = input.en.stringPropertyNames() + input.de.stringPropertyNames()

        for (key in keys) {
            if (!validKeys.matches(key)) {
                problems.add(Problem(input.raw.en, "Not a valid key: $key"))
            }

            val translations = Translations(
                en = input.en.getAndCheckProperty(key, input.raw.en),
                de = input.de.getAndCheckProperty(key, input.raw.de),
            )

            val existing = result[key]

            if (existing == null) {
                result[key] = translations
            } else {
                // Translations is a data class, so we can use == to check if all translations are the same.
                if (translations != existing) {
                    problems.add(Problem(input.raw.en, "Key $key already used, and translations do not match!"))
                }
            }
        }
    }

    if (problems.isNotEmpty()) {
        val msg = problems.joinToString("\n") { "${it.file.path}\n   ${it.msg}" }
        throw ShellCommandError("Not all translation were provided!\n$msg")
    }

    return result
}

private fun writeResources(collected: Map<String, Translations>, options: Options) {
    val enMap = mutableMapOf<String, String>()
    val deMap = mutableMapOf<String, String>()

    collected.forEach { (key, translations) ->
        enMap[key] = translations.en
        deMap[key] = translations.de
    }

    writeResources(SupportedLang.EN, enMap, options)
    writeResources(SupportedLang.DE, deMap, options)
}

private fun writeResources(lang: SupportedLang, map: Map<String, String>, options: Options) {
    val f = File(options.resOutputDir, "GameText_${lang.name.lowercase()}.properties")

    f.writer().use { writer ->
        map.forEach { (key, translation) ->
            // Keys have already been sanitized and don't need any escaping.
            writer.write(key)
            writer.write("=")

            writer.write(
                // Translation has already been trimmed, but otherwise needs escaping.
                // Our properties file will be UTF-8, so we don't need to support \uXXXX escapes.
                translation.replace("\\", "\\\\")
                    .replace("\n", "\\n")
                    .replace("\t", "\\t")
                    .replace("\r", "") // silently dropped
            )
            writer.write("\n")
        }
    }

    println("Wrote: ${f.name}")
}

private fun writeSources(collected: Map<String, Translations>, options: Options) {
    val f = File(options.srcOutputDir, "GameTextId.kt")

    f.writer().use { writer ->
        writer.write(
            """    
            |package io.github.digorydoo.titanium.game.i18n
            |/*
            | * File was generated by tool-collect-intl.
            | * DO NOT MAKE ANY MODIFICATIONS TO THIS FILE, IT WILL BE OVERWRITTEN!
            | */
            | 
            |import io.github.digorydoo.titanium.engine.core.App
            |import io.github.digorydoo.titanium.engine.i18n.ITextId
            |import org.jetbrains.annotations.PropertyKey
            |
            |enum class GameTextId(
            |    // The annotation helps the IDE understand which keys are being used.
            |    @param:PropertyKey(resourceBundle = BUNDLE_NAME)
            |    override val resId: String,
            |): ITextId {
            |""".trimMargin("|")
        )

        writer.write(collected.keys.sorted().joinToString(",\n") { "    $it(\"$it\")" } + ";\n\n")

        writer.write(
            """
            |    override val bundle get() = (App.i18n as I18nManagerImpl).game
            |
            |    companion object {
            |        const val BUNDLE_NAME = "GameText"
            |    }
            |}
            |""".trimMargin("|")
        )
    }

    println("Wrote: ${f.name} (${collected.size} keys)")
}

private fun main(options: Options) {
    println("Collecting translations from ${options.inputFiles.size} intl files...")

    checkFilesAndDirs(options)
    val perLang = divideInputsPerLang(options.inputFiles)
    val grouped = groupInputs(perLang)
    val parsed = parseInputs(grouped)
    val collected = collectTranslations(parsed)
    writeResources(collected, options)
    writeSources(collected, options)
}

/**
 * The reason why this tool exists is the following:
 *    - Game will have extensive texts in many places. That include conversations, dialogues, menus, other UI elements.
 *    - Java standard approach is to keep all resources in a separate tree. This makes finding the relevant files hard.
 *    - Keeping translations locally is what I'm used to with TypeScript projects and is very good for maintenance.
 *    - Keys don't need to be longish in order to make them unique, because the tool will detect clashes immediately.
 *    - The tool even allows redefining the exact same key as long as the provided translations match exactly.
 *    - If this approach turns out to be bad, a revert is simple, because build/generated contains the combined files.
 */
fun main(args: Array<String>) {
    try {
        val options = Options.fromCmdLine(args)
        main(options)
    } catch (e: ShellCommandError) {
        System.err.println(e.message)
        exitProcess(1)
    } catch (e: Exception) {
        System.err.println(e.toPrettyString())
        e.printStackTrace()
        exitProcess(2)
    } catch (e: Error) {
        System.err.println(e.toPrettyString())
        e.printStackTrace()
        exitProcess(3)
    }
}
