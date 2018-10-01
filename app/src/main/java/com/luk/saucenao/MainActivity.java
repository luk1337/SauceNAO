package com.luk.saucenao;

import android.annotation.SuppressLint;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_DOCUMENTS = 0;
    private static final int REQUEST_SHARE = 1;
    private static final int REQUEST_RESULT_OK = 0;
    private static final int REQUEST_RESULT_GENERIC_ERROR = 1;
    private static final int REQUEST_RESULT_TOO_MANY_REQUESTS = 2;
    private static final int REQUEST_RESULT_INVALID_API_KEY = 3;
    private static final int REQUEST_RESULT_INVALID_JSON = 4;

    private Button mSelectImageButton;
    private Spinner mSelectDatabaseSpinner;
    private ProgressDialog mProgressDialog;
    private AsyncTask<Uri, Integer, Pair<Integer, JSONObject>> mResultTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            onActivityResult(REQUEST_SHARE, RESULT_OK, intent);
        }

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
                mResultTask = new GetResultsTask();
                mResultTask.execute(data.getData());
                mProgressDialog = ProgressDialog.show(this,
                        getString(R.string.loading_results), getString(R.string.please_wait),
                        true, true);
                mProgressDialog.setOnCancelListener(dialog ->
                        mResultTask.cancel(true));
                break;
            case REQUEST_SHARE:
                mResultTask = new GetResultsTask();
                mResultTask.execute((Uri) data.getParcelableExtra(Intent.EXTRA_STREAM));
                mProgressDialog = ProgressDialog.show(this,
                        getString(R.string.loading_results), getString(R.string.please_wait),
                        true, true);
                mProgressDialog.setOnCancelListener(dialog ->
                        mResultTask.cancel(true));
                break;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetResultsTask extends AsyncTask<Uri, Integer, Pair<Integer, JSONObject>> {

        @Override
        protected Pair<Integer, JSONObject> doInBackground(Uri... params) {
            Bitmap bitmap;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), params[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to read image bitmap", e);
                return null;
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            HttpUrl httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host("saucenao.com")
                    .addPathSegment("search.php")
                    .addQueryParameter("api_key", BuildConfig.ApiKey)
                    .addQueryParameter("db", String.valueOf(
                            getResources().getIntArray(R.array.databases_values)
                                    [mSelectDatabaseSpinner.getSelectedItemPosition()]))
                    .addQueryParameter("output_type", "2")
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "someValue")
                    .addPart(
                            Headers.of("Content-Disposition", "form-data; name=\"file\";" +
                                    "filename=\"image.png\""),
                            RequestBody.create(MediaType.parse("image/*"), stream.toByteArray()))
                    .build();
            Request request = new Request.Builder()
                    .url(httpUrl)
                    .post(requestBody)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();

                if (response.code() != 200) {
                    Log.e(LOG_TAG, "HTTP request returned code: " + response.code());

                    switch (response.code()) {
                        case 403:
                            return new Pair<>(REQUEST_RESULT_INVALID_API_KEY, null);
                        case 429:
                            return new Pair<>(REQUEST_RESULT_TOO_MANY_REQUESTS, null);
                        default:
                            return new Pair<>(REQUEST_RESULT_GENERIC_ERROR, null);
                    }
                }

                return new Pair<>(REQUEST_RESULT_OK, new JSONObject(response.body().string()));
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to send HTTP request", e);
                return new Pair<>(REQUEST_RESULT_GENERIC_ERROR, null);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to parse HTTP output as JSON", e);
                return new Pair<>(REQUEST_RESULT_INVALID_JSON, null);
            }
        }

        @Override
        protected void onPostExecute(Pair<Integer, JSONObject> result) {
            mProgressDialog.dismiss();

            switch (result.first) {
                case REQUEST_RESULT_OK:
                    Bundle bundle = new Bundle();
                    bundle.putString(ResultsActivity.EXTRA_RESULTS, result.second.toString());

                    Intent intent = new Intent(MainActivity.this,
                            ResultsActivity.class);
                    intent.putExtras(bundle);

                    startActivity(intent);
                    break;
                case REQUEST_RESULT_GENERIC_ERROR:
                    Toast.makeText(MainActivity.this,
                            getString(R.string.error_cannot_load_results),
                            Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_RESULT_TOO_MANY_REQUESTS:
                    Toast.makeText(MainActivity.this,
                            getString(R.string.error_too_many_requests),
                            Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_RESULT_INVALID_API_KEY:
                    Toast.makeText(MainActivity.this,
                            getString(R.string.error_invalid_api_keys),
                            Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_RESULT_INVALID_JSON:
                    Toast.makeText(MainActivity.this,
                            getString(R.string.error_cannot_parse_results),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
