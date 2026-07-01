package dev.yinon.plantsnwater.data.repository

import android.net.Uri
import dev.yinon.plantsnwater.data.local.GrowthStage
import dev.yinon.plantsnwater.data.local.PlantDao
import dev.yinon.plantsnwater.data.local.PlantEntity
import dev.yinon.plantsnwater.data.local.PlantNoteEntity
import dev.yinon.plantsnwater.data.local.PlantPhotoEntity
import dev.yinon.plantsnwater.data.local.PlantPhotoStorage
import dev.yinon.plantsnwater.data.local.PlantStatus
import dev.yinon.plantsnwater.data.local.WateringEventEntity
import dev.yinon.plantsnwater.data.local.WateringEventStatus
import dev.yinon.plantsnwater.domain.WateringSchedule
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

class PlantRepository(
    private val dao: PlantDao,
    private val photoStorage: PlantPhotoStorage
) {
    fun observeActivePlants(): Flow<List<PlantEntity>> = dao.observeActivePlants()

    fun observeDuePlants(today: LocalDate = LocalDate.now()): Flow<List<PlantEntity>> =
        dao.observeDuePlants(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1)

    fun observePlant(id: Long): Flow<PlantEntity?> = dao.observePlant(id)

    fun observeWateringEvents(plantId: Long): Flow<List<WateringEventEntity>> =
        dao.observeWateringEvents(plantId)

    fun observePhotos(plantId: Long): Flow<List<PlantPhotoEntity>> = dao.observePhotos(plantId)

    fun observeLatestPhotos(): Flow<List<PlantPhotoEntity>> = dao.observeLatestPhotos()

    fun observeNotes(plantId: Long): Flow<List<PlantNoteEntity>> = dao.observeNotes(plantId)

    suspend fun getPlant(id: Long): PlantEntity? = dao.getPlant(id)

    suspend fun getActivePlants(): List<PlantEntity> = dao.getActivePlants()

    suspend fun addPlant(
        name: String,
        wateringIntervalDays: Int,
        species: String? = null,
        location: String? = null,
        notes: String? = null,
        careInstructions: String? = null,
        status: PlantStatus = PlantStatus.Healthy,
        notificationEnabled: Boolean = true,
        customReminderHour: Int? = null,
        customReminderMinute: Int? = null
    ): Long {
        val now = Instant.now()
        val next = WateringSchedule.nextWateringDate(null, now, wateringIntervalDays)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return dao.insertPlant(
            PlantEntity(
                name = name.trim(),
                species = species.clean(),
                location = location.clean(),
                notes = notes.clean(),
                careInstructions = careInstructions.clean(),
                createdAt = now.toEpochMilli(),
                updatedAt = now.toEpochMilli(),
                wateringIntervalDays = wateringIntervalDays,
                nextWateringAt = next,
                status = status,
                notificationEnabled = notificationEnabled,
                customReminderHour = customReminderHour,
                customReminderMinute = customReminderMinute
            )
        )
    }

    suspend fun updatePlant(plant: PlantEntity) {
        dao.updatePlant(plant.copy(updatedAt = Instant.now().toEpochMilli()))
    }

    suspend fun markWatered(plantId: Long, note: String? = null) {
        val plant = dao.getPlant(plantId) ?: return
        val now = Instant.now()
        val next = WateringSchedule.nextWateringDate(now, Instant.ofEpochMilli(plant.createdAt), plant.wateringIntervalDays)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        dao.replacePlantAfterWatering(
            plant.copy(
                lastWateredAt = now.toEpochMilli(),
                nextWateringAt = next,
                updatedAt = now.toEpochMilli()
            ),
            WateringEventEntity(
                plantId = plantId,
                scheduledFor = plant.nextWateringAt,
                completedAt = now.toEpochMilli(),
                status = WateringEventStatus.Completed,
                note = note.clean()
            )
        )
    }

    suspend fun skipWatering(plantId: Long, note: String? = null) {
        val plant = dao.getPlant(plantId) ?: return
        val next = Instant.ofEpochMilli(plant.nextWateringAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .plusDays(plant.wateringIntervalDays.toLong())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val now = Instant.now().toEpochMilli()
        dao.replacePlantAfterWatering(
            plant.copy(nextWateringAt = next, updatedAt = now),
            WateringEventEntity(
                plantId = plantId,
                scheduledFor = plant.nextWateringAt,
                completedAt = now,
                status = WateringEventStatus.Skipped,
                note = note.clean()
            )
        )
    }

    suspend fun postponeWatering(plantId: Long, days: Int = 1) {
        val plant = dao.getPlant(plantId) ?: return
        val next = Instant.ofEpochMilli(plant.nextWateringAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .plusDays(days.toLong())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val now = Instant.now().toEpochMilli()
        dao.replacePlantAfterWatering(
            plant.copy(nextWateringAt = next, updatedAt = now),
            WateringEventEntity(
                plantId = plantId,
                scheduledFor = plant.nextWateringAt,
                completedAt = now,
                status = WateringEventStatus.Postponed
            )
        )
    }

    suspend fun archivePlant(plantId: Long) {
        dao.archivePlant(plantId, Instant.now().toEpochMilli())
    }

    suspend fun deletePlant(plantId: Long) {
        val plant = dao.getPlant(plantId) ?: return
        dao.getPhotos(plantId).forEach { photoStorage.delete(it.filePath) }
        dao.deletePlant(plant)
    }

    suspend fun addNote(plantId: Long, note: String) {
        if (note.isBlank()) return
        dao.insertNote(PlantNoteEntity(plantId = plantId, createdAt = Instant.now().toEpochMilli(), note = note.trim()))
    }

    suspend fun addPhotoFromUri(
        plantId: Long,
        sourceUri: Uri,
        createdAt: Long,
        note: String?,
        growthStage: GrowthStage?,
        customGrowthStage: String?
    ): Long {
        val localReference = photoStorage.copyFromUri(sourceUri)
        return addPhotoReference(plantId, localReference, createdAt, note, growthStage, customGrowthStage)
    }

    suspend fun addPhotoReference(
        plantId: Long,
        localReference: String,
        createdAt: Long = Instant.now().toEpochMilli(),
        note: String? = null,
        growthStage: GrowthStage? = null,
        customGrowthStage: String? = null
    ): Long {
        val id = dao.insertPhoto(
            PlantPhotoEntity(
                plantId = plantId,
                filePath = localReference,
                createdAt = createdAt,
                note = note.clean(),
                growthStage = growthStage,
                customGrowthStage = customGrowthStage.clean()
            )
        )
        val plant = dao.getPlant(plantId)
        if (plant != null && plant.coverPhotoId == null) {
            dao.updatePlant(plant.copy(coverPhotoId = id, updatedAt = Instant.now().toEpochMilli()))
        }
        return id
    }

    suspend fun updatePhotoMetadata(
        photoId: Long,
        createdAt: Long,
        note: String?,
        growthStage: GrowthStage?,
        customGrowthStage: String?
    ) {
        val photo = dao.getPhoto(photoId) ?: return
        dao.updatePhoto(
            photo.copy(
                createdAt = createdAt,
                note = note.clean(),
                growthStage = growthStage,
                customGrowthStage = customGrowthStage.clean()
            )
        )
    }

    suspend fun deletePhoto(photoId: Long) {
        val photo = dao.getPhoto(photoId) ?: return
        dao.deletePhoto(photo)
        photoStorage.delete(photo.filePath)
        val plant = dao.getPlant(photo.plantId)
        if (plant?.coverPhotoId == photoId) {
            dao.updatePlant(plant.copy(coverPhotoId = null, updatedAt = Instant.now().toEpochMilli()))
        }
    }

    fun photoUri(photo: PlantPhotoEntity): Uri = photoStorage.displayUri(photo.filePath)

    suspend fun exportSnapshot(): ExportSnapshot =
        ExportSnapshot(plants = dao.getActivePlants(), wateringEvents = dao.getAllWateringEvents())
}

data class ExportSnapshot(
    val plants: List<PlantEntity>,
    val wateringEvents: List<WateringEventEntity>
)

private fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
