package dev.yinon.plantsnwater.ui.screens

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ImportExportScreen() {
    ScreenColumn {
        SectionTitle("Import and export")
        EmptyState(
            "Backup format documented",
            "Exports will use a ZIP containing manifest.json, data.json, and optional local photo files."
        )
        OutlinedButton(onClick = {}) { Text("Export data") }
        OutlinedButton(onClick = {}) { Text("Import data") }
    }
}
