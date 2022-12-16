package com.luk.saucenao.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.luk.saucenao.R
import com.luk.saucenao.ui.screen.Screen

@Composable
fun ResultsEmptyView(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = text)
    }
}

@Preview
@Composable
fun PreviewResultsEmptyView() {
    Screen {
        ResultsEmptyView(text = stringResource(id = R.string.no_results))
    }
}
