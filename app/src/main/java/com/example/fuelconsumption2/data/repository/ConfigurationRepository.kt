package com.example.fuelconsumption2.data.repository

import com.example.fuelconsumption2.data.AppDatabase
import com.example.fuelconsumption2.data.dao.ConfigurationDao
import com.example.fuelconsumption2.data.entities.Configuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ConfigurationRepository(private val configurationDao: ConfigurationDao) {
    suspend fun getConfiguration(): Configuration? {
        return configurationDao.getConfiguration()
    }

    fun getRecentVehicleId(): Flow<Int> {
        return configurationDao.getRecentVehicleId()
            .map { it ?: 0 }
    }

    fun setRecentVehicle(id: Int, vehicleId: Int?) {
        configurationDao.updateRecentVehicle(id, vehicleId)
    }

    suspend fun insertConfiguration(vararg configuration: Configuration) {
        withContext(Dispatchers.IO) {
            configurationDao.insertConfiguration(*configuration)
        }
    }

    suspend fun isConfigurationEmpty(): Boolean {
        return configurationDao.isConfigurationEmpty()
    }

}