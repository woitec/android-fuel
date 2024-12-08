package com.example.fuelconsumption2

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

class SteroidDate(private val timestamp: Long?) {
    fun displayDate(): String {
        if(timestamp == null) {
            return "null"
        } else {
            val date = Date(timestamp)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return dateFormat.format(date)
        }
    }
    fun displayTime(): String {
        if(timestamp == null) {
            return "null"
        } else {
            val date = Date(timestamp)
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return timeFormat.format(date)
        }
    }
    fun getTimestamp(): Long? {
        return timestamp
    }

    companion object {
        fun oneYearBefore(time: Instant): SteroidDate {
            return SteroidDate(time.atZone(ZoneId.systemDefault())
                .toLocalDate()
                .minus(1, ChronoUnit.YEARS)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli())
        }
    }

    //TODO("Use companion objects to handle logic here")
//    companion object {
//        fun now(): SteroidDate {
//            return SteroidDate(Instant.now().toEpochMilli())
//        }
//        fun oneYearBeforeNow(): SteroidDate {
//            return SteroidDate(Instant.now()
//                .atZone(ZoneId.systemDefault())
//                .toLocalDate()
//                .minus(1, ChronoUnit.YEARS)
//                .atStartOfDay(ZoneId.systemDefault())
//                .toInstant()
//                .toEpochMilli())
//        }
//    }
}