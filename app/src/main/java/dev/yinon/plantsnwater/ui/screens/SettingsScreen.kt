package dev.yinon.plantsnwater.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.PlantViewModelFactory
import dev.yinon.plantsnwater.ui.SettingsViewModel

@Composable
fun SettingsScreen(onImportExport: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: SettingsViewModel = viewModel(factory = PlantViewModelFactory(container))
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    ScreenColumn {
        SectionTitle("Settings")
        Card {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Watering notifications", modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled
                )
            }
        }
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Default reminder: %02d:%02d".format(settings.defaultReminderHour, settings.defaultReminderMinute))
                Text("Theme: ${settings.themeMode.name}")
                Text("Plant list: ${settings.plantListMode.name}")
                Text("First day: ${settings.firstDayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}")
            }
        }
        OutlinedButton(onClick = onImportExport) {
            Text("Backup and restore")
        }
        EmptyState("Privacy", "No accounts, ads, analytics, tracking, or cloud dependency.")
    }
}
