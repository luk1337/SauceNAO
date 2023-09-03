package com.luk.saucenao.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luk.saucenao.R
import com.luk.saucenao.Results
import com.luk.saucenao.ui.component.ResultCard
import com.luk.saucenao.ui.component.ResultsEmptyView
import com.luk.saucenao.ui.mock.FakeResult

@Composable
fun ResultsScreen(results: ArrayList<Results.Result>, serverError: String? = null) {
    if (results.isNotEmpty()) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(start = 10.dp, end = 10.dp),
        ) {
            items(results) {
                ResultCard(result = it)
            }
        }
    } else {
        ResultsEmptyView(text = serverError ?: stringResource(id = R.string.no_results))
    }
}

@Preview
@Composable
fun PreviewResultsScreen() {
    ThemedScreen {
        ResultsScreen(
            results = arrayListOf(
                FakeResult(),
                FakeResult(),
                FakeResult(),
            )
        )
    }
}

@Preview
@Composable
fun PreviewEmptyResultsScreen() {
    ThemedScreen {
        ResultsScreen(
            results = arrayListOf()
        )
    }
}
