package com.example.fuelconsumption2.data.repository

import com.example.fuelconsumption2.data.dao.VehicleDao
import com.example.fuelconsumption2.data.entities.Vehicle
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) {
    fun getVehicleById(vehicleId: Int): Flow<Vehicle> {
        return vehicleDao.getVehicleById(vehicleId)
    }

    fun getAllVehiclesForAddingTanking(): Flow<List<Vehicle>> {
        return vehicleDao.getAllVehiclesForAddingTanking()
    }

    fun insertVehicle(vararg vehicle: Vehicle) {
        vehicleDao.insertVehicle(*vehicle)
    }
}