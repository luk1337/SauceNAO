package com.luk.saucenao;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

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

    private Button mSelectImageButton;
    private ProgressDialog mProgressDialog;

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
                mProgressDialog = ProgressDialog.show(this, "Loading results",
                        "Please wait...", true, false);
                new GetResultsTask().execute(data.getData());
                break;
            case REQUEST_SHARE:
                mProgressDialog = ProgressDialog.show(this, "Loading results",
                        "Please wait...", true, false);
                new GetResultsTask().execute((Uri) data.getParcelableExtra(Intent.EXTRA_STREAM));
                break;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetResultsTask extends AsyncTask<Uri, Integer, JSONObject> {

        @Override
        protected JSONObject doInBackground(Uri... params) {
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
                    .addQueryParameter("output_type", "2")
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "someValue")
                    .addPart(
                            Headers.of("Content-Disposition", "form-data; name=\"file\"; filename=\"image.png\""),
                            RequestBody.create(MediaType.parse("image/*"), stream.toByteArray()))
                    .build();
            Request request = new Request.Builder()
                    .url(httpUrl)
                    .post(requestBody)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                return new JSONObject(response.body().string());
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to send HTTP request", e);
                return null;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to parse HTTP output as JSON", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            mProgressDialog.dismiss();

            if (result == null) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.error_cannot_parse_results), Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString(ResultsActivity.EXTRA_RESULTS, result.toString());

            Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    }
}
