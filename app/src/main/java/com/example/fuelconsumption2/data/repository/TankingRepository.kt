package com.example.fuelconsumption2.data.repository

import com.example.fuelconsumption2.data.dao.TankingDao
import com.example.fuelconsumption2.data.entities.Tanking

class TankingRepository(private val tankingDao: TankingDao) {
    fun getTankingId(tankingId: Int): Tanking {
        return tankingDao.getTankingById(tankingId)
    }

    fun insertTanking(vararg tanking: Tanking) {
        tankingDao.insertTanking(*tanking)
    }

    fun getAllTankings(): List<Tanking> {
        return tankingDao.getAllTankings()
    }
}