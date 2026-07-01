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

    @Query("SELECT * FROM plants")
    suspend fun getAllPlants(): List<PlantEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlant(plant: PlantEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlants(plants: List<PlantEntity>)

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWateringEvents(events: List<WateringEventEntity>)

    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY createdAt DESC")
    fun observePhotos(plantId: Long): Flow<List<PlantPhotoEntity>>

    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId")
    suspend fun getPhotos(plantId: Long): List<PlantPhotoEntity>

    @Query("SELECT * FROM plant_photos")
    suspend fun getAllPhotos(): List<PlantPhotoEntity>

    @Query(
        """
        SELECT * FROM plant_photos
        WHERE id IN (
            SELECT latest.id
            FROM plant_photos AS latest
            WHERE latest.plantId = plant_photos.plantId
            ORDER BY latest.createdAt DESC, latest.id DESC
            LIMIT 1
        )
        """
    )
    fun observeLatestPhotos(): Flow<List<PlantPhotoEntity>>

    @Query("SELECT * FROM plant_photos WHERE id = :photoId")
    suspend fun getPhoto(photoId: Long): PlantPhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PlantPhotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PlantPhotoEntity>)

    @Update
    suspend fun updatePhoto(photo: PlantPhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: PlantPhotoEntity)

    @Query("SELECT * FROM plant_notes WHERE plantId = :plantId ORDER BY createdAt DESC")
    fun observeNotes(plantId: Long): Flow<List<PlantNoteEntity>>

    @Query("SELECT * FROM plant_notes")
    suspend fun getAllNotes(): List<PlantNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: PlantNoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<PlantNoteEntity>)

    @Query("DELETE FROM plant_tags")
    suspend fun clearPlantTags()

    @Query("DELETE FROM tags")
    suspend fun clearTags()

    @Query("DELETE FROM plant_notes")
    suspend fun clearNotes()

    @Query("DELETE FROM plant_photos")
    suspend fun clearPhotos()

    @Query("DELETE FROM watering_events")
    suspend fun clearWateringEvents()

    @Query("DELETE FROM plants")
    suspend fun clearPlants()

    @Transaction
    suspend fun replaceBackupData(
        plants: List<PlantEntity>,
        events: List<WateringEventEntity>,
        photos: List<PlantPhotoEntity>,
        notes: List<PlantNoteEntity>
    ) {
        clearPlantTags()
        clearTags()
        clearNotes()
        clearPhotos()
        clearWateringEvents()
        clearPlants()
        insertPlants(plants)
        insertWateringEvents(events)
        insertPhotos(photos)
        insertNotes(notes)
    }

    @Transaction
    suspend fun replacePlantAfterWatering(plant: PlantEntity, event: WateringEventEntity) {
        updatePlant(plant)
        insertWateringEvent(event)
    }
}
