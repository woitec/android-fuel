package com.example.fuelconsumption2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fuelconsumption2.data.entities.Vehicle

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicle WHERE VehicleId = :vehicleId")
    fun getVehicleById(vehicleId: Int): Vehicle

    @Insert
    fun insertVehicle(vararg vehicle: Vehicle)
}