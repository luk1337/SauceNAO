package com.luk.saucenao;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 * HTML to plain-text. This example program demonstrates the use of jsoup to convert HTML input to
 * lightly-formatted plain-text. That is divergent from the general goal of jsoup's .text() methods,
 * which is to get clean data from a scrape.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */

public class HtmlToPlainText {

    /**
     * Format an Element to plain-text
     * @param element the root element to format
     * @return formatted text
     */
    public String getPlainText(Element element) {
        FormattingVisitor formatter = new FormattingVisitor();
        NodeTraversor.traverse(formatter, element);

        return formatter.toString().trim();
    }

    private class FormattingVisitor implements NodeVisitor {
        private static final int mMaxWidth = 80;
        private int mWidth = 0;
        private StringBuilder mAccum = new StringBuilder();

        public void head(Node node, int depth) {
            String name = node.nodeName();

            if (node instanceof TextNode)
                append(((TextNode) node).text());
            else if (name.equals("li"))  {
                append("\n * ");
            } else if (name.equals("dt")) {
                append("  ");
            } else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr")) {
                append("\n");
            } else if (name.equals("strong"))  {
                append(" ");
            }
        }

        // Hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();

            if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5")) {
                append("\n");
            }
        }

        // Appends text to the string builder with a simple word wrap method
        private void append(String text) {
            // Reset counter if starts with a newline. only from formats above, not in natural text
            if (text.startsWith("\n")) {
                mWidth = 0;
            }

            // Don't accumulate long runs of empty spaces
            if (text.equals(" ") && (mAccum.length() == 0 || StringUtil.in(
                    mAccum.substring(mAccum.length() - 1), " ", "\n"))) {
                return;
            }

            // Won't fit, needs to wrap
            if (text.length() + mWidth > mMaxWidth) {
                String words[] = text.split("\\s+");

                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    boolean last = i == words.length - 1;

                    // Insert a space if not the last word
                    if (!last) {
                        word = word + " ";
                    }

                    // Wrap and reset counter
                    if (word.length() + mWidth > mMaxWidth) {
                        mAccum.append("\n").append(word);
                        mWidth = word.length();
                    } else {
                        mAccum.append(word);
                        mWidth += word.length();
                    }
                }
            } else {
                // Fits as is, without need to wrap text
                mAccum.append(text);
                mWidth += text.length();
            }
        }

        @Override
        public String toString() {
            return mAccum.toString();
        }
    }
}