package com.luk.saucenao

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var databasesValues: IntArray
    private lateinit var executorService: ExecutorService
    private lateinit var selectDatabaseSpinner: Spinner
    private lateinit var progressDialog: ProgressDialog

    private val getResultsFromFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri ->
            waitForResults(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        executorService = Executors.newSingleThreadExecutor()
        databasesValues = resources.getIntArray(R.array.databases_values)
        selectDatabaseSpinner = findViewById(R.id.select_database)

        val selectImageButton = findViewById<Button>(R.id.select_image)
        selectImageButton.setOnClickListener {
            getResultsFromFile.launch("image/*")
        }

        if (Intent.ACTION_SEND == intent.action) {
            if (intent.hasExtra(Intent.EXTRA_STREAM)) {
                waitForResults(intent.getParcelableExtra(Intent.EXTRA_STREAM)!!)
            } else if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                waitForResults(intent.getStringExtra(Intent.EXTRA_TEXT)!!)
            }
        }
    }

    private fun waitForResults(data: Any) {
        val future = executorService.submit(GetResultsTask(data))
        progressDialog = ProgressDialog.show(
            this,
            getString(R.string.loading_results), getString(R.string.please_wait),
            true, true
        )
        progressDialog.setOnCancelListener { future.cancel(true) }
    }

    inner class GetResultsTask(private val data: Any?) : Callable<Void?> {
        override fun call(): Void? {
            if (isFinishing) {
                return null
            }

            val result = fetchResult()

            val handler = Handler(mainLooper)
            handler.post { progressDialog.dismiss() }

            when (result.first) {
                REQUEST_RESULT_OK -> {
                    val bundle = Bundle()
                    bundle.putString(ResultsActivity.EXTRA_RESULTS, result.second)

                    val intent = Intent(this@MainActivity, ResultsActivity::class.java)
                    intent.putExtras(bundle)

                    handler.post { startActivity(intent) }
                }
                REQUEST_RESULT_GENERIC_ERROR -> {
                    handler.post {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.error_cannot_load_results),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                REQUEST_RESULT_TOO_MANY_REQUESTS -> {
                    handler.post {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.error_too_many_requests),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            return null
        }

        private fun fetchResult(): Pair<Int, String?> {
            try {
                val database = databasesValues[selectDatabaseSpinner.selectedItemPosition]
                var response: Connection.Response? = null

                if (data is Uri) {
                    val stream = ByteArrayOutputStream()
                    try {
                        MediaStore.Images.Media.getBitmap(contentResolver, data)
                            .compress(Bitmap.CompressFormat.PNG, 100, stream)
                    } catch (e: IOException) {
                        Log.e(LOG_TAG, "Unable to read image bitmap", e)
                        return Pair(REQUEST_RESULT_GENERIC_ERROR, null)
                    }
                    response = Jsoup.connect("https://saucenao.com/search.php?db=$database")
                        .data("file", "image.png", ByteArrayInputStream(stream.toByteArray()))
                        .data("hide", BuildConfig.SAUCENAO_HIDE)
                        .method(Connection.Method.POST)
                        .execute()
                } else if (data is String) {
                    response = Jsoup.connect("https://saucenao.com/search.php?db=$database")
                        .data("url", data)
                        .data("hide", BuildConfig.SAUCENAO_HIDE)
                        .method(Connection.Method.POST)
                        .execute()
                }
                assert(response != null)
                if (response!!.statusCode() != 200) {
                    Log.e(LOG_TAG, "HTTP request returned code: ${response.statusCode()}")
                    return when (response.statusCode()) {
                        429 -> Pair(REQUEST_RESULT_TOO_MANY_REQUESTS, null)
                        else -> Pair(REQUEST_RESULT_GENERIC_ERROR, null)
                    }
                }
                val body = response.body()
                if (body.isEmpty()) {
                    return Pair(REQUEST_RESULT_INTERRUPTED, null)
                }

                return Pair(REQUEST_RESULT_OK, body)
            } catch (e: InterruptedIOException) {
                return Pair(REQUEST_RESULT_INTERRUPTED, null)
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Unable to send HTTP request", e)
                return Pair(REQUEST_RESULT_GENERIC_ERROR, null)
            }
        }
    }

    companion object {
        private val LOG_TAG = MainActivity::class.java.simpleName

        private const val REQUEST_RESULT_OK = 0
        private const val REQUEST_RESULT_INTERRUPTED = 1
        private const val REQUEST_RESULT_GENERIC_ERROR = 2
        private const val REQUEST_RESULT_TOO_MANY_REQUESTS = 3
    }
}