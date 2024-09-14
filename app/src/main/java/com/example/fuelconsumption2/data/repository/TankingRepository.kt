package com.example.fuelconsumption2.data.repository

import android.content.Context
import androidx.room.Room
import com.example.fuelconsumption2.data.AppDatabase
import com.example.fuelconsumption2.data.dao.TankingDao
import com.example.fuelconsumption2.data.entities.Tanking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TankingRepository(context: Context) {
    val db = Room.databaseBuilder(context, AppDatabase::class.java, "db-tankings-app").build()

    suspend fun getTankingId(tankingId: Int): Tanking {
        return withContext(Dispatchers.IO) {
            return@withContext db.tankingDao().getTankingById(tankingId)
        }
    }

    suspend fun insertTanking(vararg tanking: Tanking) {
        withContext(Dispatchers.IO) {
            db.tankingDao().insertTanking(*tanking)
        }
    }

    suspend fun getAllTankings(): List<Tanking> {
        return withContext(Dispatchers.IO) {
            return@withContext db.tankingDao().getAllTankings()
        }
    }
}