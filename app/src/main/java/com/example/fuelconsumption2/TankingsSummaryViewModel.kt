package com.example.fuelconsumption2

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fuelconsumption2.data.AppDatabase
import com.example.fuelconsumption2.data.entities.Configuration
import com.example.fuelconsumption2.data.entities.Tanking
import com.example.fuelconsumption2.data.entities.Vehicle
import com.example.fuelconsumption2.data.repository.ConfigurationRepository
import com.example.fuelconsumption2.data.repository.TankingRepository
import com.example.fuelconsumption2.data.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

class TankingsSummaryViewModel(private val db: AppDatabase): ViewModel() {
    private val tankingRepository = TankingRepository(db.tankingDao())
    private val vehicleRepository = VehicleRepository(db.vehicleDao())
    private val configurationRepository = ConfigurationRepository(db.configurationDao())

    private val _state = MutableStateFlow(TankingsSummaryState())
    val state: StateFlow<TankingsSummaryState> = _state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TankingsSummaryState())

    private val _events = MutableSharedFlow<TankingEvent>()
    val events: SharedFlow<TankingEvent> = _events.asSharedFlow()

    private var _currentTankings = MutableStateFlow<List<Tanking>>(emptyList())
    val currentTankings: StateFlow<List<Tanking>> get() = _currentTankings

    fun initializeState() {
        val currentTimestamp = Instant.now()
        val historyStart = _state.value.historyFilterEnd ?: SteroidDate.oneYearBefore(currentTimestamp).getTimestamp()
        val historyEnd = _state.value.historyFilterStart ?: SteroidDate(currentTimestamp.toEpochMilli()).getTimestamp()

        viewModelScope.launch {
            if(configurationRepository.isConfigurationEmpty()) {
                val defaultConfiguration = Configuration(1, null, null)
                configurationRepository.insertConfiguration(defaultConfiguration)
            }

            val recentVehicleId = configurationRepository.getRecentVehicleId()

            val fetchedCurrentTankings = tankingRepository.getAllTankingsInBetweenByVehicleId(recentVehicleId, historyStart, historyEnd)
            _currentTankings.value = fetchedCurrentTankings
            calculateAverages(fetchedCurrentTankings)
            Log.d("TEST", fetchedCurrentTankings.toString())

            val totalFuel = _currentTankings.value.fold(0f) { acc, tanking -> acc + (tanking.FuelAmount ?: 0f) }
            val kilometersBefore = _currentTankings.value.firstOrNull()?.KilometersBefore ?: 0
            val kilometersAfter = _currentTankings.value.lastOrNull()?.KilometersAfter ?: 0
            val totalKm = kilometersBefore - kilometersAfter
            val totalCost = _currentTankings.value.fold(0f) { acc, tanking -> acc + (tanking.Cost ?: 0f) }
            val averageCost = if (totalKm > 0) totalCost / totalKm * 100 else 0f
            val averageConsumption = if (totalKm > 0) (totalFuel / totalKm) * 100 else 0f

            _state.update {
                it.copy(
                    currentDate = SteroidDate(currentTimestamp.toEpochMilli()),
                    averageConsumption = averageConsumption,
                    averageCost = averageCost,
                    currentVehicle = recentVehicleId,
                    historyFilterStart = historyStart,
                    historyFilterEnd = historyEnd
                )
            }
        }
    }

    fun onEvent(event: TankingEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    //TANKINGS
    suspend fun insertTanking(tanking: Tanking) {
        viewModelScope.launch {
            tankingRepository.insertTankings(tanking)

            val currentTimestampForState = Instant.now()
            val currentSteroidDate = SteroidDate(currentTimestampForState.toEpochMilli())
            val oneYearBeforeNowTimestamp = SteroidDate.oneYearBefore(currentTimestampForState).getTimestamp()

            _state.update {
                it.copy(
                    currentDate = currentSteroidDate,
                    isAddingTanking = false,
                    currentVehicle = tanking.VehicleId,
                    historyFilterStart = oneYearBeforeNowTimestamp,
                    historyFilterEnd = currentTimestampForState.toEpochMilli()
                )
            }
        }
    }

    suspend fun updateCurrentTankings(start: Long?, end: Long?) {
        val newTankings = tankingRepository.getAllTankingsInBetweenByVehicleId(_state.value.currentVehicle, start, end)
        _currentTankings.value = newTankings
        calculateAverages(newTankings)
    }

    private fun calculateAverages(tankings: List<Tanking>) {
        var totalFuel :Float = 0.0f
        var totalCost :Float = 0.0f
        var totalDistance :Float = 0.0f

        Log.d("TEST", "calcAvg: "+tankings.toString())

        for(tanking in tankings) {
            totalFuel += tanking.FuelAmount ?: 0.0f
            totalCost += tanking.Cost ?: 0.0f
            val distance = maxOf((tanking.KilometersAfter ?: 0) - (tanking.KilometersBefore ?: 0), 0)
            totalDistance += distance
        }

        val averageConsumption = if (totalDistance > 0) (totalFuel/totalDistance) * 100 else 0.0f
        val averageCost = if (totalDistance > 0) (totalCost/totalDistance) * 100 else 0.0f

        Log.d("TEST", "avgCons: " + averageConsumption + " avgCost: " + averageCost)

        _state.update {
            it.copy(
                averageConsumption = averageConsumption,
                averageCost = averageCost
            )
        }
    }
//    suspend fun refreshVisibleTankings() {
//        val currentTankings = tankingRepository.getAllTankingsInBetweenByVehicleId(_state.value.currentVehicle, _state.value.historyFilterStart, _state.value.historyFilterEnd)
//        _state.update {
//            it.copy(visibleTankings = currentTankings)
//        }
//    }

    //VEHICLES
    suspend fun getAllVehiclesForAddingTanking(): List<Vehicle> {
        return vehicleRepository.getAllVehiclesForAddingTanking()
    }
}