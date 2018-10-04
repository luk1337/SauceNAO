package com.luk.saucenao;

import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class Results {

    private static final String LOG_TAG = Results.class.getSimpleName();

    private Document mDocument;
    private ArrayList<Result> mResults = new ArrayList<>();

    Results(Document document) {
        mDocument = document;
    }

    public ArrayList<Result> getResults() {
        return mResults;
    }

    public void parse() {
        for (Element result : mDocument.getElementsByClass("resulttable")) {
            Element resultImage = result.getElementsByClass("resultimage").first();
            Element resultMatchInfo = result.getElementsByClass("resultmatchinfo").first();
            Element resultTitle = result.getElementsByClass("resulttitle").first();
            Elements resultContentColumns =
                    result.getElementsByClass("resultcontentcolumn");

            Result newResult = new Result();
            newResult.loadSimilarityInfo(resultMatchInfo);
            newResult.loadThumbnail(resultImage);
            newResult.loadTitle(resultTitle);
            newResult.loadExtUrls(resultMatchInfo);
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

        void loadExtUrls(Element resultMatchInfo) {
            try {
                Element resultMiscInfo =
                        resultMatchInfo.getElementsByClass("resultmiscinfo")
                                .first();

                for (Element a : resultMiscInfo.getElementsByTag("a")) {
                    String href = a.attr("href");

                    if (!href.isEmpty()) {
                        mExtUrls.add(href);
                    }
                }
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                Log.e(LOG_TAG, "Unable to load external URLs", e);
            }
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
