package com.luk.saucenao.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luk.saucenao.Results
import com.luk.saucenao.ui.component.ResultCard
import com.luk.saucenao.ui.component.ResultsEmptyView
import com.luk.saucenao.ui.mock.FakeResult

@Composable
fun ResultsScreen(results: ArrayList<Results.Result>) {
    if (results.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            results.forEach {
                ResultCard(result = it)
            }
        }
    } else {
        ResultsEmptyView()
    }
}

@Preview
@Composable
fun PreviewResultsScreen() {
    Screen {
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
    Screen {
        ResultsScreen(
            results = arrayListOf()
        )
    }
}
