package com.example.fuelconsumption2.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fuelconsumption2.data.dao.ConfigurationDao
import com.example.fuelconsumption2.data.dao.TankingDao
import com.example.fuelconsumption2.data.dao.VehicleDao
import com.example.fuelconsumption2.data.entities.Configuration
import com.example.fuelconsumption2.data.entities.Tanking
import com.example.fuelconsumption2.data.entities.Vehicle
import com.example.fuelconsumption2.data.typeConverters.FuelTypeConverter

@Database(entities = [Vehicle::class, Tanking::class, Configuration::class], version = 1)
@TypeConverters(FuelTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun tankingDao(): TankingDao
    abstract fun configurationDao(): ConfigurationDao
}