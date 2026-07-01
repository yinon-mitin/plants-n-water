package dev.yinon.plantsnwater.data.repository

import dev.yinon.plantsnwater.data.local.PlantPhotoEntity

object PlantPhotoCollections {
    fun newestFirst(photos: List<PlantPhotoEntity>): List<PlantPhotoEntity> =
        photos.sortedWith(compareByDescending<PlantPhotoEntity> { it.createdAt }.thenByDescending { it.id })

    fun latestByPlant(photos: List<PlantPhotoEntity>): Map<Long, PlantPhotoEntity> =
        newestFirst(photos).distinctBy { it.plantId }.associateBy { it.plantId }
}
