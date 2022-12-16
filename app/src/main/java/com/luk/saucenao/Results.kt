package com.luk.saucenao

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import kotlin.collections.ArrayList

class Results(document: Document) {
    val results = ArrayList<Result>()

    val serverError by lazy {
        document.getElementsByClass("servererror").firstOrNull()?.text()
    }

    init {
        for (result in document.getElementsByClass(CLASS_RESULT_TABLE)) {
            HttpResult(result).let {
                // Skip hidden results
                if (it.thumbnail != "images/static/hidden.png") {
                    results.add(it)
                }
            }
        }
    }

    abstract class Result {
        abstract val similarity: String?
        abstract val thumbnail: String?
        abstract var title: String?
        abstract val extUrls: List<String>
        abstract val columns: ArrayList<String>
    }

    inner class HttpResult(result: Element) : Result() {
        private val resultMatchInfo = result.getElementsByClass(CLASS_RESULT_MATCH_INFO).first()
        private val resultContentColumns = result.getElementsByClass(CLASS_RESULT_CONTENT_COLUMN)

        override val similarity = run {
            resultMatchInfo?.getElementsByClass(CLASS_RESULT_SIMILARITY_INFO)?.first()?.text()
        }
        override val thumbnail = run {
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
        override var title: String? = null
        override val extUrls = run {
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
        override val columns = run {
            val list = arrayListOf<String>()
            resultContentColumns.forEach {
                list.add(HtmlToPlainText().getPlainText(it))
            }
            list
        }

        init {
            result.getElementsByClass(CLASS_RESULT_TITLE).first()?.let {
                HtmlToPlainText().getPlainText(it)
            }?.let {
                val titleAndMetadata = it.split("\n", limit = 2).toTypedArray()
                if (titleAndMetadata.isNotEmpty()) {
                    title = titleAndMetadata[0]
                    if (titleAndMetadata.size == 2) {
                        columns.add(0, titleAndMetadata[1])
                    }
                }
            }
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