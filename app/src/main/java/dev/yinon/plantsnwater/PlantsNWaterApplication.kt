package dev.yinon.plantsnwater

import android.app.Application
import dev.yinon.plantsnwater.core.AppContainer

class PlantsNWaterApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
