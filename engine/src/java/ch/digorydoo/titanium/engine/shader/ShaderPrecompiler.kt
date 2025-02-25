package ch.digorydoo.titanium.engine.shader

import ch.digorydoo.kutils.string.indexOfAnyExcept
import ch.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags

// NOTE: GLSL actually implements #ifdef directives. I did not realize I could simply prepend the external defines to
// the source, and wrote this precompiler instead. That isn't too bad, since I'm going to need this code anyway when
// minifying and bundling shaders into a PAK. Also, I could handle #include here, which GLSL does not support.
class ShaderPrecompiler {
    // FIXME: lineIdx can be wrong, because removeComments and handleIfDefs may remove lines.
    // Could be fixed by replacing the lines with empty strings before finally removing them.

    open class PrecompilerError(lineIdx: Int, msg: String): Exception("Line ${lineIdx + 1}: $msg")
    class BadNameForDefineError(lineIdx: Int, name: String): PrecompilerError(lineIdx, "Bad name for define: $name")
    class BlockCommentNotClosedError(lineIdx: Int): PrecompilerError(lineIdx, "Block comment not closed")
    class DirectiveCannotHaveArgsError(lineIdx: Int): PrecompilerError(lineIdx, "This directive cannot have any args")
    class DirectiveNotImplementedError(lineIdx: Int): PrecompilerError(lineIdx, "Directive not implemented")
    class DuplicateDefineError(lineIdx: Int, key: String): PrecompilerError(lineIdx, "Key $key duplicated by #define")
    class ElifdefWithoutIfError(lineIdx: Int): PrecompilerError(lineIdx, "#elifdef without corresponding #ifdef")
    class ElseWithoutIfError(lineIdx: Int): PrecompilerError(lineIdx, "#else without corresponding #ifdef")
    class EndifWithoutIfError(lineIdx: Int): PrecompilerError(lineIdx, "#endif without corresponding #ifdef")
    class InvalidCondExprError(lineIdx: Int, expr: String): PrecompilerError(lineIdx, "Invalid condition expr: $expr")
    class MissingEndifError(lineIdx: Int): PrecompilerError(lineIdx, "#ifdef is missing its #endif")
    class MissingKeyInDefineError(lineIdx: Int): PrecompilerError(lineIdx, "The #define is missing its key")
    class MissingKeyInIfError(lineIdx: Int): PrecompilerError(lineIdx, "The #if is missing its key")
    class MissingKeyInIfdefError(lineIdx: Int): PrecompilerError(lineIdx, "The #ifdef is missing its key")
    class MissingKeyInIfndefError(lineIdx: Int): PrecompilerError(lineIdx, "The #ifndef is missing its key")
    class MissingKeyInUndefError(lineIdx: Int): PrecompilerError(lineIdx, "The #undef is missing its key")
    class TooManyArgumentsError(lineIdx: Int): PrecompilerError(lineIdx, "The directive has too many arguments")

    private class If(val lineIdx: Int, val condition: Boolean, val isElif: Boolean) {
        var elseSeen = false
    }

    private val defines = mutableSetOf<String>()

    fun precompile(source: String, flags: Set<ShaderFlags>?): String {
        defines.clear()
        return source.split("\n")
            .let { removeComments(it) }
            .map { it.trim() }
            .let { handleDirectives(it, flags ?: emptySet()) }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }

