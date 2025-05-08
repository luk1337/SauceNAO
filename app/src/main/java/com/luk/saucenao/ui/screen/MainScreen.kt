package com.luk.saucenao.ui.screen

import android.app.Application
import android.os.Build
import android.webkit.URLUtil
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.luk.saucenao.MainActivity
import com.luk.saucenao.R
import com.luk.saucenao.SauceNaoViewModel
import com.luk.saucenao.ext.apiKey
import com.luk.saucenao.ext.usePhotoPicker
import com.luk.saucenao.ui.component.ProgressDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainActivity: MainActivity,
    viewModel: SauceNaoViewModel,
    onImagePickerClick: () -> Unit,
    onLegacyPickerClick: () -> Unit,
) {
    val context = LocalContext.current
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val isPhotoPickerAvailable = PickVisualMedia.isPhotoPickerAvailable(context)

    var showMenu by remember { mutableStateOf(false) }
    var usePhotoPicker by remember {
        mutableStateOf(sharedPreferences.usePhotoPicker ?: isPhotoPickerAvailable)
    }

    val togglePhotoPicker = {
        usePhotoPicker = !usePhotoPicker
        sharedPreferences.usePhotoPicker = usePhotoPicker
    }

    val apiKeyDialogState by viewModel.apiKeyDialogState.collectAsState()
    val progressState by viewModel.progressState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                actions = {
                    if (isPhotoPickerAvailable) {
                        IconButton(
                            onClick = {
                                showMenu = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = {
                            showMenu = false
                        },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.api_key),
                                    fontSize = 16.sp,
                                )
                            },
                            onClick = {
                                viewModel.toggleApiKeyDialog(true)
                                showMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 12.dp),
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.use_photo_picker),
                                        fontSize = 16.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    Checkbox(
                                        checked = usePhotoPicker,
                                        onCheckedChange = {
                                            togglePhotoPicker()
                                        },
                                    )
                                }
                            },
                            onClick = togglePhotoPicker,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.colorPrimary),
                ),
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { true },
                        target = object : DragAndDropTarget {
                            @RequiresApi(Build.VERSION_CODES.N)
                            override fun onDrop(event: DragAndDropEvent): Boolean {
                                val dragEvent = event.toAndroidDragEvent()
                                val clipItem = dragEvent.clipData.getItemAt(0)

                                when {
                                    clipItem.text != null -> {
                                        viewModel.fetchResults(
                                            data = clipItem.text.toString(),
                                            apiKey = sharedPreferences.apiKey,
                                        )
                                    }

                                    clipItem.uri != null -> runCatching {
                                        mainActivity.requestDragAndDropPermissions(dragEvent)
                                        viewModel.fetchResults(
                                            data = clipItem.uri,
                                            apiKey = sharedPreferences.apiKey,
                                        )
                                    }
                                }

                                return true
                            }
                        })
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(it),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = {
                            if (usePhotoPicker) {
                                onImagePickerClick()
                            } else {
                                onLegacyPickerClick()
                            }
                        },
                    ) {
                        Text(text = stringResource(R.string.select_image))
                    }
                    SearchByUrl(
                        waitForResults = { data ->
                            viewModel.fetchResults(
                                data = data,
                                apiKey = sharedPreferences.apiKey,
                            )
                        },
                    )
                }

                DatabaseSpinner(viewModel)
            }
        }
    )

    if (apiKeyDialogState) {
        ApiKeyDialog(
            onDismiss = { viewModel.toggleApiKeyDialog(false) },
        )
    }

    if (progressState is SauceNaoViewModel.ProgressState.Loading) {
        ProgressDialog(
            title = stringResource(id = R.string.loading_results),
            onDismissRequest = {
                viewModel.cancel()
            },
        )
    }
}

