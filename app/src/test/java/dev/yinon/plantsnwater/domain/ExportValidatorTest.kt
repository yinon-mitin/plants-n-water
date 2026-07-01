package dev.yinon.plantsnwater.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportValidatorTest {
    @Test
    fun validBackupPasses() {
        val result = ExportValidator.validate(
            BackupIndex(
                manifest = BackupManifest("plants-n-water", 1, true),
                plantIds = setOf(1, 2),
                wateringPlantIds = setOf(1),
                photoPlantIds = setOf(2),
                notePlantIds = emptySet()
            )
        )

        assertEquals(ImportValidationResult.Valid, result)
    }

    @Test
    fun foreignBackupFails() {
        val result = ExportValidator.validate(
            BackupIndex(
                manifest = BackupManifest("other-app", 1, false),
                plantIds = setOf(1),
                wateringPlantIds = emptySet(),
                photoPlantIds = emptySet(),
                notePlantIds = emptySet()
            )
        )

        assertTrue(result is ImportValidationResult.Invalid)
    }

    @Test
    fun missingReferencedPlantFails() {
        val result = ExportValidator.validate(
            BackupIndex(
                manifest = BackupManifest("plants-n-water", 1, false),
                plantIds = setOf(1),
                wateringPlantIds = setOf(2),
                photoPlantIds = emptySet(),
                notePlantIds = emptySet()
            )
        )

        assertTrue(result is ImportValidationResult.Invalid)
    }
}
