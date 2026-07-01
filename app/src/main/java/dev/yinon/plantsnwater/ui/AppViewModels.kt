package dev.yinon.plantsnwater.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.yinon.plantsnwater.core.AppContainer
import dev.yinon.plantsnwater.data.local.PlantEntity
import dev.yinon.plantsnwater.data.local.PlantStatus
import dev.yinon.plantsnwater.data.local.WateringEventEntity
import dev.yinon.plantsnwater.data.repository.PlantRepository
import dev.yinon.plantsnwater.domain.WateringSchedule
import dev.yinon.plantsnwater.settings.AppSettings
import dev.yinon.plantsnwater.settings.SettingsRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer was not provided.")
}

class HomeViewModel(private val container: AppContainer) : ViewModel() {
    private val repository = container.plantRepository

    val uiState: StateFlow<HomeUiState> = repository.observeActivePlants()
        .map { plants ->
            val today = LocalDate.now()
            val due = plants.filter { it.nextWateringLocalDate() <= today }
            val upcoming = plants.filter { it.nextWateringLocalDate() > today }.take(5)
            HomeUiState(duePlants = due, upcomingPlants = upcoming)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun markWatered(id: Long) = updateAndSchedule(id) { repository.markWatered(id) }
    fun skip(id: Long) = updateAndSchedule(id) { repository.skipWatering(id) }
    fun postpone(id: Long) = updateAndSchedule(id) { repository.postponeWatering(id) }

    private fun updateAndSchedule(id: Long, action: suspend () -> Unit) = viewModelScope.launch {
        action()
        repository.getPlant(id)?.let { container.notificationScheduler.schedule(it) }
    }
}

data class HomeUiState(
    val duePlants: List<PlantEntity> = emptyList(),
    val upcomingPlants: List<PlantEntity> = emptyList()
)

class PlantListViewModel(private val repository: PlantRepository) : ViewModel() {
    val plants: StateFlow<List<PlantEntity>> = repository.observeActivePlants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun archive(id: Long) = viewModelScope.launch { repository.archivePlant(id) }
}

class AddPlantViewModel(private val container: AppContainer) : ViewModel() {
    private val repository = container.plantRepository

    fun addPlant(
        name: String,
        intervalDays: Int,
        species: String?,
        location: String?,
        notes: String?,
        careInstructions: String?,
        status: PlantStatus,
        onCreated: (Long) -> Unit
    ) {
        if (name.isBlank() || intervalDays < 1) return
        viewModelScope.launch {
            val id = repository.addPlant(
                name = name,
                wateringIntervalDays = intervalDays,
                species = species,
                location = location,
                notes = notes,
                careInstructions = careInstructions,
                status = status
            )
            repository.getPlant(id)?.let { container.notificationScheduler.schedule(it) }
            onCreated(id)
        }
    }
}

class PlantDetailViewModel(private val plantId: Long, private val container: AppContainer) : ViewModel() {
    private val repository = container.plantRepository

    val uiState: StateFlow<PlantDetailUiState> = combine(
        repository.observePlant(plantId),
        repository.observeWateringEvents(plantId),
        repository.observePhotos(plantId),
        repository.observeNotes(plantId)
    ) { plant, events, photos, notes ->
        PlantDetailUiState(plant, events, photos.size, notes.map { it.note })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlantDetailUiState())

    fun markWatered() = updateAndSchedule { repository.markWatered(plantId) }
    fun skip() = updateAndSchedule { repository.skipWatering(plantId) }
    fun postpone() = updateAndSchedule { repository.postponeWatering(plantId) }
    fun addNote(note: String) = viewModelScope.launch { repository.addNote(plantId, note) }

    private fun updateAndSchedule(action: suspend () -> Unit) = viewModelScope.launch {
        action()
        repository.getPlant(plantId)?.let { container.notificationScheduler.schedule(it) }
    }
}

data class PlantDetailUiState(
    val plant: PlantEntity? = null,
    val events: List<WateringEventEntity> = emptyList(),
    val photoCount: Int = 0,
    val notes: List<String> = emptyList()
)

class CalendarViewModel(private val repository: PlantRepository) : ViewModel() {
    val plants: StateFlow<List<PlantEntity>> = repository.observeActivePlants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setNotificationsEnabled(enabled: Boolean) =
        viewModelScope.launch { settingsRepository.setNotificationsEnabled(enabled) }
}

class PlantViewModelFactory(
    private val container: AppContainer,
    private val plantId: Long? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            HomeViewModel::class.java -> HomeViewModel(container)
            PlantListViewModel::class.java -> PlantListViewModel(container.plantRepository)
            AddPlantViewModel::class.java -> AddPlantViewModel(container)
            CalendarViewModel::class.java -> CalendarViewModel(container.plantRepository)
            SettingsViewModel::class.java -> SettingsViewModel(container.settingsRepository)
            PlantDetailViewModel::class.java -> PlantDetailViewModel(requireNotNull(plantId), container)
            else -> error("Unknown ViewModel $modelClass")
        } as T
    }
}

fun PlantEntity.nextWateringLocalDate(): LocalDate =
    Instant.ofEpochMilli(nextWateringAt).atZone(ZoneId.systemDefault()).toLocalDate()

fun PlantEntity.wateringLabel(): String {
    val state = WateringSchedule.classify(nextWateringLocalDate())
    return when (state) {
        is dev.yinon.plantsnwater.domain.WateringState.DueToday -> "Today"
        is dev.yinon.plantsnwater.domain.WateringState.Overdue -> "${state.days}d overdue"
        is dev.yinon.plantsnwater.domain.WateringState.Upcoming -> "In ${state.days}d"
    }
}