    private fun removeComments(lines: List<String>): List<String> {
        val result = mutableListOf<String>()
        var inBlockComment = false
        var blockCommentLineIdx = -1

        for ((lineIdx, origLine) in lines.withIndex()) {
            var line = origLine

            while (true) {
                var blockCommentStartAt: Int? = null

                if (!inBlockComment) {
                    blockCommentStartAt = line.indexOf("/*")
                    val lineCommentStartAt = line.indexOf("//")

                    if (blockCommentStartAt >= 0 &&
                        (lineCommentStartAt < 0 || blockCommentStartAt < lineCommentStartAt)
                    ) {
                        inBlockComment = true
                        blockCommentLineIdx = lineIdx
                    } else if (lineCommentStartAt >= 0) {
                        result.add(line.slice(0 ..< lineCommentStartAt))
                        break
                    } else {
                        result.add(line)
                        break
                    }
                }

                // inBlockComment must be true at this point
                val commentEndAt = line.indexOf("*/", blockCommentStartAt?.let { it + 2 } ?: 0)

                if (commentEndAt >= 0) {
                    line = line.replaceRange((blockCommentStartAt ?: 0) ..< commentEndAt + 2, " ")
                    inBlockComment = false
                    // continue with the rest of this line
                } else {
                    // The end of the comment was not found on this line.
                    if (blockCommentStartAt != null && blockCommentStartAt > 0) {
                        result.add(line.slice(0 ..< blockCommentStartAt)) // keep this part from the current line
                    }
                    break // examine next line
                }
            }
        }

        if (inBlockComment) {
            throw BlockCommentNotClosedError(blockCommentLineIdx)
        }

        return result
    }

    private fun handleDirectives(lines: List<String>, flags: Set<ShaderFlags>): List<String> {
        val result = mutableListOf<String>()
        val ifStack = mutableListOf<If>()
        defines.addAll(flags.map { it.name })
        val spaceOrTab = arrayOf(' ', '\t').toCharArray()
        var combinedCondition = true

        for ((lineIdx, line) in lines.withIndex()) {
            val keepLine: Boolean

            if (!line.startsWith("#")) {
                keepLine = combinedCondition || ifStack.isEmpty()
            } else {
                val behindTag = line.indexOfAny(spaceOrTab).takeIf { it >= 0 }

                val startOfFirstArg = when (behindTag) {
                    null -> null
                    else -> line.indexOfAnyExcept(spaceOrTab, behindTag).takeIf { it >= 0 }
                }

                val behindFirstArg = when (startOfFirstArg) {
                    null -> null
                    else -> line.indexOfAny(spaceOrTab, startOfFirstArg).takeIf { it >= 0 }
                }

                val firstArg = when (startOfFirstArg) {
                    null -> ""
                    else -> line.slice(startOfFirstArg ..< (behindFirstArg ?: line.length)).trim()
                }

                val secondArg = when {
                    startOfFirstArg == null -> ""
                    behindFirstArg == null -> ""
                    else -> line.substring(behindFirstArg).trim()
                }

                when {
                    // These directives must be handled even when combinedCondition is false.
                    line.startsWith("#ifdef") -> {
                        if (firstArg.isEmpty()) throw MissingKeyInIfdefError(lineIdx)
                        if (secondArg.isNotEmpty()) throw TooManyArgumentsError(lineIdx)
                        checkNameIsValidForDefine(firstArg, lineIdx)
                        val condition = defines.contains(firstArg)
                        ifStack.add(If(lineIdx, condition = condition, isElif = false))
                        combinedCondition = reevaluate(ifStack)
                        keepLine = false
                    }
                    line.startsWith("#ifndef") -> {
                        if (firstArg.isEmpty()) throw MissingKeyInIfndefError(lineIdx)
                        if (secondArg.isNotEmpty()) throw TooManyArgumentsError(lineIdx)
                        checkNameIsValidForDefine(firstArg, lineIdx)
                        val condition = !defines.contains(firstArg)
                        ifStack.add(If(lineIdx, condition = condition, isElif = false))
                        combinedCondition = reevaluate(ifStack)
                        keepLine = false
                    }
                    line.startsWith("#if") -> { // must be after ifdef and ifndef
                        if (startOfFirstArg == null) throw MissingKeyInIfError(lineIdx)
                        val condition = evalCondition(line.substring(startOfFirstArg).trim(), lineIdx)
                        ifStack.add(If(lineIdx, condition = condition, isElif = false))
                        combinedCondition = reevaluate(ifStack)
                        keepLine = false
                    }
                    line.startsWith("#else") -> {
                        if (firstArg.isNotEmpty()) throw DirectiveCannotHaveArgsError(lineIdx)
                        if (ifStack.isEmpty()) throw ElseWithoutIfError(lineIdx)

                        val top = ifStack.last()
                        if (top.elseSeen) throw MissingEndifError(lineIdx)
                        top.elseSeen = true
                        combinedCondition = reevaluate(ifStack)
                        keepLine = false
                    }
                    line.startsWith("#elifdef") -> {
                        if (firstArg.isEmpty()) throw MissingKeyInIfdefError(lineIdx)
                        if (secondArg.isNotEmpty()) throw TooManyArgumentsError(lineIdx)
                        if (ifStack.isEmpty()) throw ElifdefWithoutIfError(lineIdx)

                        val top = ifStack.last()
                        if (top.elseSeen) throw MissingEndifError(lineIdx)
                        top.elseSeen = true
                        checkNameIsValidForDefine(firstArg, lineIdx)
                        ifStack.add(If(lineIdx, condition = defines.contains(firstArg), isElif = true))
                        combinedCondition = reevaluate(ifStack)
                        keepLine = false
                    }
                    line.startsWith("#endif") -> {
                        if (firstArg.isNotEmpty()) throw DirectiveCannotHaveArgsError(lineIdx)
                        if (ifStack.isEmpty()) throw EndifWithoutIfError(lineIdx)

                        do {
                            val last = ifStack.removeLast()
                        } while (last.isElif)

                        combinedCondition = reevaluate(ifStack)
                        keepLine = false
                    }

                    // These directives will be ignored when combinedCondition is false.
                    !combinedCondition -> keepLine = false
                    line.startsWith("#define") -> {
                        val name = firstArg.indexOf("(")
                            .takeIf { it >= 0 }
                            ?.let { firstArg.slice(0 ..< it) }
                            ?: firstArg
                        if (name.isEmpty()) throw MissingKeyInDefineError(lineIdx)
                        checkNameIsValidForDefine(name, lineIdx)
                        if (defines.contains(name)) throw DuplicateDefineError(lineIdx, name)
                        defines.add(name)
                        keepLine = true
                    }
                    line.startsWith("#undef") -> {
                        if (firstArg.isEmpty()) throw MissingKeyInUndefError(lineIdx)
                        if (secondArg.isNotEmpty()) throw TooManyArgumentsError(lineIdx)
                        checkNameIsValidForDefine(firstArg, lineIdx)
                        defines.remove(firstArg)
                        keepLine = true // we keep this in, even though GLSL appears to ignore it
                    }
                    line.startsWith("#version") -> keepLine = true
                    else -> throw DirectiveNotImplementedError(lineIdx)
                }
            }

            if (keepLine) result.add(line)
        }

        if (ifStack.isNotEmpty()) {
            throw MissingEndifError(ifStack.last().lineIdx)
        }

        return result
    }

