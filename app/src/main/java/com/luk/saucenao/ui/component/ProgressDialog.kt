package com.luk.saucenao.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.luk.saucenao.R
import com.luk.saucenao.ui.screen.Screen

@Composable
fun ProgressDialog(
    onDismissRequest: () -> Unit,
    title: String
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = AlertDialogDefaults.containerColor,
                    shape = AlertDialogDefaults.shape,
                ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                    Text(text = stringResource(id = R.string.please_wait))
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewProgressDialog() {
    Screen {
        ProgressDialog(
            onDismissRequest = {},
            title = "Title",
        )
    }
}
