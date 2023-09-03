package com.luk.saucenao.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.luk.saucenao.ui.theme.Theme

@Composable
fun Screen(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        content()
    }
}

@Composable
fun ThemedScreen(content: @Composable () -> Unit) {
    Theme {
        Screen {
            content()
        }
    }
}
