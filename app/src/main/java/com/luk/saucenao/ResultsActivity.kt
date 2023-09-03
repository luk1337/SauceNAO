package com.luk.saucenao

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import com.luk.saucenao.R.string
import com.luk.saucenao.ui.screen.ResultsScreen
import com.luk.saucenao.ui.screen.Screen
import com.luk.saucenao.ui.screen.ThemedScreen
import org.jsoup.Jsoup

class ResultsActivity : ComponentActivity() {
    private lateinit var results: Results

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar?.setDisplayHomeAsUpEnabled(true)

        intent.extras?.let {
            it.getString(EXTRA_RESULTS)?.let { results ->
                this.results = Results(Jsoup.parse(results))
            }
        }

        enableEdgeToEdge()

        setContent {
            ThemedScreen {
                val scrollBehavior =
                    TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    resources.getString(string.app_name),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    onBackPressedDispatcher.onBackPressed()
                                }) {
                                    Icon(
                                        imageVector = Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior,
                        )
                    },
                ) { innerPadding ->
                    Screen(modifier = Modifier.padding(innerPadding)) {
                        ResultsScreen(
                            results = results.results,
                            serverError = results.serverError,
                        )
                    }
                }
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