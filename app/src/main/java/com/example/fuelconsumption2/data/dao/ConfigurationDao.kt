package com.example.fuelconsumption2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fuelconsumption2.data.entities.Configuration
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigurationDao {
    @Query("SELECT * FROM configuration WHERE ConfigurationId = 1")
    suspend fun getConfiguration(): Configuration?

    @Insert
    fun insertConfiguration(vararg configuration: Configuration)

    @Query("SELECT NOT EXISTS(SELECT 1 FROM configuration LIMIT 1)")
    suspend fun isConfigurationEmpty(): Boolean

    @Query("SELECT recent_vehicle_id FROM configuration")
    fun getRecentVehicleId(): Flow<Int?>

    @Query("UPDATE configuration SET recent_vehicle_id = :vehicleId WHERE ConfigurationId = :id")
    fun updateRecentVehicle(id: Int, vehicleId: Int?)
}