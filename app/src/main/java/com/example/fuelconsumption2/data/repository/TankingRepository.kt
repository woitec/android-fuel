package com.example.fuelconsumption2.data.repository

import com.example.fuelconsumption2.SteroidDate
import com.example.fuelconsumption2.data.dao.TankingDao
import com.example.fuelconsumption2.data.entities.Tanking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

class TankingRepository(private val tankingDao: TankingDao) {
    suspend fun getTankingById(tankingId: Int): Tanking {
        return withContext(Dispatchers.IO) {
            return@withContext tankingDao.getTankingById(tankingId)
        }
    }

    suspend fun insertTankings(vararg tanking: Tanking) {
        withContext(Dispatchers.IO) {
            tankingDao.insertTankings(*tanking)
        }
    }

    fun getAllTankings(): Flow<List<Tanking>> {
        return tankingDao.getAllTankings()
    }

    fun getAllTankingsInBetweenByVehicleId(vehicleId: Int?, start: Long?, end: Long?): List<Tanking> {
        var nonNullVehicleId = -1
        if(vehicleId !== null) {
            nonNullVehicleId = vehicleId
        }
        return if (start == null || end == null) {
            emptyList()
        } else {
            tankingDao.getAllTankingsInBetweenByVehicleId(
                nonNullVehicleId,
                start,
                end
            )
        }
        //TODO("Unit test that shit. The non-null assertion must be always valid.")
    }
}