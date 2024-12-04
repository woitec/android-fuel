package com.example.fuelconsumption2

import com.example.fuelconsumption2.data.entities.Tanking
import kotlinx.coroutines.flow.Flow

data class TankingsSummaryState(
    // TODO: nie wiem czy flow we flow we flow to dobry pomysł, ale może mi się wydaje. Boję się o gubienie referencji
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
    )