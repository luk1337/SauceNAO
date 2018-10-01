package com.luk.saucenao;

import android.content.Context;
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

    private static final int DATABASE_ID_H_MAGAZINES      = 0;
    private static final int DATABASE_ID_H_GAME_CG        = 2;
    private static final int DATABASE_ID_DOUJINSHIDB      = 3;
    private static final int DATABASE_ID_PIXIV_IMAGES     = 5;
    private static final int DATABASE_ID_PIXIV_HISTORICAL = 6;
    private static final int DATABASE_ID_NICO_NICO_SEIGA  = 8;
    private static final int DATABASE_ID_DANBOORU         = 9;
    private static final int DATABASE_ID_DRAWR_IMAGES     = 10;
    private static final int DATABASE_ID_NIJIE_IMAGES     = 11;
    private static final int DATABASE_ID_YANDERE          = 12;
    private static final int DATABASE_ID_OPENINGS         = 13;
    private static final int DATABASE_ID_SHUTTERSTOCK     = 15;
    private static final int DATABASE_ID_FAKKU            = 16;
    private static final int DATABASE_ID_H_MISC           = 18;
    private static final int DATABASE_ID_2D_MARKET        = 19;
    private static final int DATABASE_ID_MEDIBANG         = 20;
    private static final int DATABASE_ID_ANIME            = 21;
    private static final int DATABASE_ID_H_ANIME          = 22;
    private static final int DATABASE_ID_MOVIES           = 23;
    private static final int DATABASE_ID_SHOWS            = 24;
    private static final int DATABASE_ID_GELBOORU         = 25;
    private static final int DATABASE_ID_KONACHAN         = 26;
    private static final int DATABASE_ID_SANKAKU_CHANNEL  = 27;
    private static final int DATABASE_ID_ANIME_PICTURES   = 28;
    private static final int DATABASE_ID_E621             = 29;
    private static final int DATABASE_ID_IDOL_COMPLEX     = 30;
    private static final int DATABASE_ID_BCY_ILLUST       = 31;
    private static final int DATABASE_ID_BCY_COSPLAY      = 32;
    private static final int DATABASE_ID_PORTALGRAPHICS   = 33;
    private static final int DATABASE_ID_DEVIANTART       = 34;
    private static final int DATABASE_ID_PAWOO            = 35;
    private static final int DATABASE_ID_MANGA            = 36;
    private static final int DATABASE_ID_TBA              = 999;

    public static final HashMap<Integer, String> DATABASE_NAMES = new HashMap<Integer, String>() {{
        put(DATABASE_ID_H_MAGAZINES, "H-Magazines");
        put(DATABASE_ID_H_GAME_CG, "H-Game CG");
        put(DATABASE_ID_DOUJINSHIDB, "DoujinshiDB");
        put(DATABASE_ID_PIXIV_IMAGES, "pixiv Images");
        put(DATABASE_ID_PIXIV_HISTORICAL, "Pixiv Historical");
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
    private ArrayList<Result> mResults = new ArrayList<>();

    Results(JSONObject jsonObject) {
        mJsonObject = jsonObject;
    }

    public ArrayList<Result> getResults() {
        return mResults;
    }

    public void parse() throws JSONException {
        JSONArray results = mJsonObject.getJSONArray(KEY_RESULTS);

        for (int i = 0; i < results.length(); i++) {
            JSONObject result = (JSONObject) results.get(i);
            JSONObject header = result.getJSONObject(KEY_HEADER);
            JSONObject data = result.getJSONObject(KEY_DATA);

            mResults.add(new Result(header, data));
        }
    }

    class Result {

        Header mHeader;
        Data mData;

        Result(JSONObject header, JSONObject data) {
            mHeader = new Header(header);
            mData = new Data(data);
        }

        public String getMetadata(Context context) {
            switch (mHeader.getIndexId()) {
                case DATABASE_ID_H_MAGAZINES:
                    return String.format("%s: %s",
                            context.getString(R.string.metadata_date),
                            mData.getDate()
                    );
                case DATABASE_ID_H_GAME_CG:
                    return String.format("%s: %s\n%s: %s",
                            context.getString(R.string.metadata_company),
                            mData.getCompany(),
                            context.getString(R.string.metadata_getchu_id),
                            mData.getGetchuId()
                    );
                case DATABASE_ID_DOUJINSHIDB:
                    return String.format("%s: %s\n%s: %s",
                            context.getString(R.string.metadata_title),
                            mData.getEngTitle(),
                            context.getString(R.string.metadata_ddb_id),
                            mData.getDdbId()
                    );
                case DATABASE_ID_PIXIV_IMAGES:
                case DATABASE_ID_PIXIV_HISTORICAL:
                    return String.format("%s: %s\n%s: %s",
                            context.getString(R.string.metadata_pixiv_id),
                            mData.getPixivId(),
                            context.getString(R.string.metadata_member),
                            mData.getMemberName()
                    );
                case DATABASE_ID_NICO_NICO_SEIGA:
                    return String.format("%s: %s\n%s: %s",
                            context.getString(R.string.metadata_seiga_id),
                            mData.getSeigaId(),
                            context.getString(R.string.metadata_member),
                            mData.getMemberName()
                    );
                case DATABASE_ID_DRAWR_IMAGES:
                    return String.format("%s: %s\n%s: %s",
                            context.getString(R.string.metadata_drawr_id),
                            mData.getDrawrId(),
                            context.getString(R.string.metadata_est_time),
                            mData.getEstTime()
                    );
                case DATABASE_ID_NIJIE_IMAGES:
                    return String.format("%s: %s\n%s: %s",
                            context.getString(R.string.metadata_nijie_id),
                            mData.getNijieId(),
                            context.getString(R.string.metadata_member),
                            mData.getMemberName()
                    );
                case DATABASE_ID_FAKKU:
                    return String.format("%s: %s",
                            context.getString(R.string.metadata_artist),
                            mData.getCreator()
                    );
                case DATABASE_ID_H_MISC:
                    return String.format("%s: %s\n%s\n%s",
                            context.getString(R.string.metadata_creators),
                            mData.getCreators(),
                            mData.getEngName(),
                            mData.getJpName()
                    );
                case DATABASE_ID_ANIME:
                case DATABASE_ID_H_ANIME:
                case DATABASE_ID_SHOWS:
                    return String.format("%s: %s\n%s: %s",
                            context.getString(R.string.metadata_year),
                            mData.getYear(),
                            context.getString(R.string.metadata_est_time),
                            mData.getEstTime()
                    );
                case DATABASE_ID_DANBOORU:
                case DATABASE_ID_YANDERE:
                case DATABASE_ID_2D_MARKET:
                case DATABASE_ID_GELBOORU:
                case DATABASE_ID_SANKAKU_CHANNEL:
                case DATABASE_ID_E621:
                    return String.format("%s: %s",
                            context.getString(R.string.metadata_creator),
                            mData.getCreator()
                    );
                case DATABASE_ID_BCY_ILLUST:
                    return String.format("%s: %s\n%s: %s",
                            context.getString(R.string.metadata_bcy_id_illust),
                            mData.getBcyId(),
                            context.getString(R.string.metadata_member),
                            mData.getMemberName()
                    );
                case DATABASE_ID_BCY_COSPLAY:
                    return String.format("%s: %s\n%s: %s",
                            context.getString(R.string.metadata_bcy_id_cosplay),
                            mData.getBcyId(),
                            context.getString(R.string.metadata_member),
                            mData.getMemberName()
                    );
                case DATABASE_ID_DEVIANTART:
                    return String.format("%s: %s\n%s: %s",
                            context.getString(R.string.metadata_da_id),
                            mData.getDaId(),
                            context.getString(R.string.metadata_author),
                            mData.getAuthorName()
                    );
                case DATABASE_ID_PAWOO:
                    return String.format("%s: %s\n%s: @%s",
                            context.getString(R.string.metadata_pawoo_id),
                            mData.getPawooId(),
                            context.getString(R.string.metadata_author),
                            mData.getPawooUserUsername()
                    );
                case DATABASE_ID_MANGA:
                    return String.format("%s 一 (%s)", mData.getPart(), mData.getType());
                default:
                    Log.w(LOG_TAG, "Unhandled database id: " + mHeader.getIndexId() +
                            " in getMetadata()");
                    return "";
            }
        }

        public String getTitle() {
            switch (mHeader.getIndexId()) {
                case DATABASE_ID_H_MAGAZINES:
                    return String.format("%s 一 %s", mData.getTitle(), mData.getPart());
                case DATABASE_ID_H_GAME_CG:
                case DATABASE_ID_DOUJINSHIDB:
                case DATABASE_ID_PIXIV_IMAGES:
                case DATABASE_ID_PIXIV_HISTORICAL:
                case DATABASE_ID_NICO_NICO_SEIGA:
                case DATABASE_ID_DRAWR_IMAGES:
                case DATABASE_ID_NIJIE_IMAGES:
                case DATABASE_ID_BCY_ILLUST:
                case DATABASE_ID_BCY_COSPLAY:
                case DATABASE_ID_DEVIANTART:
                    return mData.getTitle();
                case DATABASE_ID_FAKKU:
                case DATABASE_ID_H_MISC:
                case DATABASE_ID_2D_MARKET:
                    return mData.getSource();
                case DATABASE_ID_ANIME:
                case DATABASE_ID_H_ANIME:
                case DATABASE_ID_SHOWS:
                case DATABASE_ID_MANGA:
                    return String.format("%s 一 %s", mData.getSource(), mData.getPart());
                case DATABASE_ID_PAWOO:
                    return mData.getCreatedAt();
                default:
                    Log.w(LOG_TAG, "Unhandled database id: " + mHeader.getIndexId() +
                            " in getTitle()");
                    return "";
            }
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
                if (!jsonObject.isNull(key)) {
                    return jsonObject.getString(key);
                }
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
                String[] indexName = getIndexName().split(":");

                if (indexName.length >= 2 && indexName[0].startsWith("Index #")) {
                    return Integer.valueOf(indexName[0].substring("Index #".length()));
                }

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
            private static final String KEY_COMPANY = "company";
            private static final String KEY_CREATED_AT = "created_at";
            private static final String KEY_CREATOR = "creator";
            private static final String KEY_DA_ID = "da_id";
            private static final String KEY_DANBOORU_ID = "danbooru_id";
            private static final String KEY_DATE_ID = "date";
            private static final String KEY_DDB_ID = "ddb_id";
            private static final String KEY_DRAWR_ID = "drawr_id";
            private static final String KEY_ENG_NAME = "eng_name";
            private static final String KEY_ENG_TITLE = "eng_title";
            private static final String KEY_EST_TIME = "est_time";
            private static final String KEY_EXT_URLS = "ext_urls";
            private static final String KEY_GELBOORU_ID = "gelbooru_id";
            private static final String KEY_GETCHU_ID = "getchu_id";
            private static final String KEY_JP_NAME = "jp_name";
            private static final String KEY_MEMBER_ID = "member_id";
            private static final String KEY_MEMBER_LINK_ID = "member_link_id";
            private static final String KEY_MEMBER_NAME = "member_name";
            private static final String KEY_MU_ID = "mu_id";
            private static final String KEY_NIJIE_ID = "nijie_id";
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
            private static final String KEY_TYPE = "type";
            private static final String KEY_YANDERE_ID = "yandere_id";
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

            public String getCompany() {
                return getString(mJsonObject, KEY_COMPANY, "");
            }

            public String getCreatedAt() {
                return getString(mJsonObject, KEY_CREATED_AT, "");
            }

            public String getCreator() {
                return getString(mJsonObject, KEY_CREATOR, "");
            }

            public String getCreators() {
                JSONArray jsonArray = getArray(mJsonObject, KEY_CREATOR, new JSONArray());
                String[] creators = new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        creators[i] = jsonArray.getString(i);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Unable to read creator at index: " + i);
                    }
                }

                return String.join(", ", creators);
            }

            public int getDaId() {
                return getInt(mJsonObject, KEY_DA_ID, -1);
            }

            public int getDanbooruId() {
                return getInt(mJsonObject, KEY_DANBOORU_ID, -1);
            }

            public String getDate() {
                return getString(mJsonObject, KEY_DATE_ID, "");
            }

            public int getDdbId() {
                return getInt(mJsonObject, KEY_DDB_ID, -1);
            }

            public int getDrawrId() {
                return getInt(mJsonObject, KEY_DRAWR_ID, -1);
            }

            public String getEngName() {
                return getString(mJsonObject, KEY_ENG_NAME, "");
            }

            public String getEngTitle() {
                return getString(mJsonObject, KEY_ENG_TITLE, "");
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

            public int getGelbooruId() {
                return getInt(mJsonObject, KEY_GELBOORU_ID, -1);
            }

            public int getGetchuId() {
                return getInt(mJsonObject, KEY_GETCHU_ID, -1);
            }

            public String getJpName() {
                return getString(mJsonObject, KEY_JP_NAME, "");
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

            public String getMuId() {
                return getString(mJsonObject, KEY_MU_ID, "");
            }

            public String getNijieId() {
                return getString(mJsonObject, KEY_NIJIE_ID, "");
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

            public int getYandereId() {
                return getInt(mJsonObject, KEY_YANDERE_ID, -1);
            }

            public String getType() {
                return getString(mJsonObject, KEY_TYPE, "");
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
