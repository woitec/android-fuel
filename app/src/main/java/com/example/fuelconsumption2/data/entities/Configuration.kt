package com.example.fuelconsumption2.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "configuration")
data class Configuration (
    @PrimaryKey val ConfigurationId: Int,
    @ColumnInfo(name = "recent_vehicle_id") val RecentVehicleId: Int,
    @ColumnInfo(name = "filter_preferences") val FilterPreferences: String?
    )