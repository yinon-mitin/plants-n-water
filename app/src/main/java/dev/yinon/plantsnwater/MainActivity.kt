package dev.yinon.plantsnwater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.PlantsNWaterApp
import dev.yinon.plantsnwater.ui.theme.PlantsNWaterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as PlantsNWaterApplication).container
        container.notificationScheduler.createNotificationChannel()
        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                PlantsNWaterTheme {
                    PlantsNWaterApp()
                }
            }
        }
    }
}
