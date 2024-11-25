package com.example.fuelconsumption2.data.typeConverters

import androidx.room.TypeConverter
import com.example.fuelconsumption2.enums.FuelType

class FuelTypeConverter {
    @TypeConverter
    fun fromFuelType(value: FuelType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toFuelType(value: String?): FuelType? {
        return value?.let { FuelType.valueOf(it) }
    }
}