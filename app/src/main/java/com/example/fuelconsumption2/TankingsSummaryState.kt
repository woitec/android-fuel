package com.example.fuelconsumption2

import com.example.fuelconsumption2.data.entities.Tanking
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class TankingsSummaryState(
    val visibleTankings: Flow<List<Tanking>>? = null,
    val currentDate: SteroidDate? = null,
    val averageConsumption: Float? = 0.0f,
    val averageCost: Float? = 0.0f,
    val isAddingVehicle: Boolean = false,
    val isFilteringHistory: Boolean = false,
    val isAddingTanking: Boolean = false,
    val currentVehicle: Int? = null,
    val historyFilterStart: SteroidDate? = null,
    val historyFilterEnd: SteroidDate? = null
    ) {
    companion object {
        fun default(): TankingsSummaryState {
            val currentTimestamp = Instant.now()
            val currentTimestampAsSteroidDate = SteroidDate(currentTimestamp.toEpochMilli())
            val oneYearBeforeNowTimestampAsSteroidDate = SteroidDate(currentTimestamp
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .minus(1, ChronoUnit.YEARS)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli())

            return TankingsSummaryState(
                visibleTankings = null,
                currentDate = currentTimestampAsSteroidDate,
                averageConsumption = 0.0f,
                averageCost = 0.0f,
                isAddingVehicle = false,
                isFilteringHistory = false,
                isAddingTanking = false,
                currentVehicle = null,
                historyFilterStart = oneYearBeforeNowTimestampAsSteroidDate,
                historyFilterEnd = currentTimestampAsSteroidDate
            )
        }
    }
}