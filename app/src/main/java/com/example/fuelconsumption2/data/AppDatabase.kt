package com.example.fuelconsumption2.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fuelconsumption2.data.dao.ConfigurationDao
import com.example.fuelconsumption2.data.dao.TankingDao
import com.example.fuelconsumption2.data.dao.VehicleDao
import com.example.fuelconsumption2.data.entities.Configuration
import com.example.fuelconsumption2.data.entities.Tanking
import com.example.fuelconsumption2.data.entities.Vehicle

@Database(entities = [Vehicle::class, Tanking::class, Configuration::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun tankingDao(): TankingDao
    abstract fun configurationDao(): ConfigurationDao
}