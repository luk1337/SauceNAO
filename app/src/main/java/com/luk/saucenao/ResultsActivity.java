package com.luk.saucenao;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;

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

        mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mNoResults = findViewById(R.id.no_results);
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            String results = bundle.getString(EXTRA_RESULTS);

            if (results != null) {
                mResults = new Results(Jsoup.parse(results));
                mResults.parse();
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
            TextView metadata = template.findViewById(R.id.metadata);
            TextView similarity = template.findViewById(R.id.similarity);
            TextView title = template.findViewById(R.id.title);

            // Load thumbnail in new task
            new DownloadThumbnailTask(template.findViewById(R.id.thumbnail))
                    .execute(result.mThumbnail);

            // Load index specific data
            String[] titleAndMetadata = result.mTitle.split("\n", 2);

            if (titleAndMetadata.length > 0) {
                title.setText(titleAndMetadata[0]);

                if (titleAndMetadata.length == 2) {
                    result.mColumns.add(0, titleAndMetadata[1]);
                }
            }

            metadata.setText(TextUtils.join("\n", result.mColumns));

            // Load global data
            similarity.setText(result.mSimilarity);

            // Set on click listener
            template.findViewById(R.id.card_result).setOnClickListener(view -> {
                if (result.mExtUrls.size() == 0) {
                    return;
                }

                if (result.mExtUrls.size() == 1) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(result.mExtUrls.get(0))));
                    return;
                }

                PopupMenu popupMenu = new PopupMenu(this, view);

                for (int i = 0; i < result.mExtUrls.size(); i++) {
                    popupMenu.getMenu().add(0, i, i, result.mExtUrls.get(i));
                }

                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(item -> {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(result.mExtUrls.get(item.getItemId()))));
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
