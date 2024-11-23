package com.example.fuelconsumption2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fuelconsumption2.data.AppDatabase
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
import java.time.temporal.ChronoUnit

class TankingsSummaryViewModel(private val db: AppDatabase): ViewModel() {
    private val tankingRepository = TankingRepository(db.tankingDao())
    private val vehicleRepository = VehicleRepository(db.vehicleDao())
    private val configurationRepository = ConfigurationRepository(db.configurationDao())
    private val _state = MutableStateFlow(TankingsSummaryState())
    val state: StateFlow<TankingsSummaryState> = _state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TankingsSummaryState())

    private val _events = MutableSharedFlow<TankingEvent>()
    val events: SharedFlow<TankingEvent> = _events.asSharedFlow()

    val tankings = mutableListOf<Tanking>()

    fun populateDefaults() {
        val currentTimestamp = Instant.now().toEpochMilli()
        val oneYearBeforeNowTimestamp = Instant.now().minus(1, ChronoUnit.YEARS).toEpochMilli()

        //get consumption and cost and calculate averages from db
        val recentVehicleId: Int? = configurationRepository.getRecentVehicleId()

        val visibleTankings = tankingRepository.getAllTankingsInBetweenByVehicleId(
            recentVehicleId,
            oneYearBeforeNowTimestamp,
            currentTimestamp
        )

        viewModelScope.launch {
            visibleTankings.collect { tankings ->
                val totalFuel = tankings.fold(0f) { acc, tanking ->
                    acc + (tanking.FuelAmount ?: 0f)
                }

                val kilometersBefore = tankings.firstOrNull()?.KilometersBefore ?: 0
                val kilometersAfter = tankings.lastOrNull()?.KilometersAfter ?: 0
                val totalKm = kilometersBefore - kilometersAfter

                val totalCost = tankings.fold(0f) { acc, tanking ->
                    acc + (tanking.Cost ?: 0f)
                }
                val averageCost = totalCost / totalKm * 100

                val averageConsumption = if (totalKm > 0) {
                    (totalFuel / totalKm) * 100
                } else {
                    0f
                }

                _state.update {
                    it.copy(
                        visibleTankings = visibleTankings,
                        currentDate = SteroidDate(currentTimestamp),
                        averageConsumption = averageConsumption,
                        averageCost = averageCost,
                        currentVehicle = recentVehicleId,
                        historyFilterStart = SteroidDate(oneYearBeforeNowTimestamp),
                        historyFilterEnd = SteroidDate(currentTimestamp)
                    )
                }
            }
        }
    }

    fun onEvent(event: TankingEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

//    fun updateTankings(
//        visibleTankings: Flow<List<Tanking>>?,
//        currentDate: SteroidDate?,
//        averageConsumption: Float?,
//        averageCost: Float?,
//        isAddingVehicle: Boolean,
//        isFilteringHistory: Boolean,
//        isAddingTanking: Boolean,
//        currentVehicle: Int?,
//        historyFilterStart: SteroidDate?,
//        historyFilterEnd: SteroidDate?
//    ) {
//        _state.update {
//            it.copy(
//                visibleTankings = visibleTankings,
//                currentDate = currentDate,
//                averageConsumption = averageConsumption,
//                averageCost = averageCost,
//                isAddingVehicle = isAddingVehicle,
//                isFilteringHistory = isFilteringHistory,
//                isAddingTanking = isAddingTanking,
//                currentVehicle = currentVehicle,
//                historyFilterStart = historyFilterStart,
//                historyFilterEnd = historyFilterEnd
//            )
//        }
//    }

    fun updateTankingsRecyclerView(eventData: List<Tanking>) {
        tankings.clear()
        tankings.addAll(eventData)
    }

    suspend fun insertTankings(vararg newTanking: Tanking) {
        viewModelScope.launch {
            tankingRepository.insertTanking(*newTanking)
        }
    }

    fun getAllTankings(): Flow<List<Tanking>> {
        return tankingRepository.getAllTankings()
    }

    fun getVehicleById(vehicleId: Int): Flow<Vehicle> {
        return vehicleRepository.getVehicleById(vehicleId)
    }

    fun getAllVehiclesForAddingTanking(): Flow<List<Vehicle>> {
        return vehicleRepository.getAllVehiclesForAddingTanking()
    }

    fun getRecentVehicleId(): Int? {
        return configurationRepository.getRecentVehicleId()
    }
}