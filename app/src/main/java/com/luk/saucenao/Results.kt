package com.luk.saucenao

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.*

class Results(document: Document) {
    val results = ArrayList<Result>()

    init {
        for (result in document.getElementsByClass(CLASS_RESULT_TABLE)) {
            Result(result).let {
                // Skip hidden results
                if (it.thumbnail != "images/static/hidden.png") {
                    results.add(it)
                }
            }
        }
    }

    inner class Result(result: Element) {
        private val resultMatchInfo = result.getElementsByClass(CLASS_RESULT_MATCH_INFO).first()
        private val resultContentColumns = result.getElementsByClass(CLASS_RESULT_CONTENT_COLUMN)

        val similarity by lazy {
            resultMatchInfo?.getElementsByClass(CLASS_RESULT_SIMILARITY_INFO)?.first()?.text()
        }
        val thumbnail by lazy {
            val resultImage = result.getElementsByClass(CLASS_RESULT_IMAGE).first()
            resultImage?.getElementsByTag("img")?.first()?.let {
                when {
                    it.hasAttr("data-src2") -> it.attr("data-src2")
                    it.hasAttr("data-src") -> it.attr("data-src")
                    it.hasAttr("src") -> it.attr("src")
                    else -> null
                }
            }
        }
        val title by lazy {
            result.getElementsByClass(CLASS_RESULT_TITLE).first()?.let {
                HtmlToPlainText().getPlainText(it)
            }
        }
        val extUrls by lazy {
            val list = arrayListOf<String>()

            val elements = Elements()
            elements.add(resultMatchInfo)
            elements.addAll(resultContentColumns)

            elements.forEach {
                it.getElementsByTag("a").forEach { a ->
                    val href = a.attr("href")
                    if (href.isNotEmpty() && !href.startsWith(URL_LOOKUP_SUBSTRING)) {
                        list.add(href)
                    }
                }
            }

            list.sorted()
        }
        val columns by lazy {
            val list = arrayListOf<String>()
            resultContentColumns.forEach {
                list.add(HtmlToPlainText().getPlainText(it))
            }
            list
        }
    }

    companion object {
        private const val CLASS_RESULT_CONTENT_COLUMN = "resultcontentcolumn"
        private const val CLASS_RESULT_IMAGE = "resultimage"
        private const val CLASS_RESULT_MATCH_INFO = "resultmatchinfo"
        private const val CLASS_RESULT_SIMILARITY_INFO = "resultsimilarityinfo"
        private const val CLASS_RESULT_TABLE = "resulttable"
        private const val CLASS_RESULT_TITLE = "resulttitle"
        private const val URL_LOOKUP_SUBSTRING = "https://saucenao.com/info.php?lookup_type="
    }
}