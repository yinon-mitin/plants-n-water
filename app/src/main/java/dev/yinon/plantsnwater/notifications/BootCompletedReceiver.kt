package dev.yinon.plantsnwater.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.yinon.plantsnwater.PlantsNWaterApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        val app = context.applicationContext as PlantsNWaterApplication
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.container.notificationScheduler.scheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
