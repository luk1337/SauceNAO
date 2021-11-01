package com.luk.saucenao

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor

/**
 * HTML to plain-text. This example program demonstrates the use of jsoup to convert HTML input to
 * lightly-formatted plain-text. That is divergent from the general goal of jsoup's .text() methods,
 * which is to get clean data from a scrape.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
class HtmlToPlainText {
    /**
     * Format an Element to plain-text
     *
     * @param element the root element to format
     * @return formatted text
     */
    fun getPlainText(element: Element?): String {
        val formatter = FormattingVisitor()
        NodeTraversor.traverse(formatter, element)
        return formatter.toString().trim { it <= ' ' }
    }

    private class FormattingVisitor : NodeVisitor {
        private val accumulator = StringBuilder()
        private var width = 0

        override fun head(node: Node, depth: Int) {
            val name = node.nodeName()
            when {
                node is TextNode -> append(node.text())
                name == "li" -> append("\n * ")
                name == "dt" -> append("  ")
                listOf("p", "h1", "h2", "h3", "h4", "h5", "tr").contains(name) -> append("\n")
                name == "strong" -> append(" ")
            }
        }

        // Hit when all of the node's children (if any) have been visited
        override fun tail(node: Node, depth: Int) {
            val name = node.nodeName()
            if (listOf("br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5").contains(name)) {
                append("\n")
            }
        }

        // Appends text to the string builder with a simple word wrap method
        private fun append(text: String) {
            // Reset counter if starts with a newline. only from formats above, not in natural text
            if (text.startsWith("\n")) {
                width = 0
            }

            // Don't accumulate long runs of empty spaces
            if (text == " " &&
                (accumulator.isEmpty() || listOf(' ', '\n').contains(accumulator.last()))
            ) {
                return
            }

            // Won't fit, needs to wrap
            if (text.length + width > MAX_WIDTH) {
                val words = text.split("\\s+").toTypedArray()
                for (i in words.indices) {
                    var word = words[i]
                    val last = i == words.size - 1

                    // Insert a space if not the last word
                    if (!last) {
                        word = "$word "
                    }

                    // Wrap and reset counter
                    if (word.length + width > MAX_WIDTH) {
                        accumulator.append("\n").append(word)
                        width = word.length
                    } else {
                        accumulator.append(word)
                        width += word.length
                    }
                }
            } else {
                // Fits as is, without need to wrap text
                accumulator.append(text)
                width += text.length
            }
        }

        override fun toString(): String {
            return accumulator.toString()
        }

        companion object {
            const val MAX_WIDTH = 80
        }
    }
}