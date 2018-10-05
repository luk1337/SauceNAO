package com.luk.saucenao;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_DOCUMENTS = 0;
    private static final int REQUEST_SHARE = 1;
    private static final int REQUEST_RESULT_OK = 0;
    private static final int REQUEST_RESULT_GENERIC_ERROR = 1;
    private static final int REQUEST_RESULT_TOO_MANY_REQUESTS = 2;

    private int[] mDatabasesValues;
    private Button mSelectImageButton;
    private Spinner mSelectDatabaseSpinner;
    private ProgressDialog mProgressDialog;
    private AsyncTask<Uri, Integer, Pair<Integer, String>> mResultTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            onActivityResult(REQUEST_SHARE, RESULT_OK, intent);
        }

        mDatabasesValues = getResources().getIntArray(R.array.databases_values);

        mSelectImageButton = findViewById(R.id.select_image);
        mSelectImageButton.setOnClickListener(this);

        mSelectDatabaseSpinner = findViewById(R.id.select_database);
    }

    @Override
    public void onClick(View view) {
        if (view == mSelectImageButton) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");

            startActivityForResult(intent, REQUEST_DOCUMENTS);
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
                mResultTask = new GetResultsTask(this);
                mResultTask.execute(data.getData());
                mProgressDialog = ProgressDialog.show(this,
                        getString(R.string.loading_results), getString(R.string.please_wait),
                        true, true);
                mProgressDialog.setOnCancelListener(dialog ->
                        mResultTask.cancel(true));
                break;
            case REQUEST_SHARE:
                mResultTask = new GetResultsTask(this);
                mResultTask.execute((Uri) data.getParcelableExtra(Intent.EXTRA_STREAM));
                mProgressDialog = ProgressDialog.show(this,
                        getString(R.string.loading_results), getString(R.string.please_wait),
                        true, true);
                mProgressDialog.setOnCancelListener(dialog ->
                        mResultTask.cancel(true));
                break;
        }
    }

    private static class GetResultsTask extends AsyncTask<Uri, Integer, Pair<Integer, String>> {

        private WeakReference<MainActivity> mMainActivity;

        GetResultsTask(MainActivity mainActivity) {
            mMainActivity = new WeakReference<>(mainActivity);
        }

        @Override
        protected Pair<Integer, String> doInBackground(Uri... params) {
            MainActivity mainActivity = mMainActivity.get();

            if (mainActivity == null || mainActivity.isFinishing()) {
                return new Pair<>(REQUEST_RESULT_GENERIC_ERROR, null);
            }

            Bitmap bitmap;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(
                        mainActivity.getContentResolver(), params[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to read image bitmap", e);
                return new Pair<>(REQUEST_RESULT_GENERIC_ERROR, null);
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            try {
                int database = mainActivity.mDatabasesValues[
                        mainActivity.mSelectDatabaseSpinner.getSelectedItemPosition()];
                Connection.Response response = Jsoup.connect(
                        "https://saucenao.com/search.php?db=" + database)
                        .data("file", "image.png",
                                new ByteArrayInputStream(stream.toByteArray()))
                        .method(Connection.Method.POST)
                        .execute();

                if (response.statusCode() != 200) {
                    Log.e(LOG_TAG, "HTTP request returned code: " + response.statusCode());

                    switch (response.statusCode()) {
                        case 429:
                            return new Pair<>(REQUEST_RESULT_TOO_MANY_REQUESTS, null);
                        default:
                            return new Pair<>(REQUEST_RESULT_GENERIC_ERROR, null);
                    }
                }

                return new Pair<>(REQUEST_RESULT_OK, response.body());
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to send HTTP request", e);
                return new Pair<>(REQUEST_RESULT_GENERIC_ERROR, null);
            }
        }

        @Override
        protected void onPostExecute(Pair<Integer, String> result) {
            MainActivity mainActivity = mMainActivity.get();

            if (mainActivity == null || mainActivity.isFinishing()) {
                return;
            }

            mainActivity.mProgressDialog.dismiss();

            switch (result.first) {
                case REQUEST_RESULT_OK:
                    Bundle bundle = new Bundle();
                    bundle.putString(ResultsActivity.EXTRA_RESULTS, result.second);

                    Intent intent = new Intent(mainActivity,
                            ResultsActivity.class);
                    intent.putExtras(bundle);

                    mainActivity.startActivity(intent);
                    break;
                case REQUEST_RESULT_GENERIC_ERROR:
                    Toast.makeText(mainActivity,
                            mainActivity.getString(R.string.error_cannot_load_results),
                            Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_RESULT_TOO_MANY_REQUESTS:
                    Toast.makeText(mainActivity,
                            mainActivity.getString(R.string.error_too_many_requests),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
