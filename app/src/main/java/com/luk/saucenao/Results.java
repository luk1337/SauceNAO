package com.luk.saucenao;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;

public class Results {

    private static final String LOG_TAG = Results.class.getSimpleName();

    private static final String CLASS_RESULT_CONTENT_COLUMN  = "resultcontentcolumn";
    private static final String CLASS_RESULT_IMAGE           = "resultimage";
    private static final String CLASS_RESULT_MATCH_INFO      = "resultmatchinfo";
    private static final String CLASS_RESULT_SIMILARITY_INFO = "resultsimilarityinfo";
    private static final String CLASS_RESULT_TABLE           = "resulttable";
    private static final String CLASS_RESULT_TITLE           = "resulttitle";

    private static final String URL_LOOKUP_SUBSTRING = "https://saucenao.com/info.php?lookup_type=";

    private ArrayList<Result> mResults = new ArrayList<>();

    Results(Document document) {
        for (Element result : document.getElementsByClass(CLASS_RESULT_TABLE)) {
            Element resultImage = result.getElementsByClass(CLASS_RESULT_IMAGE).first();
            Element resultMatchInfo = result.getElementsByClass(CLASS_RESULT_MATCH_INFO).first();
            Element resultTitle = result.getElementsByClass(CLASS_RESULT_TITLE).first();
            Elements resultContentColumns = result.getElementsByClass(CLASS_RESULT_CONTENT_COLUMN);

            Result newResult = new Result();
            newResult.loadSimilarityInfo(resultMatchInfo);
            newResult.loadThumbnail(resultImage);
            newResult.loadTitle(resultTitle);
            newResult.loadExtUrls(resultMatchInfo, resultContentColumns);
            newResult.loadColumns(resultContentColumns);

            mResults.add(newResult);
        }
    }

    public ArrayList<Result> getResults() {
        return mResults;
    }

    class Result {
        String mSimilarity;
        String mThumbnail;
        String mTitle;
        ArrayList<String> mExtUrls = new ArrayList<>();
        ArrayList<String> mColumns = new ArrayList<>();

        private void loadSimilarityInfo(Element resultMatchInfo) {
            Element similarityInfo =
                    resultMatchInfo.getElementsByClass(CLASS_RESULT_SIMILARITY_INFO).first();

            if (similarityInfo != null) {
                mSimilarity = similarityInfo.text();
            }
        }

        private void loadThumbnail(Element resultImage) {
            Element img = resultImage.getElementsByTag("img").first();

            if (img != null) {
                if (img.hasAttr("data-src")) {
                    mThumbnail = img.attr("data-src");
                } else if (img.hasAttr("src")) {
                    mThumbnail = img.attr("src");
                }
            }
        }

        private void loadTitle(Element resultTitle) {
            mTitle = new HtmlToPlainText().getPlainText(resultTitle);
        }

        private void loadExtUrls(Element resultMatchInfo, Elements resultContentColumns) {
            Elements elements = new Elements();
            elements.add(resultMatchInfo);
            elements.addAll(resultContentColumns);

            for (Element element : elements) {
                for (Element a : element.getElementsByTag("a")) {
                    String href = a.attr("href");

                    if (!href.isEmpty() && !href.startsWith(URL_LOOKUP_SUBSTRING)) {
                        mExtUrls.add(href);
                    }
                }
            }

            Collections.sort(mExtUrls);
        }

        private void loadColumns(Elements resultContentColumns) {
            for (Element resultContentColumn : resultContentColumns) {
                mColumns.add(new HtmlToPlainText().getPlainText(resultContentColumn));
            }
        }
    }
}
