package com.example.fuelconsumption2

import com.example.fuelconsumption2.data.entities.Tanking

sealed interface TankingEvent {
    //Adding vehicle
    object ShowAddVehicleDialog: TankingEvent
    object HideAddVehicleDialog: TankingEvent

    //Adding tanking
    object ShowAddTankingDialog: TankingEvent
    object HideAddTankingDialog: TankingEvent

    //Filtering history
    object ShowFilterDialog: TankingEvent
    object HideFilterDialog: TankingEvent
    data class SetDefaultVehicle(val vehicleId: Int?): TankingEvent

    //Change vehicle
    data class SetCurrentVehicle(val vehicleId: Int?): TankingEvent

    //Data management
    data class DeleteTanking(val tanking: Tanking): TankingEvent
    data class EditTanking(val tanking: Tanking): TankingEvent
}