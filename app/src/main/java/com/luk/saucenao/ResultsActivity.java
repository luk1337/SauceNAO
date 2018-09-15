package com.luk.saucenao;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;

public class ResultsActivity  extends AppCompatActivity {

    private static final String LOG_TAG = ResultsActivity.class.getSimpleName();

    public static final String EXTRA_RESULTS = "extra_results";

    private ClipboardManager mClipboardManager;
    private LayoutInflater mLayoutInflater;
    private TextView mNoResults;
    private Results mResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        setTitle(R.string.results);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mClipboardManager = getSystemService(ClipboardManager.class);
        mLayoutInflater = getSystemService(LayoutInflater.class);
        mNoResults = findViewById(R.id.no_results);
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            String results = bundle.getString(EXTRA_RESULTS);

            if (results != null) {
                try {
                    mResults = new Results(new JSONObject(results));
                    mResults.parse();
                } catch (JSONException e) {
                    Toast.makeText(this, getString(R.string.error_cannot_parse_results),
                            Toast.LENGTH_SHORT).show();
                    Log.e(LOG_TAG, "Unable to parse results", e);
                }
            }
        }

        displayResults();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    private void displayResults() {
        if (mResults == null || mResults.getResults().isEmpty()) {
            mNoResults.setVisibility(View.VISIBLE);
            return;
        }

        LinearLayout results = findViewById(R.id.results);

        for (Results.Result result : mResults.getResults()) {
            View template = mLayoutInflater.inflate(R.layout.card_result, null);
            TextView databaseName = template.findViewById(R.id.database_name);
            TextView metadata = template.findViewById(R.id.metadata);
            TextView similarity = template.findViewById(R.id.similarity);
            TextView title = template.findViewById(R.id.title);

            // Load thumbnail in new task
            new DownloadThumbnailTask(template.findViewById(R.id.thumbnail))
                    .execute(result.mHeader.getThumbnail());

            // Load index specific data
            switch (result.mHeader.getIndexId()) {
                case Results.DATABASE_ID_PIXIV_IMAGES:
                    metadata.setText(String.format("%s: %s\n%s: %s",
                            getString(R.string.metadata_pixiv_id),
                            result.mData.getPixivId(),
                            getString(R.string.metadata_member),
                            result.mData.getMemberName()
                    ));
                    title.setText(result.mData.getTitle());
                    break;
                case Results.DATABASE_ID_NICO_NICO_SEIGA:
                    metadata.setText(String.format("%s: %s\n%s: %s",
                            getString(R.string.metadata_seiga_id),
                            result.mData.getSeigaId(),
                            getString(R.string.metadata_member),
                            result.mData.getMemberName()
                    ));
                    title.setText(result.mData.getTitle());
                    break;
                case Results.DATABASE_ID_ANIME:
                case Results.DATABASE_ID_H_ANIME:
                case Results.DATABASE_ID_SHOWS:
                    metadata.setText(String.format("%s: %s\n%s: %s",
                            getString(R.string.metadata_year),
                            result.mData.getYear(),
                            getString(R.string.metadata_est_time),
                            result.mData.getEstTime()
                    ));
                    title.setText(String.format("%s 一 %s",
                            result.mData.getSource(), result.mData.getPart()));
                    break;
                case Results.DATABASE_ID_SANKAKU_CHANNEL:
                    metadata.setText(String.format("%s: %s",
                            getString(R.string.metadata_creator),
                            result.mData.getCreator()
                    ));
                    break;
                case Results.DATABASE_ID_BCY_COSPLAY:
                    metadata.setText(String.format("%s: %s\n%s: %s",
                            getString(R.string.metadata_bcy_id),
                            result.mData.getBcyId(),
                            getString(R.string.metadata_member),
                            result.mData.getMemberName()
                    ));
                    title.setText(result.mData.getTitle());
                    break;
                case Results.DATABASE_ID_DEVIANTART:
                    metadata.setText(String.format("%s: %s\n%s: %s",
                            getString(R.string.metadata_da_id),
                            result.mData.getDaId(),
                            getString(R.string.metadata_author),
                            result.mData.getAuthorName()
                    ));
                    title.setText(result.mData.getTitle());
                    break;
                case Results.DATABASE_ID_PAWOO:
                    metadata.setText(String.format("%s: %s\n%s: @%s",
                            getString(R.string.metadata_pawoo_id),
                            result.mData.getPawooId(),
                            getString(R.string.metadata_author),
                            result.mData.getPawooUserUsername()
                    ));
                    title.setText(result.mData.getCreatedAt());
                    break;
            }

            // Load global data
            databaseName.setText(Results.DATABASE_NAMES.get(result.mHeader.getIndexId()));
            similarity.setText(result.mHeader.getSimilarity());

            // Set on click listener
            template.findViewById(R.id.card_result).setOnClickListener(view ->
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(result.mData.getExtUrls()[0]))));

            // Set on long click listener
            template.findViewById(R.id.card_result).setOnLongClickListener(view -> {
                mClipboardManager.setPrimaryClip(ClipData.newPlainText("", title.getText()));
                Toast.makeText(this, getString(R.string.title_copied_to_clipboard),
                        Toast.LENGTH_SHORT).show();
                return false;
            });

            results.addView(template);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadThumbnailTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView mImageView;

        DownloadThumbnailTask(ImageView imageView) {
            this.mImageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap thumbnail = null;

            try {
                thumbnail = BitmapFactory.decodeStream(new java.net.URL(params[0]).openStream());
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG,"Invalid thumbnail URL", e);
            } catch (IOException e) {
                Log.e(LOG_TAG,"Unable to load thumbnail", e);
            }

            return thumbnail;
        }

        protected void onPostExecute(Bitmap result) {
            mImageView.setImageBitmap(result);
        }
    }
}
