package dev.yinon.plantsnwater.core

import android.content.Context
import dev.yinon.plantsnwater.data.local.PlantPhotoStorage
import dev.yinon.plantsnwater.data.local.PlantsDatabase
import dev.yinon.plantsnwater.data.repository.BackupRepository
import dev.yinon.plantsnwater.data.repository.PlantRepository
import dev.yinon.plantsnwater.notifications.NotificationScheduler
import dev.yinon.plantsnwater.settings.SettingsRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = PlantsDatabase.create(appContext)

    val photoStorage = PlantPhotoStorage(appContext)
    val settingsRepository = SettingsRepository(appContext)
    val plantRepository = PlantRepository(database.plantDao(), photoStorage)
    val backupRepository = BackupRepository(appContext, database.plantDao(), photoStorage)
    val notificationScheduler = NotificationScheduler(appContext, plantRepository, settingsRepository)
}
