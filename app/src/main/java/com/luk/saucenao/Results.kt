package com.luk.saucenao

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.*

class Results(document: Document) {
    val results = ArrayList<Result>()

    init {
        for (result in document.getElementsByClass(CLASS_RESULT_TABLE)) {
            val resultImage = result.getElementsByClass(CLASS_RESULT_IMAGE).first()
            val resultMatchInfo = result.getElementsByClass(CLASS_RESULT_MATCH_INFO).first()
            val resultTitle = result.getElementsByClass(CLASS_RESULT_TITLE).first()
            val resultContentColumns = result.getElementsByClass(CLASS_RESULT_CONTENT_COLUMN)

            val newResult = Result()
            newResult.loadSimilarityInfo(resultMatchInfo)
            newResult.loadThumbnail(resultImage)
            newResult.loadTitle(resultTitle)
            newResult.loadExtUrls(resultMatchInfo, resultContentColumns)
            newResult.loadColumns(resultContentColumns)

            // Skip hidden results
            if (newResult.thumbnail != "images/static/hidden.png") {
                results.add(newResult)
            }
        }
    }

    inner class Result {
        var similarity: String? = null
        var thumbnail: String? = null
        var title: String? = null
        var extUrls = ArrayList<String>()
        var columns = ArrayList<String>()

        fun loadSimilarityInfo(resultMatchInfo: Element) {
            resultMatchInfo.getElementsByClass(CLASS_RESULT_SIMILARITY_INFO).first()?.let {
                similarity = it.text()
            }
        }

        fun loadThumbnail(resultImage: Element) {
            resultImage.getElementsByTag("img").first()?.let {
                if (it.hasAttr("data-src")) {
                    thumbnail = it.attr("data-src")
                } else if (it.hasAttr("src")) {
                    thumbnail = it.attr("src")
                }
            }
        }

        fun loadTitle(resultTitle: Element) {
            title = HtmlToPlainText().getPlainText(resultTitle)
        }

        fun loadExtUrls(resultMatchInfo: Element, resultContentColumns: Elements) {
            val elements = Elements()
            elements.add(resultMatchInfo)
            elements.addAll(resultContentColumns)

            for (element in elements) {
                for (a in element.getElementsByTag("a")) {
                    val href = a.attr("href")
                    if (href.isNotEmpty() && !href.startsWith(URL_LOOKUP_SUBSTRING)) {
                        extUrls.add(href)
                    }
                }
            }

            extUrls.sort()
        }

        fun loadColumns(resultContentColumns: Elements) {
            for (resultContentColumn in resultContentColumns) {
                columns.add(HtmlToPlainText().getPlainText(resultContentColumn))
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