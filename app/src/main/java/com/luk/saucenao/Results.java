package com.luk.saucenao;

import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;

public class Results {

    private static final String LOG_TAG = Results.class.getSimpleName();

    private static final String CLASS_RESULT_CONTENT_COLUMN = "resultcontentcolumn";
    private static final String CLASS_RESULT_IMAGE          = "resultimage";
    private static final String CLASS_RESULT_MATCH_INFO     = "resultmatchinfo";
    private static final String CLASS_RESULT_TABLE          = "resulttable";
    private static final String CLASS_RESULT_TITLE          = "resulttitle";

    private static final String URL_LOOKUP_SUBSTRING = "https://saucenao.com/info.php?lookup_type=";

    private Document mDocument;
    private ArrayList<Result> mResults = new ArrayList<>();

    Results(Document document) {
        mDocument = document;
    }

    public ArrayList<Result> getResults() {
        return mResults;
    }

    public void parse() {
        for (Element result : mDocument.getElementsByClass(CLASS_RESULT_TABLE)) {
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

    class Result {
        String mSimilarity;
        String mThumbnail;
        String mTitle;
        ArrayList<String> mExtUrls = new ArrayList<>();
        ArrayList<String> mColumns = new ArrayList<>();

        void loadSimilarityInfo(Element resultMatchInfo) {
            try {
                mSimilarity = resultMatchInfo.getElementsByClass("resultsimilarityinfo")
                        .first()
                        .text();
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, "Unable to load similarity info", e);
            }
        }

        void loadThumbnail(Element resultImage) {
            try {
                mThumbnail = resultImage.child(0).child(0).attr("data-src");
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                Log.e(LOG_TAG, "Unable to load thumbnail", e);
            }
        }

        void loadTitle(Element resultTitle) {
            try {
                mTitle = new HtmlToPlainText().getPlainText(resultTitle);
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, "Unable to load title", e);
            }
        }

        void loadExtUrls(Element resultMatchInfo, Elements resultContentColumns) {
            try {
                for (Element a : resultMatchInfo.getElementsByTag("a")) {
                    String href = a.attr("href");

                    if (!href.isEmpty() && !href.startsWith(URL_LOOKUP_SUBSTRING)) {
                        mExtUrls.add(href);
                    }
                }

                for (Element resultContentColumn : resultContentColumns) {
                    for (Element a : resultContentColumn.getElementsByTag("a")) {
                        String href = a.attr("href");

                        if (!href.isEmpty() && !href.startsWith(URL_LOOKUP_SUBSTRING)) {
                            mExtUrls.add(href);
                        }
                    }
                }
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                Log.e(LOG_TAG, "Unable to load external URLs", e);
            }

            Collections.sort(mExtUrls);
        }

        void loadColumns(Elements resultContentColumns) {
            try {
                for (Element resultContentColumn : resultContentColumns) {
                    mColumns.add(new HtmlToPlainText().getPlainText(resultContentColumn));
                }
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, "Unable to load columns", e);
            }
        }
    }
}
