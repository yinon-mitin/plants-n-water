package dev.yinon.plantsnwater.domain

data class BackupManifest(
    val app: String,
    val formatVersion: Int,
    val includesPhotos: Boolean
)

data class BackupIndex(
    val manifest: BackupManifest,
    val plantIds: Set<Long>,
    val wateringPlantIds: Set<Long>,
    val photoPlantIds: Set<Long>,
    val notePlantIds: Set<Long>
)

object ExportValidator {
    private const val CurrentFormatVersion = 1
    private const val AppId = "plants-n-water"

    fun validate(index: BackupIndex): ImportValidationResult {
        if (index.manifest.app != AppId) {
            return ImportValidationResult.Invalid("Backup was not created by Plants N Water.")
        }
        if (index.manifest.formatVersion > CurrentFormatVersion) {
            return ImportValidationResult.Invalid("Backup format is newer than this app supports.")
        }
        val referencedPlantIds = index.wateringPlantIds + index.photoPlantIds + index.notePlantIds
        val missing = referencedPlantIds - index.plantIds
        if (missing.isNotEmpty()) {
            return ImportValidationResult.Invalid("Backup references missing plant IDs: ${missing.sorted().joinToString()}.")
        }
        return ImportValidationResult.Valid
    }
}

sealed interface ImportValidationResult {
    data object Valid : ImportValidationResult
    data class Invalid(val reason: String) : ImportValidationResult
}
