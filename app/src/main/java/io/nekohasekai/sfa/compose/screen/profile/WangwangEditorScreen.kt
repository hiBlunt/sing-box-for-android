package io.nekohasekai.sfa.compose.screen.profile

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.nekohasekai.sfa.R
import io.nekohasekai.sfa.compose.base.SelectableMessageDialog
import io.nekohasekai.sfa.compose.topbar.OverrideTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WangwangEditorScreen(
    profileId: Long,
    profileName: String,
    onNavigateBack: () -> Unit,
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: WangwangEditorViewModel =
        viewModel(
            factory = WangwangEditorViewModelFactory(application, profileId, profileName),
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showErrorDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadConfiguration()
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            showErrorDialog = true
        }
    }

    if (showErrorDialog) {
        SelectableMessageDialog(
            title = stringResource(R.string.error_title),
            message = uiState.errorMessage ?: "",
            onDismiss = {
                showErrorDialog = false
                viewModel.clearError()
            },
        )
    }

    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text(stringResource(R.string.unsaved_changes)) },
            text = { Text(stringResource(R.string.unsaved_changes_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onNavigateBack()
                    },
                ) {
                    Text(
                        stringResource(R.string.discard),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedChangesDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }

    val handleBack = {
        if (uiState.hasChanges) {
            showUnsavedChangesDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(enabled = uiState.hasChanges) {
        showUnsavedChangesDialog = true
    }

    OverrideTopBar {
        TopAppBar(
            title = { Text(stringResource(R.string.title_wangwang_editor)) },
            navigationIcon = {
                IconButton(onClick = handleBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_description_back),
                    )
                }
            },
            colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (!uiState.isLoading) {
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = if (uiState.hasChanges) 88.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = uiState.server,
                    onValueChange = viewModel::updateServer,
                    label = { Text(stringResource(R.string.server_address)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = uiState.serverPort,
                    onValueChange = viewModel::updateServerPort,
                    label = { Text(stringResource(R.string.server_port)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = { Text(stringResource(R.string.password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = uiState.method,
                    onValueChange = viewModel::updateMethod,
                    label = { Text(stringResource(R.string.wangwang_method)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (uiState.showSaveSuccessMessage) {
                    Text(
                        text = stringResource(R.string.success_configuration_saved),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        if (uiState.hasChanges) {
            Surface(
                modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
            ) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(16.dp),
                ) {
                    Button(
                        onClick = { viewModel.saveConfiguration() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            }
        }
    }
}

class WangwangEditorViewModelFactory(
    private val application: Application,
    private val profileId: Long,
    private val profileName: String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WangwangEditorViewModel(application, profileId, profileName) as T
    }
}
