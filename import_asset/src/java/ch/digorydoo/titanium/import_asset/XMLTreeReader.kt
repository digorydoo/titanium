package ch.digorydoo.titanium.import_asset

import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node

abstract class XMLTreeReader(protected val node: Element) {
    protected fun checkAttributes(recognizedAttrs: Array<String>) {
        val unexpected = mutableListOf<String>()
        val map = node.attributes

        for (i in 0 ..< map.length) {
            val name = map.item(i).nodeName

            if (!recognizedAttrs.contains(name)) {
                unexpected.add(name)
            }
        }

        if (unexpected.isNotEmpty()) {
            throw Exception("Unexpected attribute(s): ${unexpected.joinToString(", ")}")
        }
    }

    protected fun getOptionalAttr(attr: String): String? {
        return if (node.hasAttribute(attr)) {
            node.getAttribute(attr)
        } else {
            null
        }
    }

    protected fun getMandatoryAttr(attr: String): String {
        return if (node.hasAttribute(attr)) {
            node.getAttribute(attr)
        } else {
            throw Exception("Missing mandatory attribute $attr")
        }
    }

    protected fun getMandatoryIntAttr(attr: String): Int =
        getMandatoryAttr(attr).let {
            it.toIntOrNull() ?: throw Exception("Attribute $attr is not an int: $it")
        }

    protected fun getOptionalIntAttr(attr: String): Int? =
        getOptionalAttr(attr)?.let {
            it.toIntOrNull() ?: throw Exception("Attribute $attr is not an int: $it")
        }

    protected fun forEachChild(lambda: (child: Element) -> Unit) {
        var child = node.firstChild

        while (child != null) {
            if (child.nodeType == Node.ELEMENT_NODE) {
                try {
                    lambda(child as Element)
                } catch (e: Exception) {
                    throw Exception("In <${child.nodeName}>\n${e.message}")
                }
            }

            child = child.nextSibling
        }
    }

    @Suppress("unused")
    private fun stringify(e: Element): String {
        var result = ""
        var child = e.firstChild

        while (child != null) {
            if (child.nodeType == Node.TEXT_NODE) {
                result += child.textContent
            } else if (child.nodeType == Node.ELEMENT_NODE) {
                (child as? Element)?.let {
                    result += stringify(it)
                }
            }

            child = child.nextSibling
        }

        val attrs = stringify(node.attributes)
        val jattrs = if (attrs.isEmpty()) "" else " $attrs"

        return if (result.isEmpty()) "<${e.nodeName}${jattrs} />"
        else "<${e.nodeName}${jattrs}>${result}</${e.nodeName}>"
    }

    private fun stringify(map: NamedNodeMap): String {
        val result = mutableListOf<String>()

        for (i in 0 ..< map.length) {
            val name = map.item(i).nodeName
            val value = node.getAttribute(name)
            result.add("$name=\"$value\"")
        }

        return result.joinToString(" ")
    }

    protected fun requireChildless() {
        forEachChild {
            throw Exception("Tag <${node.nodeName}> expected to be childless, but got: <${it.nodeName}>")
        }
    }

    protected fun getValue(): String =
        getChildValue(node, allowAttrs = true)

    protected fun getValuesList(child: Element, allowAttrs: Boolean = false) =
        getChildValue(child, allowAttrs)
            .replace("\t", " ")
            .replace("\n", " ")
            .replace("\r", " ")
            .split(" ")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    protected fun getValuesList(): List<String> =
        getValuesList(node, allowAttrs = true)

    protected fun getChildValue(child: Element, allowAttrs: Boolean = false): String {
        if (!allowAttrs && child.attributes.length > 0) {
            throw Exception("Child <${child.nodeName}> has unexpected attrs: ${stringify(child.attributes)}")
        }

        val result = mutableListOf<String>()
        var sub = child.firstChild

        while (sub != null) {
            when (sub.nodeType) {
                Node.TEXT_NODE -> result.add(sub.textContent)
                Node.ELEMENT_NODE -> throw Exception("<${child.nodeName}>: Unexpected sub element: ${sub.nodeName}")
                else -> Unit
            }

            sub = sub.nextSibling
        }

        return result.joinToString(" ")
    }

    protected fun getChildFloatValue(child: Element, allowAttrs: Boolean = false): Float =
        getChildValue(child, allowAttrs).let {
            it.toFloatOrNull() ?: throw Exception("<${child.nodeName}>: Value is not a float: $it")
        }

    protected fun requireChildValue(child: Element, requiredValue: String, allowAttrs: Boolean = false) {
        val value = getChildValue(child, allowAttrs)

        if (value != requiredValue) {
            throw Exception("Child <${child.nodeName}>\n   Got value: $value\n   instead of expected: $requiredValue")
        }
    }
}
