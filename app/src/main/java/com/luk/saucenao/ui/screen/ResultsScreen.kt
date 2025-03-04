package com.luk.saucenao.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luk.saucenao.R
import com.luk.saucenao.Results
import com.luk.saucenao.ui.component.ResultCard
import com.luk.saucenao.ui.component.ResultsEmptyView
import com.luk.saucenao.ui.mock.FakeResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    results: ArrayList<Results.Result>,
    serverError: String? = null,
    onBackPressed: () -> Unit = {},
) {
    if (results.isNotEmpty()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.app_name))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackPressed,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorResource(id = R.color.colorPrimary),
                    )
                )
            },
            content = { paddingValues ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(start = 10.dp, end = 10.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(modifier = Modifier.height(0.dp))
                    results.forEach {
                        ResultCard(result = it)
                    }
                    Spacer(modifier = Modifier.height(0.dp))
                }
            }
        )
    } else {
        ResultsEmptyView(text = serverError ?: stringResource(id = R.string.no_results))
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
            ),
        )
    }
}

@Preview
@Composable
fun PreviewEmptyResultsScreen() {
    Screen {
        ResultsScreen(
            results = arrayListOf(),
        )
    }
}
