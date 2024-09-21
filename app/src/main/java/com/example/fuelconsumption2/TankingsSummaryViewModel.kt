package com.example.fuelconsumption2

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.fuelconsumption2.data.entities.Tanking
import com.example.fuelconsumption2.data.repository.TankingRepository
import kotlinx.coroutines.flow.Flow

class TankingsSummaryViewModel(application: Application) : AndroidViewModel(application) {
    private val tankingRepository = TankingRepository(application.applicationContext)
    val tankings = mutableListOf<Tanking>()

    fun updateTankingsListView(eventData: List<Tanking>) {
        tankings.clear()
        tankings.addAll(eventData)
    }

    suspend fun updateTankings(vararg newTankings:Tanking) {
        tankingRepository.insertTanking(*newTankings)
    }

    fun getAllTankings(): Flow<List<Tanking>> {
        return tankingRepository.getAllTankings()
    }
}