package com.luk.saucenao;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ResultsActivity  extends AppCompatActivity {

    private static final String LOG_TAG = ResultsActivity.class.getSimpleName();

    public static final String EXTRA_RESULTS = "extra_results";

    private ClipboardManager mClipboardManager;
    private Results mResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            String results = bundle.getString(EXTRA_RESULTS);

            if (results != null) {
                mResults = new Results(Jsoup.parse(results));
            }
        }

        ResultsRecyclerView resultsRecyclerView = findViewById(R.id.results);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsRecyclerView.setEmptyView(findViewById(R.id.no_results));
        resultsRecyclerView.setAdapter(new ResultsAdapter(mResults.getResults()));
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

    private static class DownloadThumbnailTask extends AsyncTask<String, Void, Bitmap> {

        private WeakReference<ImageView> mImageView;

        DownloadThumbnailTask(ImageView imageView) {
            mImageView = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                return BitmapFactory.decodeStream(new URL(params[0]).openStream());
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Invalid thumbnail URL", e);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to load thumbnail", e);
            }

            return null;
        }

        protected void onPostExecute(Bitmap result) {
            ImageView imageView = mImageView.get();

            if (imageView != null) {
                imageView.setImageBitmap(result);
            }
        }
    }

    public class ResultsAdapter extends
            ResultsRecyclerView.Adapter<ResultsAdapter.ResultsViewHolder> {

        private ArrayList<Results.Result> mResults;

        class ResultsViewHolder extends RecyclerView.ViewHolder implements
                View.OnClickListener, View.OnLongClickListener {

            ArrayList<String> mExtUrls;
            ImageView mThumbnail;
            TextView mMetadata;
            TextView mSimilarity;
            TextView mTitle;

            ResultsViewHolder(View view) {
                super(view);

                mThumbnail = view.findViewById(R.id.thumbnail);
                mMetadata = view.findViewById(R.id.metadata);
                mSimilarity = view.findViewById(R.id.similarity);
                mTitle = view.findViewById(R.id.title);

                View cardResult = view.findViewById(R.id.card_result);
                cardResult.setOnClickListener(this);
                cardResult.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (mExtUrls.size() == 0) {
                    return;
                }

                if (mExtUrls.size() == 1) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(mExtUrls.get(0))));
                    return;
                }

                PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

                for (int i = 0; i < mExtUrls.size(); i++) {
                    popupMenu.getMenu().add(0, i, i, mExtUrls.get(i));
                }

                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(item -> {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(mExtUrls.get(item.getItemId()))));
                    return true;
                });
            }

            @Override
            public boolean onLongClick(View view) {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                popupMenu.inflate(R.menu.card_long_press);
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.copy_to_clipboard_item:
                            mClipboardManager.setPrimaryClip(
                                    ClipData.newPlainText("", mTitle.getText()));
                            Toast.makeText(view.getContext(),
                                    getString(R.string.title_copied_to_clipboard),
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.share_item:
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, mTitle.getText());
                            startActivity(Intent.createChooser(intent,
                                    getString(R.string.abc_shareactionprovider_share_with)));
                            break;
                    }
                    return true;
                });
                return false;
            }
        }

        ResultsAdapter(ArrayList<Results.Result> results) {
            mResults = results;
        }

        @NonNull
        @Override
        public ResultsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_result, viewGroup, false);

            return new ResultsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ResultsViewHolder resultsViewHolder, int i) {
            Results.Result result = mResults.get(i);

            // Load thumbnail in new task
            new DownloadThumbnailTask(resultsViewHolder.mThumbnail).execute(result.mThumbnail);

            // Load index specific data
            String[] titleAndMetadata = result.mTitle.split("\n", 2);

            if (titleAndMetadata.length > 0) {
                resultsViewHolder.mTitle.setText(titleAndMetadata[0]);

                if (titleAndMetadata.length == 2) {
                    result.mColumns.add(0, titleAndMetadata[1]);
                }
            }

            resultsViewHolder.mMetadata.setText(TextUtils.join("\n", result.mColumns));

            // Load global data
            resultsViewHolder.mExtUrls = result.mExtUrls;
            resultsViewHolder.mSimilarity.setText(result.mSimilarity);
        }

        @Override
        public int getItemCount() {
            return mResults.size();
        }
    }
}
