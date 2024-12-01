package com.example.fuelconsumption2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fuelconsumption2.data.entities.Tanking
import kotlinx.coroutines.flow.Flow

@Dao
interface TankingDao {
    @Query("SELECT * FROM tanking WHERE TankingId = :tankingId")
    fun getTankingById(tankingId: Int): Tanking

    @Insert
    fun insertTankings(vararg tanking: Tanking)

    @Query("SELECT * FROM tanking")
    fun getAllTankings(): Flow<List<Tanking>>

    @Query("SELECT * FROM tanking WHERE vehicle_id = :vehicleId")
    fun getAllTankingsByVehicleId(vehicleId: Int): List<Tanking>

    @Query("SELECT fuel_amount FROM tanking WHERE vehicle_id = :vehicleId")
    fun getAllFuelAmountsByVehicleId(vehicleId: Int): Flow<List<Float?>>

    @Query("SELECT * FROM tanking WHERE vehicle_id = :vehicleId AND timestamp BETWEEN :start AND :end")
    fun getAllTankingsInBetweenByVehicleId(vehicleId: Int?, start: Long, end: Long): Flow<List<Tanking>>
}