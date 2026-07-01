package dev.yinon.plantsnwater.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dev.yinon.plantsnwater.MainActivity
import dev.yinon.plantsnwater.R
import dev.yinon.plantsnwater.data.local.PlantEntity
import dev.yinon.plantsnwater.data.repository.PlantRepository
import dev.yinon.plantsnwater.settings.SettingsRepository
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.first

private const val CHANNEL_ID = "watering-reminders"
const val ACTION_MARK_WATERED = "dev.yinon.plantsnwater.action.MARK_WATERED"
const val ACTION_REMIND_LATER = "dev.yinon.plantsnwater.action.REMIND_LATER"
const val EXTRA_PLANT_ID = "plant_id"
const val EXTRA_PLANT_NAME = "plant_name"

class NotificationScheduler(
    private val context: Context,
    private val plantRepository: PlantRepository,
    private val settingsRepository: SettingsRepository
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_watering),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_watering_description)
            }
            manager.createNotificationChannel(channel)
        }
    }

    suspend fun scheduleAll() {
        val settings = settingsRepository.settings.first()
        if (!settings.notificationsEnabled) return
        plantRepository.getActivePlants().forEach { schedule(it) }
    }

    suspend fun schedule(plant: PlantEntity) {
        val settings = settingsRepository.settings.first()
        if (!settings.notificationsEnabled || !plant.notificationEnabled) return
        val reminderAt = Instant.ofEpochMilli(plant.nextWateringAt)
            .atZone(ZoneId.systemDefault())
            .withHour(plant.customReminderHour ?: settings.defaultReminderHour)
            .withMinute(plant.customReminderMinute ?: settings.defaultReminderMinute)
            .withSecond(0)
            .withNano(0)
            .toInstant()
            .toEpochMilli()
        val intent = Intent(context, WateringReminderReceiver::class.java).apply {
            putExtra(EXTRA_PLANT_ID, plant.id)
            putExtra(EXTRA_PLANT_NAME, plant.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            plant.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderAt, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderAt, pendingIntent)
        }
    }

    fun showReminder(plantId: Long, plantName: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val wateredIntent = PendingIntent.getBroadcast(
            context,
            plantId.toInt() + 100_000,
            Intent(context, WateringReminderReceiver::class.java).apply {
                action = ACTION_MARK_WATERED
                putExtra(EXTRA_PLANT_ID, plantId)
                putExtra(EXTRA_PLANT_NAME, plantName)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val laterIntent = PendingIntent.getBroadcast(
            context,
            plantId.toInt() + 200_000,
            Intent(context, WateringReminderReceiver::class.java).apply {
                action = ACTION_REMIND_LATER
                putExtra(EXTRA_PLANT_ID, plantId)
                putExtra(EXTRA_PLANT_NAME, plantName)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setContentTitle(plantName)
            .setContentText(context.getString(R.string.water_plant))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.mark_watered), wateredIntent)
            .addAction(0, context.getString(R.string.remind_later), laterIntent)
            .build()
        NotificationManagerCompat.from(context).notify(plantId.toInt(), notification)
    }
}
