package com.example.fuelconsumption2.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.fuelconsumption2.data.entities.Configuration

@Dao
interface ConfigurationDao {
    @Query("SELECT * FROM configuration WHERE ConfigurationId = 0")
    fun getConfiguration(): Configuration
}