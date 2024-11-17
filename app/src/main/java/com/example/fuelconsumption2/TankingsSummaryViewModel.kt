package com.example.fuelconsumption2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fuelconsumption2.data.AppDatabase
import com.example.fuelconsumption2.data.entities.Tanking
import com.example.fuelconsumption2.data.repository.ConfigurationRepository
import com.example.fuelconsumption2.data.repository.TankingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

class TankingsSummaryViewModel(private val db: AppDatabase): ViewModel() {
    private val tankingRepository = TankingRepository(db.tankingDao())
    private val configurationRepository = ConfigurationRepository(db.configurationDao())
    private val state = MutableStateFlow(TankingsSummaryState())
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

                state.update {
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
}