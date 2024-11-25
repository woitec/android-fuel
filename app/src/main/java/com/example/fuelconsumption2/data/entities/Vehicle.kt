package com.example.fuelconsumption2.data.entities
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fuelconsumption2.enums.FuelType

@Entity (tableName = "vehicle")
data class Vehicle (
    @PrimaryKey(autoGenerate = true) val VehicleId: Int,
    @ColumnInfo(name = "name") val Name: String?,
    @ColumnInfo(name = "registry_number") val RegistryNumber: String?,
    @ColumnInfo(name = "kilometers") val Kilometers: Int?,
    @ColumnInfo(name = "default_fuel_type") val DefaultFuelType: FuelType?
    )