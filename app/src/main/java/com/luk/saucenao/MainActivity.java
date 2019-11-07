package com.luk.saucenao;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_DOCUMENTS = 0;
    private static final int REQUEST_SHARE = 1;

    private static final int REQUEST_RESULT_OK = 0;
    private static final int REQUEST_RESULT_INTERUPTED = 1;
    private static final int REQUEST_RESULT_GENERIC_ERROR = 2;
    private static final int REQUEST_RESULT_TOO_MANY_REQUESTS = 3;

    private int[] mDatabasesValues;
    private ExecutorService mExecutorService;
    private Spinner mSelectDatabaseSpinner;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mExecutorService = Executors.newSingleThreadExecutor();

        mDatabasesValues = getResources().getIntArray(R.array.databases_values);

        mSelectDatabaseSpinner = findViewById(R.id.select_database);

        Button selectImageButton = findViewById(R.id.select_image);
        selectImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");

            startActivityForResult(intent, REQUEST_DOCUMENTS);
        });

        Intent intent = getIntent();

        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            onActivityResult(REQUEST_SHARE, RESULT_OK, intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            return;
        }

        if (data == null) {
            return;
        }

        switch (requestCode) {
            case REQUEST_DOCUMENTS:
                waitForResults(mExecutorService.submit(new GetResultsTask(data.getData())));
                break;
            case REQUEST_SHARE:
                if (data.hasExtra(Intent.EXTRA_STREAM)) {
                    waitForResults(mExecutorService.submit(
                            new GetResultsTask(data.getParcelableExtra(Intent.EXTRA_STREAM))));
                } else if (data.hasExtra(Intent.EXTRA_TEXT)) {
                    waitForResults(mExecutorService.submit(
                            new GetResultsTask(data.getStringExtra(Intent.EXTRA_TEXT))));
                }
                break;
        }
    }

    private void waitForResults(Future<?> future) {
        mProgressDialog = ProgressDialog.show(this,
                getString(R.string.loading_results), getString(R.string.please_wait),
                true, true);
        mProgressDialog.setOnCancelListener(dialog -> future.cancel(true));
    }

    public class GetResultsTask implements Callable<Void> {

        private Object mData;

        GetResultsTask(Object data) {
            mData = data;
        }

        @Override
        public Void call() {
            if (isFinishing()) {
                return null;
            }

            Pair<Integer, String> result = getResult();

            Handler handler = new Handler(getMainLooper());
            handler.post(() -> mProgressDialog.dismiss());

            switch (result.first) {
                case REQUEST_RESULT_OK:
                    Bundle bundle = new Bundle();
                    bundle.putString(ResultsActivity.EXTRA_RESULTS, result.second);

                    Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
                    intent.putExtras(bundle);

                    handler.post(() -> startActivity(intent));
                    break;
                case REQUEST_RESULT_GENERIC_ERROR:
                    handler.post(() -> Toast.makeText(MainActivity.this,
                            getString(R.string.error_cannot_load_results),
                            Toast.LENGTH_SHORT).show());
                    break;
                case REQUEST_RESULT_TOO_MANY_REQUESTS:
                    handler.post(() -> Toast.makeText(MainActivity.this,
                            getString(R.string.error_too_many_requests),
                            Toast.LENGTH_SHORT).show());
                    break;
            }

            return null;
        }

        private Pair<Integer, String> getResult() {
            try {
                int database = mDatabasesValues[mSelectDatabaseSpinner.getSelectedItemPosition()];

                Connection.Response response = null;

                if (mData instanceof Uri) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), (Uri) mData);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Unable to read image bitmap", e);
                        return new Pair<>(REQUEST_RESULT_GENERIC_ERROR, null);
                    }

                    response = Jsoup.connect(
                            "https://saucenao.com/search.php?db=" + database)
                            .data("file", "image.png",
                                    new ByteArrayInputStream(stream.toByteArray()))
                            .method(Connection.Method.POST)
                            .execute();
                } else if (mData instanceof String) {
                    response = Jsoup.connect(
                            "https://saucenao.com/search.php?db=" + database)
                            .data("url", (String) mData)
                            .method(Connection.Method.POST)
                            .execute();
                }

                assert response != null;

                if (response.statusCode() != 200) {
                    Log.e(LOG_TAG, "HTTP request returned code: " + response.statusCode());

                    switch (response.statusCode()) {
                        case 429:
                            return new Pair<>(REQUEST_RESULT_TOO_MANY_REQUESTS, null);
                        default:
                            return new Pair<>(REQUEST_RESULT_GENERIC_ERROR, null);
                    }
                }

                String body = response.body();

                if (body.isEmpty()) {
                    return new Pair<>(REQUEST_RESULT_INTERUPTED, null);
                }

                return new Pair<>(REQUEST_RESULT_OK, body);
            } catch (InterruptedIOException e) {
                return new Pair<>(REQUEST_RESULT_INTERUPTED, null);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to send HTTP request", e);
                return new Pair<>(REQUEST_RESULT_GENERIC_ERROR, null);
            }
        }
    }
}
