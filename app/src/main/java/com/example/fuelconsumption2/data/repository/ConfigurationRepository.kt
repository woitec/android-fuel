package com.example.fuelconsumption2.data.repository

import com.example.fuelconsumption2.data.dao.ConfigurationDao
import com.example.fuelconsumption2.data.entities.Configuration

class ConfigurationRepository(private val configurationDao: ConfigurationDao) {
    fun getConfiguration(): Configuration {
        return configurationDao.getConfiguration()
    }
}