package com.example.fuelconsumption2.data.repository

import android.content.Context
import androidx.room.Room
import com.example.fuelconsumption2.data.AppDatabase
import com.example.fuelconsumption2.data.dao.TankingDao
import com.example.fuelconsumption2.data.entities.Tanking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TankingRepository(private val tankingDao: TankingDao) {
    suspend fun getTankingById(tankingId: Int): Tanking {
        return withContext(Dispatchers.IO) {
            return@withContext tankingDao.getTankingById(tankingId)
        }
    }

    suspend fun insertTanking(vararg tanking: Tanking) {
        withContext(Dispatchers.IO) {
            tankingDao.insertTanking(*tanking)
        }
    }

    fun getAllTankings(): Flow<List<Tanking>> {
        return tankingDao.getAllTankings()
    }

    fun getAllTankingsInBetweenByVehicleId(vehicleId: Int?, start: Long, end: Long): Flow<List<Tanking>> {
        return tankingDao.getAllTankingsInBetweenByVehicleId(vehicleId, start, end)
    }
}