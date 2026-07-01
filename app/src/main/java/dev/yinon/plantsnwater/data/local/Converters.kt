package dev.yinon.plantsnwater.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun plantStatusToString(value: PlantStatus): String = value.name
    @TypeConverter fun plantStatusFromString(value: String): PlantStatus = PlantStatus.valueOf(value)
    @TypeConverter fun wateringStatusToString(value: WateringEventStatus): String = value.name
    @TypeConverter fun wateringStatusFromString(value: String): WateringEventStatus = WateringEventStatus.valueOf(value)
    @TypeConverter fun growthStageToString(value: GrowthStage?): String? = value?.name
    @TypeConverter fun growthStageFromString(value: String?): GrowthStage? = value?.let(GrowthStage::valueOf)
}
