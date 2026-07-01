package dev.yinon.plantsnwater.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.yinon.plantsnwater.R
import dev.yinon.plantsnwater.ui.ImportExportViewModel
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.PlantViewModelFactory
import java.time.LocalDate

@Composable
fun ImportExportScreen() {
    val container = LocalAppContainer.current
    val viewModel: ImportExportViewModel = viewModel(factory = PlantViewModelFactory(container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingImport by remember { mutableStateOf<Uri?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) viewModel.exportTo(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) pendingImport = uri
    }

    ScreenColumn {
        SectionTitle(stringResource(R.string.import_export))
        EmptyState(
            stringResource(R.string.backup_format_title),
            stringResource(R.string.backup_format_body)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                enabled = !state.isBusy,
                onClick = { exportLauncher.launch("plants-n-water-${LocalDate.now()}.zip") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.export_data))
            }
            OutlinedButton(
                enabled = !state.isBusy,
                onClick = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.import_data))
            }
        }
        state.messageRes?.let {
            Text(stringResource(it))
            state.detail?.let { detail -> Text(detail) }
        }
    }

    pendingImport?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text(stringResource(R.string.import_confirm_title)) },
            text = { Text(stringResource(R.string.import_confirm_body)) },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) { Text(stringResource(R.string.cancel)) }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importReplacingFrom(uri)
                        pendingImport = null
                    }
                ) {
                    Text(stringResource(R.string.replace))
                }
            }
        )
    }
}
