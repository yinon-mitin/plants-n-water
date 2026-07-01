package dev.yinon.plantsnwater.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        PlantEntity::class,
        WateringEventEntity::class,
        PlantPhotoEntity::class,
        PlantNoteEntity::class,
        TagEntity::class,
        PlantTagCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PlantsDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao

    companion object {
        fun create(context: Context): PlantsDatabase =
            Room.databaseBuilder(context, PlantsDatabase::class.java, "plants-n-water.db")
                .build()
    }
}
