package com.example.fuelconsumption2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.example.fuelconsumption2.data.entities.Tanking
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class TankingsSummaryState(
    val currentDate: SteroidDate? = null,
    val averageConsumption: Float? = 0.0f,
    val averageCost: Float? = 0.0f,
    val isAddingVehicle: Boolean = false,
    val isFilteringHistory: Boolean = false,
    val isAddingTanking: Boolean = false,
    val currentVehicle: Int? = null,
    val historyFilterStart: Long? = null,
    val historyFilterEnd: Long? = null
    )