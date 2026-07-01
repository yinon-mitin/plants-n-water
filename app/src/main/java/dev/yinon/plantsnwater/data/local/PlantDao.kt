package dev.yinon.plantsnwater.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants WHERE archivedAt IS NULL ORDER BY nextWateringAt ASC, name COLLATE NOCASE ASC")
    fun observeActivePlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE archivedAt IS NULL AND nextWateringAt <= :endOfDay ORDER BY nextWateringAt ASC")
    fun observeDuePlants(endOfDay: Long): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE id = :id")
    fun observePlant(id: Long): Flow<PlantEntity?>

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getPlant(id: Long): PlantEntity?

    @Query("SELECT * FROM plants WHERE archivedAt IS NULL")
    suspend fun getActivePlants(): List<PlantEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlant(plant: PlantEntity): Long

    @Update
    suspend fun updatePlant(plant: PlantEntity)

    @Delete
    suspend fun deletePlant(plant: PlantEntity)

    @Query("UPDATE plants SET archivedAt = :archivedAt, updatedAt = :archivedAt WHERE id = :plantId")
    suspend fun archivePlant(plantId: Long, archivedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWateringEvent(event: WateringEventEntity): Long

    @Query("SELECT * FROM watering_events WHERE plantId = :plantId ORDER BY scheduledFor DESC")
    fun observeWateringEvents(plantId: Long): Flow<List<WateringEventEntity>>

    @Query("SELECT * FROM watering_events ORDER BY scheduledFor DESC")
    suspend fun getAllWateringEvents(): List<WateringEventEntity>

    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY createdAt DESC")
    fun observePhotos(plantId: Long): Flow<List<PlantPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PlantPhotoEntity): Long

    @Query("SELECT * FROM plant_notes WHERE plantId = :plantId ORDER BY createdAt DESC")
    fun observeNotes(plantId: Long): Flow<List<PlantNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: PlantNoteEntity): Long

    @Transaction
    suspend fun replacePlantAfterWatering(plant: PlantEntity, event: WateringEventEntity) {
        updatePlant(plant)
        insertWateringEvent(event)
    }
}