@Composable
private fun ApiKeyDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val apiKey = remember {
        mutableStateOf(sharedPreferences.apiKey)
    }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    text = stringResource(R.string.api_key_description),
                )
                TextField(
                    value = apiKey.value,
                    placeholder = {
                        Text(text = stringResource(R.string.api_key))
                    },
                    onValueChange = {
                        apiKey.value = it
                    },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    sharedPreferences.apiKey = apiKey.value
                    onDismiss()
                },
            ) {
                Text(text = stringResource(android.R.string.ok))
            }
        },
    )
}

@Composable
private fun DatabaseSpinner(
    viewModel: SauceNaoViewModel
) {
    val context = LocalContext.current
    val resources = context.resources

    val selectedDatabases by viewModel.selectedDatabases.collectAsState()

    val databaseSelectText = remember {
        derivedStateOf {
            when {
                selectedDatabases.isEmpty() -> resources.getString(R.string.all_databases)
                selectedDatabases.size == 1 -> resources.getStringArray(
                    R.array.databases_entries
                )[selectedDatabases.first()]
                else -> resources.getQuantityString(
                    R.plurals.selected_databases,
                    selectedDatabases.size,
                    selectedDatabases.size
                )
            }
        }
    }
    val visible = remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = databaseSelectText.value,
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    visible.value = true
                },
            )
        )
        IconButton(
            onClick = {
                visible.value = true
            }
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
            )
        }
    }

    if (visible.value) {
        AlertDialog(
            onDismissRequest = {
                visible.value = false
            },
            title = {
                Text(text = stringResource(R.string.select_databases))
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    resources.getStringArray(R.array.databases_entries)
                        .forEachIndexed { index, s ->
                            val state = remember {
                                mutableStateOf(selectedDatabases.contains(index))
                            }
                            val toggle = {
                                if (state.value) {
                                    viewModel.updateSelectedDatabases(
                                        selectedDatabases.filterNot { it == index }
                                    )
                                } else {
                                    viewModel.updateSelectedDatabases(
                                        selectedDatabases + index
                                    )
                                }
                                state.value = !state.value
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = {
                                            toggle()
                                        },
                                    ),
                            ) {
                                Checkbox(
                                    checked = state.value,
                                    onCheckedChange = {
                                        toggle()
                                    },
                                )
                                Text(
                                    text = s,
                                )
                            }
                        }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        visible.value = false
                    },
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            modifier = Modifier.fillMaxHeight(fraction = 0.85f),
        )
    }
}

@Composable
private fun SearchByUrl(waitForResults: (Any) -> Unit) {
    val clipboardManager = LocalClipboardManager.current

    val text = remember { mutableStateOf(TextFieldValue()) }
    val visible = remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            text.value = TextFieldValue(
                clipboardManager.getText().let {
                    if (URLUtil.isValidUrl(it.toString())) it.toString() else ""
                }
            )
            visible.value = true
        },
    ) {
        Icon(
            painter = painterResource(R.drawable.link),
            contentDescription = null,
        )
    }

    if (visible.value) {
        AlertDialog(
            onDismissRequest = {
                visible.value = false
            },
            title = {
                Text(text = stringResource(R.string.search_by_image_url))
            },
            text = {
                TextField(
                    placeholder = {
                        Text(text = "URL")
                    },
                    isError = !URLUtil.isValidUrl(text.value.text),
                    value = text.value,
                    onValueChange = {
                        text.value = it
                    },
                )
            },
            confirmButton = {
                Button(
                    enabled = text.value.text.isNotEmpty(),
                    onClick = {
                        visible.value = false
                        waitForResults(text.value.text)
                    },
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        )
    }
}

@Preview
@Composable
fun PreviewMainScreen() {
    val mockViewModel = SauceNaoViewModel(
        application = LocalContext.current.applicationContext as Application,
    )

    Screen {
        MainScreen(
            mainActivity = MainActivity(),
            viewModel = mockViewModel,
            onImagePickerClick = {},
            onLegacyPickerClick = {},
        )
    }
}
