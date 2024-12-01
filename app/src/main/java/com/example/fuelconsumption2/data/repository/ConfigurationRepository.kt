package com.example.fuelconsumption2.data.repository

import com.example.fuelconsumption2.data.AppDatabase
import com.example.fuelconsumption2.data.dao.ConfigurationDao
import com.example.fuelconsumption2.data.entities.Configuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConfigurationRepository(private val configurationDao: ConfigurationDao) {
    suspend fun getConfiguration(): Configuration? {
        return configurationDao.getConfiguration()
    }

    suspend fun getRecentVehicleId(): Int? {
        val configuration = configurationDao.getConfiguration()
        return if(configuration == null) {
            -1
        } else {
            configuration.RecentVehicleId
        }
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