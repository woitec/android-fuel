package com.example.fuelconsumption2

import com.example.fuelconsumption2.data.entities.Tanking

sealed interface TankingsSummaryEvent {
    //Adding vehicle
    object ShowAddVehicleDialog: TankingsSummaryEvent

    //Adding tanking
    object ShowAddTankingDialog: TankingsSummaryEvent

    //Filtering history
    object ShowFilterDialog: TankingsSummaryEvent
    data class SetDefaultVehicle(val vehicleId: Int?): TankingsSummaryEvent

    //Change vehicle
    data class SetCurrentVehicle(val vehicleId: Int?): TankingsSummaryEvent

    //Data management
    data class DeleteTanking(val tanking: Tanking): TankingsSummaryEvent
    data class EditTanking(val tanking: Tanking): TankingsSummaryEvent
}