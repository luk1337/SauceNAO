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
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
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
            metadata.setText(result.getMetadata(this));
            title.setText(result.getTitle());

            // Load global data
            databaseName.setText(Results.DATABASE_NAMES.get(result.mHeader.getIndexId()));
            similarity.setText(result.mHeader.getSimilarity());

            // Set on click listener
            template.findViewById(R.id.card_result).setOnClickListener(view -> {
                String[] extUrls = result.mData.getExtUrls();

                if (extUrls.length == 0) {
                    return;
                }

                if (extUrls.length == 1) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(extUrls[0])));
                    return;
                }

                PopupMenu popupMenu = new PopupMenu(this, view);

                for (int i = 0; i < extUrls.length; i++) {
                    popupMenu.getMenu().add(0, i, i, extUrls[i]);
                }

                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(item -> {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(extUrls[item.getItemId()])));
                    return true;
                });
            });

            // Set on long click listener
            template.findViewById(R.id.card_result).setOnLongClickListener(view -> {
                PopupMenu popupMenu = new PopupMenu(this, view);
                popupMenu.inflate(R.menu.card_long_press);
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.copy_to_clipboard_item:
                            mClipboardManager.setPrimaryClip(
                                    ClipData.newPlainText("", title.getText()));
                            Toast.makeText(this,
                                    getString(R.string.title_copied_to_clipboard),
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.share_item:
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, title.getText());
                            startActivity(Intent.createChooser(intent,
                                    getString(R.string.abc_shareactionprovider_share_with)));
                            break;
                    }
                    return true;
                });
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
                Log.e(LOG_TAG, "Invalid thumbnail URL", e);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to load thumbnail", e);
            }

            return thumbnail;
        }

        protected void onPostExecute(Bitmap result) {
            mImageView.setImageBitmap(result);
        }
    }
}
