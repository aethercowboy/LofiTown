package com.example.lofitown.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lofitown.audio.LofiAudioEngine
import com.example.lofitown.data.TownDatabase
import com.example.lofitown.data.TownRepository
import com.example.lofitown.model.BuildingType
import com.example.lofitown.model.Citizen
import com.example.lofitown.model.Milestone
import com.example.lofitown.model.TerrainType
import com.example.lofitown.model.TimeOfDay
import com.example.lofitown.model.TownTile
import com.example.lofitown.model.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

data class OfflineReward(
    val minutesAway: Long,
    val vibesCollected: Long,
    val cupsOfTea: Int
)

data class ActiveCitizenThought(
    val citizen: Citizen,
    val thought: String
)

enum class TimeFlowSpeed(val title: String, val minutesPerSecond: Float) {
    SLOW("Slow", 0.5f),      // 1 game hour = 2 real minutes
    DRIFT("Drift", 2.0f),    // 1 game hour = 30 seconds
    PAUSED("Stargaze", 0.0f) // time frozen
}

class TownViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TownDatabase.getInstance(application)
    private val repository = TownRepository(database.townDao())
    val audioEngine = LofiAudioEngine()

    // Town tiles state
    private val _tiles = MutableStateFlow<List<TownTile>>(emptyList())
    val tiles: StateFlow<List<TownTile>> = _tiles.asStateFlow()

    // Selection
    private val _selectedTile = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedTile: StateFlow<Pair<Int, Int>?> = _selectedTile.asStateFlow()

    // Resources
    private val _vibes = MutableStateFlow(50.0)
    val vibes: StateFlow<Double> = _vibes.asStateFlow()

    private val _zenStars = MutableStateFlow(10L)
    val zenStars: StateFlow<Long> = _zenStars.asStateFlow()

    // Time & Atmosphere
    private val _minuteOfDay = MutableStateFlow(18 * 60 + 30) // Default 18:30 Dusk
    val minuteOfDay: StateFlow<Int> = _minuteOfDay.asStateFlow()

    private val _timeOfDay = MutableStateFlow(TimeOfDay.DUSK)
    val timeOfDay: StateFlow<TimeOfDay> = _timeOfDay.asStateFlow()

    private val _timeSpeed = MutableStateFlow(TimeFlowSpeed.SLOW)
    val timeSpeed: StateFlow<TimeFlowSpeed> = _timeSpeed.asStateFlow()

    private val _weather = MutableStateFlow(Weather.GENTLE_RAIN)
    val weather: StateFlow<Weather> = _weather.asStateFlow()

    // Citizens
    private val _citizens = MutableStateFlow(Citizen.createDefaultCitizens())
    val citizens: StateFlow<List<Citizen>> = _citizens.asStateFlow()

    private val _activeThought = MutableStateFlow<ActiveCitizenThought?>(null)
    val activeThought: StateFlow<ActiveCitizenThought?> = _activeThought.asStateFlow()

    // Milestones
    private val _milestones = MutableStateFlow(Milestone.defaultMilestones())
    val milestones: StateFlow<List<Milestone>> = _milestones.asStateFlow()

    private val _offlineReward = MutableStateFlow<OfflineReward?>(null)
    val offlineReward: StateFlow<OfflineReward?> = _offlineReward.asStateFlow()

    // Focus session (Chill study timer)
    private val _focusSecondsRemaining = MutableStateFlow(0)
    val focusSecondsRemaining: StateFlow<Int> = _focusSecondsRemaining.asStateFlow()
    private val _isFocusActive = MutableStateFlow(false)
    val isFocusActive: StateFlow<Boolean> = _isFocusActive.asStateFlow()

    // UI Sheets & Modes
    private val _isBuildMenuOpen = MutableStateFlow(false)
    val isBuildMenuOpen: StateFlow<Boolean> = _isBuildMenuOpen.asStateFlow()

    private val _isRadioSheetOpen = MutableStateFlow(false)
    val isRadioSheetOpen: StateFlow<Boolean> = _isRadioSheetOpen.asStateFlow()

    private val _isJournalOpen = MutableStateFlow(false)
    val isJournalOpen: StateFlow<Boolean> = _isJournalOpen.asStateFlow()

    private val _isFocusDialogOpen = MutableStateFlow(false)
    val isFocusDialogOpen: StateFlow<Boolean> = _isFocusDialogOpen.asStateFlow()

    private val _isZenMode = MutableStateFlow(false) // Screen saver / pure view
    val isZenMode: StateFlow<Boolean> = _isZenMode.asStateFlow()

    // Tap hearts animation
    private val _floatingHearts = MutableStateFlow<List<Pair<Float, Float>>>(emptyList())
    val floatingHearts: StateFlow<List<Pair<Float, Float>>> = _floatingHearts.asStateFlow()

    val totalVibesPerSec: Double
        get() = _tiles.value.sumOf { it.totalVibesPerSec }

    init {
        loadTown()
        startGameLoop()
        startCitizenWanderLoop()
        startFocusTimerLoop()
    }

    private fun loadTown() {
        viewModelScope.launch {
            val loadedTiles = repository.initializeDefaultTownIfNeeded()
            _tiles.value = loadedTiles

            val savedVibes = repository.getVibes()
            val savedZen = repository.getZenStars()
            val savedMinute = repository.getTimeMinute().toInt()
            val lastSavedTime = repository.getLastSavedTime()

            _vibes.value = savedVibes
            _zenStars.value = savedZen
            _minuteOfDay.value = savedMinute
            _timeOfDay.value = TimeOfDay.fromMinutes(savedMinute)

            // Calculate offline earnings
            val now = System.currentTimeMillis()
            val diffMs = now - lastSavedTime
            val diffMinutes = diffMs / 60000L

            if (diffMinutes >= 2) {
                val rate = loadedTiles.sumOf { it.totalVibesPerSec }
                val cappedMinutes = diffMinutes.coerceAtMost(720) // 12 hrs max
                val earned = (rate * cappedMinutes * 60 * 0.85).toLong() // 85% offline efficiency
                if (earned > 0) {
                    _vibes.value += earned
                    repository.saveVibes(_vibes.value)
                    _offlineReward.value = OfflineReward(
                        minutesAway = cappedMinutes,
                        vibesCollected = earned,
                        cupsOfTea = (cappedMinutes / 12).toInt().coerceAtLeast(1)
                    )
                }
            }
        }
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            var saveCounter = 0
            while (isActive) {
                delay(100L) // 10 ticks per second

                // 1. Accrue passive vibes
                val rate = totalVibesPerSec
                if (rate > 0) {
                    val gain = rate * 0.1
                    _vibes.value += gain
                }

                // 2. Slow time progression
                val speed = _timeSpeed.value.minutesPerSecond
                if (speed > 0f) {
                    val addedMinutes = speed * 0.1f
                    val newMinute = (_minuteOfDay.value + addedMinutes) % 1440f
                    _minuteOfDay.value = newMinute.toInt()
                    val newTimeOfDay = TimeOfDay.fromMinutes(_minuteOfDay.value)
                    if (newTimeOfDay != _timeOfDay.value) {
                        _timeOfDay.value = newTimeOfDay
                        checkMilestones()
                    }
                }

                // 3. Periodic persistence (every 5 seconds)
                saveCounter++
                if (saveCounter >= 50) {
                    saveCounter = 0
                    withContext(Dispatchers.IO) {
                        repository.saveVibes(_vibes.value)
                        repository.saveZenStars(_zenStars.value)
                        repository.saveTimeMinute(_minuteOfDay.value.toLong())
                        repository.updateLastSavedTime()
                    }
                }
            }
        }
    }

    private fun startCitizenWanderLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(3000L) // Citizens think about moving every 3s
                val currentTiles = _tiles.value
                val walkableTiles = currentTiles.filter { it.terrain.isWalkable && it.building == null }
                if (walkableTiles.isNotEmpty()) {
                    val updatedCitizens = _citizens.value.map { c ->
                        if (Random.nextFloat() < 0.45f) {
                            val target = walkableTiles.random()
                            c.copy(
                                gridX = target.x.toFloat(),
                                gridY = target.y.toFloat()
                            )
                        } else c
                    }
                    _citizens.value = updatedCitizens
                }
            }
        }
    }

    private fun startFocusTimerLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                if (_isFocusActive.value && _focusSecondsRemaining.value > 0) {
                    _focusSecondsRemaining.value -= 1
                    if (_focusSecondsRemaining.value == 0) {
                        _isFocusActive.value = false
                        // Reward focus completion
                        _zenStars.value += 15
                        _vibes.value += 500.0
                        completeMilestone("m_focus_zen")
                    }
                }
            }
        }
    }

    fun selectTile(x: Int, y: Int) {
        if (_selectedTile.value?.first == x && _selectedTile.value?.second == y) {
            _selectedTile.value = null
        } else {
            _selectedTile.value = Pair(x, y)
        }
    }

    fun clearSelection() {
        _selectedTile.value = null
    }

    fun buildOnSelectedTile(type: BuildingType) {
        val sel = _selectedTile.value ?: return
        val currentTile = _tiles.value.firstOrNull { it.x == sel.first && it.y == sel.second } ?: return

        if (_vibes.value >= type.cost) {
            _vibes.value -= type.cost
            val updated = currentTile.copy(building = type, level = 1)
            updateTile(updated)
            checkMilestones()
        }
    }

    fun upgradeSelectedBuilding() {
        val sel = _selectedTile.value ?: return
        val currentTile = _tiles.value.firstOrNull { it.x == sel.first && it.y == sel.second } ?: return
        val building = currentTile.building ?: return
        val cost = building.upgradeCost(currentTile.level)

        if (_vibes.value >= cost) {
            _vibes.value -= cost
            val updated = currentTile.copy(level = currentTile.level + 1)
            updateTile(updated)
            checkMilestones()
        }
    }

    fun setTerrainOnSelectedTile(terrain: TerrainType) {
        val sel = _selectedTile.value ?: return
        val currentTile = _tiles.value.firstOrNull { it.x == sel.first && it.y == sel.second } ?: return

        if (_vibes.value >= terrain.cost) {
            _vibes.value -= terrain.cost
            val updated = currentTile.copy(terrain = terrain, building = null)
            updateTile(updated)
            checkMilestones()
        }
    }

    fun demolishSelectedBuilding() {
        val sel = _selectedTile.value ?: return
        val currentTile = _tiles.value.firstOrNull { it.x == sel.first && it.y == sel.second } ?: return
        val updated = currentTile.copy(building = null, level = 1)
        updateTile(updated)
    }

    private fun updateTile(updated: TownTile) {
        val newList = _tiles.value.map {
            if (it.x == updated.x && it.y == updated.y) updated else it
        }
        _tiles.value = newList
        viewModelScope.launch {
            repository.saveTile(updated)
        }
    }

    fun tapCitizen(citizen: Citizen) {
        val thought = citizen.randomThought()
        _activeThought.value = ActiveCitizenThought(citizen, thought)
        // Bonus vibes on tap
        _vibes.value += 5.0
        if (citizen.isCat) {
            completeMilestone("m_pet_cat")
        }
    }

    fun dismissThought() {
        _activeThought.value = null
    }

    fun dismissOfflineReward() {
        _offlineReward.value = null
    }

    fun cycleTimeSpeed() {
        _timeSpeed.value = when (_timeSpeed.value) {
            TimeFlowSpeed.SLOW -> TimeFlowSpeed.DRIFT
            TimeFlowSpeed.DRIFT -> TimeFlowSpeed.PAUSED
            TimeFlowSpeed.PAUSED -> TimeFlowSpeed.SLOW
        }
    }

    fun cycleWeather() {
        val weathers = Weather.entries
        val nextIdx = (weathers.indexOf(_weather.value) + 1) % weathers.size
        _weather.value = weathers[nextIdx]
        checkMilestones()
    }

    fun setWeather(w: Weather) {
        _weather.value = w
        checkMilestones()
    }

    fun startFocusSession(minutes: Int) {
        _focusSecondsRemaining.value = minutes * 60
        _isFocusActive.value = true
        _isFocusDialogOpen.value = false
    }

    fun cancelFocusSession() {
        _isFocusActive.value = false
        _focusSecondsRemaining.value = 0
    }

    fun setBuildMenuOpen(open: Boolean) {
        _isBuildMenuOpen.value = open
    }

    fun setRadioSheetOpen(open: Boolean) {
        _isRadioSheetOpen.value = open
    }

    fun setJournalOpen(open: Boolean) {
        _isJournalOpen.value = open
    }

    fun setFocusDialogOpen(open: Boolean) {
        _isFocusDialogOpen.value = open
    }

    fun toggleZenMode() {
        _isZenMode.value = !_isZenMode.value
    }

    private fun checkMilestones() {
        val buildings = _tiles.value.mapNotNull { it.building }
        if (buildings.any { it == BuildingType.COFFEE_SHOP }) completeMilestone("m_first_cafe")
        if (buildings.any { it == BuildingType.VINYL_SHOP }) completeMilestone("m_vinyl")
        if (buildings.any { it == BuildingType.BOOKSTORE }) completeMilestone("m_bookstore")
        if (buildings.any { it == BuildingType.RAMEN_STALL }) completeMilestone("m_ramen")
        if (_timeOfDay.value == TimeOfDay.DUSK) completeMilestone("m_dusk_watcher")
        if (_timeOfDay.value == TimeOfDay.NIGHT) completeMilestone("m_night_sky")
        if (_weather.value == Weather.GENTLE_RAIN) completeMilestone("m_rain_listener")
        if (_vibes.value >= 10000) completeMilestone("m_vibes_10k")
    }

    private fun completeMilestone(id: String) {
        val current = _milestones.value
        val target = current.firstOrNull { it.id == id } ?: return
        if (!target.isCompleted) {
            _zenStars.value += target.rewardZen
            _milestones.value = current.map {
                if (it.id == id) it.copy(isCompleted = true) else it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
