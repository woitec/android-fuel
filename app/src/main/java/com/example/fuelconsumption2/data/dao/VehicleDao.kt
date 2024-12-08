package com.example.fuelconsumption2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fuelconsumption2.data.entities.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicle WHERE VehicleId = :vehicleId")
    fun getVehicleById(vehicleId: Int): Flow<Vehicle>

    @Query("SELECT * FROM vehicle")
    fun getAllVehicles(): List<Vehicle>

    @Query("SELECT VehicleId, name, kilometers, default_fuel_type FROM vehicle")
    fun getAllVehiclesForAddingTanking(): List<Vehicle>

    @Insert
    fun insertVehicle(vararg vehicle: Vehicle)
}