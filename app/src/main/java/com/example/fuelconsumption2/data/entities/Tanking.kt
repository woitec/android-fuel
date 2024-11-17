package com.example.fuelconsumption2.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fuelconsumption2.SteroidDate

@Entity (tableName = "tanking")
data class Tanking (
    @PrimaryKey(autoGenerate = true) val TankingId: Int,
    @ColumnInfo(name = "vehicle_id") val VehicleId: Int?,
    @ColumnInfo(name = "kilometers_before") val KilometersBefore: Int?,
    @ColumnInfo(name = "kilometers_after") val KilometersAfter: Int?,
    @ColumnInfo(name = "fuel_amount") val FuelAmount: Float?,
    @ColumnInfo(name = "timestamp") val Timestamp: Long?,
    @ColumnInfo(name = "price") val Price: Float?,
    @ColumnInfo(name = "fuel_type") val FuelType: String?,
    @ColumnInfo(name = "cost") val Cost: Float?
    ) {
    fun steroidDate(): SteroidDate? {
        return if(Timestamp == null) {
            null
        } else {
            SteroidDate(Timestamp)
        }
    }
}