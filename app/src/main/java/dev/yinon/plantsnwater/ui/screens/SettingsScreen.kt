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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.yinon.plantsnwater.R
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.PlantViewModelFactory
import dev.yinon.plantsnwater.ui.SettingsViewModel

@Composable
fun SettingsScreen(onImportExport: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: SettingsViewModel = viewModel(factory = PlantViewModelFactory(container))
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val repoUrl = "https://github.com/yinon-mitin/plants-n-water"

    ScreenColumn {
        SectionTitle(stringResource(R.string.settings))
        Card {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.watering_notifications), modifier = Modifier.weight(1f))
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
                Text(stringResource(R.string.default_reminder, settings.defaultReminderHour, settings.defaultReminderMinute))
                Text(stringResource(R.string.theme_label, settings.themeMode.name))
                Text(stringResource(R.string.plant_list_label, settings.plantListMode.name))
                Text(stringResource(R.string.first_day_label, settings.firstDayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }))
            }
        }
        OutlinedButton(onClick = onImportExport) {
            Text(stringResource(R.string.backup_and_restore))
        }
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.about))
                Text(stringResource(R.string.app_name))
                Text(stringResource(R.string.author_label))
                Text(stringResource(R.string.year_label))
                Text(stringResource(R.string.version_label, viewModel.versionName.removePrefix("v")))
                OutlinedButton(onClick = { uriHandler.openUri(repoUrl) }) {
                    Text(stringResource(R.string.repository_label, repoUrl))
                }
            }
        }
        EmptyState(stringResource(R.string.privacy), stringResource(R.string.privacy_body))
    }
}
