package com.luk.saucenao;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Results {

    private static final String LOG_TAG = Results.class.getSimpleName();

    private static final String KEY_DATA = "data";
    private static final String KEY_HEADER = "header";
    private static final String KEY_RESULTS = "results";

    public static final int DATABASE_ID_H_MAGAZINES     = 0;
    public static final int DATABASE_ID_H_GAME_CG       = 2;
    public static final int DATABASE_ID_DOUJINSHIDB     = 3;
    public static final int DATABASE_ID_PIXIV_IMAGES    = 5;
    public static final int DATABASE_ID_NICO_NICO_SEIGA = 8;
    public static final int DATABASE_ID_DANBOORU        = 9;
    public static final int DATABASE_ID_DRAWR_IMAGES    = 10;
    public static final int DATABASE_ID_NIJIE_IMAGES    = 11;
    public static final int DATABASE_ID_YANDERE         = 12;
    public static final int DATABASE_ID_OPENINGS        = 13;
    public static final int DATABASE_ID_SHUTTERSTOCK    = 15;
    public static final int DATABASE_ID_FAKKU           = 16;
    public static final int DATABASE_ID_H_MISC          = 18;
    public static final int DATABASE_ID_2D_MARKET       = 19;
    public static final int DATABASE_ID_MEDIBANG        = 20;
    public static final int DATABASE_ID_ANIME           = 21;
    public static final int DATABASE_ID_H_ANIME         = 22;
    public static final int DATABASE_ID_MOVIES          = 23;
    public static final int DATABASE_ID_SHOWS           = 24;
    public static final int DATABASE_ID_GELBOORU        = 25;
    public static final int DATABASE_ID_KONACHAN        = 26;
    public static final int DATABASE_ID_SANKAKU_CHANNEL = 27;
    public static final int DATABASE_ID_ANIME_PICTURES  = 28;
    public static final int DATABASE_ID_E621            = 29;
    public static final int DATABASE_ID_IDOL_COMPLEX    = 30;
    public static final int DATABASE_ID_BCY_ILLUST      = 31;
    public static final int DATABASE_ID_BCY_COSPLAY     = 32;
    public static final int DATABASE_ID_PORTALGRAPHICS  = 33;
    public static final int DATABASE_ID_DEVIANTART      = 34;
    public static final int DATABASE_ID_PAWOO           = 35;
    public static final int DATABASE_ID_MANGA           = 36;
    public static final int DATABASE_ID_TBA             = 999;

    public static final HashMap<Integer, String> DATABASE_NAMES = new HashMap<Integer, String>() {{
        put(DATABASE_ID_H_MAGAZINES, "H-Magazines");
        put(DATABASE_ID_H_GAME_CG, "H-Game CG");
        put(DATABASE_ID_DOUJINSHIDB, "DoujinshiDB");
        put(DATABASE_ID_PIXIV_IMAGES, "pixiv Images");
        put(DATABASE_ID_NICO_NICO_SEIGA, "Nico Nico Seiga");
        put(DATABASE_ID_DANBOORU, "Danbooru");
        put(DATABASE_ID_DRAWR_IMAGES, "drawr Images");
        put(DATABASE_ID_NIJIE_IMAGES, "Nijie Images");
        put(DATABASE_ID_YANDERE, "Yande.re");
        put(DATABASE_ID_OPENINGS, "Openings.moe");
        put(DATABASE_ID_SHUTTERSTOCK, "Shutterstock");
        put(DATABASE_ID_FAKKU, "FAKKU");
        put(DATABASE_ID_H_MISC, "H-Misc");
        put(DATABASE_ID_2D_MARKET, "2D-Market");
        put(DATABASE_ID_MEDIBANG, "MediBang");
        put(DATABASE_ID_ANIME, "Anime");
        put(DATABASE_ID_H_ANIME, "H-Anime");
        put(DATABASE_ID_MOVIES, "Movies");
        put(DATABASE_ID_SHOWS, "Shows");
        put(DATABASE_ID_GELBOORU, "Gelbooru");
        put(DATABASE_ID_KONACHAN, "Konachan");
        put(DATABASE_ID_SANKAKU_CHANNEL, "Sankaku Channel");
        put(DATABASE_ID_ANIME_PICTURES, "Anime-Pictures.net");
        put(DATABASE_ID_E621, "e621.net");
        put(DATABASE_ID_IDOL_COMPLEX, "Idol Complex");
        put(DATABASE_ID_BCY_ILLUST, "bcy.net Illust");
        put(DATABASE_ID_BCY_COSPLAY, "bcy.net Cosplay");
        put(DATABASE_ID_PORTALGRAPHICS, "PortalGraphics.net (Hist)");
        put(DATABASE_ID_DEVIANTART, "deviantArt");
        put(DATABASE_ID_PAWOO, "Pawoo.net");
        put(DATABASE_ID_MANGA, "Manga");
        put(DATABASE_ID_TBA, "TBA...");
    }};

    private JSONObject mJsonObject;
    private ArrayList<Result> mResults = new ArrayList<>();;

    Results(JSONObject jsonObject) {
        mJsonObject = jsonObject;
    }

    public ArrayList<Result> getResults() {
        return mResults;
    }

    public boolean parse() throws JSONException {
        JSONArray results = mJsonObject.getJSONArray(KEY_RESULTS);

        for (int i = 0; i < results.length(); i++) {
            JSONObject result = (JSONObject) results.get(i);
            JSONObject header = result.getJSONObject(KEY_HEADER);
            JSONObject data = result.getJSONObject(KEY_DATA);

            mResults.add(new Result(header, data));
        }

        return true;
    }

    class Result {

        Header mHeader;
        Data mData;

        Result(JSONObject header, JSONObject data) {
            mHeader = new Header(header);
            mData = new Data(data);
        }

        private JSONArray getArray(JSONObject jsonObject, String key, JSONArray def) {
            try {
                return jsonObject.getJSONArray(key);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to read key: " + key);
            }
            return def;
        }

        private int getInt(JSONObject jsonObject, String key, int def) {
            try {
                return jsonObject.getInt(key);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to read key: " + key);
            }
            return def;
        }

        private String getString(JSONObject jsonObject, String key, String def) {
            try {
                return jsonObject.getString(key);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to read key: " + key);
            }
            return def;
        }

        class Header {

            private static final String KEY_INDEX_ID = "index_id";
            private static final String KEY_INDEX_NAME = "index_name";
            private static final String KEY_SIMILARITY = "similarity";
            private static final String KEY_THUMBNAIL = "thumbnail";

            private JSONObject mJsonObject;

            Header(JSONObject jsonObject) {
                mJsonObject = jsonObject;
            }

            public int getIndexId() {
                return getInt(mJsonObject, KEY_INDEX_ID, -1);
            }

            public String getIndexName() {
                return getString(mJsonObject, KEY_INDEX_NAME, "");
            }

            public String getSimilarity() {
                return getString(mJsonObject, KEY_SIMILARITY, "0") + "%";
            }

            public String getThumbnail() {
                return getString(mJsonObject, KEY_THUMBNAIL, "");
            }
        }

        class Data {

            private static final String KEY_ANIDB_AID = "anidb_aid";
            private static final String KEY_AUTHOR_NAME = "author_name";
            private static final String KEY_AUTHOR_URL = "author_url";
            private static final String KEY_BCY_ID = "bcy_id";
            private static final String KEY_BCY_TYPE =  "bcy_type";
            private static final String KEY_CREATED_AT = "created_at";
            private static final String KEY_CREATOR = "creator";
            private static final String KEY_DA_ID = "da_id";
            private static final String KEY_EST_TIME = "est_time";
            private static final String KEY_EXT_URLS = "ext_urls";
            private static final String KEY_MEMBER_ID = "member_id";
            private static final String KEY_MEMBER_LINK_ID = "member_link_id";
            private static final String KEY_MEMBER_NAME = "member_name";
            private static final String KEY_PART = "part";
            private static final String KEY_PAWOO_ID = "pawoo_id";
            private static final String KEY_PAWOO_USER_ACCT = "pawoo_user_acct";
            private static final String KEY_PAWOO_USER_DISPLAY_NAME = "pawoo_user_display_name";
            private static final String KEY_PAWOO_USER_USERNAME = "pawoo_user_username";
            private static final String KEY_PIXIV_ID = "pixiv_id";
            private static final String KEY_SANKAKU_ID = "sankaku_id";
            private static final String KEY_SEIGA_ID = "seiga_id";
            private static final String KEY_SOURCE = "source";
            private static final String KEY_TITLE = "title";
            private static final String KEY_YEAR = "year";

            private JSONObject mJsonObject;

            Data(JSONObject jsonObject) {
                mJsonObject = jsonObject;
            }

            public int getAnidbAid() {
                return getInt(mJsonObject, KEY_ANIDB_AID, -1);
            }

            public String getAuthorName() {
                return getString(mJsonObject, KEY_AUTHOR_NAME, "");
            }

            public String getAuthorUrl() {
                return getString(mJsonObject, KEY_AUTHOR_URL, "");
            }

            public int getBcyId() {
                return getInt(mJsonObject, KEY_BCY_ID, -1);
            }

            public String getBcyType() {
                return getString(mJsonObject, KEY_BCY_TYPE, "");
            }

            public String getCreatedAt() {
                return getString(mJsonObject, KEY_CREATED_AT, "");
            }

            public String getCreator() {
                return getString(mJsonObject, KEY_CREATOR, "");
            }

            public int getDaId() {
                return getInt(mJsonObject, KEY_DA_ID, -1);
            }

            public String getEstTime() {
                return getString(mJsonObject, KEY_EST_TIME, "");
            }

            public String[] getExtUrls() {
                JSONArray jsonArray = getArray(mJsonObject, KEY_EXT_URLS, new JSONArray());
                String[] extUrls = new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        extUrls[i] = jsonArray.getString(i);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Unable to read external URL at index: " + i);
                    }
                }

                return extUrls;
            }

            public int getMemberLinkId() {
                return getInt(mJsonObject, KEY_MEMBER_LINK_ID, -1);
            }

            public int getMemberId() {
                return getInt(mJsonObject, KEY_MEMBER_ID, -1);
            }

            public String getMemberName() {
                return getString(mJsonObject, KEY_MEMBER_NAME, "");
            }

            public String getPart() {
                return getString(mJsonObject, KEY_PART, "");
            }

            public int getPawooId() {
                return getInt(mJsonObject, KEY_PAWOO_ID, -1);
            }

            public String getPawooUserAcct() {
                return getString(mJsonObject, KEY_PAWOO_USER_ACCT, "");
            }

            public String getPawooUserUsername() {
                return getString(mJsonObject, KEY_PAWOO_USER_USERNAME, "");
            }

            public String getPawooUserDisplayName() {
                return getString(mJsonObject, KEY_PAWOO_USER_DISPLAY_NAME, "");
            }

            public int getPixivId() {
                return getInt(mJsonObject, KEY_PIXIV_ID, -1);
            }

            public int getSankakuId() {
                return getInt(mJsonObject, KEY_SANKAKU_ID, -1);
            }

            public int getSeigaId() {
                return getInt(mJsonObject, KEY_SEIGA_ID, -1);
            }

            public String getSource() {
                return getString(mJsonObject, KEY_SOURCE, "");
            }

            public String getTitle() {
                return getString(mJsonObject, KEY_TITLE, "");
            }

            public String getYear() {
                String[] year = getString(mJsonObject, KEY_YEAR, "").split("-");

                if (year.length != 2) {
                    return "";
                }

                if (year[0].equals(year[1])) {
                    return year[0];
                }

                return String.join("-", year);
            }
        }
    }
}
