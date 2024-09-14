package com.example.fuelconsumption2.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "tanking")
data class Tanking (
    @PrimaryKey val TankingId: Int,
    @ColumnInfo(name = "vehicle_id") val VehicleId: Int,
    @ColumnInfo(name = "kilometers_before") val KilometersBefore: Int,
    @ColumnInfo(name = "kilometers_after") val KilometersAfter: Int,
    @ColumnInfo(name = "fuel_amount") val FuelAmount: Float,
    @ColumnInfo(name = "date") val Date: String?,
    @ColumnInfo(name = "time") val Time: String?,
    @ColumnInfo(name = "price") val Price: Float,
    @ColumnInfo(name = "fuel_type") val FuelType: String?,
    @ColumnInfo(name = "cost") val Cost: Float
    )