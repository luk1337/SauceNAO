package com.luk.saucenao

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.util.Pair
import com.luk.saucenao.R.string
import com.luk.saucenao.ui.screen.MainScreen
import com.luk.saucenao.ui.screen.Screen
import com.luk.saucenao.ui.theme.Theme
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class MainActivity : ComponentActivity() {
    private val executorService = Executors.newSingleThreadExecutor()

    private val databasesValues by lazy { resources.getIntArray(R.array.databases_values) }

    internal val progressDialogFuture = mutableStateOf<Future<Void?>?>(null)

    internal var selectedDatabases = mutableStateListOf<Int>()

    internal val getResultsFromFile =
        registerForActivityResult(PickVisualMedia()) { uri: Uri? ->
            uri?.let { waitForResults(it) }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            Theme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    resources.getString(string.app_name),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                        )
                    },
                ) { innerPadding ->
                    Screen(modifier = Modifier.padding(innerPadding)) {
                        MainScreen(mainActivity = this)
                    }
                }
            }
        }

        if (Intent.ACTION_SEND == intent.action) {
            if (intent.hasExtra(Intent.EXTRA_STREAM)) {
                waitForResults(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_STREAM, Any::class.java)!!
                    } else {
                        @Suppress("Deprecation")
                        intent.getParcelableExtra(Intent.EXTRA_STREAM)!!
                    }
                )
            } else if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                waitForResults(intent.getStringExtra(Intent.EXTRA_TEXT)!!)
            }
        }
    }

    internal fun waitForResults(data: Any) {
        progressDialogFuture.value = executorService.submit(GetResultsTask(data))
    }

    inner class GetResultsTask(private val data: Any?) : Callable<Void?> {
        override fun call(): Void? {
            if (isFinishing) {
                return null
            }

            val result = fetchResult()

            val handler = Handler(mainLooper)
            handler.post { progressDialogFuture.value = null }

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
                            R.string.error_cannot_load_results,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                REQUEST_RESULT_TOO_MANY_REQUESTS -> {
                    handler.post {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.error_too_many_requests,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            return null
        }

        private fun fetchResult(): Pair<Int, String?> {
            try {
                val connection = Jsoup.connect("https://saucenao.com/search.php")
                    .method(Connection.Method.POST)
                    .data("hide", BuildConfig.SAUCENAO_HIDE)
                selectedDatabases.forEach {
                    connection.data("dbs[]", databasesValues[it].toString())
                }

                if (data is Uri) {
                    val stream = ByteArrayOutputStream()
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(contentResolver, data)
                            ).compress(Bitmap.CompressFormat.PNG, 100, stream)
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(contentResolver, data)
                                .compress(Bitmap.CompressFormat.PNG, 100, stream)
                        }
                    } catch (e: IOException) {
                        Log.e(LOG_TAG, "Unable to read image bitmap", e)
                        return Pair(REQUEST_RESULT_GENERIC_ERROR, null)
                    }
                    connection.data("file", "image.png", ByteArrayInputStream(stream.toByteArray()))
                } else if (data is String) {
                    connection.data("url", data)
                }

                val response = connection.execute()
                if (response.statusCode() != 200) {
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