package dev.yinon.plantsnwater.data.repository

import android.content.Context
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
import dev.yinon.plantsnwater.domain.BackupIndex
import dev.yinon.plantsnwater.domain.BackupManifest
import dev.yinon.plantsnwater.domain.ExportValidator
import dev.yinon.plantsnwater.domain.ImportValidationResult
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BackupRepository(
    context: Context,
    private val dao: PlantDao,
    private val photoStorage: PlantPhotoStorage
) {
    private val contentResolver = context.applicationContext.contentResolver

    suspend fun exportTo(uri: Uri): Result<Unit> = runCatching {
        val plants = dao.getAllPlants()
        val events = dao.getAllWateringEvents()
        val photos = dao.getAllPhotos()
        val notes = dao.getAllNotes()
        val data = JSONObject()
            .put("plants", plants.toJsonArray { it.toJson() })
            .put("wateringEvents", events.toJsonArray { it.toJson() })
            .put("plantPhotos", photos.toJsonArray { it.toJson() })
            .put("plantNotes", notes.toJsonArray { it.toJson() })
            .put("tags", JSONArray())
            .put("plantTags", JSONArray())
            .put("settings", JSONObject())
            .toString(2)
        val manifest = JSONObject()
            .put("app", APP_ID)
            .put("formatVersion", FORMAT_VERSION)
            .put("createdAt", Instant.now().toString())
            .put("includesPhotos", photos.isNotEmpty())
            .toString(2)

        withContext(Dispatchers.IO) {
            val output = requireNotNull(contentResolver.openOutputStream(uri)) {
                "Could not open destination file."
            }
            ZipOutputStream(output.buffered()).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(manifest.toByteArray())
                zip.closeEntry()

                zip.putNextEntry(ZipEntry("data.json"))
                zip.write(data.toByteArray())
                zip.closeEntry()

                photos.forEach { photo ->
                    zip.putNextEntry(ZipEntry("media/plant-photos/${photo.filePath}"))
                    photoStorage.copyPhotoTo(photo.filePath, zip)
                    zip.closeEntry()
                }
            }
        }
    }

    suspend fun importReplacingFrom(uri: Uri): Result<Unit> = runCatching {
        val entries = readZip(uri)
        val manifestJson = JSONObject(requireNotNull(entries["manifest.json"]) { "manifest.json is missing." }.toString(Charsets.UTF_8))
        val dataJson = JSONObject(requireNotNull(entries["data.json"]) { "data.json is missing." }.toString(Charsets.UTF_8))
        val plants = dataJson.getJSONArray("plants").mapObjects { it.toPlant() }
        val events = dataJson.optJSONArray("wateringEvents").orEmpty().mapObjects { it.toWateringEvent() }
        val photos = dataJson.optJSONArray("plantPhotos").orEmpty().mapObjects { it.toPlantPhoto() }
        val notes = dataJson.optJSONArray("plantNotes").orEmpty().mapObjects { it.toPlantNote() }
        val validation = ExportValidator.validate(
            BackupIndex(
                manifest = BackupManifest(
                    app = manifestJson.optString("app"),
                    formatVersion = manifestJson.optInt("formatVersion"),
                    includesPhotos = manifestJson.optBoolean("includesPhotos")
                ),
                plantIds = plants.map { it.id }.toSet(),
                wateringPlantIds = events.map { it.plantId }.toSet(),
                photoPlantIds = photos.map { it.plantId }.toSet(),
                notePlantIds = notes.map { it.plantId }.toSet()
            )
        )
        if (validation is ImportValidationResult.Invalid) {
            error(validation.reason)
        }

        dao.replaceBackupData(plants, events, photos, notes)
        photoStorage.clearAll()
        photos.forEach { photo ->
            entries["media/plant-photos/${photo.filePath}"]?.inputStream()?.use { input ->
                photoStorage.writePhoto(photo.filePath, input)
            }
        }
    }

    private suspend fun readZip(uri: Uri): Map<String, ByteArray> = withContext(Dispatchers.IO) {
        val bytesByName = mutableMapOf<String, ByteArray>()
        val input = requireNotNull(contentResolver.openInputStream(uri)) { "Could not open backup file." }
        ZipInputStream(input.buffered()).use { zip ->
            generateSequence { zip.nextEntry }.forEach { entry ->
                if (!entry.isDirectory) {
                    bytesByName[entry.name] = zip.readBytes()
                }
                zip.closeEntry()
            }
        }
        bytesByName
    }

    private companion object {
        const val APP_ID = "plants-n-water"
        const val FORMAT_VERSION = 1
    }
}

