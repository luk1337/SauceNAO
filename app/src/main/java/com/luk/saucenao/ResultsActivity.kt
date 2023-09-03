package com.luk.saucenao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.luk.saucenao.ui.screen.ResultsScreen
import com.luk.saucenao.ui.screen.Screen
import org.jsoup.Jsoup

class ResultsActivity : ComponentActivity() {
    private lateinit var results: Results

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.let {
            it.getString(EXTRA_RESULTS)?.let { results ->
                this.results = Results(Jsoup.parse(results))
            }
        }

        enableEdgeToEdge()

        setContent {
            Screen {
                ResultsScreen(
                    results = results.results,
                    serverError = results.serverError,
                    onBackPressed = {
                        onBackPressedDispatcher.onBackPressed()
                    },
                )
            }
        }
    }

    companion object {
        const val EXTRA_RESULTS = "extra_results"
    }
}