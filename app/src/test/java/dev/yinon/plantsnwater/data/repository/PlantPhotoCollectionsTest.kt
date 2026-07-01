package dev.yinon.plantsnwater.data.repository

import dev.yinon.plantsnwater.data.local.PlantPhotoEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantPhotoCollectionsTest {
    @Test
    fun newestFirstOrdersTimelineByDateThenId() {
        val photos = listOf(
            photo(id = 1, plantId = 1, createdAt = 100),
            photo(id = 2, plantId = 1, createdAt = 300),
            photo(id = 3, plantId = 1, createdAt = 300),
            photo(id = 4, plantId = 1, createdAt = 200)
        )

        val ordered = PlantPhotoCollections.newestFirst(photos)

        assertEquals(listOf(3L, 2L, 4L, 1L), ordered.map { it.id })
    }

    @Test
    fun latestByPlantReturnsMostRecentThumbnailPhoto() {
        val latest = PlantPhotoCollections.latestByPlant(
            listOf(
                photo(id = 1, plantId = 10, createdAt = 100),
                photo(id = 2, plantId = 10, createdAt = 200),
                photo(id = 3, plantId = 20, createdAt = 150)
            )
        )

        assertEquals(2L, latest.getValue(10).id)
        assertEquals(3L, latest.getValue(20).id)
    }

    @Test
    fun latestByPlantHandlesPlantsWithNoPhotos() {
        val latest = PlantPhotoCollections.latestByPlant(emptyList())

        assertTrue(latest.isEmpty())
    }

    private fun photo(id: Long, plantId: Long, createdAt: Long): PlantPhotoEntity =
        PlantPhotoEntity(
            id = id,
            plantId = plantId,
            filePath = "$id.jpg",
            createdAt = createdAt
        )
}