private fun <T> List<T>.toJsonArray(mapper: (T) -> JSONObject): JSONArray =
    JSONArray().also { array -> forEach { array.put(mapper(it)) } }

private fun JSONArray?.orEmpty(): JSONArray = this ?: JSONArray()

private fun <T> JSONArray.mapObjects(mapper: (JSONObject) -> T): List<T> =
    (0 until length()).map { index -> mapper(getJSONObject(index)) }

private fun JSONObject.putNullable(name: String, value: Any?): JSONObject =
    put(name, value ?: JSONObject.NULL)

private fun JSONObject.optNullableString(name: String): String? =
    if (isNull(name)) null else optString(name)

private fun JSONObject.optNullableLong(name: String): Long? =
    if (isNull(name)) null else optLong(name)

private fun JSONObject.optNullableInt(name: String): Int? =
    if (isNull(name)) null else optInt(name)

private fun PlantEntity.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("name", name)
    .putNullable("species", species)
    .putNullable("location", location)
    .putNullable("notes", notes)
    .putNullable("careInstructions", careInstructions)
    .put("createdAt", createdAt)
    .put("updatedAt", updatedAt)
    .putNullable("archivedAt", archivedAt)
    .put("wateringIntervalDays", wateringIntervalDays)
    .putNullable("lastWateredAt", lastWateredAt)
    .put("nextWateringAt", nextWateringAt)
    .put("status", status.name)
    .put("notificationEnabled", notificationEnabled)
    .putNullable("customReminderHour", customReminderHour)
    .putNullable("customReminderMinute", customReminderMinute)
    .putNullable("coverPhotoId", coverPhotoId)

private fun JSONObject.toPlant(): PlantEntity = PlantEntity(
    id = getLong("id"),
    name = getString("name"),
    species = optNullableString("species"),
    location = optNullableString("location"),
    notes = optNullableString("notes"),
    careInstructions = optNullableString("careInstructions"),
    createdAt = getLong("createdAt"),
    updatedAt = getLong("updatedAt"),
    archivedAt = optNullableLong("archivedAt"),
    wateringIntervalDays = getInt("wateringIntervalDays"),
    lastWateredAt = optNullableLong("lastWateredAt"),
    nextWateringAt = getLong("nextWateringAt"),
    status = PlantStatus.valueOf(getString("status")),
    notificationEnabled = optBoolean("notificationEnabled", true),
    customReminderHour = optNullableInt("customReminderHour"),
    customReminderMinute = optNullableInt("customReminderMinute"),
    coverPhotoId = optNullableLong("coverPhotoId")
)

private fun WateringEventEntity.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("plantId", plantId)
    .put("scheduledFor", scheduledFor)
    .putNullable("completedAt", completedAt)
    .put("status", status.name)
    .putNullable("note", note)

private fun JSONObject.toWateringEvent(): WateringEventEntity = WateringEventEntity(
    id = getLong("id"),
    plantId = getLong("plantId"),
    scheduledFor = getLong("scheduledFor"),
    completedAt = optNullableLong("completedAt"),
    status = WateringEventStatus.valueOf(getString("status")),
    note = optNullableString("note")
)

private fun PlantPhotoEntity.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("plantId", plantId)
    .put("filePath", filePath)
    .put("createdAt", createdAt)
    .putNullable("note", note)
    .putNullable("growthStage", growthStage?.name)
    .putNullable("customGrowthStage", customGrowthStage)

private fun JSONObject.toPlantPhoto(): PlantPhotoEntity = PlantPhotoEntity(
    id = getLong("id"),
    plantId = getLong("plantId"),
    filePath = getString("filePath"),
    createdAt = getLong("createdAt"),
    note = optNullableString("note"),
    growthStage = optNullableString("growthStage")?.let(GrowthStage::valueOf),
    customGrowthStage = optNullableString("customGrowthStage")
)

private fun PlantNoteEntity.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("plantId", plantId)
    .put("createdAt", createdAt)
    .put("note", note)

private fun JSONObject.toPlantNote(): PlantNoteEntity = PlantNoteEntity(
    id = getLong("id"),
    plantId = getLong("plantId"),
    createdAt = getLong("createdAt"),
    note = getString("note")
)
