package com.example.fuelconsumption2

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
            val visibleTankingsFetched = tankingRepository.getAllTankingsInBetweenByVehicleId(recentVehicleId, historyStart, historyEnd)
            val totalFuel = visibleTankingsFetched.fold(0f) { acc, tanking -> acc + (tanking.FuelAmount ?: 0f) }
            val kilometersBefore = visibleTankingsFetched.firstOrNull()?.KilometersBefore ?: 0
            val kilometersAfter = visibleTankingsFetched.lastOrNull()?.KilometersAfter ?: 0
            val totalKm = kilometersBefore - kilometersAfter
            val totalCost = visibleTankingsFetched.fold(0f) { acc, tanking -> acc + (tanking.Cost ?: 0f) }
            val averageCost = if (totalKm > 0) totalCost / totalKm * 100 else 0f
            val averageConsumption = if (totalKm > 0) (totalFuel / totalKm) * 100 else 0f

            _state.update {
                it.copy(
                    visibleTankings = visibleTankingsFetched,
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
    suspend fun insertTankings(vararg newTanking: Tanking) {
        viewModelScope.launch {
            tankingRepository.insertTankings(*newTanking)
        }
    }

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

    suspend fun refreshVisibleTankings() {
        val currentTankings = tankingRepository.getAllTankingsInBetweenByVehicleId(_state.value.currentVehicle, _state.value.historyFilterStart, _state.value.historyFilterEnd)
        _state.update {
            it.copy(visibleTankings = currentTankings)
        }
    }

    fun getAllTankings(): Flow<List<Tanking>> {
        return tankingRepository.getAllTankings()
    }

    //VEHICLES
    fun getVehicleById(vehicleId: Int): Flow<Vehicle> {
        return vehicleRepository.getVehicleById(vehicleId)
    }

    suspend fun getAllVehiclesForAddingTanking(): List<Vehicle> {
        return vehicleRepository.getAllVehiclesForAddingTanking()
    }
}