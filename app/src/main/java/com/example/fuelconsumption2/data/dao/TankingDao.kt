package com.example.fuelconsumption2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fuelconsumption2.data.entities.Tanking

@Dao
interface TankingDao {
    @Query("SELECT * FROM tanking WHERE TankingId = :tankingId")
    fun getTankingById(tankingId: Int): Tanking

    @Insert
    fun insertTanking(vararg tanking: Tanking)

    @Query("SELECT * FROM tanking")
    fun getAllTankings(): List<Tanking>

    @Query("SELECT * FROM tanking WHERE vehicle_id = :vehicleId")
    fun getAllTankingsByVehicleId(vehicleId: Int): List<Tanking>
}