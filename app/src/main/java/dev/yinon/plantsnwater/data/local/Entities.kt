package dev.yinon.plantsnwater.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PlantStatus {
    Healthy,
    NeedsAttention,
    Dry,
    Overwatered,
    Recovering
}

enum class WateringEventStatus {
    Scheduled,
    Completed,
    Skipped,
    Postponed
}

enum class GrowthStage {
    Seed,
    Sprout,
    YoungPlant,
    MaturePlant,
    Flowering,
    Fruiting,
    Recovering,
    Custom
}

@Entity(
    tableName = "plants",
    indices = [
        Index("archivedAt"),
        Index("nextWateringAt"),
        Index("name")
    ]
)
data class PlantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val species: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val careInstructions: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long? = null,
    val wateringIntervalDays: Int,
    val lastWateredAt: Long? = null,
    val nextWateringAt: Long,
    val status: PlantStatus = PlantStatus.Healthy,
    val notificationEnabled: Boolean = true,
    val customReminderHour: Int? = null,
    val customReminderMinute: Int? = null,
    val coverPhotoId: Long? = null
)

@Entity(
    tableName = "watering_events",
    foreignKeys = [
        ForeignKey(
            entity = PlantEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plantId"), Index("scheduledFor"), Index("status")]
)
data class WateringEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val scheduledFor: Long,
    val completedAt: Long? = null,
    val status: WateringEventStatus,
    val note: String? = null
)

@Entity(
    tableName = "plant_photos",
    foreignKeys = [
        ForeignKey(
            entity = PlantEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plantId"), Index("createdAt")]
)
data class PlantPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val filePath: String,
    val createdAt: Long,
    val note: String? = null,
    val growthStage: GrowthStage? = null,
    val customGrowthStage: String? = null
)

@Entity(
    tableName = "plant_notes",
    foreignKeys = [
        ForeignKey(
            entity = PlantEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plantId"), Index("createdAt")]
)
data class PlantNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val createdAt: Long,
    val note: String
)

@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(defaultValue = "#6A8F3A") val color: String = "#6A8F3A"
)

@Entity(
    tableName = "plant_tags",
    primaryKeys = ["plantId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = PlantEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tagId")]
)
data class PlantTagCrossRef(
    val plantId: Long,
    val tagId: Long
)
