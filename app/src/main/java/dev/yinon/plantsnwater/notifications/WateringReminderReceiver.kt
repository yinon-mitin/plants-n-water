package dev.yinon.plantsnwater.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.yinon.plantsnwater.PlantsNWaterApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WateringReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as PlantsNWaterApplication
        val plantId = intent.getLongExtra(EXTRA_PLANT_ID, -1)
        val plantName = intent.getStringExtra(EXTRA_PLANT_NAME).orEmpty()
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_MARK_WATERED -> app.container.plantRepository.markWatered(plantId)
                    ACTION_REMIND_LATER -> app.container.plantRepository.postponeWatering(plantId, 1)
                    else -> app.container.notificationScheduler.showReminder(plantId, plantName)
                }
                app.container.plantRepository.getPlant(plantId)?.let {
                    app.container.notificationScheduler.schedule(it)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
