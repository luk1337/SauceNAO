package com.luk.saucenao

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.preference.PreferenceManager
import com.luk.saucenao.ext.apiKey
import com.luk.saucenao.ui.screen.MainScreen
import com.luk.saucenao.ui.screen.Screen

class MainActivity : ComponentActivity() {
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val viewModel: SauceNaoViewModel by viewModels { SauceNaoViewModel.Factory }
            val progressState by viewModel.progressState.collectAsState()

            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = PickVisualMedia(),
                onResult = { uri ->
                    uri?.let { handleImageSelection(it, viewModel) }
                }
            )

            val legacyDocumentPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument(),
                onResult = { uri ->
                    uri?.let { handleImageSelection(it, viewModel) }
                }
            )

            LaunchedEffect(intent) {
                handleIntent(intent, viewModel)

                addOnNewIntentListener {
                    handleIntent(it, viewModel)
                }
            }

            Screen {
                MainScreen(
                    mainActivity = this,
                    viewModel = viewModel,
                    onImagePickerClick = {
                        imagePickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    },
                    onLegacyPickerClick = {
                        legacyDocumentPickerLauncher.launch(arrayOf("image/*"))
                    }
                )
            }

            LaunchedEffect(progressState) {
                when (val state = progressState) {
                    is SauceNaoViewModel.ProgressState.Success -> {
                        val intent = Intent(this@MainActivity, ResultsActivity::class.java).apply {
                            putExtra(ResultsActivity.EXTRA_RESULTS, state.results)
                        }
                        startActivity(intent)
                    }

                    is SauceNaoViewModel.ProgressState.Error -> {
                        Toast.makeText(
                            this@MainActivity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {} // Idle state, do nothing
                }
            }
        }
    }

    private fun handleImageSelection(uri: Uri, viewModel: SauceNaoViewModel) {
        viewModel.fetchResults(
            data = uri,
            apiKey = sharedPreferences.apiKey,
        )
    }

    private fun handleIntent(intent: Intent, viewModel: SauceNaoViewModel) {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                when {
                    intent.hasExtra(Intent.EXTRA_STREAM) -> {
                        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(Intent.EXTRA_STREAM)
                        }
                        uri?.let { handleImageSelection(it, viewModel) }
                    }

                    intent.hasExtra(Intent.EXTRA_TEXT) -> {
                        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { url ->
                            viewModel.fetchResults(
                                data = url,
                                apiKey = sharedPreferences.apiKey,
                            )
                        }
                    }
                }
            }
        }
    }
}
