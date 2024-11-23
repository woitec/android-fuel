package com.example.fuelconsumption2

import com.example.fuelconsumption2.data.entities.Tanking

sealed interface TankingEvent {
    //Adding vehicle
    object showAddVehicleDialog: TankingEvent
    object hideAddVehicleDialog: TankingEvent

    //Adding tanking
    object showAddTankingDialog: TankingEvent
    object hideAddTankingDialog: TankingEvent
    data class SetVehicle(val vehicleId: Int?): TankingEvent
    data class SetAmount(val amount: Float?): TankingEvent
    data class SetKilometersBefore(val kilometersBefore: Int?): TankingEvent
    data class SetKilometersAfter(val kilometersAfter: Int?): TankingEvent
    data class SetPrice(val price: Float?): TankingEvent
    object SaveTanking: TankingEvent

    //Filtering history
    object showFilterDialog: TankingEvent
    object hideFilterDialog: TankingEvent
    data class SetDefaultVehicle(val vehicleId: Int?): TankingEvent

    //Change vehicle
    data class SetCurrentVehicle(val vehicleId: Int?): TankingEvent

    //Data management
    data class DeleteTanking(val tanking: Tanking): TankingEvent
    data class EditTanking(val tanking: Tanking): TankingEvent
}