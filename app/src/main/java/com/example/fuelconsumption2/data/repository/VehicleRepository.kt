package com.example.fuelconsumption2.data.repository

import com.example.fuelconsumption2.data.dao.VehicleDao
import com.example.fuelconsumption2.data.entities.Vehicle

class VehicleRepository(private val vehicleDao: VehicleDao) {
    fun getVehicleById(vehicleId: Int): Vehicle {
        return vehicleDao.getVehicleById(vehicleId)
    }
    fun insertVehicle(vararg vehicle: Vehicle) {
        vehicleDao.insertVehicle(*vehicle)
    }
}