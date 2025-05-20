package com.luk.saucenao

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.luk.saucenao.ext.pngDataStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream

class SauceNaoViewModel(private val applicationContext: Context) : ViewModel() {
    private val _selectedDatabases = MutableStateFlow<List<Int>>(emptyList())
    val selectedDatabases = _selectedDatabases.asStateFlow()

    private val _progressState = MutableStateFlow<ProgressState>(ProgressState.Idle)
    val progressState = _progressState.asStateFlow()

    private val _apiKeyDialogState = MutableStateFlow(false)
    val apiKeyDialogState = _apiKeyDialogState.asStateFlow()

    sealed class ProgressState {
        data object Idle : ProgressState()
        data object Loading : ProgressState()
        data class Success(val results: String) : ProgressState()
        data class Error(val message: Int) : ProgressState()
    }

    fun updateSelectedDatabases(databases: List<Int>) {
        _selectedDatabases.value = databases
    }

    fun toggleApiKeyDialog(show: Boolean) {
        _apiKeyDialogState.value = show
    }

    private val databasesValues by lazy {
        applicationContext.resources.getIntArray(R.array.databases_values)
    }

    fun fetchResults(
        data: Any,
        apiKey: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _progressState.value = ProgressState.Loading

            try {
                val url = "https://saucenao.com/search.php".toUri()
                    .buildUpon()
                    .appendQueryParameter("api_key", apiKey)
                    .appendQueryParameter("output_type", "0")
                    .build()

                val connection = Jsoup.connect(url.toString())
                    .method(Connection.Method.POST)
                    .ignoreHttpErrors(true)
                    .data("hide", BuildConfig.SAUCENAO_HIDE)

                _selectedDatabases.value.forEach {
                    connection.data("dbs[]", databasesValues[it].toString())
                }

                when (data) {
                    is ByteArrayInputStream -> connection.data("file", "image.png", data)
                    is Uri -> connection.data("file", "image.png", data.pngDataStream(applicationContext))
                    is String -> connection.data("url", data)
                    else -> throw IllegalArgumentException("Unsupported data type")
                }

                val response = connection.execute()

                when (response.statusCode()) {
                    200 -> {
                        val body = response.body()
                        _progressState.value = if (body.isNotEmpty())
                            ProgressState.Success(body)
                        else
                            ProgressState.Error(R.string.error_cannot_load_results)
                    }

                    403 -> {
                        _progressState.value = ProgressState.Error(
                            if (response.headers()["cf-mitigated"] == "challenge")
                                R.string.error_cloudflare_challenge
                            else
                                R.string.error_cannot_load_results
                        )
                    }

                    429 -> {
                        _progressState.value = ProgressState.Error(R.string.error_too_many_requests)
                    }

                    else -> {
                        _progressState.value =
                            ProgressState.Error(R.string.error_cannot_load_results)
                    }
                }
            } catch (_: Exception) {
                _progressState.value = if (isActive)
                    ProgressState.Error(R.string.error_cannot_load_results)
                else
                    ProgressState.Idle
            }
        }
    }

    fun cancel() {
        viewModelScope.coroutineContext.cancelChildren()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SauceNaoViewModel(
                    applicationContext = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                )
            }
        }
    }
}
