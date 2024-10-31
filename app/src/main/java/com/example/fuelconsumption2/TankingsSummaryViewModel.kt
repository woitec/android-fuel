package com.example.fuelconsumption2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.fuelconsumption2.data.AppDatabase
import com.example.fuelconsumption2.data.entities.Tanking
import com.example.fuelconsumption2.data.repository.TankingRepository
import kotlinx.coroutines.flow.Flow

class TankingsSummaryViewModel(private val db: AppDatabase): ViewModel() {
    private val tankingRepository = TankingRepository(db)
    val tankings = mutableListOf<Tanking>()

    fun updateTankingsRecyclerView(eventData: List<Tanking>) {
        tankings.clear()
        tankings.addAll(eventData)
    }

    suspend fun updateTankings(vararg newTanking: Tanking) {
        tankingRepository.insertTanking(*newTanking)
    }

    fun getAllTankings(): Flow<List<Tanking>> {
        return tankingRepository.getAllTankings()
    }
}