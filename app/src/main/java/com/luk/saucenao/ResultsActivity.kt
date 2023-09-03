package com.luk.saucenao

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.luk.saucenao.ui.screen.ResultsScreen
import com.luk.saucenao.ui.screen.Screen
import org.jsoup.Jsoup

class ResultsActivity : ComponentActivity() {
    private lateinit var results: Results

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar?.setDisplayHomeAsUpEnabled(true)

        intent.extras?.let {
            it.getString(EXTRA_RESULTS)?.let { results ->
                this.results = Results(Jsoup.parse(results))
            }
        }

        setContent {
            Screen {
                ResultsScreen(
                    results = results.results,
                    serverError = results.serverError,
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return true
    }

    companion object {
        const val EXTRA_RESULTS = "extra_results"
    }
}