    private fun reevaluate(ifStack: List<If>) =
        ifStack.fold(true) { result, ifStackEntry ->
            if (!result) {
                false
            } else if (ifStackEntry.elseSeen) {
                !ifStackEntry.condition
            } else {
                ifStackEntry.condition
            }
        }

    private fun checkNameIsValidForDefine(name: String, lineIdx: Int) {
        if (!Regex("[a-zA-Z_][a-zA-Z0-9_]*").matches(name)) {
            throw BadNameForDefineError(lineIdx, name)
        }
    }

    // This is currently very simple and does not implement the syntax fully.
    private fun evalCondition(expr: String, lineIdx: Int): Boolean =
        when {
            expr.contains("||") -> expr.split("||").fold(false) { result, part ->
                result || evalCondition(part.trim(), lineIdx)
            }
            expr.contains("&&") -> expr.split("&&").fold(true) { result, part ->
                result && evalCondition(part.trim(), lineIdx)
            }
            expr.startsWith("defined(") && expr.endsWith(")") -> expr.slice(8 ..< expr.length - 1)
                .trim()
                .takeIf { it.isNotEmpty() }
                ?.let { defines.contains(it) }
                ?: throw InvalidCondExprError(lineIdx, expr)
            expr.startsWith("!defined(") && expr.endsWith(")") -> !evalCondition(expr.substring(1), lineIdx)
            Regex("[0-9]*").matches(expr) -> expr.toIntOrNull() != 0
            else -> throw InvalidCondExprError(lineIdx, expr)
        }
}
