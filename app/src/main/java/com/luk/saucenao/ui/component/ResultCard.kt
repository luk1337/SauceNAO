package com.luk.saucenao.ui.component

import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.luk.saucenao.R
import com.luk.saucenao.Results
import com.luk.saucenao.ui.mock.FakeResult
import com.luk.saucenao.ui.screen.ThemedScreen

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ResultCard(result: Results.Result) {
    val actionsExpanded = remember { mutableStateOf(false) }
    val extLinksExpanded = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    extLinksExpanded.value = true
                },
                onLongClick = {
                    actionsExpanded.value = true
                },
            ),
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = result.thumbnail),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxHeight(),
            )
            Column(
                modifier = Modifier.padding(10.dp),
            ) {
                Row {
                    Text(
                        text = result.title ?: "",
                        modifier = Modifier.weight(1f),
                    )
                    Text(text = result.similarity ?: "")
                }
                Text(
                    text = result.columns.joinToString("\n"),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
            }
        }

        ActionsDropdownMenu(actionsExpanded, result)
        ExtLinksDropdownMenu(extLinksExpanded, result.extUrls)
    }
}

@Composable
private fun ActionsDropdownMenu(expanded: MutableState<Boolean>, result: Results.Result) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = {
            expanded.value = false
        }
    ) {
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.copy_to_clipboard_item))
            },
            onClick = {
                clipboardManager.setText(AnnotatedString(result.title ?: ""))
                Toast.makeText(
                    context,
                    R.string.title_copied_to_clipboard,
                    Toast.LENGTH_SHORT
                ).show()
                expanded.value = false
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.share_item))
            },
            onClick = {
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = ClipDescription.MIMETYPE_TEXT_PLAIN
                            putExtra(Intent.EXTRA_TEXT, result.title)
                        },
                        context.resources.getString(R.string.share_with)
                    )
                )
                expanded.value = false
            }
        )
    }
}

@Composable
private fun ExtLinksDropdownMenu(expanded: MutableState<Boolean>, extUrls: List<String>) {
    val context = LocalContext.current

    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = {
            expanded.value = false
        }
    ) {
        extUrls.forEach {
            DropdownMenuItem(
                text = {
                    Text(text = it)
                },
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(it))
                        )
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun PreviewResultCard() {
    ThemedScreen {
        ResultCard(result = FakeResult())
    }
}
